package com.notesassistant.app.speech

import android.content.Context
import android.util.Log
import com.argmaxinc.whisperkit.WhisperKit
import com.argmaxinc.whisperkit.ExperimentalWhisperKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalWhisperKit::class)
class WhisperKitASR(private val context: Context) {

    private var whisperKit: WhisperKit? = null
    private var isInitialized = false
    private var latestTranscription = ""
    private var isProcessing = false

    companion object {
        private const val TAG = "WhisperKitASR"
        private const val SAMPLE_RATE = 16000
    }

    suspend fun initialize(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Initializing WhisperKit...")

                whisperKit = WhisperKit.Builder()
                    .setModel(WhisperKit.Builder.QUALCOMM_BASE_EN)
                    .setApplicationContext(context.applicationContext)
                    .setEncoderBackend(WhisperKit.Builder.CPU_AND_NPU)
                    .setDecoderBackend(WhisperKit.Builder.CPU_AND_NPU)
                    .setCallback { what, result ->
                        // Log every callback event for deep debugging
                        Log.d(TAG, "WhisperKit Callback [WHAT=$what]: text='${result.text}', data='${result}'")

                        when (what) {
                            WhisperKit.TextOutputCallback.MSG_INIT -> {
                                Log.i(TAG, "WhisperKit ready (MSG_INIT)")
                                isInitialized = true
                            }
                            WhisperKit.TextOutputCallback.MSG_TEXT_OUT -> {
                                if (result.text.isNotEmpty()) {
                                    latestTranscription = result.text
                                    Log.i(TAG, "Transcription Update: $latestTranscription")
                                }
                            }
                            WhisperKit.TextOutputCallback.MSG_CLOSE -> {
                                Log.d(TAG, "WhisperKit session closed (MSG_CLOSE)")
                                isProcessing = false
                            }
                        }
                    }
                    .build()

                Log.d(TAG, "Loading model...")
                whisperKit?.loadModel()?.collect { progress ->
                    // Progress might be 0.0 to 1.0 or 0.0 to 100.0
                    Log.d(TAG, "Model load progress: ${progress.fractionCompleted}")
                }
                
                // Using duration = 0 for streaming mode or automatic detection
                Log.d(TAG, "Initializing engine (16kHz, mono, duration=0)")
                whisperKit?.init(frequency = SAMPLE_RATE, channels = 1, duration = 0)
                
                var waitCount = 0
                while (!isInitialized && waitCount < 100) {
                    kotlinx.coroutines.delay(100)
                    waitCount++
                }
                
                Log.d(TAG, "Initialization result: $isInitialized")
                isInitialized
            } catch (e: Exception) {
                Log.e(TAG, "Initialization failed", e)
                false
            }
        }
    }

    suspend fun transcribe(audioFile: File): String {
        if (!isInitialized || whisperKit == null) return "ASR Not Initialized"

        return withContext(Dispatchers.IO) {
            try {
                val rawPcmData = readRawPcmFromWav(audioFile)
                if (rawPcmData.isEmpty()) return@withContext "Error: No audio data found"

                // PADDING: Whisper models (especially TFLite versions) often expect 
                // exactly 30 seconds of audio to trigger the full decoding pass.
                // 30s * 16000 samples/s * 2 bytes/sample = 960,000 bytes
                val targetSize = 30 * SAMPLE_RATE * 2
                val paddedData = if (rawPcmData.size < targetSize) {
                    Log.d(TAG, "Padding audio from ${rawPcmData.size} to $targetSize bytes (30 seconds)")
                    val buffer = ByteArray(targetSize)
                    System.arraycopy(rawPcmData, 0, buffer, 0, rawPcmData.size)
                    buffer
                } else {
                    rawPcmData.copyOfRange(0, targetSize)
                }

                latestTranscription = ""
                isProcessing = true
                
                Log.d(TAG, "Sending padded audio to model...")
                val startReturn = whisperKit?.transcribe(paddedData)
                Log.d(TAG, "transcribe() call returned immediately: $startReturn")
                
                // Wait for the asynchronous callback
                var attempts = 0
                while (latestTranscription.isEmpty() && attempts < 300) { // 30 second limit
                    kotlinx.coroutines.delay(100)
                    attempts++
                    if (attempts % 50 == 0) Log.d(TAG, "Awaiting transcription result... (${attempts/10}s)")
                }

                if (latestTranscription.isEmpty()) {
                    Log.w(TAG, "Transcription failed to return text within 30 seconds.")
                }

                val finalTranscription = cleanTranscription(latestTranscription)
                Log.d(TAG, "Final Transcription: '$finalTranscription'")
                finalTranscription.ifEmpty { "No speech detected" }
            } catch (e: Exception) {
                Log.e(TAG, "Transcription execution error", e)
                "Error: ${e.message}"
            } finally {
                isProcessing = false
            }
        }
    }

    private fun cleanTranscription(text: String): String {
        // Remove Whisper special tokens like <|startoftranscript|>, <|0.00|>, <|endoftext|>, etc.
        return text.replace(Regex("<\\|.*?\\|>"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun readRawPcmFromWav(file: File): ByteArray {
        val bytes = file.readBytes()
        // Standard WAV header is 44 bytes
        return if (bytes.size > 44) bytes.copyOfRange(44, bytes.size) else ByteArray(0)
    }

    fun release() {
        whisperKit?.deinitialize()
        whisperKit = null
        isInitialized = false
        Log.d(TAG, "WhisperKit released")
    }
}
