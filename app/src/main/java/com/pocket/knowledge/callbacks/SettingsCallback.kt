package com.pocket.knowledge.callbacks

import com.pocket.knowledge.models.Setting
import java.io.Serializable

class SettingsCallback : Serializable {
    @JvmField
    var status = ""
    @JvmField
    var post: Setting? = null
}