package com.example.ui.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.FilterSettings
import com.example.data.TransparencySettings
import com.example.filter.ContentFilterManager
import com.example.transparency.FacebookTransparencyManager

const val FACEBOOK_HOME_URL = "https://m.facebook.com"

data class WebViewErrorState(
    val hasError: Boolean = false,
    val errorCode: Int = 0,
    val description: String = "",
    val failingUrl: String = "",
    val isSslError: Boolean = false
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FacebookWebView(
    modifier: Modifier = Modifier,
    webViewInstance: WebView?,
    filterSettings: FilterSettings,
    contentFilterManager: ContentFilterManager,
    transparencySettings: TransparencySettings,
    transparencyManager: FacebookTransparencyManager,
    isDark: Boolean,
    accentColorHex: Long,
    onLoadingChanged: (Boolean, Float) -> Unit,
    onNavigationStateChanged: (canGoBack: Boolean, canGoForward: Boolean, url: String) -> Unit,
    onErrorOccurred: (WebViewErrorState) -> Unit,
    onScrollDelta: (dy: Int) -> Unit
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            (webViewInstance ?: createConfiguredWebView(context)).apply {
                setupClients(
                    filterSettings = filterSettings,
                    contentFilterManager = contentFilterManager,
                    transparencySettings = transparencySettings,
                    transparencyManager = transparencyManager,
                    isDark = isDark,
                    accentColorHex = accentColorHex,
                    onLoadingChanged = onLoadingChanged,
                    onNavigationStateChanged = onNavigationStateChanged,
                    onErrorOccurred = onErrorOccurred,
                    onScrollDelta = onScrollDelta
                )
                if (url.isNullOrEmpty()) {
                    loadUrl(FACEBOOK_HOME_URL)
                }
            }
        },
        update = { webView ->
            // Re-apply live filter configuration whenever filterSettings changes
            contentFilterManager.updateLiveFilters(webView, filterSettings)
            // Re-apply live transparency configuration without page reload
            transparencyManager.updateLiveTransparency(webView, transparencySettings, isDark, accentColorHex)
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            // CookieManager flush
            CookieManager.getInstance().flush()
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
fun createConfiguredWebView(context: Context): WebView {
    val webView = WebView(context)
    webView.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    // Make WebView base background transparent to reveal Layer 1 Wallpaper
    webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)

    // Ensure hardware acceleration and smooth scrolling
    webView.isVerticalScrollBarEnabled = false
    webView.isHorizontalScrollBarEnabled = false

    // Cookie configuration - keep user logged in between sessions
    val cookieManager = CookieManager.getInstance()
    cookieManager.setAcceptCookie(true)
    cookieManager.setAcceptThirdPartyCookies(webView, true)

    val settings = webView.settings
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.databaseEnabled = true
    settings.cacheMode = WebSettings.LOAD_DEFAULT

    // Media and video playback
    settings.mediaPlaybackRequiresUserGesture = false
    settings.loadWithOverviewMode = true
    settings.useWideViewPort = true

    // Security best practices
    settings.allowFileAccess = false
    settings.allowContentAccess = true
    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

    // Modern Chrome mobile user agent for full Facebook mobile site fidelity
    val defaultUa = settings.userAgentString
    // Remove "; wv" token if present so Facebook serves full modern responsive UI
    val cleanUa = defaultUa.replace("; wv", "").replace("Version/4.0 ", "")
    settings.userAgentString = cleanUa

    return webView
}

fun WebView.setupClients(
    filterSettings: FilterSettings,
    contentFilterManager: ContentFilterManager,
    transparencySettings: TransparencySettings,
    transparencyManager: FacebookTransparencyManager,
    isDark: Boolean,
    accentColorHex: Long,
    onLoadingChanged: (Boolean, Float) -> Unit,
    onNavigationStateChanged: (canGoBack: Boolean, canGoForward: Boolean, url: String) -> Unit,
    onErrorOccurred: (WebViewErrorState) -> Unit,
    onScrollDelta: (dy: Int) -> Unit
) {
    // Scroll listener for auto-hiding / showing floating glass toolbar
    setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
        val dy = scrollY - oldScrollY
        onScrollDelta(dy)
    }

    webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            val isLoading = newProgress < 100
            val progressFloat = newProgress / 100f
            onLoadingChanged(isLoading, progressFloat)
            if (!isLoading) {
                CookieManager.getInstance().flush()
            }
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            onNavigationStateChanged(canGoBack(), canGoForward(), url ?: "")
        }
    }

    webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val targetUri = request?.url ?: return false
            val scheme = targetUri.scheme?.lowercase() ?: return false
            val host = targetUri.host?.lowercase() ?: ""

            // Handle custom intents (tel, mailto, fb, etc.)
            if (scheme != "http" && scheme != "https") {
                return try {
                    val intent = Intent(Intent.ACTION_VIEW, targetUri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    view?.context?.startActivity(intent)
                    true
                } catch (e: Exception) {
                    true
                }
            }

            // Normal Facebook or authentication redirect navigation inside WebView
            return false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            onLoadingChanged(true, 0.15f)
            onNavigationStateChanged(canGoBack(), canGoForward(), url ?: "")
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onLoadingChanged(false, 1f)
            onNavigationStateChanged(canGoBack(), canGoForward(), url ?: "")

            if (view != null) {
                // Inject content filtering engine once page DOM is ready
                contentFilterManager.injectFilterEngine(view, filterSettings)
                // Inject Facebook transparency engine and live DOM observer
                transparencyManager.injectTransparencyEngine(view, transparencySettings, isDark, accentColorHex)
            }
            CookieManager.getInstance().flush()
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            // Only trigger error screen for primary frame navigation failures
            if (request?.isForMainFrame == true) {
                onErrorOccurred(
                    WebViewErrorState(
                        hasError = true,
                        errorCode = error?.errorCode ?: 0,
                        description = error?.description?.toString() ?: "Network connection failed",
                        failingUrl = request.url.toString(),
                        isSslError = false
                    )
                )
            }
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            // NEVER ignore SSL errors (mandated by Google Play and prompt guidelines)
            handler?.cancel()
            onErrorOccurred(
                WebViewErrorState(
                    hasError = true,
                    errorCode = error?.primaryError ?: 0,
                    description = "SSL Certificate validation error. Connection is not secure.",
                    failingUrl = error?.url ?: "",
                    isSslError = true
                )
            )
        }
    }
}
