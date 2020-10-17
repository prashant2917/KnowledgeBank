package com.pocket.knowledge.rests;

import com.pocket.knowledge.callbacks.AppDataCallback;
import com.pocket.knowledge.callbacks.CategoriesCallback;
import com.pocket.knowledge.callbacks.CategoryDetailsCallback;
import com.pocket.knowledge.callbacks.CommentsCallback;
import com.pocket.knowledge.callbacks.PostDetailCallback;
import com.pocket.knowledge.callbacks.RecentCallback;
import com.pocket.knowledge.callbacks.SettingsCallback;
import com.pocket.knowledge.callbacks.UserCallback;
import com.pocket.knowledge.models.User;
import com.pocket.knowledge.models.Value;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: Android News App";

    @Headers({CACHE, AGENT})
    @GET("api/get_news_detail")
    Call<PostDetailCallback> getNewsDetail(
            @Query("id") long id
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_recent_posts")
    Call<RecentCallback> getRecentPost(
            @Query("api_key") String api_key,
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_video_posts")
    Call<RecentCallback> getVideoPost(
            @Query("api_key") String api_key,
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_category_index")
    Call<CategoriesCallback> getAllCategories(
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_category_posts")
    Call<CategoryDetailsCallback> getCategoryDetailsByPage(
            @Query("id") long id,
            @Query("api_key") String api_key,
            @Query("page") long page,
            @Query("count") long count
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_search_results")
    Call<RecentCallback> getSearchPosts(
            @Query("api_key") String api_key,
            @Query("search") String search,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_comments")
    Call<CommentsCallback> getComments(@Query("nid") Long nid
    );

    @FormUrlEncoded
    @POST("api/post_comment")
    Call<Value> sendComment(@Field("nid") Long nid,
                            @Field("user_id") String user_id,
                            @Field("content") String content,
                            @Field("date_time") String date_time);

    @FormUrlEncoded
    @POST("api/update_comment")
    Call<Value> updateComment(@Field("comment_id") String comment_id,
                              @Field("date_time") String date_time,
                              @Field("content") String content);

    @FormUrlEncoded
    @POST("api/delete_comment")
    Call<Value> deleteComment(@Field("comment_id") String comment_id);

    @FormUrlEncoded
    @POST("api/update_user_data")
    Call<User> updateUserData(
            @Field("id") String id,
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("api/update_photo_profile")
    Call<User> updatePhotoProfile(
            @Field("id") String id,
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password,
            @Field("old_image") String old_image,
            @Field("image") String image
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_settings")
    Call<SettingsCallback> getSettings();

    @Headers({CACHE, AGENT})
    @GET("api/get_user_data")
    Call<UserCallback> getUser(
            @Query("id") String id
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_app_data")
    Call<AppDataCallback> getAppData();

    @Headers({CACHE, AGENT})
    @GET("api/get_user_token")
    Call<UserCallback> getUserToken(
            @Query("user_unique_id") String user_unique_id
    );

}
