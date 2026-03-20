package com.example.fishial_recog
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

// View model for managing image data
class ImageViewModel : ViewModel() {
    // This stores the fake uploaded images
    val images = mutableStateListOf<String>()

    // Function to add a new fake image
    fun addImage(imageName: String) {
        images.add(imageName)
    }
}
