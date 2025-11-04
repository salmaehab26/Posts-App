package com.example.postsapp.domain.useCase

import com.example.newsapplication.data.dataSource.local.PostEntity
import com.example.postsapp.domain.repository.IPostsRepository
import javax.inject.Inject

class AddPostUseCase @Inject constructor(
    private val repository: IPostsRepository
) {
    suspend operator fun invoke(title: String, body: String): PostEntity {
      return  repository.addPost(title, body)
    }
}
