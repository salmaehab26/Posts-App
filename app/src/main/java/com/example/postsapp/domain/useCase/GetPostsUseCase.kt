package com.example.postsapp.domain.useCase
import com.example.postsapp.domain.repository.IPostsRepository
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(
    private val repository: IPostsRepository
) {    operator fun invoke() = repository.getPosts()
}