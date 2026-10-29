package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechRecognizerManager(
    private val context: Context,
    private val onResultReceived: (String) -> Unit,
    private val onErrorReceived: (String) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    // Error codes introduced in API 33 (Android 13)
    companion object {
        const val ERROR_SERVER_DISCONNECTED = 11
        const val ERROR_LANGUAGE_NOT_SUPPORTED = 12
        const val ERROR_LANGUAGE_UNAVAILABLE = 13
        const val ERROR_TOO_MANY_REQUESTS = 14
    }

    private var lastRequestedLangCode: String = "en"
    private var hasAttemptedFallback: Boolean = false

    init {
        createRecognizer()
    }

    private fun createRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                            _rmsLevel.value = 0f
                            _partialText.value = ""
                        }

                        override fun onBeginningOfSpeech() {
                            _isListening.value = true
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            // Normalize -2dB..10dB to 0..1f
                            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                            _rmsLevel.value = normalized
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                            _rmsLevel.value = 0f
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            _rmsLevel.value = 0f

                            val isApi33OrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

                            // Handle API 33+ language errors
                            if (isApi33OrHigher && (error == ERROR_LANGUAGE_NOT_SUPPORTED || error == ERROR_LANGUAGE_UNAVAILABLE) && !hasAttemptedFallback) {
                                hasAttemptedFallback = true
                                Log.w("SpeechRecognizerManager", "API 33+ language error ($error) for $lastRequestedLangCode. Initiating fallback.")
                                retryWithDefaultLocale()
                                return
                            }

                            // Handle pre-API 33 or OEM/Vendor proprietary error codes (>= 10)
                            if (!isApi33OrHigher && error >= 10) {
                                Log.e("SpeechRecognizerManager", "OEM/Vendor proprietary speech error ($error). Resetting recognizer binder.")
                                recreateRecognizer()
                                onErrorReceived("OEM speech service binding failed ($error). Voice engine has been reset. Please try again.")
                                return
                            }

                            val message = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Check microphone."
                                SpeechRecognizer.ERROR_CLIENT -> {
                                    recreateRecognizer()
                                    "Client recognition protocol error. Session reset."
                                }
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                                SpeechRecognizer.ERROR_NETWORK -> "Network required for online recognition."
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network connection timed out."
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking again."
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                                    recreateRecognizer()
                                    "Speech recognizer was busy. Session has been refreshed."
                                }
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected in time window."
                                ERROR_SERVER_DISCONNECTED -> "Speech recognition server disconnected."
                                ERROR_LANGUAGE_NOT_SUPPORTED -> {
                                    "Language '$lastRequestedLangCode' model is not supported or downloaded on this device."
                                }
                                ERROR_LANGUAGE_UNAVAILABLE -> {
                                    "Language '$lastRequestedLangCode' offline model is currently unavailable."
                                }
                                ERROR_TOO_MANY_REQUESTS -> "Too many speech recognition requests. Please wait a moment."
                                else -> "Voice recognition error ($error)"
                            }
                            onErrorReceived(message)
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            _rmsLevel.value = 0f
                            hasAttemptedFallback = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull() ?: ""
                            if (text.isNotBlank()) {
                                onResultReceived(text)
                            } else {
                                onErrorReceived("No clear speech detected")
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            matches?.firstOrNull()?.let {
                                _partialText.value = it
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            } catch (e: Exception) {
                Log.e("SpeechRecognizerManager", "Failed to create SpeechRecognizer", e)
                speechRecognizer = null
            }
        } else {
            Log.w("SpeechRecognizerManager", "Speech recognition not available on this device")
        }
    }

    fun recreateRecognizer() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        createRecognizer()
    }

    fun startListening(langCode: String = "en") {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onErrorReceived("Speech recognition service is not available on this system")
            return
        }

        if (speechRecognizer == null) {
            createRecognizer()
        }

        lastRequestedLangCode = langCode
        hasAttemptedFallback = false

        val speechLocale = when (langCode.lowercase(Locale.ROOT)) {
            "hi", "hinglish" -> "hi-IN"
            "bn" -> "bn-IN"
            else -> "en-US"
        }

        launchRecognitionIntent(speechLocale, preferOffline = true)
    }

    private fun retryWithDefaultLocale() {
        val fallbackLocale = Locale.getDefault().toLanguageTag().ifBlank { "en-US" }
        Log.i("SpeechRecognizerManager", "Retrying speech recognition with fallback locale: $fallbackLocale")
        onErrorReceived("Language model not downloaded on device. Falling back to $fallbackLocale...")
        launchRecognitionIntent(fallbackLocale, preferOffline = false)
    }

    private fun launchRecognitionIntent(localeTag: String, preferOffline: Boolean) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, localeTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            if (preferOffline) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
        }

        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            _isListening.value = false
            recreateRecognizer()
            onErrorReceived("Failed to bind to speech engine: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _isListening.value = false
        _rmsLevel.value = 0f
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }
}
