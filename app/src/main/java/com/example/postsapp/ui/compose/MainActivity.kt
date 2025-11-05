package com.example.postsapp.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.newsapplication.data.dataSource.local.PostEntity
import com.example.postsapp.domain.viewModel.PostsViewModel
import com.example.postsapp.ui.theme.Pink40
import com.example.postsapp.ui.theme.PostsAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
                val navController = rememberNavController()
                MainScreen(navController)

        }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    var selectedScreen by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Pink40) {
                NavigationBarItem(
                    selected = selectedScreen == "home",
                    onClick = { selectedScreen = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedScreen == "favorites",
                    onClick = { selectedScreen = "favorites" },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") }
                )
                NavigationBarItem(
                    selected = selectedScreen == "settings",
                    onClick = { selectedScreen = "settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedScreen) {
                "home" -> AppNavHost(navController)
                "favorites" -> SimpleScreen("Favorites Screen")
                "settings" -> SimpleScreen("Settings Screen")
            }
        }
    }
}

@Composable
fun SimpleScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.titleLarge)
    }
}

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

@Composable
fun PostItem(post: PostEntity, onClick: (PostEntity) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clickable { onClick(post) },
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "post_list") {
        composable("post_list") {
            PostsScreen(navController)
        }
        composable(
            route = "post_detail/{postTitle}/{postBody}",
            arguments = listOf(
                navArgument("postTitle") { type = NavType.StringType },
                navArgument("postBody") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("postTitle") ?: ""
            val body = backStackEntry.arguments?.getString("postBody") ?: ""
            PostDetailScreen(title, body)
        }
    }
}

@Composable
fun PostDetailScreen(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Post Details", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                Spacer(Modifier.height(12.dp))
                Text(text = body, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
