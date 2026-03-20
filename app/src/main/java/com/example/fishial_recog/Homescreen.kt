package com.example.fishial_recog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Home Screen
@Composable
fun HomeScreen(navController: NavController) {
    Column( // main vertical container
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button( // Navigate to Photo Screen
            onClick = { navController.navigate(Screen.Picture.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go to Picture Screen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button( // Navigate to History Screen
            onClick = { navController.navigate(Screen.History.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go to History Screen")
        }
    }
}