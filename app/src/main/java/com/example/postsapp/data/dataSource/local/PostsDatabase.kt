package com.example.newsapplication.data.dataSource.local
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PostEntity::class], version = 2, exportSchema = false)
abstract class PostsDatabase : RoomDatabase() {
    abstract fun postDao(): IPostDao
}