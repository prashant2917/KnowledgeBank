package com.pocket.knowledge.utils

import com.pocket.knowledge.BuildConfig

class AppDataUtil {
companion object{
    fun getAppVersionCode():Int{
        return BuildConfig.VERSION_CODE
    }

    fun getAppVersionName():String{
        return BuildConfig.VERSION_NAME
    }
}
}