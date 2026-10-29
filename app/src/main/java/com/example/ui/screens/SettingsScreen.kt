package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanCore
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.StatusReady
import com.example.ui.theme.TechTextCyan
import com.example.ui.theme.TechTextMuted
import com.example.ui.theme.TechTextWhite

data class LanguageOption(val code: String, val name: String, val localScript: String)

@Composable
fun SettingsScreen(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    isForceOffline: Boolean,
    onToggleOffline: () -> Unit,
    onSpeechRateChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var speechRate by remember { mutableFloatStateOf(1.0f) }

    val languages = listOf(
        LanguageOption("en", "English", "English (UK/US/IN)"),
        LanguageOption("hi", "Hindi", "हिन्दी (भारत)"),
        LanguageOption("hinglish", "Hinglish", "Hindi in Latin Script"),
        LanguageOption("bn", "Bangla", "বাংলা (বাংলাদেশ / ভারত)")
    )

    // Permission launchers
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "SYSTEM CONFIGURATION",
            fontFamily = OrbitronFontFamily,
            color = TechTextCyan,
            fontSize = 18.sp
        )
        Text(
            text = "Core Assistant Parameters & Android Security",
            fontFamily = RajdhaniFontFamily,
            color = TechTextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Language Protocols
        SettingsSectionHeader("LANGUAGE PROTOCOL")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            languages.forEach { lang ->
                val isSelected = currentLanguage == lang.code
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onLanguageSelected(lang.code) }
                        .testTag("lang_${lang.code}"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) CyanCore.copy(alpha = 0.15f) else JarvisCardBg,
                    border = BorderStroke(1.dp, if (isSelected) CyanCore else JarvisCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = lang.name,
                                fontFamily = OrbitronFontFamily,
                                color = if (isSelected) CyanBright else TechTextWhite,
                                fontSize = 13.sp
                            )
                            Text(
                                text = lang.localScript,
                                fontFamily = RajdhaniFontFamily,
                                color = TechTextMuted,
                                fontSize = 12.sp
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = CyanCore
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Offline Mode
        SettingsSectionHeader("OPERATIONAL MODE")
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = JarvisCardBg,
            border = BorderStroke(1.dp, JarvisCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isForceOffline) "OFFLINE DEFENSE MODE" else "QUANTUM DUAL-LINK",
                        fontFamily = OrbitronFontFamily,
                        color = if (isForceOffline) Color(0xFFFFB300) else CyanBright,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isForceOffline)
                            "All processing restricted strictly to local device. No external network transmissions."
                        else
                            "Device commands execute offline; complex knowledge queries access online neural network.",
                        fontFamily = RajdhaniFontFamily,
                        color = TechTextMuted,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = isForceOffline,
                    onCheckedChange = { onToggleOffline() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFFB300),
                        checkedTrackColor = Color(0xFFFFB300).copy(alpha = 0.3f),
                        uncheckedThumbColor = CyanCore,
                        uncheckedTrackColor = CyanCore.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("offline_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Voice Synthesis Speed
        SettingsSectionHeader("SYNTHESIZER CALIBRATION")
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = JarvisCardBg,
            border = BorderStroke(1.dp, JarvisCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SPEECH VELOCITY",
                        fontFamily = OrbitronFontFamily,
                        color = TechTextWhite,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${String.format("%.1f", speechRate)}x",
                        fontFamily = RajdhaniFontFamily,
                        color = CyanBright,
                        fontSize = 13.sp
                    )
                }
                Slider(
                    value = speechRate,
                    onValueChange = {
                        speechRate = it
                        onSpeechRateChanged(it)
                    },
                    valueRange = 0.7f..1.5f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanCore,
                        activeTrackColor = CyanCore,
                        inactiveTrackColor = JarvisCardBorder
                    ),
                    modifier = Modifier.testTag("speech_rate_slider")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Permissions & Android Security
        SettingsSectionHeader("HARDWARE PERMISSIONS & SECURITY")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PermissionTile(
                title = "MICROPHONE SENSOR",
                desc = "Required for real-time speech-to-text recognition",
                icon = Icons.Default.Mic,
                isGranted = hasMicPermission,
                onRequest = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            )
            PermissionTile(
                title = "CAMERA / TORCH",
                desc = "Required to toggle device flashlight and hardware camera",
                icon = Icons.Default.Videocam,
                isGranted = hasCameraPermission,
                onRequest = { cameraLauncher.launch(Manifest.permission.CAMERA) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Android OS Constraints & Privacy
        SettingsSectionHeader("ANDROID PLATFORM POLICIES")
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = JarvisCardBg,
            border = BorderStroke(1.dp, JarvisCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = CyanBright,
                        modifier = Modifier.width(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CALL & SMS PROTOCOLS",
                        fontFamily = OrbitronFontFamily,
                        color = TechTextWhite,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Telephony & Calls: To answer or screen phone calls, Android requires the app to be designated as the Default Phone/Call Screening app under System Settings.\n" +
                            "• SMS Handling: In compliance with Google Play Developer Policy, SMS messages are drafted for user review and explicit confirmation before sending.\n" +
                            "• Privacy Guarantee: No biometric, voice, or private conversation data is stored on remote servers.",
                    fontFamily = RajdhaniFontFamily,
                    color = TechTextMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = OrbitronFontFamily,
        color = CyanBright,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun PermissionTile(
    title: String,
    desc: String,
    icon: ImageVector,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = JarvisCardBg,
        border = BorderStroke(1.dp, JarvisCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isGranted) StatusReady.copy(alpha = 0.15f) else Color(0xFFFFB300).copy(alpha = 0.15f),
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isGranted) StatusReady else Color(0xFFFFB300),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontFamily = OrbitronFontFamily,
                        color = TechTextWhite,
                        fontSize = 12.sp
                    )
                    Text(
                        text = desc,
                        fontFamily = RajdhaniFontFamily,
                        color = TechTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
            if (isGranted) {
                Text(
                    text = "ACTIVE",
                    fontFamily = OrbitronFontFamily,
                    color = StatusReady,
                    fontSize = 11.sp
                )
            } else {
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanCore),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("request_permission_${title.take(4)}")
                ) {
                    Text(
                        text = "GRANT",
                        fontFamily = OrbitronFontFamily,
                        color = Color(0xFF060B14),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
