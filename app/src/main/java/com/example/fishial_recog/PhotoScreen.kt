package com.example.fishial_recog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun PictureScreen(navController: NavController, imageViewModel: ImageViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Title Screen
        Text("Picture Screen", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { // plan to make this open users phone camera
            // Simulate taking a picture
            val fakeImage = "fake_camera_image_${imageViewModel.images.size + 1}.jpg"
            imageViewModel.addImage(fakeImage)
        }) {
            Text("Take Picture")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { // Plan to make this open users photos
            // Simulate uploading an image
            val fakeUpload = "fake_uploaded_image_${imageViewModel.images.size + 1}.jpg"
            imageViewModel.addImage(fakeUpload)
        }) {
            Text("Upload Image")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // back to main screen
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}