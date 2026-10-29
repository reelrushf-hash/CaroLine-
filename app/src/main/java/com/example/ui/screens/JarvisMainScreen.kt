package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.AssistantStatus
import com.example.ui.JarvisViewModel
import com.example.ui.components.ArcReactorPulse
import com.example.ui.components.QuickActionPill
import com.example.ui.components.StatusBadge
import com.example.ui.components.WaveformVisualizer
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
import kotlinx.coroutines.launch

enum class MainTab {
    JARVIS, HISTORY, SETTINGS
}

@Composable
fun JarvisMainScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by remember { mutableStateOf(MainTab.JARVIS) }
    var inputText by remember { mutableStateOf("") }

    // Dialog states
    var showCapabilities by remember { mutableStateOf(false) }
    var showTasks by remember { mutableStateOf(false) }
    var showStatus by remember { mutableStateOf(false) }

    // Observed viewmodel states
    val status by viewModel.status.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isForceOffline by viewModel.forceOffline.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()
    val lastReply by viewModel.lastAssistantReply.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    // Permission launcher for Voice input
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleListening()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Microphone permission required for voice assistant")
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisDarkBg),
        containerColor = JarvisDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = JarvisDarkBg,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == MainTab.JARVIS,
                    onClick = { currentTab = MainTab.JARVIS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "JARVIS"
                        )
                    },
                    label = {
                        Text(
                            text = "JARVIS",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanCore,
                        selectedTextColor = CyanCore,
                        unselectedIconColor = TechTextMuted,
                        unselectedTextColor = TechTextMuted,
                        indicatorColor = JarvisCardBg
                    ),
                    modifier = Modifier.testTag("nav_tab_jarvis")
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.HISTORY,
                    onClick = { currentTab = MainTab.HISTORY },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History"
                        )
                    },
                    label = {
                        Text(
                            text = "History",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanCore,
                        selectedTextColor = CyanCore,
                        unselectedIconColor = TechTextMuted,
                        unselectedTextColor = TechTextMuted,
                        indicatorColor = JarvisCardBg
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.SETTINGS,
                    onClick = { currentTab = MainTab.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = {
                        Text(
                            text = "Settings",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanCore,
                        selectedTextColor = CyanCore,
                        unselectedIconColor = TechTextMuted,
                        unselectedTextColor = TechTextMuted,
                        indicatorColor = JarvisCardBg
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.JARVIS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 1. Top Header (Exactly like screenshot)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text(
                                text = "J.A.R.V.I.S.",
                                fontFamily = OrbitronFontFamily,
                                color = TechTextCyan,
                                fontSize = 28.sp,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Just A Rather Very Intelligent System",
                                fontFamily = RajdhaniFontFamily,
                                color = TechTextMuted,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StatusBadge(
                                isOnline = diagnostics.isOnline,
                                language = currentLanguage,
                                onClick = { showStatus = true }
                            )
                        }

                        // 2. Center Arc Reactor Visualizer + Status Text
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            ArcReactorPulse(
                                isListening = isListening,
                                isSpeaking = isSpeaking,
                                audioLevel = audioLevel
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = status.displayText,
                                fontFamily = OrbitronFontFamily,
                                color = when (status) {
                                    AssistantStatus.READY -> TechTextCyan
                                    AssistantStatus.LISTENING -> Color(0xFFFFB300)
                                    AssistantStatus.THINKING -> CyanBright
                                    AssistantStatus.SPEAKING -> CyanCore
                                    AssistantStatus.ERROR -> Color(0xFFFF5252)
                                },
                                fontSize = 16.sp,
                                letterSpacing = 2.sp
                            )

                            // Display subtitle if speaking
                            AnimatedVisibility(
                                visible = lastReply.isNotBlank() && (status == AssistantStatus.SPEAKING || status == AssistantStatus.ERROR),
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Text(
                                    text = lastReply,
                                    fontFamily = RajdhaniFontFamily,
                                    color = TechTextWhite,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                                )
                            }
                        }

                        // 3. Audio Waveform
                        WaveformVisualizer(
                            isListening = isListening,
                            isSpeaking = isSpeaking,
                            audioLevel = audioLevel,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // 4. Input Row & Quick Action Buttons
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Text Input Field + Mic + Send
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, JarvisCardBorder, RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    color = JarvisCardBg
                                ) {
                                    TextField(
                                        value = inputText,
                                        onValueChange = { inputText = it },
                                        placeholder = {
                                            Text(
                                                text = "Type your command, sir...",
                                                fontFamily = RajdhaniFontFamily,
                                                color = TechTextMuted,
                                                fontSize = 14.sp
                                            )
                                        },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            focusedTextColor = TechTextWhite,
                                            unfocusedTextColor = TechTextWhite
                                        ),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                        keyboardActions = KeyboardActions(
                                            onSend = {
                                                if (inputText.isNotBlank()) {
                                                    viewModel.processUserCommand(inputText)
                                                    inputText = ""
                                                    keyboardController?.hide()
                                                }
                                            }
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.testTag("command_input_field")
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Mic Button
                                IconButton(
                                    onClick = {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPermission) {
                                            viewModel.toggleListening()
                                        } else {
                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = if (isListening) Color(0xFFFFB300) else CyanCore
                                    ),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("mic_button")
                                ) {
                                    Icon(
                                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                        contentDescription = "Voice Input",
                                        tint = JarvisDarkBg
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Send Button
                                IconButton(
                                    onClick = {
                                        if (inputText.isNotBlank()) {
                                            viewModel.processUserCommand(inputText)
                                            inputText = ""
                                            keyboardController?.hide()
                                        }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = CyanCore
                                    ),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("send_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send Command",
                                        tint = JarvisDarkBg
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Quick Action Pills ("Capabilities", "My Tasks", "Status")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                QuickActionPill(
                                    label = "Capabilities",
                                    onClick = { showCapabilities = true },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                QuickActionPill(
                                    label = "My Tasks",
                                    onClick = { showTasks = true },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                QuickActionPill(
                                    label = "Status",
                                    onClick = { showStatus = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                MainTab.HISTORY -> {
                    HistoryScreen(
                        messages = chatMessages,
                        onClearHistory = { viewModel.clearHistory() },
                        modifier = Modifier.statusBarsPadding()
                    )
                }
                MainTab.SETTINGS -> {
                    SettingsScreen(
                        currentLanguage = currentLanguage,
                        onLanguageSelected = { viewModel.setLanguage(it) },
                        isForceOffline = isForceOffline,
                        onToggleOffline = { viewModel.toggleForceOffline() },
                        onSpeechRateChanged = { viewModel.ttsManager.setSpeechRate(it) },
                        modifier = Modifier.statusBarsPadding()
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (showCapabilities) {
        CapabilitiesDialog(
            onDismiss = { showCapabilities = false },
            onExecuteCommand = { cmd ->
                viewModel.processUserCommand(cmd)
            }
        )
    }

    if (showTasks) {
        TasksDialog(
            onDismiss = { showTasks = false },
            onRunTask = { taskCmd ->
                viewModel.processUserCommand(taskCmd)
            }
        )
    }

    if (showStatus) {
        StatusDialog(
            diagnostics = diagnostics,
            selectedLanguage = currentLanguage,
            onDismiss = { showStatus = false }
        )
    }
}
