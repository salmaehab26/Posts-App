package com.example.postsapp.data.dataSource.remote.response

import com.example.newsapplication.data.dataSource.local.PostEntity

data class PostDto(
    val body: String,
    val id: Int,
    val title: String,
    val userId: Int
)
fun PostDto.toEntity() = PostEntity(id, title, body)
