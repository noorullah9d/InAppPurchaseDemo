package com.example.inapppurchasedemo.core.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PrefUtils {
    private lateinit var sharedPreferences: SharedPreferences
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)
    }

    var lastPremiumShownTime: Long
        get() = sharedPreferences.getLong(LAST_PREMIUM_SHOWN_TIME, 0)
        set(value) = sharedPreferences.edit { putLong(LAST_PREMIUM_SHOWN_TIME, value) }

    var isAdsRemoved
        get() = sharedPreferences.getBoolean(IS_PREMIUM, false)
        set(value) = sharedPreferences.edit { putBoolean(IS_PREMIUM, value) }

    var hasTermsAndConditionsAccepted
        get() = sharedPreferences.getBoolean(HAS_TERM_AND_CONDITION_ACCEPTED, false)
        set(value) = sharedPreferences.edit { putBoolean(HAS_TERM_AND_CONDITION_ACCEPTED, value) }
}