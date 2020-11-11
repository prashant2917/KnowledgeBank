package com.pocket.knowledge.callbacks

import com.pocket.knowledge.models.Comments
import java.util.*

class CommentsCallback {
    @JvmField
    var status = ""

    @JvmField
    var comments: List<Comments> = ArrayList()
}