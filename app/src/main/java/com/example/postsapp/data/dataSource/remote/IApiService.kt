package com.example.newsapplication.data.dataSource.remote

import com.example.postsapp.data.dataSource.remote.response.PostDto
import com.example.postsapp.data.dataSource.remote.response.PostRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IApiService {
    @GET("posts")
    suspend  fun getPosts(@Query("_page") page: Int,
                          @Query("_limit") limit: Int): List<PostDto>
    @POST("posts")
    suspend fun createPost(@Body post: PostRequest): PostDto
}