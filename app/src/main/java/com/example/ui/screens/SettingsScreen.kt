package com.example.ui.screens

import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccentSettings
import com.example.data.BackgroundSource
import com.example.data.FilterSettings
import com.example.data.ThemeMode
import com.example.data.TransparencySettings
import com.example.data.UserPreferences
import com.example.ui.components.GLASS_BACKGROUND_PRESETS
import com.example.ui.components.GLASS_PRESET_NAMES
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassConfirmationDialog
import com.example.ui.components.GlassSectionHeader
import com.example.ui.components.GlassSwitchRow
import com.example.ui.theme.AccentColorPresets
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.LocalGlassTokens
import com.example.ui.theme.liquidGlassSurface

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onClose: () -> Unit,
    onOpenInBrowser: () -> Unit,
    onUpdateFilters: (hideAds: Boolean?, hideSponsored: Boolean?, hideStories: Boolean?, hidePymk: Boolean?) -> Unit,
    onUpdateTheme: (ThemeMode) -> Unit,
    onUpdateAccent: (useSystem: Boolean, presetIndex: Int, customHex: Long?) -> Unit,
    onUpdateTransparency: (
        percent: Int?,
        isGlassMode: Boolean?,
        isAutoReadability: Boolean?,
        source: BackgroundSource?,
        customUri: String?,
        presetIndex: Int?,
        blur: Int?,
        brightness: Int?,
        dim: Int?
    ) -> Unit,
    onResetSettings: () -> Unit,
    onClearCookies: () -> Unit,
    onClearCache: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    val accent = LocalAccentColor.current
    val context = LocalContext.current

    var showClearCookiesDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var showCustomColorDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdateTransparency(null, null, null, BackgroundSource.CUSTOM_IMAGE, uri.toString(), null, null, null, null)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        if (tokens.isDark) Color(0xFF1A1C1E) else Color(0xFFFFFFFF),
                        if (tokens.isDark) Color(0xFF2D3033) else Color(0xFFF0F2F5),
                        if (tokens.isDark) Color(0xFF1A1C1E) else Color(0xFFFFFFFF)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .liquidGlassSurface(
                                shape = CircleShape,
                                tokens = tokens,
                                elevation = 4.dp,
                                borderWidth = 1.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Settings",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onOpenInBrowser,
                        modifier = Modifier
                            .testTag("settings_open_external_browser_button")
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Open in External Browser",
                            tint = tokens.textSecondary
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .testTag("settings_close_button")
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Settings",
                            tint = tokens.textPrimary
                        )
                    }
                }
            }

            // Scrollable Settings Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Section 0: Facebook Background Transparency
                GlassSectionHeader(title = "FACEBOOK TRANSPARENCY", icon = Icons.Default.Opacity)
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Background Transparency",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = tokens.textPrimary
                                )
                                Text(
                                    text = "Renders Facebook webpage background with live transparency to show your wallpaper.",
                                    fontSize = 12.sp,
                                    color = tokens.textSecondary
                                )
                            }
                            Text(
                                text = "${preferences.transparencySettings.transparencyPercent}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                modifier = Modifier.testTag("settings_transparency_percent_text")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    onUpdateTransparency(
                                        (preferences.transparencySettings.transparencyPercent - 10).coerceAtLeast(0),
                                        null, null, null, null, null, null, null, null
                                    )
                                },
                                modifier = Modifier
                                    .testTag("settings_transparency_minus")
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(tokens.textPrimary.copy(alpha = 0.08f))
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = tokens.textPrimary)
                            }

                            Slider(
                                value = preferences.transparencySettings.transparencyPercent.toFloat(),
                                onValueChange = {
                                    onUpdateTransparency(it.toInt(), null, null, null, null, null, null, null, null)
                                },
                                valueRange = 0f..100f,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                                    .testTag("settings_transparency_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = accent,
                                    activeTrackColor = accent,
                                    inactiveTrackColor = tokens.textPrimary.copy(alpha = 0.15f)
                                )
                            )

                            IconButton(
                                onClick = {
                                    onUpdateTransparency(
                                        (preferences.transparencySettings.transparencyPercent + 10).coerceAtMost(100),
                                        null, null, null, null, null, null, null, null
                                    )
                                },
                                modifier = Modifier
                                    .testTag("settings_transparency_plus")
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(tokens.textPrimary.copy(alpha = 0.08f))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = tokens.textPrimary)
                            }
                        }

                        // Presets
                        Text(
                            text = "Transparency Presets",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = tokens.textSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val currentPercent = preferences.transparencySettings.transparencyPercent
                            val isGlass = preferences.transparencySettings.isGlassModeEnabled

                            TransparencyPresetChip(
                                title = "Normal",
                                subtitle = "0% (Opaque)",
                                isSelected = currentPercent == 0,
                                onClick = {
                                    onUpdateTransparency(0, null, null, null, null, null, null, null, null)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            TransparencyPresetChip(
                                title = "50%",
                                subtitle = "Balanced",
                                isSelected = currentPercent == 50 && !isGlass,
                                onClick = {
                                    onUpdateTransparency(50, false, null, null, null, null, null, null, null)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            TransparencyPresetChip(
                                title = "Glass",
                                subtitle = "80% (High)",
                                isSelected = currentPercent >= 75 && isGlass,
                                onClick = {
                                    onUpdateTransparency(80, true, null, null, null, null, null, null, null)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = tokens.borderColor.copy(alpha = 0.4f))

                        GlassSwitchRow(
                            title = "Liquid Glass Mode",
                            description = "Frosted translucent cards, subtle backdrop blur, and glow border highlights.",
                            checked = preferences.transparencySettings.isGlassModeEnabled,
                            testTag = "settings_switch_glass_mode",
                            onCheckedChange = { onUpdateTransparency(null, it, null, null, null, null, null, null, null) }
                        )

                        HorizontalDivider(color = tokens.borderColor.copy(alpha = 0.4f))

                        GlassSwitchRow(
                            title = "Auto Readability",
                            description = "Ensures text, buttons, and comments remain clearly legible over any wallpaper background.",
                            checked = preferences.transparencySettings.isAutoReadabilityEnabled,
                            testTag = "settings_switch_auto_readability",
                            onCheckedChange = { onUpdateTransparency(null, null, it, null, null, null, null, null, null) }
                        )
                    }
                }

                // Section 0.5: Wallpaper & Background Surface
                GlassSectionHeader(title = "WALLPAPER & BACKGROUND", icon = Icons.Default.Wallpaper)
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Background Surface Source",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "Choose what is displayed behind Facebook's translucent page.",
                            fontSize = 12.sp,
                            color = tokens.textSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val currentSource = preferences.transparencySettings.backgroundSource

                            WallpaperSourceChip(
                                title = "Wallpaper",
                                icon = Icons.Default.Wallpaper,
                                isSelected = currentSource == BackgroundSource.SYSTEM_WALLPAPER,
                                onClick = {
                                    onUpdateTransparency(null, null, null, BackgroundSource.SYSTEM_WALLPAPER, null, null, null, null, null)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            WallpaperSourceChip(
                                title = "Custom Photo",
                                icon = Icons.Default.Image,
                                isSelected = currentSource == BackgroundSource.CUSTOM_IMAGE,
                                onClick = {
                                    onUpdateTransparency(null, null, null, BackgroundSource.CUSTOM_IMAGE, null, null, null, null, null)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            WallpaperSourceChip(
                                title = "Glass Mesh",
                                icon = Icons.Default.AutoAwesome,
                                isSelected = currentSource == BackgroundSource.PRESET_GRADIENT,
                                onClick = {
                                    onUpdateTransparency(null, null, null, BackgroundSource.PRESET_GRADIENT, null, null, null, null, null)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (preferences.transparencySettings.backgroundSource == BackgroundSource.CUSTOM_IMAGE) {
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_select_wallpaper_image"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accent,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (preferences.transparencySettings.customImageUri != null) "Change Selected Photo" else "Select Device Photo",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else if (preferences.transparencySettings.backgroundSource == BackgroundSource.PRESET_GRADIENT) {
                            Text(
                                text = "Glass Gradient Presets",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = tokens.textSecondary
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                itemsIndexed(GLASS_BACKGROUND_PRESETS) { idx, colors ->
                                    val isSelected = preferences.transparencySettings.backgroundPresetIndex == idx
                                    val name = GLASS_PRESET_NAMES.getOrElse(idx) { "Preset $idx" }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable {
                                                onUpdateTransparency(null, null, null, null, null, idx, null, null, null)
                                            }
                                            .padding(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Brush.linearGradient(colors))
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) accent else tokens.borderColor.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(12.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = name,
                                            fontSize = 10.sp,
                                            color = if (isSelected) accent else tokens.textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = tokens.borderColor.copy(alpha = 0.4f))

                        // Adjustment Sliders
                        AdjustmentSliderRow(
                            title = "Blur Amount",
                            icon = Icons.Default.BlurOn,
                            value = preferences.transparencySettings.blurAmount,
                            onValueChange = { onUpdateTransparency(null, null, null, null, null, null, it, null, null) },
                            testTag = "slider_wallpaper_blur"
                        )

                        AdjustmentSliderRow(
                            title = "Brightness",
                            icon = Icons.Default.Brightness6,
                            value = preferences.transparencySettings.brightnessAmount,
                            onValueChange = { onUpdateTransparency(null, null, null, null, null, null, null, it, null) },
                            testTag = "slider_wallpaper_brightness"
                        )

                        AdjustmentSliderRow(
                            title = "Dimming",
                            icon = Icons.Default.Contrast,
                            value = preferences.transparencySettings.dimAmount,
                            onValueChange = { onUpdateTransparency(null, null, null, null, null, null, null, null, it) },
                            testTag = "slider_wallpaper_dim"
                        )
                    }
                }

                // Section 1: Content Filtering
                GlassSectionHeader(title = "CONTENT FILTERING", icon = Icons.Default.FilterList)
                GlassCard {
                    Column {
                        GlassSwitchRow(
                            title = "Hide Ads",
                            description = "Attempts to remove advertisements displayed in your Facebook feed.",
                            checked = preferences.filterSettings.hideAds,
                            testTag = "switch_hide_ads",
                            onCheckedChange = { onUpdateFilters(it, null, null, null) }
                        )

                        HorizontalDivider(color = tokens.borderColor.copy(alpha = 0.4f))

                        GlassSwitchRow(
                            title = "Hide Sponsored Posts",
                            description = "Attempts to remove posts marked as sponsored.",
                            checked = preferences.filterSettings.hideSponsored,
                            testTag = "switch_hide_sponsored",
                            onCheckedChange = { onUpdateFilters(null, it, null, null) }
                        )

                        HorizontalDivider(color = tokens.borderColor.copy(alpha = 0.4f))

                        GlassSwitchRow(
                            title = "Hide Stories",
                            description = "Optionally hide the Stories section from the Facebook feed.",
                            checked = preferences.filterSettings.hideStories,
                            testTag = "switch_hide_stories",
                            onCheckedChange = { onUpdateFilters(null, null, it, null) }
                        )

                        HorizontalDivider(color = tokens.borderColor.copy(alpha = 0.4f))

                        GlassSwitchRow(
                            title = "Hide People You May Know",
                            description = "Hide recommendation cards and friend suggestions.",
                            checked = preferences.filterSettings.hidePeopleYouMayKnow,
                            testTag = "switch_hide_pymk",
                            onCheckedChange = { onUpdateFilters(null, null, null, it) }
                        )
                    }
                }

                // Section 2: Accent Colour
                GlassSectionHeader(title = "ACCENT COLOUR", icon = Icons.Default.Palette)
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // System Dynamic Colour Toggle
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(color = accent),
                                        role = Role.Switch,
                                        onClick = {
                                            onUpdateAccent(!preferences.accentSettings.useSystemColor, preferences.accentSettings.presetIndex, null)
                                        }
                                    )
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Use system colour",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = tokens.textPrimary
                                    )
                                    Text(
                                        text = "Matches your Android wallpaper dynamic theme.",
                                        fontSize = 12.sp,
                                        color = tokens.textSecondary
                                    )
                                }
                                Switch(
                                    checked = preferences.accentSettings.useSystemColor,
                                    onCheckedChange = {
                                        onUpdateAccent(it, preferences.accentSettings.presetIndex, null)
                                    },
                                    modifier = Modifier.testTag("switch_use_system_color"),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = accent
                                    )
                                )
                            }
                            HorizontalDivider(color = tokens.borderColor.copy(alpha = 0.4f))
                        }

                        // Presets list
                        Text(
                            text = "Preset Accents",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = tokens.textSecondary
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(AccentColorPresets) { index, preset ->
                                val isSelected = !preferences.accentSettings.useSystemColor && preferences.accentSettings.presetIndex == index

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(color = preset.color),
                                            onClick = { onUpdateAccent(false, index, null) }
                                        )
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(preset.color)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) tokens.textPrimary else Color.Transparent,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = preset.name,
                                        fontSize = 11.sp,
                                        color = if (isSelected) tokens.textPrimary else tokens.textSecondary
                                    )
                                }
                            }

                            // Custom color chip
                            item {
                                val isCustomSelected = !preferences.accentSettings.useSystemColor && preferences.accentSettings.presetIndex == 8
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(color = accent),
                                            onClick = { showCustomColorDialog = true }
                                        )
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(preferences.accentSettings.customColorHex))
                                            .border(
                                                width = if (isCustomSelected) 3.dp else 1.dp,
                                                color = if (isCustomSelected) tokens.textPrimary else tokens.borderColor,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ColorLens,
                                            contentDescription = "Custom Color",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Custom",
                                        fontSize = 11.sp,
                                        color = if (isCustomSelected) tokens.textPrimary else tokens.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: Appearance
                GlassSectionHeader(title = "APPEARANCE", icon = Icons.Default.Palette)
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Theme mode",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "Adapts liquid glass panels between bright frosted surfaces and dark obsidian glass.",
                            fontSize = 12.sp,
                            color = tokens.textSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                val isCurrent = preferences.themeMode == mode
                                val label = when (mode) {
                                    ThemeMode.SYSTEM -> "System"
                                    ThemeMode.LIGHT -> "Light"
                                    ThemeMode.DARK -> "Dark"
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isCurrent) accent.copy(alpha = 0.22f) else tokens.surfaceColor
                                        )
                                        .border(
                                            width = if (isCurrent) 1.5.dp else 0.8.dp,
                                            color = if (isCurrent) accent else tokens.borderColor,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(color = accent),
                                            onClick = { onUpdateTheme(mode) }
                                        )
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrent) accent else tokens.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 4: Privacy
                GlassSectionHeader(title = "PRIVACY & LOCAL STORAGE", icon = Icons.Default.Shield)
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Privacy Statement",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "• Facebook Glass operates as a native Android WebView wrapper.\n" +
                                    "• Facebook authentication is securely managed by Facebook directly.\n" +
                                    "• Your Facebook credentials are never accessed or stored by this app.\n" +
                                    "• Browsing activity is never transmitted to any third-party servers.\n" +
                                    "• All preferences and filtering toggles remain exclusively on your device.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = tokens.textSecondary
                        )

                        HorizontalDivider(color = tokens.borderColor.copy(alpha = 0.4f))

                        // Clear Facebook Cookies & Data
                        Button(
                            onClick = { showClearCookiesDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_clear_cookies"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                                contentColor = Color(0xFFEF4444)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear Facebook Cookies & Data", fontWeight = FontWeight.SemiBold)
                        }

                        // Clear Cache
                        OutlinedButton(
                            onClick = onClearCache,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_clear_cache"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = tokens.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear Cache", color = tokens.textPrimary)
                        }

                        // Reset Settings
                        OutlinedButton(
                            onClick = { showResetSettingsDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_reset_settings"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = tokens.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Settings", color = tokens.textSecondary)
                        }
                    }
                }

                // Section 5: App Version
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Facebook Glass • Liquid Glass v1.0",
                        fontSize = 12.sp,
                        color = tokens.textSecondary.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    // Confirmation Dialog: Clear Cookies & Data
    if (showClearCookiesDialog) {
        GlassConfirmationDialog(
            title = "Clear Cookies & Session Data?",
            message = "This will log you out of Facebook and remove stored site cookies from this device. Are you sure you wish to continue?",
            confirmText = "Clear & Log Out",
            isDestructive = true,
            onConfirm = {
                showClearCookiesDialog = false
                onClearCookies()
            },
            onDismiss = { showClearCookiesDialog = false }
        )
    }

    // Confirmation Dialog: Reset Settings
    if (showResetSettingsDialog) {
        GlassConfirmationDialog(
            title = "Reset All Settings?",
            message = "This will restore content filtering options, appearance, and accent color to their original default values.",
            confirmText = "Reset",
            isDestructive = true,
            onConfirm = {
                showResetSettingsDialog = false
                onResetSettings()
            },
            onDismiss = { showResetSettingsDialog = false }
        )
    }

    // Custom Color Dialog
    if (showCustomColorDialog) {
        CustomColorDialog(
            initialHex = preferences.accentSettings.customColorHex,
            onDismiss = { showCustomColorDialog = false },
            onApply = { hex ->
                showCustomColorDialog = false
                onUpdateAccent(false, 8, hex)
            }
        )
    }
}

