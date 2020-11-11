package com.pocket.knowledge.models

import java.io.Serializable

class News : Serializable {
    var id = 0
    @JvmField
    var nid: Long = -1
    @JvmField
    var news_title = ""
    @JvmField
    var category_name = ""
    @JvmField
    var news_date = ""
    @JvmField
    var news_image = ""
    @JvmField
    var news_description = ""
    @JvmField
    var content_type = ""
    @JvmField
    var video_url = ""
    @JvmField
    var video_id = ""
    @JvmField
    var comments_count: Long = -1

    constructor(nid: Long, news_title: String, category_name: String, news_date: String, news_image: String, news_description: String, content_type: String, video_url: String, video_id: String, comments_count: Long) {
        this.nid = nid
        this.news_title = news_title
        this.category_name = category_name
        this.news_date = news_date
        this.news_image = news_image
        this.news_description = news_description
        this.content_type = content_type
        this.video_url = video_url
        this.video_id = video_id
        this.comments_count = comments_count
    }

    constructor() {}
    constructor(nid: Long) {
        this.nid = nid
    }

}