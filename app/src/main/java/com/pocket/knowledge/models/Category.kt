package com.pocket.knowledge.models

import java.io.Serializable

class Category : Serializable {
    @JvmField
    var cid: Long = -1
    @JvmField
    var categoryName = ""
    @JvmField
    var categoryImage = ""
    @JvmField
    var postCount: Long = -1
}