package com.example.labportal.network

import android.content.Context
import android.content.SharedPreferences

object ApiConstants {
    private const val PREFS_NAME = "lab_portal_settings"
    private const val KEY_SERVER_URL = "key_server_url"

    // Official Production Cloud Backend on Vercel
    const val VERCEL_URL = "https://lab-portal-one.vercel.app/"
    const val VERCEL_DIRECT_URL = "https://lab-portal-fwmrimn0q-adarsh-9213.vercel.app/"

    // Local Fallback URLs
    const val USB_ADB_URL = "http://127.0.0.1:8000/"
    const val HOTSPOT_WIFI_URL = "http://10.87.254.19:8000/"

    private var currentUrl: String = VERCEL_URL

    fun init(context: Context) {
        try {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            currentUrl = prefs.getString(KEY_SERVER_URL, VERCEL_URL) ?: VERCEL_URL
        } catch (_: Exception) {
            currentUrl = VERCEL_URL
        }
    }

    fun setServerUrl(context: Context, newUrl: String) {
        val formatted = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        currentUrl = formatted
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SERVER_URL, formatted).apply()
        NetworkClient.rebuildClient()
    }

    val BASE_URL: String
        get() = currentUrl
}
