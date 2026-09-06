package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "facebook_glass_prefs")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class BackgroundSource {
    SYSTEM_WALLPAPER,
    CUSTOM_IMAGE,
    PRESET_GRADIENT
}

data class FilterSettings(
    val hideAds: Boolean = true,
    val hideSponsored: Boolean = true,
    val hideStories: Boolean = false,
    val hidePeopleYouMayKnow: Boolean = false
)

data class AccentSettings(
    val useSystemColor: Boolean = true,
    val presetIndex: Int = 0, // 0 = Blue, 1 = Purple, 2 = Pink, 3 = Red, 4 = Orange, 5 = Green, 6 = Teal, 7 = Cyan, 8 = Custom
    val customColorHex: Long = 0xFF1877F2
)

data class TransparencySettings(
    val transparencyPercent: Int = 50, // 0 to 100
    val isGlassModeEnabled: Boolean = true,
    val isAutoReadabilityEnabled: Boolean = true,
    val backgroundSource: BackgroundSource = BackgroundSource.SYSTEM_WALLPAPER,
    val customImageUri: String? = null,
    val backgroundPresetIndex: Int = 0, // 0 = Midnight Aurora, 1 = Deep Indigo, 2 = Obsidian Glow, 3 = Sunset Prism
    val blurAmount: Int = 20, // 0 to 100
    val brightnessAmount: Int = 85, // 0 to 100
    val dimAmount: Int = 20 // 0 to 100
)

data class UserPreferences(
    val hasSeenWelcome: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val filterSettings: FilterSettings = FilterSettings(),
    val accentSettings: AccentSettings = AccentSettings(),
    val transparencySettings: TransparencySettings = TransparencySettings()
)

class PreferenceManager(private val context: Context) {

    companion object {
        private val KEY_SEEN_WELCOME = booleanPreferencesKey("has_seen_welcome")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        
        private val KEY_HIDE_ADS = booleanPreferencesKey("filter_hide_ads")
        private val KEY_HIDE_SPONSORED = booleanPreferencesKey("filter_hide_sponsored")
        private val KEY_HIDE_STORIES = booleanPreferencesKey("filter_hide_stories")
        private val KEY_HIDE_PYMK = booleanPreferencesKey("filter_hide_pymk")
        
        private val KEY_USE_SYSTEM_COLOR = booleanPreferencesKey("accent_use_system")
        private val KEY_ACCENT_PRESET_INDEX = intPreferencesKey("accent_preset_index")
        private val KEY_CUSTOM_ACCENT_HEX = longPreferencesKey("accent_custom_hex")

        private val KEY_TRANSPARENCY_PERCENT = intPreferencesKey("transparency_percent")
        private val KEY_GLASS_MODE = booleanPreferencesKey("transparency_glass_mode")
        private val KEY_AUTO_READABILITY = booleanPreferencesKey("transparency_auto_readability")
        private val KEY_BG_SOURCE = stringPreferencesKey("transparency_bg_source")
        private val KEY_BG_CUSTOM_URI = stringPreferencesKey("transparency_bg_custom_uri")
        private val KEY_BG_PRESET_INDEX = intPreferencesKey("transparency_bg_preset_index")
        private val KEY_BG_BLUR = intPreferencesKey("transparency_bg_blur")
        private val KEY_BG_BRIGHTNESS = intPreferencesKey("transparency_bg_brightness")
        private val KEY_BG_DIM = intPreferencesKey("transparency_bg_dim")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val themeModeStr = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }

        val bgSourceStr = prefs[KEY_BG_SOURCE] ?: BackgroundSource.SYSTEM_WALLPAPER.name
        val bgSource = try {
            BackgroundSource.valueOf(bgSourceStr)
        } catch (e: Exception) {
            BackgroundSource.SYSTEM_WALLPAPER
        }

