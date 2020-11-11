package com.pocket.knowledge.rests

import com.pocket.knowledge.callbacks.*
import com.pocket.knowledge.models.User
import com.pocket.knowledge.models.Value
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {
    @Headers(CACHE, AGENT)
    @GET("api/get_news_detail")
    fun getNewsDetail(
            @Query("id") id: Long
    ): Call<PostDetailCallback?>?

    @Headers(CACHE, AGENT)
    @GET("api/get_recent_posts")
    fun getRecentPost(
            @Query("api_key") api_key: String?,
            @Query("page") page: Int,
            @Query("count") count: Int
    ): Call<RecentCallback?>?

    @Headers(CACHE, AGENT)
    @GET("api/get_video_posts")
    fun getVideoPost(
            @Query("api_key") api_key: String?,
            @Query("page") page: Int,
            @Query("count") count: Int
    ): Call<RecentCallback?>?

    @Headers(CACHE, AGENT)
    @GET("api/get_category_index")
    fun getAllCategories(
            @Query("api_key") api_key: String?
    ): Call<CategoriesCallback?>?

    @Headers(CACHE, AGENT)
    @GET("api/get_category_posts")
    fun getCategoryDetailsByPage(
            @Query("id") id: Long,
            @Query("api_key") api_key: String?,
            @Query("page") page: Long,
            @Query("count") count: Long
    ): Call<CategoryDetailsCallback?>?

    @Headers(CACHE, AGENT)
    @GET("api/get_search_results")
    fun getSearchPosts(
            @Query("api_key") api_key: String?,
            @Query("search") search: String?,
            @Query("count") count: Int
    ): Call<RecentCallback?>?

    @Headers(CACHE, AGENT)
    @GET("api/get_comments")
    fun getComments(@Query("nid") nid: Long?
    ): Call<CommentsCallback?>?

    @FormUrlEncoded
    @POST("api/post_comment")
    fun sendComment(@Field("nid") nid: Long?,
                    @Field("user_id") user_id: String?,
                    @Field("content") content: String?,
                    @Field("date_time") date_time: String?): Call<Value?>?

    @FormUrlEncoded
    @POST("api/update_comment")
    fun updateComment(@Field("comment_id") comment_id: String?,
                      @Field("date_time") date_time: String?,
                      @Field("content") content: String?): Call<Value?>?

    @FormUrlEncoded
    @POST("api/delete_comment")
    fun deleteComment(@Field("comment_id") comment_id: String?): Call<Value?>?

    @FormUrlEncoded
    @POST("api/update_user_data")
    fun updateUserData(
            @Field("id") id: String?,
            @Field("name") name: String?,
            @Field("email") email: String?,
            @Field("password") password: String?
    ): Call<User?>?

    @FormUrlEncoded
    @POST("api/update_photo_profile")
    fun updatePhotoProfile(
            @Field("id") id: String?,
            @Field("name") name: String?,
            @Field("email") email: String?,
            @Field("password") password: String?,
            @Field("old_image") old_image: String?,
            @Field("image") image: String?
    ): Call<User?>?

    @get:GET("api/get_settings")
    @get:Headers(CACHE, AGENT)
    val settings: Call<SettingsCallback?>?

    @Headers(CACHE, AGENT)
    @GET("api/get_user_data")
    fun getUser(
            @Query("id") id: String?
    ): Call<UserCallback?>?

    @get:GET("api/get_app_data")
    @get:Headers(CACHE, AGENT)
    val appData: Call<AppDataCallback?>?

    @Headers(CACHE, AGENT)
    @GET("api/get_user_token")
    fun getUserToken(
            @Query("user_unique_id") user_unique_id: String?
    ): Call<UserCallback?>?

    companion object {
        const val CACHE = "Cache-Control: max-age=0"
        const val AGENT = "Data-Agent: Android News App"
    }
}