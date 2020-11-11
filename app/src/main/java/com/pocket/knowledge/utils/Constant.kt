package com.pocket.knowledge.utils

import com.pocket.knowledge.config.AppConfig

object Constant {
    const val REGISTER_URL = AppConfig.ADMIN_PANEL_URL + "/api/user_register/?user_type=normal&name="
    const val NORMAL_LOGIN_URL = AppConfig.ADMIN_PANEL_URL + "/api/get_user_login/?email="
    const val FORGET_PASSWORD_URL = AppConfig.ADMIN_PANEL_URL + "/api/forgot_password/?email="
    const val CATEGORY_ARRAY_NAME = "result"
    @JvmField
    var GET_SUCCESS_MSG = 0
    const val MSG = "msg"
    const val SUCCESS = "success"
    const val USER_NAME = "name"
    const val USER_ID = "user_id"
    const val DELAY_REFRESH: Long = 1000
    const val DELAY_PROGRESS_DIALOG = 2000
    const val DELAY_TIME: Long = 1000
    const val YOUTUBE_IMG_FRONT = "https://img.youtube.com/vi/"
    const val YOUTUBE_IMG_BACK = "/mqdefault.jpg"
    const val MAX_SEARCH_RESULT = 100
    const val REGISTRATION_COMPLETE = "registrationComplete"
    const val PUSH_NOTIFICATION = "pushNotification"
    const val NOTIFICATION_CHANNEL_NAME = "news_channel_01"
    const val EXTRA_OBJC = "key.EXTRA_OBJC"
}