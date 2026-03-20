package com.example.fishial_recog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.fishial_recog.ui.theme.FishialRecogTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel

// all possible screens
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Picture : Screen("picture")
    object History : Screen("history")
}

// handles navigation between screens
@Composable
fun AppNavigation(imageViewModel: ImageViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Picture.route) {
            PictureScreen(navController, imageViewModel)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController, imageViewModel)
        }
    }
}

// set up UI and ViewModel
class MainActivity : ComponentActivity() {
    private val imageViewModel by viewModels<ImageViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FishialRecogTheme {
                AppNavigation(imageViewModel)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    val fakeViewModel: ImageViewModel = viewModel() // Provides a preview ViewModel
    FishialRecogTheme {
        AppNavigation(fakeViewModel)
    }
}