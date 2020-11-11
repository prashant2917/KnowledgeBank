package com.pocket.knowledge.callbacks

import com.pocket.knowledge.models.Images
import com.pocket.knowledge.models.News
import java.io.Serializable
import java.util.*

class PostDetailCallback : Serializable {
    @JvmField
    var status = ""
    @JvmField
    var post: News? = null
    @JvmField
    var images: List<Images> = ArrayList()
    @JvmField
    var related: List<News> = ArrayList()
}