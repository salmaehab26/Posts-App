package com.example.postsapp.di

import android.content.Context
import androidx.room.Room
import com.example.newsapplication.data.dataSource.local.PostsDatabase
import com.example.newsapplication.data.dataSource.remote.IApiService
import com.example.newsapplication.data.dataSource.remote.RetrofitInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PostsDatabase {
        return Room.databaseBuilder(
            context,
            PostsDatabase::class.java,
            "posts_db"
        )
            .fallbackToDestructiveMigration() // عشان لو غيرت version أثناء التطوير
            .build()
    }

    @Provides
    @Singleton
    fun providePostDao(db: PostsDatabase) = db.postDao()

    @Provides
    @Singleton
    fun provideApiService(): IApiService = RetrofitInstance.api
}
