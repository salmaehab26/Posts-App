package com.example.postsapp.data.dataSource.repository

import android.content.Context
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.newsapplication.data.dataSource.local.PostEntity
import com.example.newsapplication.data.dataSource.local.PostsDatabase
import com.example.newsapplication.data.dataSource.remote.IApiService
import com.example.postsapp.data.dataSource.mediator.PostsRemoteMediator
import com.example.postsapp.data.dataSource.remote.response.PostRequest
import com.example.postsapp.domain.repository.IPostsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostsRepositoryImpl @Inject constructor(
    private val db: PostsDatabase,
    private val api: IApiService,
    @ApplicationContext private val context: Context
) : IPostsRepository {

    private val dao = db.postDao()

    @OptIn(ExperimentalPagingApi::class)
    override fun getPosts(): Flow<PagingData<PostEntity>> {
        val pagingSourceFactory = { dao.pagingSource() }

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                prefetchDistance = 2,
                initialLoadSize = 10
            ),
            remoteMediator = PostsRemoteMediator(api, db, context),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    override suspend fun addPost(title: String, body: String): PostEntity {
        val newId = System.currentTimeMillis().toInt()

        val newPost = PostEntity(
            id = newId,
            title = title,
            body = body,
            isPending = false
        )

        dao.insert(newPost)
        Log.d("PostsRepository", "Post saved to Room: $newPost")

        try {
            val response = api.createPost(PostRequest(title, body))
            Log.d("PostsRepository", "Post synced with API: $response")
        } catch (e: Exception) {
            Log.e("PostsRepository", "Failed to sync with API (post still saved locally)", e)
        }

        return newPost
    }
}