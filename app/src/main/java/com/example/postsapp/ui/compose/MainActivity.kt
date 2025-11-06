package com.example.postsapp.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

import com.example.postsapp.ui.theme.Pink40
import dagger.hilt.android.AndroidEntryPoint

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
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home",
                            tint = if (selectedScreen == "home") Color.Black.copy(
                                alpha = 0.6f
                            )
                            else Color.White
                        )
                    },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedScreen == "favorites",
                    onClick = { selectedScreen = "favorites" },
                    icon = {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Favorites",
                            tint = if (selectedScreen == "favorites") Color.Black.copy(
                                alpha = 0.6f
                            )
                            else Color.White
                        )
                    },
                    label = { Text("Favorites") }
                )
                NavigationBarItem(
                    selected = selectedScreen == "settings",
                    onClick = { selectedScreen = "settings" },
                    icon = {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (selectedScreen == "Settings") Color.Black.copy(
                                alpha = 0.6f
                            )
                            else Color.White
                        )
                    },
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

