package com.pocket.knowledge.models

import java.io.Serializable

class Setting : Serializable {
    @JvmField
    var package_name: String? = null
    @JvmField
    var comment_approval: String? = null
    @JvmField
    var privacy_policy: String? = null

}