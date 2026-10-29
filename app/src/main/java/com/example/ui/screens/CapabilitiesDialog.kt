package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanCore
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisDarkBg
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TechTextCyan
import com.example.ui.theme.TechTextMuted
import com.example.ui.theme.TechTextWhite

data class CommandSample(
    val category: String,
    val commandText: String,
    val languageLabel: String,
    val description: String
)

@Composable
fun CapabilitiesDialog(
    onDismiss: () -> Unit,
    onExecuteCommand: (String) -> Unit
) {
    val sampleCommands = listOf(
        CommandSample("DEVICE CONTROL", "Jarvis, torch on karo", "Hindi / Hinglish", "Turns on device camera flashlight"),
        CommandSample("DEVICE CONTROL", "টর্চ বন্ধ করো", "Bangla", "Turns off device flashlight"),
        CommandSample("APPS & MEDIA", "Open YouTube", "English", "Launches YouTube application"),
        CommandSample("APPS & MEDIA", "আমার ব্রাউজার খোলো", "Bangla", "Opens web browser"),
        CommandSample("TIME PROTOCOL", "Abhi kitne baje hain?", "Hindi / Hinglish", "Reads current local time via voice"),
        CommandSample("TIME PROTOCOL", "What time is it?", "English", "Reports precise current time"),
        CommandSample("DIAGNOSTICS", "Battery status", "English", "Reports battery percentage and power reserves"),
        CommandSample("COMMUNICATION", "Reply ka draft banao", "Hindi / Hinglish", "Creates a polite SMS reply draft"),
        CommandSample("SYSTEM SETTINGS", "Open settings", "English", "Opens Android system settings"),
        CommandSample("AI CONVERSATION", "Who are you?", "English", "Introduces CARO LINE AI JARVIS capabilities"),
        CommandSample("AI CONVERSATION", "তুমি কেমন আছো?", "Bangla", "Conversational response in Bangla")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .testTag("capabilities_dialog"),
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
                    Column {
                        Text(
                            text = "COMMAND PROTOCOLS",
                            fontFamily = OrbitronFontFamily,
                            color = TechTextCyan,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Multilingual Offline & Online Controls",
                            fontFamily = RajdhaniFontFamily,
                            color = TechTextMuted,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_capabilities")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TechTextCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(sampleCommands) { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onExecuteCommand(item.commandText)
                                    onDismiss()
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = JarvisCardBg,
                            border = BorderStroke(1.dp, JarvisCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.category,
                                            fontFamily = RajdhaniFontFamily,
                                            color = CyanBright,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${item.languageLabel}",
                                            fontFamily = RajdhaniFontFamily,
                                            color = TechTextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "\"${item.commandText}\"",
                                        fontFamily = RajdhaniFontFamily,
                                        color = TechTextWhite,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = item.description,
                                        fontFamily = RajdhaniFontFamily,
                                        color = TechTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                Button(
                                    onClick = {
                                        onExecuteCommand(item.commandText)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyanCore.copy(alpha = 0.2f),
                                        contentColor = CyanCore
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.testTag("try_cmd_${item.commandText.take(6)}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Run",
                                        modifier = Modifier.width(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RUN", fontFamily = OrbitronFontFamily, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
