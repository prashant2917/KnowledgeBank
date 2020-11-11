package com.pocket.knowledge.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class NetworkCheck // constructor
(private val _context: Context) {

    companion object {
        @JvmStatic
        fun getJSONString(url: String?): String? {
            var jsonString: String? = null
            var linkConnection: HttpURLConnection? = null
            try {
                val linkurl = URL(url)
                linkConnection = linkurl.openConnection() as HttpURLConnection
                val responseCode = linkConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val linkinStream = linkConnection.inputStream
                    val baos = ByteArrayOutputStream()
                    var j = 0
                    while (linkinStream.read().also { j = it } != -1) {
                        baos.write(j)
                    }
                    val data = baos.toByteArray()
                    jsonString = String(data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                linkConnection?.disconnect()
            }
            return jsonString
        }

        @JvmStatic
        fun isConnect(context: Context): Boolean {
            return try {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null) {
                    activeNetworkInfo.isConnected || activeNetworkInfo.isConnectedOrConnecting
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }

        @JvmStatic
        fun isNetworkAvailable(activity: Activity): Boolean {
            val connectivity = activity
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivity == null) {
                return false
            } else {
                val info = connectivity.allNetworkInfo
                if (info != null) {
                    for (i in info.indices) {
                        if (info[i].state == NetworkInfo.State.CONNECTED) {
                            return true
                        }
                    }
                }
            }
            return false
        }
    }

}