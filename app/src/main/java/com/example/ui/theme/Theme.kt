package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val JarvisColorScheme = darkColorScheme(
    primary = CyanCore,
    onPrimary = JarvisDarkBg,
    primaryContainer = JarvisCardBg,
    onPrimaryContainer = CyanBright,
    secondary = CyanDim,
    onSecondary = JarvisDarkBg,
    secondaryContainer = JarvisSurfaceDark,
    onSecondaryContainer = TechTextWhite,
    background = JarvisDarkBg,
    onBackground = TechTextWhite,
    surface = JarvisSurfaceDark,
    onSurface = TechTextWhite,
    surfaceVariant = JarvisCardBg,
    onSurfaceVariant = TechTextMuted,
    outline = JarvisCardBorder,
    error = StatusError,
    onError = TechTextWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = JarvisDarkBg.toArgb()
                it.navigationBarColor = JarvisDarkBg.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
