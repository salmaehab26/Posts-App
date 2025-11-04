package com.example.postsapp.data.dataSource.mediator

import android.content.Context
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
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.APPEND -> currentPage + 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
        }

        val isConnected = isNetworkAvailable(context)


        return try {
            if (!isConnected) {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            val response = api.getPosts(page, state.config.pageSize)
            val remotePosts = response.map { it.toEntity().copy(isLocal = false) }

            db.withTransaction {
                // 👇 only delete remote posts, not local ones
                if (loadType == LoadType.REFRESH && remotePosts.isNotEmpty()) {
                    dao.deleteNonLocalPosts()
                }
                dao.insertAll(remotePosts)
            }

            if (remotePosts.isNotEmpty()) currentPage = page

            MediatorResult.Success(endOfPaginationReached = remotePosts.isEmpty())

        } catch (e: Exception) {
            e.printStackTrace()
            MediatorResult.Error(e)
        }
    }
}
