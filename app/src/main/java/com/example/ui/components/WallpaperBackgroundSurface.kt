package com.example.ui.components

import android.app.WallpaperManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.BackgroundSource
import com.example.data.TransparencySettings

/**
 * Curated artistic gradient presets for background layer.
 */
val GLASS_BACKGROUND_PRESETS = listOf(
    // 0: Midnight Aurora
    listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF003049), Color(0xFF14213D)),
    // 1: Deep Indigo
    listOf(Color(0xFF1E1B4B), Color(0xFF311042), Color(0xFF2E1065), Color(0xFF0F172A)),
    // 2: Obsidian Glow
    listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF090D16), Color(0xFF1A1C1E)),
    // 3: Sunset Prism
    listOf(Color(0xFF4C0519), Color(0xFF431407), Color(0xFF2E1065), Color(0xFF18181B))
)

val GLASS_PRESET_NAMES = listOf(
    "Midnight Aurora",
    "Deep Indigo",
    "Obsidian Glow",
    "Sunset Prism"
)

/**
 * Layer 1 of the app: The wallpaper or custom background surface placed behind Facebook WebView.
 * Handles blur, brightness, and dimming adjustments in real time.
 */
@Composable
fun WallpaperBackgroundSurface(
    settings: TransparencySettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var systemWallpaperDrawable by remember { mutableStateOf<Drawable?>(null) }

    LaunchedEffect(settings.backgroundSource) {
        if (settings.backgroundSource == BackgroundSource.SYSTEM_WALLPAPER) {
            try {
                val wm = WallpaperManager.getInstance(context)
                systemWallpaperDrawable = wm.drawable ?: wm.builtInDrawable
            } catch (e: Exception) {
                systemWallpaperDrawable = null
            }
        }
    }

    // Calculate blur (0 to 100 maps to 0.dp to 35.dp)
    val blurRadius = (settings.blurAmount * 0.35f).dp

    // Calculate brightness color matrix factor (0 to 100 maps to 0.4f to 1.4f)
    val brightnessFactor = (0.4f + (settings.brightnessAmount / 100f) * 1.0f).coerceIn(0.2f, 2.0f)
    val colorMatrix = remember(brightnessFactor) {
        ColorMatrix().apply {
            setToScale(brightnessFactor, brightnessFactor, brightnessFactor, 1f)
        }
    }

    // Dim factor (0 to 100 maps to 0f to 0.85f overlay)
    val dimAlpha = (settings.dimAmount / 100f * 0.85f).coerceIn(0f, 0.95f)

    Box(modifier = modifier.fillMaxSize()) {
        // Base Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (settings.blurAmount > 0) Modifier.blur(blurRadius) else Modifier
                )
        ) {
            when (settings.backgroundSource) {
                BackgroundSource.SYSTEM_WALLPAPER -> {
                    val drawable = systemWallpaperDrawable
                    if (drawable is BitmapDrawable) {
                        Image(
                            bitmap = drawable.bitmap.asImageBitmap(),
                            contentDescription = "Device Wallpaper",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.colorMatrix(colorMatrix)
                        )
                    } else {
                        // Fallback to Preset 0 if system wallpaper unavailable
                        PresetGradientBackground(
                            presetIndex = settings.backgroundPresetIndex,
                            colorMatrix = colorMatrix
                        )
                    }
                }

                BackgroundSource.CUSTOM_IMAGE -> {
                    if (!settings.customImageUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(settings.customImageUri))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Custom Background Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.colorMatrix(colorMatrix)
                        )
                    } else {
                        // Fallback
                        PresetGradientBackground(
                            presetIndex = settings.backgroundPresetIndex,
                            colorMatrix = colorMatrix
                        )
                    }
                }

                BackgroundSource.PRESET_GRADIENT -> {
                    PresetGradientBackground(
                        presetIndex = settings.backgroundPresetIndex,
                        colorMatrix = colorMatrix
                    )
                }
            }
        }

        // Dimming scrim overlay
        if (dimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimAlpha))
            )
        }
    }
}

@Composable
private fun PresetGradientBackground(
    presetIndex: Int,
    colorMatrix: ColorMatrix
) {
    val colors = GLASS_BACKGROUND_PRESETS.getOrElse(presetIndex) {
        GLASS_BACKGROUND_PRESETS[0]
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = colors
                )
            )
    )
}
