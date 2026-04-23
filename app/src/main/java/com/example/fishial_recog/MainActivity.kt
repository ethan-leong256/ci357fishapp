package com.example.fishial_recog

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fishial_recog.ui.theme.FishialRecogTheme

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Picture : Screen("picture")
    object History : Screen("history")
    object Result : Screen("result")  // Optional: separate result screen
}

@Composable
fun AppNavigation(imageViewModel: ImageViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Permission launcher for camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imageViewModel.onCameraPermissionGranted()
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { imageViewModel.handleSelectedImage(context, it) }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Picture.route) {
            PictureScreen(
                navController = navController,
                imageViewModel = imageViewModel,
                onRequestCameraPermission = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                onLaunchGallery = { galleryLauncher.launch("image/*") }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(navController, imageViewModel)
        }
    }
}

class MainActivity : ComponentActivity() {
    private val imageViewModel by viewModels<ImageViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FishialRecogTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(imageViewModel)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    val fakeViewModel: ImageViewModel = viewModel()
    FishialRecogTheme {
        AppNavigation(fakeViewModel)
    }
}