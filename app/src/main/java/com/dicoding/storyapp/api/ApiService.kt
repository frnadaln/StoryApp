package com.dicoding.storyapp.api

import com.dicoding.storyapp.data.DetailResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.dicoding.storyapp.data.StoryResponse
import com.dicoding.storyapp.data.LoginResponse
import com.dicoding.storyapp.data.SignupResponse
import com.dicoding.storyapp.data.UpResponse
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @FormUrlEncoded
    @POST("register")
    suspend fun signup(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
    ): SignupResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String,
    ): LoginResponse

    @GET("stories")
    fun getStory(
        @Header("Authorization") token: String,
    ): Call<StoryResponse>

    @GET("stories")
    suspend fun getStories(
        @Query("page") page : Int = 1,
        @Query("size") size : Int = 20
    ): StoryResponse

    @GET("stories/{id}")
    fun detailStory(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Call<DetailResponse>

    @Multipart
    @POST("stories")
    suspend fun upStory(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat : RequestBody? = null,
        @Part("lon") lon : RequestBody? = null
    ): UpResponse

    @GET("stories")
    suspend fun getStoriesWithLocation(
        @Header("Authorization") token: String,
        @Query("location") location : Int = 1,
    ) : StoryResponse
}
