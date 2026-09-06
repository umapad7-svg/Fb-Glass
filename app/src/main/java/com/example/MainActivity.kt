package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BackgroundSource
import com.example.data.PreferenceManager
import com.example.data.UserPreferences
import com.example.filter.ContentFilterManager
import com.example.transparency.FacebookTransparencyManager
import com.example.ui.components.GlassFloatingToolbar
import com.example.ui.components.QuickTransparencyControl
import com.example.ui.components.WallpaperBackgroundSurface
import com.example.ui.screens.GlassErrorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.FacebookGlassTheme
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.LocalGlassTokens
import com.example.ui.webview.FACEBOOK_HOME_URL
import com.example.ui.webview.FacebookWebView
import com.example.ui.webview.WebViewErrorState
import com.example.ui.webview.createConfiguredWebView
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferenceManager = PreferenceManager(applicationContext)
        val contentFilterManager = ContentFilterManager()

        setContent {
            val preferences by preferenceManager.preferencesFlow.collectAsStateWithLifecycle(
                initialValue = UserPreferences()
            )

            FacebookGlassTheme(
                themeMode = preferences.themeMode,
                accentSettings = preferences.accentSettings
            ) {
                FacebookGlassApp(
                    preferences = preferences,
                    preferenceManager = preferenceManager,
                    contentFilterManager = contentFilterManager,
                    onGetOrCreateWebView = {
                        if (webView == null) {
                            webView = createConfiguredWebView(this@MainActivity)
                        }
                        webView!!
                    },
                    onOpenUrlExternally = { url ->
                        val target = if (url.isNotEmpty()) url else FACEBOOK_HOME_URL
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(target))
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "No browser found to open link", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView?.destroy()
        webView = null
    }
}

@Composable
fun FacebookGlassApp(
    preferences: UserPreferences,
    preferenceManager: PreferenceManager,
    contentFilterManager: ContentFilterManager,
    onGetOrCreateWebView: () -> WebView,
    onOpenUrlExternally: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val webView = remember { onGetOrCreateWebView() }
    val transparencyManager = remember { FacebookTransparencyManager() }

    var isSettingsOpen by remember { mutableStateOf(false) }
    var isQuickTransparencyOpen by remember { mutableStateOf(false) }
    var isToolbarVisible by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var currentUrl by remember { mutableStateOf(FACEBOOK_HOME_URL) }
    var errorState by remember { mutableStateOf(WebViewErrorState()) }

    // Intercept back navigation
    BackHandler {
        when {
            isQuickTransparencyOpen -> isQuickTransparencyOpen = false
            isSettingsOpen -> isSettingsOpen = false
            errorState.hasError -> {
                errorState = WebViewErrorState()
                webView.reload()
            }
            canGoBack && webView.canGoBack() -> {
                webView.goBack()
            }
        }
    }

    // First Launch Onboarding
    if (!preferences.hasSeenWelcome) {
        WelcomeScreen(
            onContinue = {
                coroutineScope.launch {
                    preferenceManager.setSeenWelcome(true)
                }
            }
        )
        return
    }

    val tokens = LocalGlassTokens.current
    val accent = LocalAccentColor.current
    val accentColorHex = (accent.value.toLong() and 0xFFFFFFFFL)
    val hasActiveFilters = preferences.filterSettings.hideAds ||
            preferences.filterSettings.hideSponsored ||
            preferences.filterSettings.hideStories ||
            preferences.filterSettings.hidePeopleYouMayKnow

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // User tap anywhere toggles toolbar visibility
                        if (!isToolbarVisible) {
                            isToolbarVisible = true
                        }
                    }
                )
            }
    ) {
        // Layer 1: Background Surface (System Wallpaper, Custom Photo, or Mesh Gradient)
        WallpaperBackgroundSurface(
            settings = preferences.transparencySettings,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 2: Facebook Native WebView with Dynamic CSS/JS Webpage Transparency
        FacebookWebView(
            modifier = Modifier.fillMaxSize(),
            webViewInstance = webView,
            filterSettings = preferences.filterSettings,
            contentFilterManager = contentFilterManager,
            transparencySettings = preferences.transparencySettings,
            transparencyManager = transparencyManager,
            isDark = tokens.isDark,
            accentColorHex = accentColorHex,
            onLoadingChanged = { loading, progress ->
                isLoading = loading
                loadingProgress = progress
                if (!loading && errorState.hasError) {
                    errorState = WebViewErrorState()
                }
            },
            onNavigationStateChanged = { back, forward, url ->
                canGoBack = back
                canGoForward = forward
                if (url.isNotEmpty()) {
                    currentUrl = url
                }
            },
            onErrorOccurred = { err ->
                errorState = err
            },
            onScrollDelta = { dy ->
                // Scroll down: minimize/hide toolbar. Scroll up: reveal toolbar.
                if (dy > 14 && isToolbarVisible && !isQuickTransparencyOpen) {
                    isToolbarVisible = false
                } else if (dy < -10 && !isToolbarVisible) {
                    isToolbarVisible = true
                }
            }
        )

        // Top Floating "Filtering Active" status badge (High Density theme)
        AnimatedVisibility(
            visible = hasActiveFilters,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (tokens.isDark) 0.22f else 0.40f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.28f),
                        shape = CircleShape
                    )
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "FILTERING ACTIVE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (tokens.isDark) Color.White else Color(0xFF1A1C1E),
                    letterSpacing = 1.6.sp
                )
            }
        }

        // Glass Error Screen if loading fails
        if (errorState.hasError) {
            GlassErrorScreen(
                errorState = errorState,
                onTryAgain = {
                    errorState = WebViewErrorState()
                    webView.reload()
                },
                onOpenInBrowser = {
                    onOpenUrlExternally(errorState.failingUrl.ifEmpty { currentUrl })
                },
                onOpenSettings = {
                    isSettingsOpen = true
                }
            )
        }

        // Layer 3: Floating Liquid Glass Toolbar
        GlassFloatingToolbar(
            modifier = Modifier.align(Alignment.BottomCenter),
            isVisible = isToolbarVisible,
            isLoading = isLoading,
            loadingProgress = loadingProgress,
            canGoBack = canGoBack,
            canGoForward = canGoForward,
            hasActiveFilters = hasActiveFilters,
            isTransparencyActive = preferences.transparencySettings.transparencyPercent > 0,
            onBack = {
                if (webView.canGoBack()) {
                    webView.goBack()
                }
            },
            onForward = {
                if (webView.canGoForward()) {
                    webView.goForward()
                }
            },
            onHome = {
                webView.loadUrl(FACEBOOK_HOME_URL)
            },
            onReload = {
                errorState = WebViewErrorState()
                webView.reload()
            },
            onTransparencyClick = {
                isQuickTransparencyOpen = !isQuickTransparencyOpen
            },
            onSettings = {
                isSettingsOpen = true
            },
            onExpandRequest = {
                isToolbarVisible = true
            }
        )

        // Layer 3: Quick Transparency Control Slider Sheet
        QuickTransparencyControl(
            visible = isQuickTransparencyOpen,
            settings = preferences.transparencySettings,
            onTransparencyChange = { percent ->
                coroutineScope.launch {
                    preferenceManager.setTransparencyPercent(percent)
                }
            },
            onGlassModeToggle = { enabled ->
                coroutineScope.launch {
                    preferenceManager.setGlassModeEnabled(enabled)
                }
            },
            onAutoReadabilityToggle = { enabled ->
                coroutineScope.launch {
                    preferenceManager.setAutoReadabilityEnabled(enabled)
                }
            },
            onOpenWallpaperSettings = {
                isQuickTransparencyOpen = false
                isSettingsOpen = true
            },
            onDismiss = {
                isQuickTransparencyOpen = false
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Settings Modal View
        AnimatedVisibility(
            visible = isSettingsOpen,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            SettingsScreen(
                preferences = preferences,
                onClose = { isSettingsOpen = false },
                onOpenInBrowser = {
                    onOpenUrlExternally(currentUrl)
                },
                onUpdateFilters = { hideAds, hideSponsored, hideStories, hidePymk ->
                    coroutineScope.launch {
                        preferenceManager.updateFilters(
                            hideAds = hideAds,
                            hideSponsored = hideSponsored,
                            hideStories = hideStories,
                            hidePeopleYouMayKnow = hidePymk
                        )
                    }
                },
                onUpdateTheme = { themeMode ->
                    coroutineScope.launch {
                        preferenceManager.setThemeMode(themeMode)
                    }
                },
                onUpdateAccent = { useSystem, presetIndex, customHex ->
                    coroutineScope.launch {
                        preferenceManager.setAccentSettings(
                            useSystem = useSystem,
                            presetIndex = presetIndex,
                            customColorHex = customHex
                        )
                    }
                },
                onUpdateTransparency = { percent, isGlass, isAutoRead, source, customUri, presetIndex, blur, brightness, dim ->
                    coroutineScope.launch {
                        preferenceManager.updateTransparency(
                            percent = percent,
                            isGlassMode = isGlass,
                            isAutoReadability = isAutoRead,
                            source = source,
                            customUri = customUri,
                            presetIndex = presetIndex,
                            blur = blur,
                            brightness = brightness,
                            dim = dim
                        )
                    }
                },
                onResetSettings = {
                    coroutineScope.launch {
                        preferenceManager.resetAllSettings()
                        Toast.makeText(webView.context, "Settings reset to defaults", Toast.LENGTH_SHORT).show()
                    }
                },
                onClearCookies = {
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.removeAllCookies {
                        cookieManager.flush()
                        webView.clearCache(true)
                        webView.loadUrl(FACEBOOK_HOME_URL)
                        Toast.makeText(webView.context, "Facebook cookies and data cleared", Toast.LENGTH_SHORT).show()
                    }
                },
                onClearCache = {
                    webView.clearCache(true)
                    Toast.makeText(webView.context, "Browser cache cleared", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}

