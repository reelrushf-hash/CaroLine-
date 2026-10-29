package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

data class TaskRoutine(
    val title: String,
    val command: String,
    val icon: ImageVector,
    val description: String
)

@Composable
fun TasksDialog(
    onDismiss: () -> Unit,
    onRunTask: (String) -> Unit
) {
    val tasks = listOf(
        TaskRoutine(
            "MORNING STATUS BRIEFING",
            "What time is it?",
            Icons.Default.Schedule,
            "Check current time, date, and operational readiness"
        ),
        TaskRoutine(
            "TACTICAL FLASHLIGHT",
            "Jarvis, torch on karo",
            Icons.Default.FlashlightOn,
            "Toggle camera torch subsystem"
        ),
        TaskRoutine(
            "MEDIA UPLINK (YOUTUBE)",
            "Open YouTube",
            Icons.Default.VideoLibrary,
            "Access YouTube media stream"
        ),
        TaskRoutine(
            "SECURE BROWSER",
            "Open browser",
            Icons.Default.Language,
            "Launch web search protocols"
        ),
        TaskRoutine(
            "COMMUNICATION DRAFT",
            "Reply ka draft banao",
            Icons.Default.Sms,
            "Draft instant response for SMS messaging"
        ),
        TaskRoutine(
            "SYSTEM CONFIGURATION",
            "Open settings",
            Icons.Default.Settings,
            "Inspect Android device settings"
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tasks_dialog"),
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
                        text = "TASK AUTOMATIONS",
                        fontFamily = OrbitronFontFamily,
                        color = TechTextCyan,
                        fontSize = 16.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_tasks")
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks) { task ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onRunTask(task.command)
                                    onDismiss()
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = JarvisCardBg,
                            border = BorderStroke(1.dp, JarvisCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CyanCore.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = task.icon,
                                        contentDescription = task.title,
                                        tint = CyanBright,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 12.sp,
                                        color = TechTextWhite
                                    )
                                    Text(
                                        text = task.description,
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 12.sp,
                                        color = TechTextMuted
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Run",
                                    tint = CyanCore
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
