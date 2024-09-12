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
import com.example.inapppurchasedemo.core.extensions.formatFreeTrialFooter
import com.example.inapppurchasedemo.core.extensions.isOnline
import com.example.inapppurchasedemo.core.extensions.setColoredText
import com.example.inapppurchasedemo.core.extensions.setDebouncedOnClick
import com.example.inapppurchasedemo.core.extensions.showSubscriptionDetailsDialog
import com.example.inapppurchasedemo.core.extensions.toast
import com.example.inapppurchasedemo.core.utils.Utils.openPrivacyPolicy
import com.example.inapppurchasedemo.databinding.ActivityFreeTrialBinding
import com.example.inapppurchasedemo.core.utils.PrefUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FreeTrialActivity : AppCompatActivity() {
    private val binding: ActivityFreeTrialBinding by lazy {
        ActivityFreeTrialBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var viewModel: BillingViewModel

    private var selected: ProductDetails? = null
    private var yearly: ProductDetails? = null
    private var shouldShowToast = false
    private var doesHaveCurrentPurchase = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initViews()
        setupClickListeners()

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
                if (!viewModel.isLanguageShown()) {
                    navigateToLanguageSelection()
                } else {
                    navigateToMainActivity()
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

    private fun initViews() {
        // Set the text with highlighted part
        binding.apply {
            title.setColoredText(
                getString(R.string.free_trial_heading),
                "3",
                getColor(R.color.primary),
                getColor(R.color.white)
            )

            tvFooter.formatFreeTrialFooter(
                onPrivacyPolicyClicked = {
                    openPrivacyPolicy()
                },
                onRestorePurchaseClicked = {
                    viewModel.startBillingConnection()
                    shouldShowToast = true
                },
                onDetailsClicked = {
                    showSubscriptionDetailsDialog()
                }
            )
        }

        lifecycleScope.launch {
            viewModel.monthlyProductDetails().observe(this@FreeTrialActivity) {
                setPrices(it)
            }

            viewModel.hasActiveSubs.observe(this@FreeTrialActivity) {
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

            viewModel.isBillingConnected().observe(this@FreeTrialActivity) {
                if (!it) {
                    toast(getString(R.string.failed_to_connect_to_billing_server))
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            icClose.setDebouncedOnClick {
                it.startAnimation(
                    AnimationUtils.loadAnimation(
                        this@FreeTrialActivity,
                        R.anim.click
                    )
                )

                if (!viewModel.isLanguageShown()) {
                    navigateToLanguageSelection()
                } else {
                    navigateToMainActivity()
                }
            }

            btnSubscribeNow.setDebouncedOnClick {
                try {
                    handleSubscriptionPurchase()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setPrices(products: Map<String, ProductDetails>) {
        var annualPostTrialPrice = ""
        yearly = products[BillingViewModel.ANNUAL_SUBSCRIPTION]
        selected = yearly

        yearly?.subscriptionOfferDetails?.forEach { offerDetails ->
            offerDetails.pricingPhases.pricingPhaseList.let { phases ->
                for (pricingPhase in phases) {
                    if (pricingPhase.priceAmountMicros > 0) {
                        annualPostTrialPrice =
                            "${pricingPhase.priceCurrencyCode} ${(pricingPhase.priceAmountMicros / 1_000_000)}/yr"
                        break
                    }
                }
            }
        }

        if (annualPostTrialPrice.trim().isNotEmpty()) {
            binding.tvOfferDetails.text = getString(R.string.free_trial_offer_details, annualPostTrialPrice)
        } else binding.tvOfferDetails.text = getString(R.string.free_trial_offer_details, "USD 19.99")
    }

    private fun handleSubscriptionPurchase() {
        if (isOnline()) {
            if (viewModel.isBillingConnected().value == true) {
                if (!doesHaveCurrentPurchase) {
                    if (selected != null) {
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
}