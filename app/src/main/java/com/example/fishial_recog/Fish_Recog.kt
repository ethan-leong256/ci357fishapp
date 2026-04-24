package com.example.fishial_recog

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class FishClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    companion object {
        const val MODEL_FILE = "fish_model.tflite"
        const val LABELS_FILE = "labels.txt"
        const val IMAGE_SIZE = 224
        const val NUM_CHANNELS = 3
        const val NUM_BYTES_PER_CHANNEL = 4 // Float32
    }

    init {
        try {
            setupInterpreter()
        } catch (e: Exception) {
            throw RuntimeException("Failed to load model '$MODEL_FILE': ${e.message}", e)
        }
        try {
            loadLabels()
        } catch (e: Exception) {
            throw RuntimeException("Failed to load labels '$LABELS_FILE': ${e.message}", e)
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(MODEL_FILE)
        val fileChannel = FileInputStream(assetFileDescriptor.fileDescriptor).channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.declaredLength
        )
    }

    private fun setupInterpreter() {
        val modelBuffer = loadModelFile()
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }
        interpreter = Interpreter(modelBuffer, options)
    }

    private fun loadLabels() {
        labels = context.assets.open(LABELS_FILE)
            .bufferedReader()
            .readLines()
            .filter { it.isNotBlank() } // ignore empty lines
    }

    fun classify(bitmap: Bitmap): Pair<String, Float> {
        val interp = interpreter
            ?: throw IllegalStateException("Interpreter not initialized")

        if (labels.isEmpty()) {
            throw IllegalStateException("Labels file is empty or failed to load")
        }

        val inputBuffer = preprocessImage(bitmap)
        val outputArray = Array(1) { FloatArray(labels.size) }

        interp.run(inputBuffer, outputArray)

        val scores = outputArray[0]
        val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: 0
        return Pair(labels[maxIndex], scores[maxIndex])
    }

    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val softwareBitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O
            && bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }

        // Change all remaining references from 'bitmap' to 'softwareBitmap'
        val resized = Bitmap.createScaledBitmap(softwareBitmap, IMAGE_SIZE, IMAGE_SIZE, true)
        val byteBuffer = ByteBuffer.allocateDirect(
            IMAGE_SIZE * IMAGE_SIZE * NUM_CHANNELS * NUM_BYTES_PER_CHANNEL
        ).apply { order(ByteOrder.nativeOrder()) }

        val pixels = IntArray(IMAGE_SIZE * IMAGE_SIZE)
        resized.getPixels(pixels, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE)

        for (pixel in pixels) {
            byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f) // R
            byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)  // G
            byteBuffer.putFloat((pixel and 0xFF) / 255.0f)           // B
        }

        return byteBuffer
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}