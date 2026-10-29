package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.components.MessageBubble
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanCore
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisDarkBg
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.StatusError
import com.example.ui.theme.TechTextCyan
import com.example.ui.theme.TechTextMuted
import com.example.ui.theme.TechTextWhite

@Composable
fun HistoryScreen(
    messages: List<ChatMessage>,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TRANSMISSION ARCHIVES",
                    fontFamily = OrbitronFontFamily,
                    color = TechTextCyan,
                    fontSize = 18.sp
                )
                Text(
                    text = "${messages.size} PROTOCOLS RECORDED",
                    fontFamily = RajdhaniFontFamily,
                    color = TechTextMuted,
                    fontSize = 12.sp
                )
            }
            if (messages.isNotEmpty()) {
                IconButton(
                    onClick = { showClearConfirmation = true },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = TechTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "No archives",
                        tint = CyanCore.copy(alpha = 0.4f),
                        modifier = Modifier
                            .height(64.dp)
                            .width(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ARCHIVES EMPTY",
                        fontFamily = OrbitronFontFamily,
                        color = TechTextCyan.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Initiate voice commands or chat to record telemetry.",
                        fontFamily = RajdhaniFontFamily,
                        color = TechTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message = message)
                }
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            containerColor = JarvisDarkBg,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "PURGE ARCHIVES?",
                    fontFamily = OrbitronFontFamily,
                    color = TechTextCyan,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "This will permanently purge all local conversation logs from secure device storage.",
                    fontFamily = RajdhaniFontFamily,
                    color = TechTextWhite,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_purge_button")
                ) {
                    Text("PURGE", fontFamily = OrbitronFontFamily, fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearConfirmation = false },
                    border = BorderStroke(1.dp, CyanCore),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CANCEL", fontFamily = OrbitronFontFamily, color = CyanBright, fontSize = 12.sp)
                }
            }
        )
    }
}
