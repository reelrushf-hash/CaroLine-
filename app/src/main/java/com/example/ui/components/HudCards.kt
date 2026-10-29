package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanCore
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.StatusReady
import com.example.ui.theme.TechTextCyan
import com.example.ui.theme.TechTextMuted
import com.example.ui.theme.TechTextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuickActionPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .testTag("action_pill_${label.lowercase()}")
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = JarvisCardBg,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, JarvisCardBorder)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontFamily = RajdhaniFontFamily,
                color = TechTextMuted,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun StatusBadge(
    isOnline: Boolean,
    language: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .testTag("status_badge")
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = JarvisCardBg.copy(alpha = 0.8f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, JarvisCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (isOnline) StatusReady else Color(0xFFFFB300),
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOnline) "ONLINE" else "OFFLINE",
                fontFamily = RajdhaniFontFamily,
                color = if (isOnline) CyanBright else Color(0xFFFFB300),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "|",
                color = JarvisCardBorder,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = language.uppercase(Locale.ROOT),
                fontFamily = RajdhaniFontFamily,
                color = TechTextWhite,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isJarvis = message.sender == "JARVIS"
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isJarvis) Alignment.Start else Alignment.End
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isJarvis) 2.dp else 14.dp,
                bottomEnd = if (isJarvis) 14.dp else 2.dp
            ),
            color = if (isJarvis) JarvisCardBg else Color(0xFF0F2642),
            border = BorderStroke(
                1.dp,
                if (isJarvis) JarvisCardBorder else CyanCore.copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("chat_bubble_${message.id}")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(if (isJarvis) 0.85f else 0.75f)
                ) {
                    Text(
                        text = if (isJarvis) "CARO LINE AI" else "YOU",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 11.sp,
                        color = if (isJarvis) TechTextCyan else CyanBright
                    )
                    Text(
                        text = timeStr,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 10.sp,
                        color = TechTextMuted
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 14.sp,
                    color = TechTextWhite,
                    lineHeight = 20.sp
                )
                if (message.actionType != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PROTOCOLS: [${message.actionType}]",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 10.sp,
                        color = CyanCore.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
