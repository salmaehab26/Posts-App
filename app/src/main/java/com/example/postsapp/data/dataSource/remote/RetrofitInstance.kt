package com.example.newsapplication.data.dataSource.remote

import com.example.postsapp.utils.TimeoutInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

//    private val logging = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(TimeoutInterceptor())

        .build()

    val api: IApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(IApiService::class.java)
    }
}