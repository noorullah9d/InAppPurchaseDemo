package com.example.inapppurchasedemo.core.extensions

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import com.example.inapppurchasedemo.R
import com.example.inapppurchasedemo.databinding.DialogSubscriptionDetailsBinding

fun Activity.showSubscriptionDetailsDialog() {
    val dialog = Dialog(this)
    val binding = DialogSubscriptionDetailsBinding.inflate(layoutInflater)
    dialog.setContentView(binding.root)
    dialog.setCancelable(false)
    dialog.show()

    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
    val height = ViewGroup.LayoutParams.WRAP_CONTENT
    dialog.window?.setLayout(width, height)

    binding.apply {
        val text = getString(R.string.subscription_details)
        message.setStyledTextWithBullets(text, R.color.primary, R.color.textSecondary)

        btnOkay.click {
            dialog.dismiss()
        }
    }
}