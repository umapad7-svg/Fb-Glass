package com.example.transparency

import android.webkit.WebView
import com.example.data.TransparencySettings

/**
 * Manages live transparency level and applies dynamic CSS variable updates directly
 * into the running Facebook WebView without page reloads.
 */
class TransparencyController {

    private var currentSettings: TransparencySettings = TransparencySettings()
    private var isDark: Boolean = false
    private var accentColorHex: Long = 0xFF1877F2

    fun setTransparency(value: Float) {
        val percent = (value * 100f).toInt().coerceIn(0, 100)
        currentSettings = currentSettings.copy(transparencyPercent = percent)
    }

    fun setGlassMode(enabled: Boolean) {
        currentSettings = currentSettings.copy(isGlassModeEnabled = enabled)
    }

    fun setAutoReadability(enabled: Boolean) {
        currentSettings = currentSettings.copy(isAutoReadabilityEnabled = enabled)
    }

    fun setTheme(dark: Boolean) {
        isDark = dark
    }

    fun setAccent(colorHex: Long) {
        accentColorHex = colorHex
    }

    fun updateSettings(settings: TransparencySettings, dark: Boolean, accent: Long) {
        currentSettings = settings
        isDark = dark
        accentColorHex = accent
    }

    /**
     * Updates CSS custom properties on document.documentElement in real time.
     * Takes < 1ms to execute, updating all feed cards, headers, and backgrounds instantaneously.
     */
    fun applyLiveUpdates(webView: WebView?) {
        if (webView == null) return

        val cssVariables = CssInjector.buildCssVariables(currentSettings, isDark, accentColorHex)
        val lines = cssVariables.lines()

        val jsStatements = lines.mapNotNull { line ->
            val trimmed = line.trim().removeSuffix(";")
            val colonIdx = trimmed.indexOf(':')
            if (colonIdx != -1) {
                val prop = trimmed.substring(0, colonIdx).trim()
                val value = trimmed.substring(colonIdx + 1).trim()
                "document.documentElement.style.setProperty('$prop', '$value');"
            } else {
                null
            }
        }.joinToString(" ")

        val script = """
            (function() {
                try {
                    $jsStatements
                } catch (e) {
                    console.warn('Live transparency update error:', e);
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(script, null)
    }
}
