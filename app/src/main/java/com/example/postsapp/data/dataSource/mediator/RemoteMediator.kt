package com.example.postsapp.data.dataSource.mediator

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.newsapplication.data.dataSource.local.PostEntity
import com.example.newsapplication.data.dataSource.local.PostsDatabase
import com.example.newsapplication.data.dataSource.remote.IApiService
import com.example.newsapplication.utils.NetworkUtils.isNetworkAvailable
import com.example.postsapp.data.dataSource.remote.response.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeoutException

@OptIn(ExperimentalPagingApi::class)
class PostsRemoteMediator(
    private val api: IApiService,
    private val db: PostsDatabase,
    private val context: Context
) : RemoteMediator<Int, PostEntity>() {

    private val dao = db.postDao()
    private var currentPage = 1

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.APPEND -> currentPage + 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            }

            if (!isNetworkAvailable(context)) {
                Log.d("RemoteMediator", "No network - using cached data")
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            val response = withTimeoutOrNull(30_000L) {
                api.getPosts(page, state.config.pageSize)
            }

            if (response == null) {
                Log.d("RemoteMediator", "Request timeout")

                val hasLocalData = dao.getAllPostsOnce().isNotEmpty()
                return if (hasLocalData) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Request timeout: showing saved posts",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    MediatorResult.Success(endOfPaginationReached = true)
                } else {
                    MediatorResult.Error(TimeoutException("Connection timeout"))
                }
            }

            val remotePosts = response.map { it.toEntity().copy(isPending = false) }

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {

                    dao.deleteOldApiPosts()
                }
                dao.insertAll(remotePosts)
            }

            if (remotePosts.isNotEmpty()) {
                currentPage = page
            }

            Log.d("RemoteMediator", "Loaded ${remotePosts.size} posts for page $page")
            MediatorResult.Success(endOfPaginationReached = remotePosts.isEmpty())

        } catch (e: Exception) {
            Log.e("RemoteMediator", "Load failed", e)

            val hasLocalData = dao.getAllPostsOnce().isNotEmpty()
            if (hasLocalData && loadType == LoadType.REFRESH) {

                 MediatorResult.Success(endOfPaginationReached = true)
            }

            MediatorResult.Error(e)
        }
    }


}
