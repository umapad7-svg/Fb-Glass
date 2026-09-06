package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.LocalGlassTokens
import com.example.ui.theme.liquidGlassSurface

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    val accent = LocalAccentColor.current

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
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glass Logo Emblem
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .liquidGlassSurface(
                        shape = CircleShape,
                        tokens = tokens,
                        elevation = 16.dp,
                        borderWidth = 1.5.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "f",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = accent
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "Facebook Glass",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = tokens.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Facebook in a cleaner, customizable interface.",
                fontSize = 16.sp,
                color = tokens.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Feature Highlights
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FeatureRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Liquid Glass Interface",
                        subtitle = "Frosted glass navigation with smooth scrolling animations."
                    )
                    FeatureRow(
                        icon = Icons.Default.FilterAlt,
                        title = "Intelligent Filtering",
                        subtitle = "Optional filters to declutter ads, sponsored posts, and suggestions."
                    )
                    FeatureRow(
                        icon = Icons.Default.Palette,
                        title = "Dynamic Accent & Themes",
                        subtitle = "Personalize colors with system dynamic palette and dark mode."
                    )
                    FeatureRow(
                        icon = Icons.Default.Shield,
                        title = "Private & Secure",
                        subtitle = "Standard native WebView. No credentials gathered, zero external tracking."
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Primary Action Button
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("welcome_continue_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "Continue to Facebook",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val tokens = LocalGlassTokens.current
    val accent = LocalAccentColor.current

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = tokens.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = tokens.textSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
