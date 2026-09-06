package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.LocalGlassTokens
import com.example.ui.theme.liquidGlassSurface

@Composable
fun GlassFloatingToolbar(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    isLoading: Boolean,
    loadingProgress: Float,
    canGoBack: Boolean,
    canGoForward: Boolean,
    hasActiveFilters: Boolean,
    isTransparencyActive: Boolean = false,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onReload: () -> Unit,
    onTransparencyClick: () -> Unit,
    onSettings: () -> Unit,
    onExpandRequest: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    val accent = LocalAccentColor.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Minimized handle when toolbar is hidden
        AnimatedVisibility(
            visible = !isVisible,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            Box(
                modifier = Modifier
                    .testTag("toolbar_minimized_pill")
                    .liquidGlassSurface(
                        shape = RoundedCornerShape(16.dp),
                        tokens = tokens,
                        elevation = 8.dp,
                        borderWidth = 1.dp
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = accent),
                        role = Role.Button,
                        onClick = onExpandRequest
                    )
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                    Icon(
                        imageVector = Icons.Default.UnfoldMore,
                        contentDescription = "Expand Toolbar",
                        tint = tokens.textPrimary.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Full Floating Glass Toolbar
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) +
                    slideInVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) { it },
            exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                    slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { it }
        ) {
            Box(
                modifier = Modifier
                    .testTag("glass_toolbar")
                    .widthIn(max = 384.dp)
                    .fillMaxWidth(0.90f)
                    .height(64.dp)
                    .liquidGlassSurface(
                        shape = RoundedCornerShape(32.dp),
                        tokens = tokens,
                        elevation = 20.dp,
                        borderWidth = 1.dp
                    )
                    .clip(RoundedCornerShape(32.dp))
            ) {
                // Subtle loading progress bar at the top edge of the toolbar
                if (isLoading) {
                    LinearProgressIndicator(
                        progress = { loadingProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.TopCenter),
                        color = accent,
                        trackColor = Color.Transparent
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToolbarGlassButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        testTag = "toolbar_back_button",
                        enabled = canGoBack,
                        onClick = onBack
                    )

                    ToolbarGlassButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        testTag = "toolbar_forward_button",
                        enabled = canGoForward,
                        onClick = onForward
                    )

                    ToolbarGlassHomeButton(
                        onClick = onHome,
                        testTag = "toolbar_home_button"
                    )

                    ToolbarGlassButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        testTag = "toolbar_reload_button",
                        enabled = true,
                        onClick = onReload
                    )

                    Box(contentAlignment = Alignment.TopEnd) {
                        ToolbarGlassButton(
                            icon = Icons.Default.Opacity,
                            contentDescription = "Transparency",
                            testTag = "toolbar_transparency_button",
                            enabled = true,
                            onClick = onTransparencyClick
                        )
                        if (isTransparencyActive) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp, end = 8.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(accent)
                            )
                        }
                    }

                    Box(contentAlignment = Alignment.TopEnd) {
                        ToolbarGlassButton(
                            icon = Icons.Default.Settings,
                            contentDescription = "Settings",
                            testTag = "toolbar_settings_button",
                            enabled = true,
                            onClick = onSettings
                        )
                        // Content filter badge dot
                        if (hasActiveFilters) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp, end = 8.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(accent)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolbarGlassHomeButton(
    onClick: () -> Unit,
    testTag: String
) {
    val accent = LocalAccentColor.current

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .testTag(testTag)
            .size(48.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.85f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.35f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Facebook Home",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ToolbarGlassButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    val alpha by animateFloatAsState(if (enabled) 1f else 0.35f, label = "button_alpha")

    val iconColor = if (enabled) tokens.textPrimary.copy(alpha = 0.90f) else tokens.textSecondary.copy(alpha = 0.35f)

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .testTag(testTag)
            .size(48.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
