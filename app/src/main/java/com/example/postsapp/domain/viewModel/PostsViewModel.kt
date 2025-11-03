package com.example.postsapp.domain.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.postsapp.domain.useCase.GetPostsUseCase
import com.example.postsapp.domain.useCase.AddPostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val addPostUseCase: AddPostUseCase
) : ViewModel() {

    val posts = getPostsUseCase()
        .cachedIn(viewModelScope)

    fun addNewPost(title: String, body: String) {
        viewModelScope.launch {
            try {
                addPostUseCase(title, body)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
