package com.example.inapppurchasedemo.core.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun Context.isOnline(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return try {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        capabilities.checkConnectivity()
    } catch (e: SecurityException) {
        e.printStackTrace()
        false
    }
}

fun NetworkCapabilities.checkConnectivity(): Boolean {
    return hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
            hasTransport(NetworkCapabilities.TRANSPORT_VPN)
}