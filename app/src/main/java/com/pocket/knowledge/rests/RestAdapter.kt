package com.pocket.knowledge.rests

import com.pocket.knowledge.BuildConfig
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.rests.ApiInterface
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RestAdapter {
    @JvmStatic
    fun createAPI(): ApiInterface {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(10, TimeUnit.SECONDS)
        builder.writeTimeout(10, TimeUnit.SECONDS)
        builder.readTimeout(30, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(logging)
        }
        builder.cache(null)
        val okHttpClient = builder.build()
        val retrofit = Retrofit.Builder()
                .baseUrl(AppConfig.ADMIN_PANEL_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        return retrofit.create(ApiInterface::class.java)
    }
}