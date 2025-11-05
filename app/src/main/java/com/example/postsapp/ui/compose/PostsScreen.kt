package com.example.postsapp.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.postsapp.domain.viewModel.PostsViewModel
import com.example.postsapp.ui.theme.Pink40
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun PostsScreen(navController: NavHostController, vm: PostsViewModel = hiltViewModel()) {
    val posts = vm.posts.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    val isRefreshing = posts.loadState.refresh is LoadState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { posts.refresh() }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Posts",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Pink40
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Pink40
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Post")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            when (val state = posts.loadState.refresh) {
                is LoadState.Loading -> if (posts.itemCount == 0)
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                is LoadState.Error -> if (posts.itemCount == 0)
                    ErrorItem(
                        message = "Failed to load posts: ${state.error.message}",
                        onRetry = { posts.refresh() }
                    )

                is LoadState.NotLoading -> if (posts.itemCount == 0)
                    ErrorItem(
                        message = "No posts available.",
                        onRetry = { posts.refresh() }
                    )
            }

            if (posts.itemCount > 0) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts.itemCount) { index ->
                        posts[index]?.let { post ->
                            PostItem(post) {
                                navController.navigate("post_detail/${post.title}/${post.body}")
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    if (showDialog) {
        AddPostDialog(
            onDismiss = { showDialog = false },
            onAdd = { title, body ->
                vm.addNewPost(title, body)
                showDialog = false
                scope.launch {
                    delay(200)
                    listState.animateScrollToItem(0)
                }
            }
        )
    }
}
