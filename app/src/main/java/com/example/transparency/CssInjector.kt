package com.example.transparency

import com.example.data.TransparencySettings

/**
 * Builds the centralized CSS stylesheet and dynamic CSS variable declarations
 * injected into Facebook WebView.
 */
object CssInjector {

    const val STYLE_TAG_ID = "fb-glass-transparency-style"

    /**
     * Builds the complete CSS stylesheet string injected into document head.
     */
    fun buildStylesheet(
        settings: TransparencySettings,
        isDark: Boolean,
        accentColorHex: Long
    ): String {
        val rootSelectors = ElementClassifier.ROOT_CONTAINER_SELECTORS.joinToString(", ")
        val cardSelectors = ElementClassifier.CARD_CONTAINER_SELECTORS.joinToString(", ")
        val headerSelectors = ElementClassifier.HEADER_NAV_SELECTORS.joinToString(", ")
        val commentSelectors = ElementClassifier.COMMENT_CONTAINER_SELECTORS.joinToString(", ")
        val overlaySelectors = ElementClassifier.OVERLAY_CONTAINER_SELECTORS.joinToString(", ")
        val protectedSelectors = ElementClassifier.PROTECTED_CONTENT_SELECTORS.joinToString(", ")

        return """
            :root {
                ${buildCssVariables(settings, isDark, accentColorHex)}
            }

            /* Root background containers: completely transparent to reveal wallpaper */
            $rootSelectors {
                background: rgba(var(--fb-surface-rgb), var(--fb-surface-opacity)) !important;
                background-color: rgba(var(--fb-surface-rgb), var(--fb-surface-opacity)) !important;
            }

            /* Feed cards, articles and post containers */
            $cardSelectors {
                background-color: rgba(var(--fb-surface-rgb), var(--fb-card-opacity)) !important;
                backdrop-filter: var(--fb-backdrop-filter) !important;
                -webkit-backdrop-filter: var(--fb-backdrop-filter) !important;
                border: var(--fb-card-border) !important;
                box-shadow: var(--fb-card-shadow) !important;
                border-radius: var(--fb-card-radius) !important;
                transition: background-color 0.2s ease, border 0.2s ease !important;
            }

            /* Top and bottom navigation headers */
            $headerSelectors {
                background-color: rgba(var(--fb-surface-rgb), var(--fb-overlay-opacity)) !important;
                backdrop-filter: var(--fb-backdrop-filter) !important;
                -webkit-backdrop-filter: var(--fb-backdrop-filter) !important;
                border-bottom: var(--fb-card-border) !important;
                box-shadow: var(--fb-card-shadow) !important;
            }

            /* Comment bubbles and conversation threads */
            $commentSelectors {
                background-color: rgba(var(--fb-surface-rgb), var(--fb-comment-opacity)) !important;
                border-radius: 12px !important;
                border: var(--fb-card-border) !important;
            }

            /* Overlays, dialogs, flyouts */
            $overlaySelectors {
                background-color: rgba(var(--fb-surface-rgb), var(--fb-overlay-opacity)) !important;
                backdrop-filter: var(--fb-backdrop-filter) !important;
                -webkit-backdrop-filter: var(--fb-backdrop-filter) !important;
                border: var(--fb-card-border) !important;
                box-shadow: 0 16px 36px rgba(0, 0, 0, 0.35) !important;
            }

            /* CRITICAL: Keep all interactive and media content 100% opaque */
            $protectedSelectors {
                opacity: 1 !important;
            }

            /* Ensure image/video containers maintain full display clarity */
            img, video, canvas, picture {
                border-radius: inherit;
            }

            /* Readability protection for post text and titles */
            article span,
            article p,
            article h1,
            article h2,
            article h3,
            article div[dir="auto"],
            div[role="article"] span,
            div[role="article"] p,
            div[role="article"] div[dir="auto"],
            .fb-glass-readable-text {
                text-shadow: var(--fb-text-shadow) !important;
            }
        """.trimIndent()
    }