@Composable
private fun CustomColorDialog(
    initialHex: Long,
    onDismiss: () -> Unit,
    onApply: (Long) -> Unit
) {
    val tokens = LocalGlassTokens.current
    val accent = LocalAccentColor.current
    var hexInput by remember {
        mutableStateOf(String.format("%06X", 0xFFFFFF and initialHex.toInt()))
    }

    val parsedColor = try {
        Color(android.graphics.Color.parseColor("#$hexInput"))
    } catch (e: Exception) {
        accent
    }

    GlassConfirmationDialog(
        title = "Custom Accent Color",
        message = "Enter a 6-digit RGB hex code (e.g., 1877F2, 00C853, 9C27B0):",
        confirmText = "Apply",
        onConfirm = {
            try {
                val colorInt = android.graphics.Color.parseColor("#$hexInput")
                onApply(0xFF000000L or (colorInt.toLong() and 0xFFFFFFL))
            } catch (e: Exception) {
                onDismiss()
            }
        },
        onDismiss = onDismiss
    )
}

@Composable
private fun TransparencyPresetChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassTokens.current
    val accent = LocalAccentColor.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) accent.copy(alpha = 0.18f)
                else tokens.textPrimary.copy(alpha = 0.05f)
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accent else tokens.borderColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accent),
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) accent else tokens.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (isSelected) accent.copy(alpha = 0.85f) else tokens.textSecondary
            )
        }
    }
}

@Composable
private fun WallpaperSourceChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassTokens.current
    val accent = LocalAccentColor.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) accent.copy(alpha = 0.18f)
                else tokens.textPrimary.copy(alpha = 0.05f)
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accent else tokens.borderColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accent),
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) accent else tokens.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) accent else tokens.textPrimary
            )
        }
    }
}

@Composable
private fun AdjustmentSliderRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Int,
    onValueChange: (Int) -> Unit,
    testTag: String
) {
    val tokens = LocalGlassTokens.current
    val accent = LocalAccentColor.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = tokens.textPrimary
                )
            }
            Text(
                text = "$value%",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = tokens.textPrimary.copy(alpha = 0.15f)
            )
        )
    }
}