        UserPreferences(
            hasSeenWelcome = prefs[KEY_SEEN_WELCOME] ?: false,
            themeMode = themeMode,
            filterSettings = FilterSettings(
                hideAds = prefs[KEY_HIDE_ADS] ?: true,
                hideSponsored = prefs[KEY_HIDE_SPONSORED] ?: true,
                hideStories = prefs[KEY_HIDE_STORIES] ?: false,
                hidePeopleYouMayKnow = prefs[KEY_HIDE_PYMK] ?: false
            ),
            accentSettings = AccentSettings(
                useSystemColor = prefs[KEY_USE_SYSTEM_COLOR] ?: true,
                presetIndex = prefs[KEY_ACCENT_PRESET_INDEX] ?: 0,
                customColorHex = prefs[KEY_CUSTOM_ACCENT_HEX] ?: 0xFF1877F2
            ),
            transparencySettings = TransparencySettings(
                transparencyPercent = prefs[KEY_TRANSPARENCY_PERCENT] ?: 50,
                isGlassModeEnabled = prefs[KEY_GLASS_MODE] ?: true,
                isAutoReadabilityEnabled = prefs[KEY_AUTO_READABILITY] ?: true,
                backgroundSource = bgSource,
                customImageUri = prefs[KEY_BG_CUSTOM_URI],
                backgroundPresetIndex = prefs[KEY_BG_PRESET_INDEX] ?: 0,
                blurAmount = prefs[KEY_BG_BLUR] ?: 20,
                brightnessAmount = prefs[KEY_BG_BRIGHTNESS] ?: 85,
                dimAmount = prefs[KEY_BG_DIM] ?: 20
            )
        )
    }

    suspend fun setSeenWelcome(seen: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SEEN_WELCOME] = seen
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun updateFilters(
        hideAds: Boolean? = null,
        hideSponsored: Boolean? = null,
        hideStories: Boolean? = null,
        hidePeopleYouMayKnow: Boolean? = null
    ) {
        context.dataStore.edit { prefs ->
            hideAds?.let { prefs[KEY_HIDE_ADS] = it }
            hideSponsored?.let { prefs[KEY_HIDE_SPONSORED] = it }
            hideStories?.let { prefs[KEY_HIDE_STORIES] = it }
            hidePeopleYouMayKnow?.let { prefs[KEY_HIDE_PYMK] = it }
        }
    }

    suspend fun setAccentSettings(useSystem: Boolean, presetIndex: Int, customColorHex: Long? = null) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USE_SYSTEM_COLOR] = useSystem
            prefs[KEY_ACCENT_PRESET_INDEX] = presetIndex
            customColorHex?.let { prefs[KEY_CUSTOM_ACCENT_HEX] = it }
        }
    }

    suspend fun setTransparencyPercent(percent: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TRANSPARENCY_PERCENT] = percent.coerceIn(0, 100)
        }
    }

    suspend fun setGlassMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GLASS_MODE] = enabled
        }
    }

    suspend fun setGlassModeEnabled(enabled: Boolean) = setGlassMode(enabled)

    suspend fun setAutoReadability(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_READABILITY] = enabled
        }
    }

    suspend fun setAutoReadabilityEnabled(enabled: Boolean) = setAutoReadability(enabled)

    suspend fun setBackgroundSource(source: BackgroundSource) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BG_SOURCE] = source.name
        }
    }

    suspend fun setCustomImageUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri != null) {
                prefs[KEY_BG_CUSTOM_URI] = uri
            } else {
                prefs.remove(KEY_BG_CUSTOM_URI)
            }
        }
    }

    suspend fun setBackgroundPresetIndex(index: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BG_PRESET_INDEX] = index
        }
    }

    suspend fun setBackgroundAdjustments(blur: Int? = null, brightness: Int? = null, dim: Int? = null) {
        context.dataStore.edit { prefs ->
            blur?.let { prefs[KEY_BG_BLUR] = it.coerceIn(0, 100) }
            brightness?.let { prefs[KEY_BG_BRIGHTNESS] = it.coerceIn(0, 100) }
            dim?.let { prefs[KEY_BG_DIM] = it.coerceIn(0, 100) }
        }
    }

    suspend fun updateTransparencySettings(
        percent: Int? = null,
        isGlassMode: Boolean? = null,
        isAutoReadability: Boolean? = null,
        source: BackgroundSource? = null,
        customUri: String? = null,
        presetIndex: Int? = null,
        blur: Int? = null,
        brightness: Int? = null,
        dim: Int? = null
    ) {
        context.dataStore.edit { prefs ->
            percent?.let { prefs[KEY_TRANSPARENCY_PERCENT] = it.coerceIn(0, 100) }
            isGlassMode?.let { prefs[KEY_GLASS_MODE] = it }
            isAutoReadability?.let { prefs[KEY_AUTO_READABILITY] = it }
            source?.let { prefs[KEY_BG_SOURCE] = it.name }
            if (customUri != null) prefs[KEY_BG_CUSTOM_URI] = customUri
            presetIndex?.let { prefs[KEY_BG_PRESET_INDEX] = it }
            blur?.let { prefs[KEY_BG_BLUR] = it.coerceIn(0, 100) }
            brightness?.let { prefs[KEY_BG_BRIGHTNESS] = it.coerceIn(0, 100) }
            dim?.let { prefs[KEY_BG_DIM] = it.coerceIn(0, 100) }
        }
    }

    suspend fun updateTransparency(
        percent: Int? = null,
        isGlassMode: Boolean? = null,
        isAutoReadability: Boolean? = null,
        source: BackgroundSource? = null,
        customUri: String? = null,
        presetIndex: Int? = null,
        blur: Int? = null,
        brightness: Int? = null,
        dim: Int? = null
    ) = updateTransparencySettings(
        percent = percent,
        isGlassMode = isGlassMode,
        isAutoReadability = isAutoReadability,
        source = source,
        customUri = customUri,
        presetIndex = presetIndex,
        blur = blur,
        brightness = brightness,
        dim = dim
    )

    suspend fun resetAllSettings() {
        context.dataStore.edit { prefs ->
            prefs.clear()
            prefs[KEY_SEEN_WELCOME] = true
        }
    }
}
