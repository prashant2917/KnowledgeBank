package com.pocket.knowledge.callbacks

import com.pocket.knowledge.models.News
import java.util.*

class RecentCallback {
    @JvmField
    var status = ""
    var countTotal = -1

    @JvmField
    var posts: List<News> = ArrayList()
}