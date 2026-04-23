package com.example.fishial_recog

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Data classes - what your ML code needs to return

// THIS IS WHAT YOUR ML CODE MUST RETURN
// Fill this object with the fish species and fishing tips
data class FishRecognitionResult(
    val species: String,           // e.g., "Smallmouth Bass"
    val recommendedLures: String,  // e.g., "Ned rigs, Tubes, Swimbaits"
    val conditions: String,        // e.g., "65-80°F, Clear water"
    val bestTime: String,          // e.g., "Early morning, Late evening"
    val bestSeason: String,        // e.g., "Spring, Fall"
    val confidence: Float? = null  // Optional: 0.0 to 1.0
)

// Saved catch record - auto created when user saves
data class CatchRecord(
    val id: String = UUID.randomUUID().toString(),
    val species: String,
    val date: String,
    val imageBitmap: Bitmap? = null,
    val location: String? = null,
    val lures: String,
    val conditions: String
)

// UI state - handles loading, errors, and results
data class PictureUiState(
    val selectedImageBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val recognitionResult: FishRecognitionResult? = null,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

class ImageViewModel : ViewModel() {

    val images = mutableStateListOf<String>()

    private val _uiState = MutableStateFlow(PictureUiState())
    val uiState: StateFlow<PictureUiState> = _uiState.asStateFlow()

    private val _catchHistory = MutableStateFlow<List<CatchRecord>>(emptyList())
    val catchHistory: StateFlow<List<CatchRecord>> = _catchHistory.asStateFlow()

    private var currentBitmap: Bitmap? = null
    private var currentResult: FishRecognitionResult? = null

    fun onCameraPermissionGranted() {
        // Called after permission granted
    }

    // Called when user takes a photo with camera
    fun processCapturedImage(context: Context, bitmap: Bitmap) {
        currentBitmap = bitmap
        _uiState.value = _uiState.value.copy(
            selectedImageBitmap = bitmap,
            isLoading = true,
            error = null,
            recognitionResult = null
        )
        performRecognition(bitmap)
    }

    // Called when user picks an image from gallery
    fun handleSelectedImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                currentBitmap = bitmap
                _uiState.value = _uiState.value.copy(
                    selectedImageBitmap = bitmap,
                    isLoading = true,
                    error = null,
                    recognitionResult = null
                )
                performRecognition(bitmap)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load image: ${e.message}"
                )
            }
        }
    }

    // ******************************
    // PUT YOUR ML CODE HERE
    // ******************************
    // Input: bitmap (the fish photo)
    // Output: FishRecognitionResult object
    //
    // Example of what to return:
    // val result = FishRecognitionResult(
    //     species = "Your detected species",
    //     recommendedLures = "Your lure recommendations",
    //     conditions = "Your conditions info",
    //     bestTime = "Your best time info",
    //     bestSeason = "Your best season info",
    //     confidence = yourConfidenceScore
    // )
    // ******************************
    private fun performRecognition(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                // Simulate processing delay (remove this)
                delay(1500)

                // TODO: REPLACE THIS MOCK CODE WITH YOUR ML RECOGNITION
                // Example: val result = YourMLClassifier.identifyFish(bitmap)
                // Example: val result = FishRecognitionService.recognize(bitmap)

                // MOCK RESULT - DELETE THIS AND USE YOUR REAL RESULT
                val mockResult = FishRecognitionResult(
                    species = "Smallmouth Bass",
                    recommendedLures = "Ned rigs, Tubes, Swimbaits",
                    conditions = "65-80°F, Clear water",
                    bestTime = "Early morning, Late evening",
                    bestSeason = "Spring, Fall",
                    confidence = 0.92f
                )

                currentResult = mockResult
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    recognitionResult = mockResult
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Recognition failed: ${e.message}"
                )
            }
        }
    }

    // Saves the current catch to history
    fun saveCurrentCatch() {
        val bitmap = currentBitmap ?: return
        val result = currentResult ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            delay(500)

            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            val catch = CatchRecord(
                species = result.species,
                date = dateFormat.format(Date()),
                imageBitmap = bitmap,
                location = "Lake Michigan",
                lures = result.recommendedLures,
                conditions = result.conditions
            )

            _catchHistory.value = _catchHistory.value + catch
            images.add("${result.species} - ${catch.date}")

            _uiState.value = _uiState.value.copy(
                isSaving = false,
                saveSuccess = true
            )
        }
    }

    // Deletes a catch from history
    fun deleteCatch(id: String) {
        _catchHistory.value = _catchHistory.value.filter { it.id != id }
    }

    fun addImage(imageName: String) {
        images.add(imageName)
    }

    fun resetUiState() {
        _uiState.value = PictureUiState()
        currentBitmap = null
        currentResult = null
    }
}