package com.example.postsapp.di

import com.example.postsapp.data.dataSource.repository.PostsRepositoryImpl
import com.example.postsapp.domain.repository.IPostsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPostsRepository(
        impl: PostsRepositoryImpl
    ): IPostsRepository
}
