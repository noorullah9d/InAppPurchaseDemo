package com.example.inapppurchasedemo.premium

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.billingclient.api.ProductDetails
import com.example.inapppurchasedemo.MainActivity
import com.example.inapppurchasedemo.R
import com.example.inapppurchasedemo.StartingActivity
import com.example.inapppurchasedemo.core.extensions.formatRestorePurchase
import com.example.inapppurchasedemo.core.extensions.isOnline
import com.example.inapppurchasedemo.core.extensions.setColoredText
import com.example.inapppurchasedemo.core.extensions.setDebouncedOnClick
import com.example.inapppurchasedemo.core.extensions.showSubscriptionDetailsDialog
import com.example.inapppurchasedemo.core.extensions.toast
import com.example.inapppurchasedemo.core.utils.PrefUtils
import com.example.inapppurchasedemo.databinding.ActivityPremiumBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PremiumActivity : AppCompatActivity() {
    private val binding: ActivityPremiumBinding by lazy {
        ActivityPremiumBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var viewModel: BillingViewModel

    private var selected: ProductDetails? = null
    private var monthly: ProductDetails? = null
    private var yearly: ProductDetails? = null
    private var shouldShowToast = false
    private var doesHaveCurrentPurchase = false
    private var fromSplash = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
//        isPremiumScreenShowing = true

        // Check if the activity was opened from SplashActivity
        fromSplash = intent.getBooleanExtra("from_splash", false)

        initViews()
        setupClickListeners()
        setupListeners()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.purchases.collect { purchaseList ->
                    Log.d("Tag","purchase list: $purchaseList")
                    PrefUtils.isAdsRemoved = purchaseList?.isNotEmpty() ?: false
                }
            }
        }

        handleBackPress()
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (fromSplash) {
                    if (!viewModel.isLanguageShown()) {
                        navigateToLanguageSelection()
                    } else {
                        navigateToMainActivity()
                    }
                } else {
                    finish()
                }
            }
        })
    }

    private fun navigateToLanguageSelection() {
//        startActivity(Intent(this, SelectLanguageActivity::class.java))
//        finish()
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setupListeners() {
        binding.apply {
            radioBtnMonthly.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selected = monthly
                    radioBtnYearly.isChecked = false
                }
            }

            radioBtnYearly.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selected = yearly
                    radioBtnMonthly.isChecked = false
                }
            }
        }
    }

    private fun initViews() {
        binding.title.setColoredText(
            getString(R.string.premium_heading),
            getString(R.string.free_trial),
            getColor(R.color.primary),
            getColor(R.color.text)
        )

        lifecycleScope.launch {
            viewModel.monthlyProductDetails().observe(this@PremiumActivity) {
                setPrices(it)
            }

            viewModel.hasActiveSubs.observe(this@PremiumActivity) {
                Log.d("Tag","called! $doesHaveCurrentPurchase")
                if (!doesHaveCurrentPurchase && shouldShowToast) {
                    shouldShowToast = false
                    toast(getString(R.string.you_don_t_have_any_purchases_yet))
                }
            }

            viewModel.acknowledgeCallback = {
//                Core.stopService()
                val intent = Intent(binding.root.context, StartingActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }

            viewModel.isBillingConnected().observe(this@PremiumActivity) {
                if (!it) {
                    toast(getString(R.string.failed_to_connect_to_billing_server))
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            icClose.setDebouncedOnClick {
                it.startAnimation(AnimationUtils.loadAnimation(this@PremiumActivity, R.anim.click))
                if (fromSplash) {
                    if (!viewModel.isLanguageShown()) {
                        navigateToLanguageSelection()
                    } else {
                        navigateToMainActivity()
                    }
                } else {
                    finish()
                }
            }

            btnSubscribeNow.setDebouncedOnClick {
                try {
                    handleSubscriptionPurchase()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            tvRestoreSubscription.formatRestorePurchase(
                onRestorePurchaseClicked = {
                    viewModel.startBillingConnection()
                    shouldShowToast = true
                },
                onDetailsClicked = {
                    showSubscriptionDetailsDialog()
                }
            )
        }
    }

    private fun setPrices(products: Map<String, ProductDetails>) {
        var annualPostTrialPrice = ""
//        var savedPrice = "Not Available"
        yearly = products[BillingViewModel.ANNUAL_SUBSCRIPTION]

        binding.radioBtnYearly.isChecked = true
        yearly?.subscriptionOfferDetails?.forEach { offerDetails ->
            offerDetails.pricingPhases.pricingPhaseList.let { phases ->
                for (pricingPhase in phases) {
                    if (pricingPhase.priceAmountMicros > 0) {
//                        savedPrice = "${pricingPhase.priceCurrencyCode} ${(pricingPhase.priceAmountMicros / 1_000_000) / 12}/mo"

                        annualPostTrialPrice =
                            "${pricingPhase.priceCurrencyCode} ${(pricingPhase.priceAmountMicros / 1_000_000)}/yr"
                        break
                    }
                }
            }
        }

        var monthlyPostTrialPrice = ""
        monthly = products[BillingViewModel.MONTHLY_SUBSCRIPTION]

        monthly?.subscriptionOfferDetails?.forEach { offerDetails ->
            offerDetails.pricingPhases.pricingPhaseList.let { phases ->
                for (pricingPhase in phases) {
                    if (pricingPhase.priceAmountMicros > 0) {
                        monthlyPostTrialPrice =
                            "${pricingPhase.priceCurrencyCode} ${(pricingPhase.priceAmountMicros / 1_000_000)}/mo"
                        break
                    }
                }
            }
        }

        binding.apply {
            if (monthlyPostTrialPrice.trim().isNotEmpty()) {
                tvAmountMonthly.text = monthlyPostTrialPrice
            } else tvAmountMonthly.text = "USD 2.99"
            if (annualPostTrialPrice.trim().isNotEmpty()) {
                tvAmountYearly.text = annualPostTrialPrice
            } else tvAmountYearly.text = "USD 19.99"
        }
    }

    private fun handleSubscriptionPurchase() {
        if (isOnline()) {
            if (viewModel.isBillingConnected().value == true) {
                if (!doesHaveCurrentPurchase) {
                    if (selected != null) {
//                            MyPostAnalytics.myPostAnalytic("premium_continue")
                        viewModel.buySubscription(
                            productDetails = selected!!,
                            this
                        )
                    } else {
                        resources?.getString(R.string.failed_to_connect)?.let {
                            toast(it)
                        }
                    }
                } else {
                    resources?.getString(R.string.subscription_already_purchased)
                        ?.let {
                            toast(it)
                        }
                }
            } else {
                resources?.getString(R.string.failed_to_connect)?.let {
                    toast(it)
                }
            }
        } else {
            toast(getString(R.string.no_internet_connection))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        isPremiumScreenShowing = false
    }
}