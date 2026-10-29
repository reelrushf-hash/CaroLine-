package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiAssistantEngine
import com.example.carolineai.CaroVoiceManager
import com.example.commands.LocalCommandParser
import com.example.commands.SystemActionController
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.voice.TextToSpeechManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AssistantStatus(val displayText: String) {
    READY("READY, SIR."),
    LISTENING("LISTENING..."),
    THINKING("PROCESSING PROTOCOLS..."),
    SPEAKING("TRANSMITTING VOICE..."),
    ERROR("SYSTEM ANOMALY DETECTED")
}

data class SystemDiagnostics(
    val batteryLevel: Int = 100,
    val isOnline: Boolean = true,
    val isFlashlightOn: Boolean = false,
    val speechServiceAvailable: Boolean = true
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val chatDao = db.chatDao()

    val systemController = SystemActionController(application)
    val localParser = LocalCommandParser(systemController)
    val aiEngine = AiAssistantEngine(application, localParser, systemController)

    val ttsManager = TextToSpeechManager(application)

    private val _status = MutableStateFlow(AssistantStatus.READY)
    val status: StateFlow<AssistantStatus> = _status.asStateFlow()

    private val _currentLanguage = MutableStateFlow("en") // "en", "hi", "bn", "hinglish"
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _forceOffline = MutableStateFlow(false)
    val forceOffline: StateFlow<Boolean> = _forceOffline.asStateFlow()

    private val _diagnostics = MutableStateFlow(SystemDiagnostics())
    val diagnostics: StateFlow<SystemDiagnostics> = _diagnostics.asStateFlow()

    private val _lastAssistantReply = MutableStateFlow("")
    val lastAssistantReply: StateFlow<String> = _lastAssistantReply.asStateFlow()

    val chatMessages: StateFlow<List<ChatMessage>> = chatDao.getAllMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSpeaking = ttsManager.isSpeaking

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val voiceListener = object : CaroVoiceManager.VoiceListener {
        override fun onReadyForSpeech() {
            _status.value = AssistantStatus.LISTENING
            _isListening.value = true
        }

        override fun onBeginningOfSpeech() {
            _status.value = AssistantStatus.LISTENING
            _isListening.value = true
        }

        override fun onRmsdBChanged(rmsdB: Float) {
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _audioLevel.value = normalized
        }

        override fun onResults(recognizedText: String) {
            _isListening.value = false
            _audioLevel.value = 0f
            processUserCommand(recognizedText)
        }

        override fun onError(errorCode: Int, humanReadableMessage: String) {
            _isListening.value = false
            _audioLevel.value = 0f
            _status.value = AssistantStatus.ERROR
            _lastAssistantReply.value = humanReadableMessage

            // Language fallback mechanism for code 12 (API 33+ language not supported)
            if (errorCode == 12 && caroVoiceManager.selectedLanguage != CaroVoiceManager.SupportedLanguage.ENGLISH) {
                caroVoiceManager.selectedLanguage = CaroVoiceManager.SupportedLanguage.ENGLISH
                _currentLanguage.value = "en"
            }

            viewModelScope.launch {
                delay(4000)
                if (_status.value == AssistantStatus.ERROR) {
                    _status.value = AssistantStatus.READY
                }
            }
        }

        override fun onStateChanged(isListening: Boolean) {
            _isListening.value = isListening
            if (isListening) {
                _status.value = AssistantStatus.LISTENING
            } else if (_status.value == AssistantStatus.LISTENING) {
                _status.value = AssistantStatus.READY
                _audioLevel.value = 0f
            }
        }
    }

    val caroVoiceManager: CaroVoiceManager = CaroVoiceManager(application, voiceListener).apply {
        initialize()
    }

    init {
        refreshDiagnostics()
        // Listen to TTS speaking state
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { speaking ->
                if (speaking) {
                    _status.value = AssistantStatus.SPEAKING
                    startSpeakingWaveform()
                } else if (_status.value == AssistantStatus.SPEAKING) {
                    _status.value = AssistantStatus.READY
                    _audioLevel.value = 0f
                }
            }
        }
    }

    private fun startSpeakingWaveform() {
        viewModelScope.launch {
            while (ttsManager.isSpeaking.value) {
                _audioLevel.value = (0.25f + (Math.random() * 0.75f).toFloat())
                delay(120)
            }
            _audioLevel.value = 0f
        }
    }

    fun toggleListening() {
        if (caroVoiceManager.isListening) {
            caroVoiceManager.stopListening()
            _status.value = AssistantStatus.READY
        } else {
            ttsManager.stop()
            caroVoiceManager.startListening()
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
        _status.value = AssistantStatus.READY
    }

    fun processUserCommand(input: String) {
        if (input.isBlank()) return
        val query = input.trim()

        viewModelScope.launch {
            // Save User message
            val userMsg = ChatMessage(
                sender = "USER",
                text = query,
                isOffline = _forceOffline.value,
                language = _currentLanguage.value
            )
            chatDao.insertMessage(userMsg)

            _status.value = AssistantStatus.THINKING
            val history = chatDao.getRecentMessages(6)

            val result = aiEngine.processQuery(
                rawQuery = query,
                history = history,
                language = _currentLanguage.value,
                forceOffline = _forceOffline.value
            )

            // Save JARVIS message
            val jarvisMsg = ChatMessage(
                sender = "JARVIS",
                text = result.spokenResponse,
                isOffline = result.isHandledOffline,
                actionType = result.actionCode,
                language = _currentLanguage.value
            )
            chatDao.insertMessage(jarvisMsg)

            _lastAssistantReply.value = result.spokenResponse
            _status.value = AssistantStatus.SPEAKING

            refreshDiagnostics()

            // Speak response
            ttsManager.setLanguage(_currentLanguage.value)
            ttsManager.speak(result.spokenResponse) {
                _status.value = AssistantStatus.READY
            }
        }
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
        ttsManager.setLanguage(lang)
        caroVoiceManager.selectedLanguage = when (lang.lowercase()) {
            "hi", "hinglish" -> CaroVoiceManager.SupportedLanguage.HINDI
            "bn" -> CaroVoiceManager.SupportedLanguage.BENGALI
            else -> CaroVoiceManager.SupportedLanguage.ENGLISH
        }
    }

    fun toggleForceOffline() {
        _forceOffline.value = !_forceOffline.value
        refreshDiagnostics()
    }

    fun refreshDiagnostics() {
        val battery = systemController.getBatteryLevel()
        val online = systemController.isOnline() && !_forceOffline.value
        val torch = systemController.isFlashlightOn()
        _diagnostics.value = SystemDiagnostics(
            batteryLevel = battery,
            isOnline = online,
            isFlashlightOn = torch,
            speechServiceAvailable = true
        )
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatDao.clearHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        caroVoiceManager.destroy()
        ttsManager.shutdown()
    }
}
