package com.example.postsapp.domain.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.newsapplication.data.dataSource.local.PostEntity
import com.example.postsapp.domain.useCase.AddPostUseCase
import com.example.postsapp.domain.useCase.GetPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val addPostUseCase: AddPostUseCase
) : ViewModel() {
    private val _localPosts = mutableStateListOf<PostEntity>()
    val localPosts: SnapshotStateList<PostEntity> get() = _localPosts
    val posts = getPostsUseCase().
    cachedIn(viewModelScope)



    fun addNewPost(title: String, body: String) {
        val newPost = PostEntity(
            id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            title = title,
            body = body,
            isLocal = true
        )
        _localPosts.add(0, newPost)

        viewModelScope.launch {
            try {
                addPostUseCase(title, body)
                _localPosts.removeAll { it.title == title && it.body == body }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
