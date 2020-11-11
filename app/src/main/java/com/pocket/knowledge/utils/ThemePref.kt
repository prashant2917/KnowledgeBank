package com.pocket.knowledge.utils

import android.content.Context
import android.content.SharedPreferences

class ThemePref(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor
    var isDarkTheme: Boolean?
        get() = sharedPreferences.getBoolean("theme", false)
        set(isDarkTheme) {
            editor.putBoolean("theme", isDarkTheme!!)
            editor.apply()
        }

    companion object {
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
    }

    init {
        editor = sharedPreferences.edit()
    }
}