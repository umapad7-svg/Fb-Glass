package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransparencySettings
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.LocalGlassTokens
import com.example.ui.theme.liquidGlassSurface

/**
 * Compact floating Liquid Glass control for adjusting Facebook background transparency in real time
 * without leaving the browsing experience.
 */
@Composable
fun QuickTransparencyControl(
    visible: Boolean,
    settings: TransparencySettings,
    onTransparencyChange: (Int) -> Unit,
    onGlassModeToggle: (Boolean) -> Unit,
    onAutoReadabilityToggle: (Boolean) -> Unit,
    onOpenWallpaperSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassTokens.current
    val accent = LocalAccentColor.current

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .liquidGlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    tokens = tokens,
                    elevation = 24.dp,
                    borderWidth = 1.2.dp
                )
                .clip(RoundedCornerShape(24.dp))
                .padding(18.dp)
                .testTag("quick_transparency_panel")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Facebook Transparency",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = tokens.textPrimary
                            )
                            Text(
                                text = "Real-time DOM glass rendering",
                                fontSize = 11.sp,
                                color = tokens.textSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .testTag("quick_transparency_close")
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = tokens.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Percentage and Stepper / Slider Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Opacity Level",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = tokens.textSecondary
                    )

                    Text(
                        text = "${settings.transparencyPercent}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        modifier = Modifier.testTag("quick_transparency_value")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            onTransparencyChange((settings.transparencyPercent - 10).coerceAtLeast(0))
                        },
                        modifier = Modifier
                            .testTag("quick_transparency_decrement")
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(tokens.textPrimary.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = tokens.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Slider(
                        value = settings.transparencyPercent.toFloat(),
                        onValueChange = { onTransparencyChange(it.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                            .testTag("quick_transparency_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = accent,
                            activeTrackColor = accent,
                            inactiveTrackColor = tokens.textPrimary.copy(alpha = 0.15f)
                        )
                    )

                    IconButton(
                        onClick = {
                            onTransparencyChange((settings.transparencyPercent + 10).coerceAtMost(100))
                        },
                        modifier = Modifier
                            .testTag("quick_transparency_increment")
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(tokens.textPrimary.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = tokens.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Presets: Normal, 50%, Glass
                Text(
                    text = "PRESETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.textSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransparencyPresetButton(
                        title = "Normal",
                        subtitle = "0%",
                        isSelected = settings.transparencyPercent == 0,
                        onClick = {
                            onTransparencyChange(0)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "preset_normal"
                    )

                    TransparencyPresetButton(
                        title = "50%",
                        subtitle = "Balanced",
                        isSelected = settings.transparencyPercent == 50 && !settings.isGlassModeEnabled,
                        onClick = {
                            onTransparencyChange(50)
                            onGlassModeToggle(false)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "preset_50"
                    )

                    TransparencyPresetButton(
                        title = "Glass",
                        subtitle = "80% + Blur",
                        isSelected = settings.transparencyPercent >= 75 && settings.isGlassModeEnabled,
                        onClick = {
                            onTransparencyChange(80)
                            onGlassModeToggle(true)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "preset_glass"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle Row: Liquid Glass Mode & Auto Readability
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Liquid Glass Mode",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "Subtle card blur & glow borders",
                            fontSize = 11.sp,
                            color = tokens.textSecondary
                        )
                    }

                    Switch(
                        checked = settings.isGlassModeEnabled,
                        onCheckedChange = onGlassModeToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accent
                        ),
                        modifier = Modifier.testTag("switch_glass_mode")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto Readability",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "Ensures text contrast over wallpaper",
                            fontSize = 11.sp,
                            color = tokens.textSecondary
                        )
                    }

                    Switch(
                        checked = settings.isAutoReadabilityEnabled,
                        onCheckedChange = onAutoReadabilityToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accent
                        ),
                        modifier = Modifier.testTag("switch_auto_readability")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Customize Wallpaper Shortcut
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.textPrimary.copy(alpha = 0.05f))
                        .clickable(onClick = onOpenWallpaperSettings)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("btn_wallpaper_settings"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wallpaper,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Wallpaper, Blur & Brightness",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = tokens.textPrimary
                        )
                    }

                    Text(
                        text = "Edit >",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
            }
        }
    }
}

@Composable
private fun TransparencyPresetButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val tokens = LocalGlassTokens.current
    val accent = LocalAccentColor.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) accent.copy(alpha = 0.22f) else tokens.textPrimary.copy(alpha = 0.06f)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) accent else tokens.borderColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSelected) accent else tokens.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = tokens.textSecondary
            )
        }
    }
}
