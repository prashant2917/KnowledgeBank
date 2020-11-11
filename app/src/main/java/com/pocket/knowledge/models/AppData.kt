package com.pocket.knowledge.models

import java.io.Serializable

class AppData : Serializable {
    var app_id = 0
    @JvmField
    var version_code: String? = null
    var version_name: String? = null

}