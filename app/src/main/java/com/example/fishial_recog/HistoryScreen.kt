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
fun HistoryScreen(navController: NavController, imageViewModel: ImageViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title Screen
        Text("History", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn { // Scrollable list that renders only visible items
            // iterate over all stored images
            items(imageViewModel.images) { imageName ->
                // Displays each image as text
                Text(text = imageName, modifier = Modifier.padding(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to return to the main screen
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}