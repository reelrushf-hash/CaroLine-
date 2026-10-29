package com.example.carolineai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class CaroVoiceManager(
    private val context: Context,
    private val callback: VoiceListener
) : RecognitionListener {

    interface VoiceListener {
        fun onReadyForSpeech()
        fun onBeginningOfSpeech()
        fun onRmsdBChanged(rmsdB: Float)
        fun onResults(recognizedText: String)
        fun onError(errorCode: Int, humanReadableMessage: String)
        fun onStateChanged(isListening: Boolean)
    }

    private var speechRecognizer: SpeechRecognizer? = null
    var isListening: Boolean = false
        private set

    enum class SupportedLanguage(val tag: String, val displayName: String) {
        ENGLISH("en-US", "English (US)"),
        HINDI("hi-IN", "Hindi (India)"),
        BENGALI("bn-IN", "Bengali (India)")
    }

    var selectedLanguage: SupportedLanguage = SupportedLanguage.ENGLISH

    fun initialize() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            callback.onError(
                -1,
                "No speech recognition service found. Install or enable Google Speech Services / Google App from the Play Store."
            )
            return
        }
        recreateRecognizer()
    }

    private fun recreateRecognizer() {
        destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(this@CaroVoiceManager)
        }
    }

    fun startListening() {
        if (isListening) {
            return
        }

        if (speechRecognizer == null) {
            recreateRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage.tag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, selectedLanguage.tag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, selectedLanguage.tag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            callback.onStateChanged(true)
        } catch (e: Exception) {
            isListening = false
            callback.onStateChanged(false)
            callback.onError(-2, "Failed to initialize listener: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        if (isListening) {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
            isListening = false
            callback.onStateChanged(false)
        }
    }

    fun cancelListening() {
        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {}
        isListening = false
        callback.onStateChanged(false)
    }

    fun destroy() {
        isListening = false
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    override fun onReadyForSpeech(params: Bundle?) {
        callback.onReadyForSpeech()
    }

    override fun onBeginningOfSpeech() {
        callback.onBeginningOfSpeech()
    }

    override fun onRmsChanged(rmsdB: Float) {
        callback.onRmsdBChanged(rmsdB)
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        isListening = false
        callback.onStateChanged(false)
    }

    override fun onError(error: Int) {
        isListening = false
        callback.onStateChanged(false)

        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO ->
                "Audio recording error. Check if another app is using the microphone."
            SpeechRecognizer.ERROR_CLIENT ->
                "Client-side processing error. Try restarting the recognition session."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                "Microphone permission not granted. Please allow microphone access in app settings."
            SpeechRecognizer.ERROR_NETWORK ->
                "Network connection failed. Verify internet connectivity or enable offline language packs."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "Network operation timed out. Verify your connection stability."
            SpeechRecognizer.ERROR_NO_MATCH ->
                "No speech detected. Please speak clearly into the microphone."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                "Voice engine busy. Pending operations have been cleared; please try again."
            SpeechRecognizer.ERROR_SERVER ->
                "Recognition server error. Try switching networks or retry shortly."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                "No speech heard within the expected time limit."
            12 -> { // Matches SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED on API 33+
                "Language '${selectedLanguage.displayName}' is not installed or supported offline by your voice engine. Fallback to English recommended, or download the pack in Google App settings."
            }
            13 -> "Language pack not yet downloaded on this device."
            14 -> "Device engine cannot verify language support status."
            else -> "Speech recognition error (Code $error). Verify engine status."
        }

        // Clean internal state on critical or engine-busy conditions
        if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_CLIENT) {
            cancelListening()
        }

        callback.onError(error, message)
    }

    override fun onResults(results: Bundle?) {
        isListening = false
        callback.onStateChanged(false)
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            callback.onResults(matches[0])
        } else {
            callback.onError(-3, "No speech transcribed.")
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            callback.onResults(matches[0])
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
