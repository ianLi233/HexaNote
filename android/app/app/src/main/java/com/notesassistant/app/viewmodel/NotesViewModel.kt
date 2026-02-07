package com.notesassistant.app.viewmodel

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notesassistant.app.network.ChatRequest
import com.notesassistant.app.network.HexaNoteRetrofitClient
import com.notesassistant.app.network.TokenRequest
import com.notesassistant.app.network.SemanticSearchResult
import com.notesassistant.app.speech.AudioRecorder
import com.notesassistant.app.speech.TTSManager
import com.notesassistant.app.speech.WhisperKitASR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class ChatMode {
    RAG, SEMANTIC
}

data class ChatMessage(
    val role: String, // "user", "assistant", "system"
    val content: String,
    val sources: List<String> = emptyList()
)

/**
 * App state data class
 */
data class AppState(
    val isRecording: Boolean = false,
    val isProcessing: Boolean = false,
    val isTranscribing: Boolean = false,
    val isSpeaking: Boolean = false,
    val isAsrReady: Boolean = false,
    val lastTranscript: String = "",
    val lastResponse: String = "",
    val errorMessage: String? = null,
    val sessionId: String? = null,
    val authToken: String? = null,
    val processingStep: String = "Initializing...",
    val mode: ChatMode = ChatMode.RAG,
    val messages: List<ChatMessage> = emptyList(),
    val searchResults: List<SemanticSearchResult> = emptyList(),
    val searchQuery: String = "",
    val selectedNote: SemanticSearchResult? = null
)

/**
 * ViewModel managing the complete voice query flow
 */
class NotesViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()
    
    private var audioRecorder: AudioRecorder? = null
    private var whisperASR: WhisperKitASR? = null
    private var ttsManager: TTSManager? = null
    private var recordingFile: File? = null
    
    companion object {
        private const val TAG = "NotesViewModel"
    }
    
    fun initialize(context: Context) {
        audioRecorder = AudioRecorder(context)
        whisperASR = WhisperKitASR(context)
        ttsManager = TTSManager(
            context = context,
            onInitComplete = { success ->
                if (!success) {
                    _state.value = _state.value.copy(errorMessage = "TTS initialization failed")
                }
            },
            onSpeakingStateChange = { isSpeaking ->
                _state.value = _state.value.copy(
                    isSpeaking = isSpeaking,
                    processingStep = if (isSpeaking) "Speaking..." else "Ready"
                )
            }
        )
        
        // Initialize Whisper model in background
        viewModelScope.launch {
            _state.value = _state.value.copy(processingStep = "Loading AI models...")
            val initialized = whisperASR?.initialize() ?: false
            _state.value = _state.value.copy(
                isAsrReady = initialized,
                processingStep = if (initialized) "Ready" else "AI Model failed to load"
            )
            if (!initialized) {
                Log.w(TAG, "Whisper initialization failed")
            }
        }
        
        authenticateAndSetup()
    }

    private fun authenticateAndSetup() {
        viewModelScope.launch {
            try {
                val tokenResponse = withContext(Dispatchers.IO) {
                    HexaNoteRetrofitClient.api.getToken(TokenRequest(password = "hexanote"))
                }
                
                if (tokenResponse.isSuccessful) {
                    val token = "Bearer ${tokenResponse.body()?.accessToken}"
                    _state.value = _state.value.copy(authToken = token)
                    createSession(token)
                } else {
                    _state.value = _state.value.copy(errorMessage = "Server authentication failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Connection error during auth", e)
                _state.value = _state.value.copy(errorMessage = "Server connection error")
            }
        }
    }
    
    private fun createSession(token: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    HexaNoteRetrofitClient.api.createSession(token)
                }
                if (response.isSuccessful) {
                    _state.value = _state.value.copy(sessionId = response.body()?.sessionId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create session", e)
            }
        }
    }

    fun setMode(mode: ChatMode) {
        _state.value = _state.value.copy(mode = mode)
    }

    fun selectNote(note: SemanticSearchResult?) {
        _state.value = _state.value.copy(selectedNote = note)
    }
    
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun toggleRecording() {
        if (!_state.value.isAsrReady) {
            _state.value = _state.value.copy(errorMessage = "Please wait, AI models are still loading...")
            return
        }

        if (_state.value.isRecording) {
            stopRecordingAndProcess()
        } else {
            startRecording()
        }
    }
    
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startRecording() {
        // Stop any current speech when starting a new recording
        stopSpeaking()
        
        recordingFile = audioRecorder?.startRecording()
        if (recordingFile != null) {
            _state.value = _state.value.copy(
                isRecording = true,
                errorMessage = null,
                processingStep = "Listening..."
            )
        } else {
            _state.value = _state.value.copy(errorMessage = "Microphone error")
        }
    }
    
    private fun stopRecordingAndProcess() {
        val file = audioRecorder?.stopRecording()
        _state.value = _state.value.copy(isRecording = false)
        
        if (file != null && file.exists()) {
            processAudioToResponse(file)
        } else {
            _state.value = _state.value.copy(errorMessage = "Recording failed")
        }
    }
    
    private fun processAudioToResponse(audioFile: File) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessing = true, errorMessage = null)
            
            try {
                _state.value = _state.value.copy(isTranscribing = true, processingStep = "Transcribing...")
                val transcript = transcribeAudio(audioFile)
                
                if (transcript.isBlank() || transcript == "ASR Not Initialized") {
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        isTranscribing = false,
                        errorMessage = "Transcription failed. Try again."
                    )
                    return@launch
                }
                
                _state.value = _state.value.copy(
                    isTranscribing = false,
                    lastTranscript = transcript
                )
                
                // Important: We call the internal search logic directly to bypass the isProcessing check
                performQuery(transcript)
                
            } catch (e: Exception) {
                Log.e(TAG, "Processing error", e)
                _state.value = _state.value.copy(isProcessing = false, errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    fun sendQuery(query: String) {
        if (query.isBlank() || _state.value.isProcessing) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isProcessing = true, 
                errorMessage = null,
                lastTranscript = query
            )
            performQuery(query)
        }
    }

    private suspend fun performQuery(query: String) {
        if (_state.value.mode == ChatMode.RAG) {
            val userMsg = ChatMessage("user", query)
            _state.value = _state.value.copy(
                messages = _state.value.messages + userMsg,
                processingStep = "Thinking...",
                searchQuery = query
            )
            
            val response = queryHexaNoteServer(query)
            val assistantMsg = ChatMessage("assistant", response)
            
            _state.value = _state.value.copy(
                messages = _state.value.messages + assistantMsg,
                isProcessing = false,
                processingStep = "Ready",
                lastResponse = response
            )
            
            speakText(response)
        } else {
            _state.value = _state.value.copy(
                processingStep = "Searching...",
                searchQuery = query
            )
            try {
                val token = _state.value.authToken ?: ""
                val response = withContext(Dispatchers.IO) {
                    HexaNoteRetrofitClient.api.searchNotes(token, query)
                }
                if (response.isSuccessful) {
                    _state.value = _state.value.copy(
                        searchResults = response.body()?.results ?: emptyList(),
                        isProcessing = false,
                        processingStep = "Ready"
                    )
                } else {
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        errorMessage = "Search failed: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isProcessing = false,
                    errorMessage = "Connection error"
                )
            }
        }
    }
    
    private suspend fun transcribeAudio(audioFile: File): String {
        return withContext(Dispatchers.IO) {
            whisperASR?.transcribe(audioFile) ?: ""
        }
    }
    
    private suspend fun queryHexaNoteServer(question: String): String {
        val token = _state.value.authToken ?: return "Error: Not authenticated."
        return withContext(Dispatchers.IO) {
            try {
                val request = ChatRequest(message = question, sessionId = _state.value.sessionId)
                val response = HexaNoteRetrofitClient.api.chatQuery(token, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    _state.value = _state.value.copy(sessionId = body?.sessionId ?: _state.value.sessionId)
                    body?.message ?: "No response message"
                } else {
                    "Server error: ${response.code()}"
                }
            } catch (e: Exception) {
                "Connection failed"
            }
        }
    }
    
    fun speakText(text: String) {
        ttsManager?.speak(text)
    }
    
    fun stopSpeaking() {
        ttsManager?.stop()
        _state.value = _state.value.copy(isSpeaking = false)
    }
    
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        whisperASR?.release()
        ttsManager?.shutdown()
    }
}
