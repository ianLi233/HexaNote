package com.notesassistant.app.speech

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.*

/**
 * Text-to-Speech Manager
 * Handles converting text responses to speech output
 */
class TTSManager(
    private val context: Context, 
    private val onInitComplete: (Boolean) -> Unit = {},
    private val onSpeakingStateChange: (Boolean) -> Unit = {}
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var isSpeaking = false
    
    companion object {
        private const val TAG = "TTSManager"
        private const val UTTERANCE_ID = "hexanote_utterance"
        private const val GOOGLE_TTS_ENGINE = "com.google.android.tts"
    }
    
    init {
        initializeTTS(GOOGLE_TTS_ENGINE)
    }

    private fun initializeTTS(engine: String?) {
        tts = TextToSpeech(context, { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Set default language to US English
                val result = tts?.setLanguage(Locale.US)
                isInitialized = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                
                if (isInitialized) {
                    Log.d(TAG, "TTS initialized with engine: ${engine ?: "default"}")
                    configureTTS()
                    onInitComplete(true)
                } else {
                    Log.e(TAG, "TTS language not supported")
                    onInitComplete(false)
                }
            } else {
                // Fallback: If Google TTS failed, try the default system engine
                if (engine == GOOGLE_TTS_ENGINE) {
                    Log.w(TAG, "Google TTS engine not found, falling back to default")
                    initializeTTS(null)
                } else {
                    Log.e(TAG, "TTS initialization failed completely")
                    onInitComplete(false)
                }
            }
        }, engine)
        
        setupListeners()
    }
    
    /**
     * Configure TTS settings for optimal speech quality.
     * Specifically targets US or UK accents and prioritizes Neural/Network voices.
     */
    private fun configureTTS() {
        tts?.apply {
            val allVoices = voices?.toList() ?: return@apply
            
            // We specifically want US or UK English, not just any "en" (which includes Indian accents)
            val preferredRegions = listOf(Locale.US, Locale.UK)
            var selectedVoice: Voice? = null

            for (targetLocale in preferredRegions) {
                val regionalVoices = allVoices.filter { 
                    it.locale.language == targetLocale.language && 
                    it.locale.country == targetLocale.country 
                }
                
                // Priority 1: Network-based Neural voices for this region (very human)
                selectedVoice = regionalVoices.find { it.name.contains("network", ignoreCase = true) }
                
                // Priority 2: High quality local voices for this region
                if (selectedVoice == null) {
                    selectedVoice = regionalVoices.filter { !it.isNetworkConnectionRequired }
                        .maxByOrNull { it.quality }
                }
                
                if (selectedVoice != null) break
            }

            selectedVoice?.let {
                voice = it
                Log.d(TAG, "Selected voice: ${it.name} (Locale: ${it.locale}, Network: ${it.isNetworkConnectionRequired})")
            }

            // Natural sounding defaults
            setSpeechRate(1.0f)
            setPitch(1.0f)
        }
    }
    
    /**
     * Setup progress listeners
     */
    private fun setupListeners() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
                onSpeakingStateChange(true)
            }
            
            override fun onDone(utteranceId: String?) {
                isSpeaking = false
                onSpeakingStateChange(false)
            }
            
            override fun onError(utteranceId: String?) {
                isSpeaking = false
                onSpeakingStateChange(false)
            }
            
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?, errorCode: Int) {
                isSpeaking = false
                onSpeakingStateChange(false)
            }
        })
    }
    
    /**
     * Speak the given text
     */
    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!isInitialized) return
        if (text.isBlank()) return
        
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID)
        
        // Re-verify voice selection if it's missing (happens on some engine resets)
        if (tts?.voice == null) configureTTS()
        
        tts?.speak(text, queueMode, params, UTTERANCE_ID)
    }
    
    fun stop() {
        if (isSpeaking || isSpeaking()) {
            tts?.stop()
            isSpeaking = false
            onSpeakingStateChange(false)
        }
    }
    
    fun isSpeaking(): Boolean = tts?.isSpeaking ?: false
    
    fun isReady(): Boolean = isInitialized
    
    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
