package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.AccentSettings
import com.example.data.ThemeMode

@Composable
fun FacebookGlassTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentSettings: AccentSettings = AccentSettings(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    // Determine Accent Color
    val resolvedAccent: Color = if (accentSettings.useSystemColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val dynamicScheme = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dynamicScheme.primary
    } else {
        if (accentSettings.presetIndex in AccentColorPresets.indices) {
            AccentColorPresets[accentSettings.presetIndex].color
        } else {
            Color(accentSettings.customColorHex)
        }
    }

    val glassTokens = rememberGlassTokens(isDark = isDark, accentColor = resolvedAccent)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = resolvedAccent,
            onPrimary = Color.White,
            primaryContainer = resolvedAccent.copy(alpha = 0.25f),
            onPrimaryContainer = Color.White,
            surface = Color(0xFF2D3033),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF24272A),
            onSurfaceVariant = Color(0xFF94A3B8),
            background = Color(0xFF1A1C1E),
            onBackground = Color(0xFFF1F5F9)
        )
    } else {
        lightColorScheme(
            primary = resolvedAccent,
            onPrimary = Color.White,
            primaryContainer = resolvedAccent.copy(alpha = 0.15f),
            onPrimaryContainer = resolvedAccent,
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1C1E),
            surfaceVariant = Color(0xFFF0F2F5),
            onSurfaceVariant = Color(0xFF64748B),
            background = Color(0xFFF0F2F5),
            onBackground = Color(0xFF1A1C1E)
        )
    }

    CompositionLocalProvider(
        LocalAccentColor provides resolvedAccent,
        LocalGlassTokens provides glassTokens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    FacebookGlassTheme(content = content)
}

