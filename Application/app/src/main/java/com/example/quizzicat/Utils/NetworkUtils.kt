package com.example.quizzicat.Utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo

class NetworkUtils {
    companion object { // static methods and attributes
        var TYPE_WIFI = 1
        var TYPE_MOBILE = 2
        var TYPE_NO_CONNECTION = 0

        fun getConnectivityStatus(context: Context) : Int {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return TYPE_NO_CONNECTION
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return TYPE_NO_CONNECTION
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> TYPE_WIFI
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> TYPE_MOBILE
                else -> TYPE_NO_CONNECTION
            }
        }
    }
}