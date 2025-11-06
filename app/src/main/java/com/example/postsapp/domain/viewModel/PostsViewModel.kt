package com.example.postsapp.domain.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.newsapplication.data.dataSource.local.PostEntity
import com.example.postsapp.domain.useCase.AddPostUseCase
import com.example.postsapp.domain.useCase.GetPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val addPostUseCase: AddPostUseCase
) : ViewModel() {

    val posts = getPostsUseCase().cachedIn(viewModelScope)

    private val _isAddingPost = MutableStateFlow(false)

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