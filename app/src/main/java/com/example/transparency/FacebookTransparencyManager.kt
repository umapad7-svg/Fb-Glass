package com.example.transparency

import android.webkit.WebView
import com.example.data.TransparencySettings

/**
 * Master manager for Facebook Background Transparency and Liquid Glass styling.
 * Coordinates TransparencyController, CssInjector, DomObserver, and ElementClassifier.
 */
class FacebookTransparencyManager {

    val controller = TransparencyController()

    /**
     * Injects the root transparency stylesheet and the dynamic DOM observer into the WebView.
     */
    fun injectTransparencyEngine(
        webView: WebView,
        settings: TransparencySettings,
        isDark: Boolean,
        accentColorHex: Long
    ) {
        controller.updateSettings(settings, isDark, accentColorHex)

        val cssContent = CssInjector.buildStylesheet(settings, isDark, accentColorHex)
            .replace("\n", " ")
            .replace("'", "\\'")

        val observerJs = DomObserver.buildObserverScript()

        val injectionScript = """
            (function() {
                try {
                    // 1. Inject or update CSS stylesheet in document.head
                    var styleTag = document.getElementById('${CssInjector.STYLE_TAG_ID}');
                    if (!styleTag) {
                        styleTag = document.createElement('style');
                        styleTag.id = '${CssInjector.STYLE_TAG_ID}';
                        styleTag.type = 'text/css';
                        (document.head || document.documentElement).appendChild(styleTag);
                    }
                    styleTag.textContent = '$cssContent';

                    // 2. Inject DOM Observer for lazy loaded feed posts
                    $observerJs
                } catch (e) {
                    console.warn('Facebook Transparency injection error:', e);
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(injectionScript) {
            // Apply live properties immediately
            controller.applyLiveUpdates(webView)
        }
    }

    /**
     * Applies instantaneous live transparency updates without reloading Facebook.
     */
    fun updateLiveTransparency(
        webView: WebView,
        settings: TransparencySettings,
        isDark: Boolean,
        accentColorHex: Long
    ) {
        controller.updateSettings(settings, isDark, accentColorHex)

        // Test if style tag is injected
        val checkScript = "!!document.getElementById('${CssInjector.STYLE_TAG_ID}')"
        webView.evaluateJavascript(checkScript) { result ->
            if (result == "true") {
                controller.applyLiveUpdates(webView)
            } else {
                injectTransparencyEngine(webView, settings, isDark, accentColorHex)
            }
        }
    }

    /**
     * Cleanly detaches DOM observers and releases resources.
     */
    fun destroy(webView: WebView) {
        webView.evaluateJavascript(DomObserver.buildCleanupScript(), null)
    }
}
