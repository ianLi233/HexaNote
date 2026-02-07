package com.notesassistant.app.speech

import android.Manifest
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioRecorder(private val context: Context) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var outputFile: File? = null
    
    companion object {
        private const val TAG = "AudioRecorder"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording(): File? {
        try {
            outputFile = File(context.cacheDir, "recording_${System.currentTimeMillis()}.wav")
            
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                return null
            }

            audioRecord?.startRecording()
            isRecording = true

            // Start a thread to write PCM data to a WAV file
            Thread {
                writeAudioDataToFile()
            }.start()

            Log.d(TAG, "Recording started: ${outputFile!!.absolutePath}")
            return outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            return null
        }
    }

    private fun writeAudioDataToFile() {
        val data = ByteArray(BUFFER_SIZE)
        val fileOutputStream = FileOutputStream(outputFile)

        // Write placeholder for WAV header
        fileOutputStream.write(ByteArray(44))

        while (isRecording) {
            val read = audioRecord?.read(data, 0, BUFFER_SIZE) ?: 0
            if (read > 0) {
                fileOutputStream.write(data, 0, read)
            }
        }

        fileOutputStream.close()
        updateWavHeader(outputFile!!)
    }

    fun stopRecording(): File? {
        try {
            isRecording = false
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.d(TAG, "Recording stopped")
            return outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            return null
        }
    }

    fun isRecording(): Boolean = isRecording

    /**
     * Updates the WAV header with correct file and data sizes
     */
    private fun updateWavHeader(file: File) {
        val totalAudioLen = file.length() - 44
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = SAMPLE_RATE * 2 * channels // 16bit = 2 bytes

        val header = ByteArray(44)
        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("RIFF".toByteArray())
        buffer.putInt(totalDataLen.toInt())
        buffer.put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // Sub-chunk size
        buffer.putShort(1.toShort()) // PCM format
        buffer.putShort(channels.toShort())
        buffer.putInt(SAMPLE_RATE)
        buffer.putInt(byteRate)
        buffer.putShort((channels * 2).toShort()) // Block align
        buffer.putShort(16.toShort()) // Bits per sample
        buffer.put("data".toByteArray())
        buffer.putInt(totalAudioLen.toInt())

        val raf = file.randomAccessFile("rw")
        raf.seek(0)
        raf.write(header)
        raf.close()
    }

    private fun File.randomAccessFile(mode: String) = java.io.RandomAccessFile(this, mode)
}
