package com.example.newsapplication.data.dataSource.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)

    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, PostEntity>


    @Query("DELETE FROM posts WHERE id < 1000000000")
    suspend fun deleteOldApiPosts()

    @Query("SELECT * FROM posts ORDER BY id DESC")
    suspend fun getAllPostsOnce(): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)
}