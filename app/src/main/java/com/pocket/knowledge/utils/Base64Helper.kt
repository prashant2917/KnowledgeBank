package com.pocket.knowledge.utils

import android.util.Base64
import java.io.UnsupportedEncodingException

object Base64Helper {

    @JvmStatic
    @Throws(UnsupportedEncodingException::class)
    fun encrypt(text: String): String {
        val data = text.toByteArray(charset("UTF-8"))
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

}