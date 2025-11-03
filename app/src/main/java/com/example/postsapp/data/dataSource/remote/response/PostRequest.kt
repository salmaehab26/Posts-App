package com.example.postsapp.data.dataSource.remote.response

data class PostRequest(
    val title: String,
    val body: String,
    val userId: Int = 1
)
