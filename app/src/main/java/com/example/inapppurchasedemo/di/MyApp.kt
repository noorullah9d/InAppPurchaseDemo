package com.example.inapppurchasedemo.di

import android.app.Application
import android.util.Log
import com.example.inapppurchasedemo.core.utils.PrefUtils
import com.example.inapppurchasedemo.core.utils.PrefUtils.isAdsRemoved
import com.example.inapppurchasedemo.premium.BillingViewModel
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MyApp: Application() {

    @Inject
    lateinit var billingViewModel: BillingViewModel

    override fun onCreate() {
        super.onCreate()
        PrefUtils.init(this)

        billingViewModel.startBillingConnection()

        CoroutineScope(Dispatchers.IO).launch {
            billingViewModel.purchases.collect { purchaseList ->
                Log.d("","purchase list: $purchaseList")
                isAdsRemoved = purchaseList?.isNotEmpty() ?: false
            }
        }

        /*if (!isAdsRemoved) {
            appOpen = AppOpenManager(this)
        }*/
    }
}