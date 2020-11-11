package com.pocket.knowledge.callbacks

import com.pocket.knowledge.models.Category
import com.pocket.knowledge.models.News
import java.util.*

class CategoryDetailsCallback {
    @JvmField
    var status = ""
    var category: Category? = null
    @JvmField
    var posts: List<News> = ArrayList()
}