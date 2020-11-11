package com.pocket.knowledge.activities

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.multidex.MultiDex
import com.onesignal.OneSignal

class MyApplication : Application() {
    private var preferences: SharedPreferences? = null
    var activity: Activity? = null
    private var prefName = "news"
    override fun onCreate() {
        super.onCreate()
        instance = this

        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun saveIsLogin(flag: Boolean) {
        preferences = getSharedPreferences(prefName, 0)
        val editor = preferences!!.edit()
        editor.putBoolean("IsLoggedIn", flag)
        editor.commit()
    }

    val isLogin: Boolean
        get() {
            preferences = getSharedPreferences(prefName, 0)
            return if (preferences != null) {
                preferences!!.getBoolean(
                        "IsLoggedIn", false)
            } else false
        }

    fun saveLogin(user_id: String?, user_name: String?, email: String?) {
        preferences = getSharedPreferences(prefName, 0)
        val editor = preferences!!.edit()
        editor.putString("user_id", user_id)
        editor.putString("user_name", user_name)
        editor.putString("email", email)
        editor.commit()
    }

    val userId: String?
        get() {
            preferences = getSharedPreferences(prefName, 0)
            return if (preferences != null) {
                preferences!!.getString(
                        "user_id", "")
            } else ""
        }

    val userName: String?
        get() {
            preferences = getSharedPreferences(prefName, 0)
            return if (preferences != null) {
                preferences!!.getString(
                        "user_name", "")
            } else ""
        }

    val userEmail: String?
        get() {
            preferences = getSharedPreferences(prefName, 0)
            return if (preferences != null) {
                preferences!!.getString(
                        "email", "")
            } else ""
        }

    val type: String?
        get() {
            preferences = getSharedPreferences(prefName, 0)
            return if (preferences != null) {
                preferences!!.getString(
                        "type", "")
            } else ""
        }

    fun saveType(type: String?) {
        preferences = getSharedPreferences(prefName, 0)
        val editor = preferences!!.edit()
        editor.putString("type", type)
        editor.commit()
    }

    companion object {
        @get:Synchronized
        lateinit var instance: MyApplication
            private set
    }

    init {
        instance = this
    }
}