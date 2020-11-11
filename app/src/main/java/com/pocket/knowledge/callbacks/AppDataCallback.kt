package com.pocket.knowledge.callbacks

import com.pocket.knowledge.models.AppData
import java.io.Serializable

class AppDataCallback : Serializable {
    @JvmField
    var status = ""
    @JvmField
    var appData: AppData? = null
}