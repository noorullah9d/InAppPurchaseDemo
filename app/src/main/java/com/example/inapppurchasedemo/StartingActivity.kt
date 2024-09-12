package com.example.inapppurchasedemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.inapppurchasedemo.core.utils.PrefUtils
import com.example.inapppurchasedemo.core.utils.PrefUtils.isAdsRemoved
import com.example.inapppurchasedemo.premium.BillingViewModel
import com.example.inapppurchasedemo.premium.FreeTrialActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StartingActivity : AppCompatActivity() {

    @Inject
    lateinit var billingViewModel: BillingViewModel

    private var splashTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starting)

        lifecycleScope.launch {
            billingViewModel.purchases.collect { purchaseList ->
                Log.d("Tag", "purchase list: $purchaseList")
                isAdsRemoved = purchaseList?.isNotEmpty() ?: false
            }
        }

        startPremiumCountDownTimer()
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun handleNavigation() {
        try {
            val currentTime = System.currentTimeMillis()
            val lastShownTime = PrefUtils.lastPremiumShownTime
            if (shouldShowPremiumActivity(currentTime, lastShownTime) && !isAdsRemoved) {
                Log.d("Splash", "SPLASH: show premium screen")
                PrefUtils.lastPremiumShownTime = currentTime
                navigateToFreeTrial()
            } /*else if (!viewModel.isLanguageShown()) {
                Log.d("Splash","SPLASH: show languages screen")
                navigateToLanguageSelection()
            } else if (!viewModel.isOnboardingShown()) {
                Log.d("Splash","SPLASH: show onboarding screen")
                navigateToOnboarding()
            } */ else {
                Log.d("Splash", "SPLASH: show main screen")
                navigateToMainActivity()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shouldShowPremiumActivity(currentTime: Long, lastShownTime: Long): Boolean {
        val oneDayInMillis = 24 * 60 * 60 * 1000
        return (currentTime - lastShownTime) >= oneDayInMillis
    }

    private fun Activity.navigateToFreeTrial() {
        val intent = Intent(this, FreeTrialActivity::class.java)
        intent.putExtra("from_splash", true)
        startActivity(intent)
        finish()
    }

    private fun startPremiumCountDownTimer() {
        cancelTimer()
        splashTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                handleNavigation()
            }
        }
        splashTimer?.start()
    }

    private fun cancelTimer() {
        splashTimer?.cancel()
        splashTimer = null
    }

    override fun onPause() {
        super.onPause()
        cancelTimer()
    }

    override fun onResume() {
        super.onResume()
        startPremiumCountDownTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimer()
    }
}