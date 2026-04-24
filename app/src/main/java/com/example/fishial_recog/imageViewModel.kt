package com.example.fishial_recog

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class FishRecognitionResult(
    val species: String,
    val recommendedLures: String,
    val conditions: String,
    val bestTime: String,
    val bestSeason: String,
    val confidence: Float? = null
)

data class CatchRecord(
    val id: String = UUID.randomUUID().toString(),
    val species: String,
    val date: String,
    val imageBitmap: Bitmap? = null,
    val location: String? = null,
    val lures: String,
    val conditions: String
)

data class PictureUiState(
    val selectedImageBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val recognitionResult: FishRecognitionResult? = null,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

class ImageViewModel(application: Application) : AndroidViewModel(application) {

    val images = mutableStateListOf<String>()

    private val _uiState = MutableStateFlow(PictureUiState())
    val uiState: StateFlow<PictureUiState> = _uiState.asStateFlow()

    private val _catchHistory = MutableStateFlow<List<CatchRecord>>(emptyList())
    val catchHistory: StateFlow<List<CatchRecord>> = _catchHistory.asStateFlow()

    private var currentBitmap: Bitmap? = null
    private var currentResult: FishRecognitionResult? = null

    private var fishClassifier: FishClassifier? = null

    private fun getClassifier(): FishClassifier {
        return fishClassifier ?: FishClassifier(
            getApplication<Application>().applicationContext
        ).also { fishClassifier = it }
    }

    fun onCameraPermissionGranted() { }

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

    private fun performRecognition(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val (species, confidence) = getClassifier().classify(bitmap)

                val result = FishRecognitionResult(
                    species = species,
                    recommendedLures = "Check local guides",
                    conditions = "Varies by species",
                    bestTime = "Early morning, Late evening",
                    bestSeason = "Spring, Fall",
                    confidence = confidence
                )

                currentResult = result
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    recognitionResult = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Recognition failed: ${e.message}"
                )
            }
        }
    }

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

    override fun onCleared() {
        super.onCleared()
        fishClassifier?.close()
        fishClassifier = null
    }
}