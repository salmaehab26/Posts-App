package com.example.postsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.newsapplication.data.dataSource.local.PostEntity
import com.example.postsapp.domain.viewModel.PostsViewModel
import com.example.postsapp.ui.theme.Pink40
import com.example.postsapp.ui.theme.PostsAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeoutException

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PostsAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PostsScreen()
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun PostsScreen(vm: PostsViewModel = hiltViewModel()) {
    val posts = vm.posts.collectAsLazyPagingItems()
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val isRefreshing = posts.loadState.refresh is LoadState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { posts.refresh() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Posts") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Pink40)
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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            when (val state = posts.loadState.refresh) {
                is LoadState.Loading -> {
                    // لو في تحميل (بما في ذلك أول تحميل لو النت بطئ) - نعرض دائرة تحميل
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is LoadState.Error -> {
                    // لو حصل خطأ (بما في ذلك TimeoutException) - نعرض رسالة و Try Again
                    ErrorItem(
                        message = when {
                            state.error is java.util.concurrent.TimeoutException ->
                                "Connection timeout. Please check your internet and try again."
                            else ->
                                "Failed to load posts: ${state.error.message ?: "Unknown error"}"
                        },
                        onRetry = { posts.retry() }
                    )
                }

                is LoadState.NotLoading -> {
                    if (posts.itemCount == 0) {
                        Text(
                            "No posts available. Pull to refresh.",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                count = posts.itemCount,
                                key = { index -> posts.peek(index)?.id ?: index }
                            ) { index ->
                                posts[index]?.let { post ->
                                    PostItem(post)
                                }
                            }

                            if (posts.loadState.append is LoadState.Loading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }

                            if (posts.loadState.append is LoadState.Error) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Button(onClick = { posts.retry() }) {
                                            Text("Load More")
                                        }
                                    }
                                }
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
                    delay(100)
                    listState.animateScrollToItem(0)
                }
            }
        )
    }
}
@Composable
fun PostItem(post: PostEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ErrorItem(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}
