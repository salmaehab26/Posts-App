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

    override suspend fun addPost(title: String, body: String) {
        val localId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

        val localPost = PostEntity(
            id = localId,
            title = title,
            body = body,
            isLocal = true
        )
        dao.insert(localPost)
        Log.d("PostsRepositoryImpl", "Local post: $localPost")


        Log.d("PostsRepositoryImpl", "Local post inserted with ID: $localId")

        try {
            val response = api.createPost(PostRequest(title, body))
            Log.d("PostsRepositoryImpl", "Response: $response")

            val syncedPost = localPost.copy(
                id = localId,
                title = response.title,
                body = response.body,
            )
            Log.d("PostsRepositoryImpl", "Local post: $syncedPost")

            dao.insert(syncedPost)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}