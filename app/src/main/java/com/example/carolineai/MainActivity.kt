package com.example.carolineai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.R
import com.example.commands.LocalCommandParser
import com.example.commands.SystemActionController
import com.example.voice.TextToSpeechManager

class MainActivity : AppCompatActivity(), CaroVoiceManager.VoiceListener {

    private lateinit var voiceManager: CaroVoiceManager
    private lateinit var actionController: SystemActionController
    private lateinit var localCommandParser: LocalCommandParser
    private lateinit var ttsManager: TextToSpeechManager

    // UI Element references
    private lateinit var tvStatus: TextView
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnMic: ImageButton
    private lateinit var etCommandInput: EditText
    private lateinit var btnSend: Button
    private lateinit var radarContainer: View

    private val recordAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceCapture()
        } else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                showPermissionSettingsDialog()
            } else {
                displayAnomaly("Microphone access is required for JARVIS voice commands.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        actionController = SystemActionController(this)
        localCommandParser = LocalCommandParser(actionController)
        ttsManager = TextToSpeechManager(this)

        initViews()
        setupListeners()

        voiceManager = CaroVoiceManager(this, this)
        voiceManager.initialize()
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatus)
        tvErrorMessage = findViewById(R.id.tvErrorMessage)
        btnMic = findViewById(R.id.btnMic)
        etCommandInput = findViewById(R.id.etCommandInput)
        btnSend = findViewById(R.id.btnSend)
        radarContainer = findViewById(R.id.radarContainer)
    }

    private fun setupListeners() {
        btnMic.setOnClickListener {
            if (voiceManager.isListening) {
                voiceManager.stopListening()
            } else {
                checkPermissionAndListen()
            }
        }

        btnSend.setOnClickListener {
            val textCommand = etCommandInput.text.toString().trim()
            if (textCommand.isNotEmpty()) {
                executeCommand(textCommand)
                etCommandInput.text.clear()
            }
        }
    }

    private fun checkPermissionAndListen() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceCapture()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                AlertDialog.Builder(this)
                    .setTitle("Microphone Permission Required")
                    .setMessage("CARO LINE AI needs access to your microphone to process voice commands.")
                    .setPositiveButton("Allow") { _, _ ->
                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            else -> {
                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startVoiceCapture() {
        clearAnomaly()
        voiceManager.startListening()
    }

    private fun executeCommand(command: String) {
        tvStatus.text = "PROCESSING: $command"
        val langCode = when (voiceManager.selectedLanguage) {
            CaroVoiceManager.SupportedLanguage.HINDI -> "hi"
            CaroVoiceManager.SupportedLanguage.BENGALI -> "bn"
            else -> "en"
        }

        // Execute via multilingual local command parser
        val result = localCommandParser.parseAndExecute(command, langCode)
        if (result.isHandledOffline) {
            tvStatus.text = result.spokenResponse
            ttsManager.setLanguage(langCode)
            ttsManager.speak(result.spokenResponse)
        } else {
            val response = "Command acknowledged: $command"
            tvStatus.text = response
            ttsManager.speak(response)
        }
    }

    private fun displayAnomaly(detail: String) {
        tvErrorMessage.visibility = View.VISIBLE
        tvErrorMessage.text = "SYSTEM ANOMALY DETECTED\n$detail"
    }

    private fun clearAnomaly() {
        tvErrorMessage.visibility = View.GONE
        tvErrorMessage.text = ""
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Microphone permission was permanently denied. Please grant it in App Settings to enable voice recognition.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Type Instead") { dialog, _ ->
                dialog.dismiss()
                etCommandInput.requestFocus()
            }
            .show()
    }

    // VoiceListener Callbacks
    override fun onReadyForSpeech() {
        tvStatus.text = "LISTENING..."
    }

    override fun onBeginningOfSpeech() {
        tvStatus.text = "RECEIVING AUDIO..."
    }

    override fun onRmsdBChanged(rmsdB: Float) {
        radarContainer.alpha = (0.3f + (rmsdB.coerceAtLeast(0f) / 10f)).coerceIn(0.3f, 1.0f)
    }

    override fun onResults(recognizedText: String) {
        etCommandInput.setText(recognizedText)
        executeCommand(recognizedText)
    }

    override fun onError(errorCode: Int, humanReadableMessage: String) {
        displayAnomaly(humanReadableMessage)
        tvStatus.text = "STANDBY"

        // Language fallback mechanism for code 12
        if (errorCode == 12 && voiceManager.selectedLanguage != CaroVoiceManager.SupportedLanguage.ENGLISH) {
            Toast.makeText(
                this,
                "Language not supported by current speech engine. Falling back to English.",
                Toast.LENGTH_LONG
            ).show()
            voiceManager.selectedLanguage = CaroVoiceManager.SupportedLanguage.ENGLISH
        }
    }

    override fun onStateChanged(isListening: Boolean) {
        btnMic.isSelected = isListening
        if (!isListening) {
            radarContainer.alpha = 0.3f
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.destroy()
        ttsManager.shutdown()
    }
}
