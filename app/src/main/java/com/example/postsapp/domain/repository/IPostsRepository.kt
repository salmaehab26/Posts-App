package com.example.postsapp.domain.repository

import androidx.paging.PagingData
import com.example.newsapplication.data.dataSource.local.PostEntity
import kotlinx.coroutines.flow.Flow

interface IPostsRepository {
    fun getPosts(): Flow<PagingData<PostEntity>>
    suspend fun addPost(title: String, body: String)
}