package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.SystemDiagnostics
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanCore
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisDarkBg
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.StatusReady
import com.example.ui.theme.TechTextCyan
import com.example.ui.theme.TechTextMuted
import com.example.ui.theme.TechTextWhite

@Composable
fun StatusDialog(
    diagnostics: SystemDiagnostics,
    selectedLanguage: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("status_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = JarvisDarkBg,
            border = BorderStroke(1.dp, CyanCore.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "JARVIS DIAGNOSTICS",
                        fontFamily = OrbitronFontFamily,
                        color = TechTextCyan,
                        fontSize = 16.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_status")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TechTextCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                DiagnosticRow("SYSTEM STATUS", "OPERATIONAL (OPTIMAL)", StatusReady)
                DiagnosticRow(
                    "NETWORK LINK",
                    if (diagnostics.isOnline) "SYNCHRONIZED (ONLINE)" else "ISOLATED (OFFLINE MODE)",
                    if (diagnostics.isOnline) CyanBright else CyanCore
                )
                DiagnosticRow("POWER RESERVES", "${diagnostics.batteryLevel}% REMAINING", TechTextWhite)
                DiagnosticRow("LANGUAGE PROTOCOL", selectedLanguage.uppercase(), CyanBright)
                DiagnosticRow(
                    "TORCH SUBSYSTEM",
                    if (diagnostics.isFlashlightOn) "ACTIVE" else "STANDBY",
                    if (diagnostics.isFlashlightOn) StatusReady else TechTextMuted
                )
                DiagnosticRow("SPEECH SYNTHESIS", "TTS ENGINE READY", StatusReady)
                DiagnosticRow("OFFLINE PARSER", "ACTIVE (HI, EN, BN)", StatusReady)

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = JarvisCardBg,
                    border = BorderStroke(1.dp, JarvisCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "SECURITY NOTICE",
                            fontFamily = OrbitronFontFamily,
                            color = CyanBright,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Private API keys and conversation audio never leave device memory. Offline commands execute strictly on local CPU architecture.",
                            fontFamily = RajdhaniFontFamily,
                            color = TechTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    title: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = JarvisCardBg,
        border = BorderStroke(1.dp, JarvisCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = RajdhaniFontFamily,
                color = TechTextMuted,
                fontSize = 12.sp
            )
            Text(
                text = value,
                fontFamily = RajdhaniFontFamily,
                color = valueColor,
                fontSize = 12.sp
            )
        }
    }
}
