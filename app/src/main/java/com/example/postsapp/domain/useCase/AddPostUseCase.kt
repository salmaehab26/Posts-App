package com.example.postsapp.domain.useCase

import com.example.postsapp.domain.repository.IPostsRepository
import javax.inject.Inject

class AddPostUseCase @Inject constructor(
    private val repository: IPostsRepository
) {
    suspend operator fun invoke(title: String, body: String) {
        repository.addPost(title, body)
    }
}