    /**
     * Calculates the CSS variable values for dynamic real-time updates.
     */
    fun buildCssVariables(
        settings: TransparencySettings,
        isDark: Boolean,
        accentColorHex: Long
    ): String {
        val transparencyFraction = (settings.transparencyPercent / 100f).coerceIn(0f, 1f)

        // Surface opacity (html, body, main wrapper)
        val surfaceOpacity = (1f - (transparencyFraction * 1.0f)).coerceIn(0f, 1f)

        // Card opacity: if Auto Readability is enabled, keep a baseline opacity so text has contrast
        val minCardOpacity = if (settings.isAutoReadabilityEnabled) {
            if (isDark) 0.38f else 0.48f
        } else {
            0.05f
        }
        val rawCardOpacity = 1f - transparencyFraction
        val cardOpacity = rawCardOpacity.coerceAtLeast(minCardOpacity)

        // Overlay / header opacity
        val overlayOpacity = (1f - (transparencyFraction * 0.45f)).coerceIn(0.55f, 1f)
        val commentOpacity = (1f - (transparencyFraction * 0.65f)).coerceIn(0.35f, 1f)

        val surfaceRgb = if (isDark) "26, 28, 30" else "255, 255, 255"

        // Accent RGB from Long hex (0xAARRGGBB)
        val red = ((accentColorHex shr 16) and 0xFF).toInt()
        val green = ((accentColorHex shr 8) and 0xFF).toInt()
        val blue = (accentColorHex and 0xFF).toInt()
        val accentRgb = "$red, $green, $blue"
        val accentHexStr = String.format("#%06X", (0xFFFFFF and accentColorHex.toInt()))

        val backdropFilter = if (settings.isGlassModeEnabled && settings.transparencyPercent > 0) {
            val blurPx = (settings.transparencyPercent * 0.18f + 6f).toInt()
            "blur(${blurPx}px) saturate(180%)"
        } else {
            "none"
        }

        val cardBorder = if (settings.isGlassModeEnabled && settings.transparencyPercent > 0) {
            val borderAlpha = if (isDark) 0.22f else 0.18f
            "1px solid rgba($surfaceRgb, $borderAlpha)"
        } else {
            "none"
        }

        val cardShadow = if (settings.isGlassModeEnabled && settings.transparencyPercent > 0) {
            if (isDark) "0 8px 32px 0 rgba(0, 0, 0, 0.35)" else "0 8px 24px 0 rgba(0, 0, 0, 0.12)"
        } else {
            "none"
        }

        val cardRadius = if (settings.isGlassModeEnabled) "16px" else "8px"

        val textShadow = if (settings.isAutoReadabilityEnabled && settings.transparencyPercent > 20) {
            if (isDark) {
                "0 1px 3px rgba(0, 0, 0, 0.85)"
            } else {
                "0 1px 2px rgba(255, 255, 255, 0.85)"
            }
        } else {
            "none"
        }

        return """
            --fb-glass-opacity: ${String.format(java.util.Locale.US, "%.3f", transparencyFraction)};
            --fb-surface-opacity: ${String.format(java.util.Locale.US, "%.3f", surfaceOpacity)};
            --fb-card-opacity: ${String.format(java.util.Locale.US, "%.3f", cardOpacity)};
            --fb-overlay-opacity: ${String.format(java.util.Locale.US, "%.3f", overlayOpacity)};
            --fb-comment-opacity: ${String.format(java.util.Locale.US, "%.3f", commentOpacity)};
            --fb-surface-rgb: $surfaceRgb;
            --fb-accent-rgb: $accentRgb;
            --fb-accent-hex: $accentHexStr;
            --fb-backdrop-filter: $backdropFilter;
            --fb-card-border: $cardBorder;
            --fb-card-shadow: $cardShadow;
            --fb-card-radius: $cardRadius;
            --fb-text-shadow: $textShadow;
        """.trimIndent()
    }
}
