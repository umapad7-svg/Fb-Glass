package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AccentPreset(
    val name: String,
    val color: Color
)

val AccentColorPresets = listOf(
    AccentPreset("Blue", Color(0xFF1877F2)),
    AccentPreset("Purple", Color(0xFF7C4DFF)),
    AccentPreset("Pink", Color(0xFFFF4081)),
    AccentPreset("Red", Color(0xFFFF5252)),
    AccentPreset("Orange", Color(0xFFFF6D00)),
    AccentPreset("Green", Color(0xFF00C853)),
    AccentPreset("Teal", Color(0xFF00BFA5)),
    AccentPreset("Cyan", Color(0xFF00B4D8))
)

@Immutable
data class GlassTokens(
    val surfaceColor: Color,
    val surfaceGradient: Brush,
    val borderColor: Color,
    val borderBrush: Brush,
    val specularHighlight: Brush,
    val textPrimary: Color,
    val textSecondary: Color,
    val isDark: Boolean
)

val LocalAccentColor = compositionLocalOf { Color(0xFF1877F2) }
val LocalGlassTokens = compositionLocalOf {
    GlassTokens(
        surfaceColor = Color(0xCCFFFFFF),
        surfaceGradient = Brush.verticalGradient(listOf(Color(0xF0FFFFFF), Color(0xCCF5F8FC))),
        borderColor = Color(0x66FFFFFF),
        borderBrush = Brush.linearGradient(listOf(Color(0x80FFFFFF), Color(0x26FFFFFF))),
        specularHighlight = Brush.linearGradient(listOf(Color(0x99FFFFFF), Color(0x00FFFFFF))),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF475569),
        isDark = false
    )
}

@Composable
fun rememberGlassTokens(isDark: Boolean, accentColor: Color): GlassTokens {
    val animatedAccent by animateColorAsState(targetValue = accentColor, animationSpec = tween(350), label = "accent")

    return if (isDark) {
        GlassTokens(
            surfaceColor = Color(0xCC1A1C1E),
            surfaceGradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xE61A1C1E),
                    Color(0xD92D3033),
                    Color(0xE61A1C1E)
                ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            ),
            borderColor = Color(0x33FFFFFF),
            borderBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0x4DFFFFFF),
                    animatedAccent.copy(alpha = 0.45f),
                    Color(0x1FFFFFFF)
                )
            ),
            specularHighlight = Brush.verticalGradient(
                colors = listOf(Color(0x26FFFFFF), Color(0x00000000))
            ),
            textPrimary = Color(0xFFF1F5F9),
            textSecondary = Color(0xFF94A3B8),
            isDark = true
        )
    } else {
        GlassTokens(
            surfaceColor = Color(0xECFFFFFF),
            surfaceGradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xF8FFFFFF),
                    Color(0xE6F0F2F5),
                    animatedAccent.copy(alpha = 0.05f)
                ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            ),
            borderColor = Color(0x33000000),
            borderBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0x66FFFFFF),
                    animatedAccent.copy(alpha = 0.35f),
                    Color(0x1F000000)
                )
            ),
            specularHighlight = Brush.verticalGradient(
                colors = listOf(Color(0x80FFFFFF), Color(0x00FFFFFF))
            ),
            textPrimary = Color(0xFF1A1C1E),
            textSecondary = Color(0xFF64748B),
            isDark = false
        )
    }
}

/**
 * Modifier that renders a rich Liquid Glass surface with translucent backdrop,
 * specular edge refraction border, and soft ambient shadow.
 */
fun Modifier.liquidGlassSurface(
    shape: Shape = RoundedCornerShape(24.dp),
    tokens: GlassTokens,
    elevation: Dp = 12.dp,
    borderWidth: Dp = 1.dp
): Modifier = this
    .shadow(elevation = elevation, shape = shape, spotColor = tokens.borderColor)
    .clip(shape)
    .background(tokens.surfaceGradient)
    .border(border = BorderStroke(borderWidth, tokens.borderBrush), shape = shape)
    .drawBehind {
        // Specular top highlight refraction line
        drawRect(
            brush = tokens.specularHighlight,
            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.35f)
        )
    }
