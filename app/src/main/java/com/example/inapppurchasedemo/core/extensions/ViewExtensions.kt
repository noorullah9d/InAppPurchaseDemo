package com.example.inapppurchasedemo.core.extensions

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.VectorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.inapppurchasedemo.R
import com.example.inapppurchasedemo.core.utils.Utils.openPrivacyPolicy
import com.example.inapppurchasedemo.core.utils.Utils.openTermsAndConditions
import com.google.android.material.button.MaterialButton

fun Context.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun TextView.formatFreeTrialFooter(
    onPrivacyPolicyClicked: (() -> Unit)? = null,
    onRestorePurchaseClicked: (() -> Unit)? = null,
    onDetailsClicked: (() -> Unit)? = null
) {
    val fullText = context.getString(R.string.free_trial_footer_text)
    val privacyPolicyText = context.getString(R.string.privacy_policy)
    val restorePurchaseText = context.getString(R.string.restore_purchase)
    val detailsText = context.getString(R.string.details)
    val spannableString = SpannableString(fullText)

    val privacyPolicyClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onPrivacyPolicyClicked?.invoke()
        }
    }

    val restorePurchaseClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onRestorePurchaseClicked?.invoke()
        }
    }

    val detailsClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onDetailsClicked?.invoke()
        }
    }

    // Find the starting index of tags in the full text
    val privacyPolicyStart = fullText.indexOf(privacyPolicyText)
    val restorePurchaseStart = fullText.indexOf(restorePurchaseText)
    val detailsStart = fullText.indexOf(detailsText)

    // Apply bold style and clickable span to "Privacy Policy"
    spannableString.setSpan(
        StyleSpan(Typeface.BOLD),
        privacyPolicyStart,
        privacyPolicyStart + privacyPolicyText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannableString.setSpan(
        privacyPolicyClickableSpan,
        privacyPolicyStart,
        privacyPolicyStart + privacyPolicyText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    // Apply bold style and clickable span to "Restore Purchase"
    spannableString.setSpan(
        StyleSpan(Typeface.BOLD),
        restorePurchaseStart,
        restorePurchaseStart + restorePurchaseText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannableString.setSpan(
        restorePurchaseClickableSpan,
        restorePurchaseStart,
        restorePurchaseStart + restorePurchaseText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    // Apply bold style and clickable span to "Details"
    spannableString.setSpan(
        StyleSpan(Typeface.BOLD),
        detailsStart,
        detailsStart + detailsText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannableString.setSpan(
        detailsClickableSpan,
        detailsStart,
        detailsStart + detailsText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    // Set the text to the TextView
    text = spannableString

    // Make the TextView clickable
    movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.setColoredText(
    target: String,
    highlight: String,
    highlightColor: Int,
    defaultColor: Int
) {
    val spannableString = SpannableString(target)
    val index = target.indexOf(highlight)

    if (index != -1) {
        // Set the color for the highlight part
        spannableString.setSpan(
            ForegroundColorSpan(highlightColor),
            index,
            index + highlight.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set the remaining text color to default
        spannableString.setSpan(
            ForegroundColorSpan(defaultColor),
            0,
            index,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(defaultColor),
            index + highlight.length,
            target.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    this.text = spannableString
}

fun ViewGroup.safeAddView(adView: View) {
    // Check if the ad view already has a parent
    if (adView.parent != null) {
        // Remove the ad view from its previous parent
        (adView.parent as ViewGroup).removeView(adView)
    }
    // Add the ad view to the new parent
    this.addView(adView)
}

fun TextView.setStyledTextWithBullets(text: String, bulletColorResId: Int, textColorResId: Int) {
    val spannableString = SpannableString(text)
    val bullet = "•"
    val bulletColor = ContextCompat.getColor(context, bulletColorResId)
    val textColor = ContextCompat.getColor(context, textColorResId)

    Log.d("","setStyledTextWithBullets: Bullet Color: $bulletColor, Text Color: $textColor")

    // Apply bullet color
    var start = 0
    var end: Int
    while (text.indexOf(bullet, start).also { end = it } != -1) {
        spannableString.setSpan(
            ForegroundColorSpan(bulletColor),
            end,
            end + bullet.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        Log.d("","setStyledTextWithBullets: Applied bullet color from $end to ${end + bullet.length}")
        start = end + bullet.length
    }

    // Apply text color
    start = 0
    while (text.indexOf(bullet, start).also { end = it } != -1) {
        spannableString.setSpan(
            ForegroundColorSpan(textColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        Log.d("","setStyledTextWithBullets: Applied text color from $start to $end")
        start = end + 1
    }
    // Apply color to the last part of the text after the last bullet
    spannableString.setSpan(
        ForegroundColorSpan(textColor),
        start,
        text.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    Log.d("","setStyledTextWithBullets: Applied text color from $start to ${text.length}")

    this.text = spannableString
}

fun TextView.formatRestorePurchase(
    onRestorePurchaseClicked: (() -> Unit)? = null,
    onDetailsClicked: (() -> Unit)? = null
) {
    val fullText = context.getString(R.string.restore_purchase_summary)
    val restorePurchaseText = context.getString(R.string.restore_purchase)
    val detailsText = context.getString(R.string.details)
    val spannableString = SpannableString(fullText)

    val restorePurchaseClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onRestorePurchaseClicked?.invoke()
        }
    }

    val detailsClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onDetailsClicked?.invoke()
        }
    }

    // Find the starting index of "Restore Purchase" in the full text
    val restorePurchaseStart = fullText.indexOf(restorePurchaseText)
    val detailsStart = fullText.indexOf(detailsText)

    // Apply bold style and clickable span to "Restore Purchase"
    spannableString.setSpan(
        StyleSpan(Typeface.BOLD),
        restorePurchaseStart,
        restorePurchaseStart + restorePurchaseText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannableString.setSpan(
        restorePurchaseClickableSpan,
        restorePurchaseStart,
        restorePurchaseStart + restorePurchaseText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    // Apply bold style and clickable span to "Details"
    spannableString.setSpan(
        StyleSpan(Typeface.BOLD),
        detailsStart,
        detailsStart + detailsText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannableString.setSpan(
        detailsClickableSpan,
        detailsStart,
        detailsStart + detailsText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    // Set the text to the TextView
    text = spannableString

    // Make the TextView clickable
    movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.setClickableText() {
    val fullText = context.getString(R.string.terms_and_conditions_summary)
    val privacyPolicyText = context.getString(R.string.privacy_policy)
    val termsAndConditionText = context.getString(R.string.terms_and_conditions)
    val spannableString = SpannableString(fullText)

    val privacyPolicyClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            // Handle click on Privacy Policy
            widget.context.openPrivacyPolicy()
        }
    }

    val termsClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            // Handle click on Terms and Conditions
            widget.context.openTermsAndConditions()
        }
    }

    // Find the starting index of "Privacy Policy" and "Terms and Conditions" in the full text
    val privacyPolicyStart = fullText.indexOf(privacyPolicyText)
    val termsStart = fullText.indexOf(termsAndConditionText)

    // Apply bold style to "Privacy Policy" and "Terms and Conditions"
    spannableString.setSpan(
        StyleSpan(Typeface.BOLD),
        privacyPolicyStart,
        privacyPolicyStart + privacyPolicyText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    spannableString.setSpan(
        StyleSpan(Typeface.BOLD),
        termsStart,
        termsStart + termsAndConditionText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    // Apply underline style to "Privacy Policy" and "Terms and Conditions"
    spannableString.setSpan(
        privacyPolicyClickableSpan,
        privacyPolicyStart,
        privacyPolicyStart + privacyPolicyText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    spannableString.setSpan(
        termsClickableSpan,
        termsStart,
        termsStart + termsAndConditionText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    // Apply color span to "Privacy Policy" and "Terms and Conditions"
    val primaryColor = context.getColor(R.color.white) // Replace with your primary color
    spannableString.setSpan(
        ForegroundColorSpan(primaryColor),
        privacyPolicyStart,
        privacyPolicyStart + privacyPolicyText.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    spannableString.setSpan(
        ForegroundColorSpan(primaryColor),
        termsStart,
        termsStart + termsAndConditionText.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    // Set the text to the TextView
    text = spannableString

    // Make the TextView clickable
    movementMethod = LinkMovementMethod.getInstance()
}

fun MaterialButton.disableClick() {
    isEnabled = false
    setTextColor(context.getColor(R.color.textSecondary))
    setBackgroundTint(R.color.tile)
}

fun MaterialButton.enableClick() {
    isEnabled = true
    setTextColor(context.getColor(R.color.btnText))
    setBackgroundTint(R.color.primary)
}

fun MaterialButton.setBackgroundTint(colorResId: Int) {
    val color = ContextCompat.getColor(context, colorResId)
    val colorStateList = ColorStateList.valueOf(color)
    backgroundTintList = colorStateList
}

fun TextView.setDrawableStartColor(colorResId: Int) {
    // Get the drawable start (if set)
    val drawables = compoundDrawablesRelative

    // Check if the drawable start is set
    val drawableStart = drawables[0]

    // Check if the drawable is not null and is a VectorDrawable or ShapeDrawable
    if (drawableStart != null && (drawableStart is VectorDrawable || drawableStart is ShapeDrawable || drawableStart is GradientDrawable)) {
        // Get the color you want to use for tinting
        val color = ContextCompat.getColor(context, colorResId)

        // Mutate the drawable to avoid tinting other drawables with the same reference
        val mutableDrawable = drawableStart.mutate()

        // Tint the drawable
        DrawableCompat.setTint(mutableDrawable, color)

        // Set the tinted drawable back to the TextView
        setCompoundDrawablesRelativeWithIntrinsicBounds(mutableDrawable, null, null, null)
    }
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.hide() {
    visibility = View.GONE
}


private var lastClickTime = 0L  // Declare outside the function
fun View.setDebouncedOnClick(debounceTime: Long = 1000L, action: (view: View) -> Unit) {
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            action(it)
        }
    }
}

infix fun View.click(onClick: (View) -> Unit) {
    this.setOnClickListener {
        onClick.invoke(it)
    }
}

fun TextView.setColor(color: Int) {
    setTextColor(ContextCompat.getColor(context, color))
}

fun Activity.showKeyboard(view: View) {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}