package com.example.inapppurchasedemo.core.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.inapppurchasedemo.core.extensions.toast

object Utils {

    fun Context.gotoManageSubscription(newTask: Boolean = false): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data =
                Uri.parse("https://play.google.com/store/account/subscriptions?package=$packageName")
            if (newTask) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            isAnySystemDialogShown = true
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            false
        }
    }


    fun Activity.rateAppOnPlayStore() {
        val uri = Uri.parse("market://details?id=$packageName")
        val playStoreIntent = Intent(Intent.ACTION_VIEW, uri)

        try {
            startActivity(playStoreIntent)
        } catch (e: ActivityNotFoundException) {
            // If the Play Store app is not installed, open the Play Store website instead
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            startActivity(webIntent)
        }
    }

    fun Context.openPrivacyPolicy() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://9dstackers.blogspot.com/2024/01/vpn-proxy-privacy-policy.html")
        )
        try {
            isAnySystemDialogShown = true
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast("Activity not found!")
        }
    }

    fun Context.openTermsAndConditions() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://9dstackers.blogspot.com/2024/01/vpn-proxy-terms-conditions.html")
        )
        try {
            isAnySystemDialogShown = true
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast("Activity not found!")
        }
    }

    fun Activity.shareApp() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Hey check out my app at: https://play.google.com/store/apps/details?id=${packageName}"
        )
        sendIntent.type = "text/plain"
        try {
            startActivity(sendIntent)
        } catch (e: ActivityNotFoundException) {
            toast("Activity not found!")
        }
    }
}