package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = KiranaGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = KiranaGreenLight,
    onPrimaryContainer = KiranaGreenDark,
    secondary = KiranaNavyHeader,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = KiranaNavyText,
    tertiary = AmberWarning,
    onTertiary = Color.White,
    tertiaryContainer = AmberWarningBg,
    onTertiaryContainer = Color(0xFF78350F),
    background = PosBg,
    onBackground = KiranaNavyText,
    surface = SurfaceWhite,
    onSurface = KiranaNavyText,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = KiranaNavySubtle,
    outline = KiranaNavyBorder,
    outlineVariant = DividerColor,
    error = UdhaarRed,
    errorContainer = UdhaarRedBg,
    onError = Color.White,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF34D399),
    onPrimary = Color(0xFF064E3B),
    primaryContainer = Color(0xFF065F46),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = Color(0xFF93C5FD),
    onSecondary = Color(0xFF1E3A8A),
    background = Color(0xFF0B132B),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF1C2541),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF283655),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

@Composable
fun SmartKiranaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
