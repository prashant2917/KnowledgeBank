package com.pocket.knowledge.utils

import android.app.Activity
import android.content.Context
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.pocket.knowledge.R
import com.pocket.knowledge.utils.GDPR.getBundleAd
import java.text.ParseException
import java.text.SimpleDateFormat

object Tools {
    @JvmStatic
    fun getTheme(context: Context) {
        val themePref = ThemePref(context)
        if (themePref.isDarkTheme!!) {
            context.setTheme(R.style.AppDarkTheme)
        } else {
            context.setTheme(R.style.AppTheme)
        }
    }

    @JvmStatic
    fun getAdRequest(activity: Activity?): AdRequest {
        return AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, getBundleAd(activity))
                .build()
    }

    @JvmStatic
    fun timeStringtoMilis(time: String?): Long {
        var milis: Long = 0
        try {
            val sd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = sd.parse(time)
            milis = date.time
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return milis
    }

    @JvmStatic
    fun getFormatedDate(date_str: String?): String {
        return if (date_str != null && date_str.trim { it <= ' ' } != "") {
            val oldFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
            val newFormat = SimpleDateFormat("MMMM dd, yyyy HH:mm")
            try {
                newFormat.format(oldFormat.parse(date_str))
            } catch (e: ParseException) {
                ""
            }
        } else {
            ""
        }
    }

    @JvmStatic
    fun getFormatedDateSimple(date_str: String?): String {
        return if (date_str != null && date_str.trim { it <= ' ' } != "") {
            val oldFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
            val newFormat = SimpleDateFormat("MMMM dd, yyyy")
            try {
                newFormat.format(oldFormat.parse(date_str))
            } catch (e: ParseException) {
                ""
            }
        } else {
            ""
        }
    }
}