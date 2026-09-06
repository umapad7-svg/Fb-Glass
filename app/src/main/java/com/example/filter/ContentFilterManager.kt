package com.example.filter

import android.webkit.WebView
import com.example.data.FilterSettings
import org.json.JSONObject

/**
 * Orchestrates Facebook content filtering rules and injects scoped, debounced JavaScript
 * into the WebView. Avoids re-injecting full scripts, uses MutationObserver, and supports
 * live configuration updates without page reloads.
 */
class ContentFilterManager {

    val adFilter = AdFilter()
    val sponsoredPostFilter = SponsoredPostFilter()
    val storiesFilter = StoriesFilter()
    val peopleYouMayKnowFilter = PeopleYouMayKnowFilter()

    private val allFilters = listOf(
        adFilter,
        sponsoredPostFilter,
        storiesFilter,
        peopleYouMayKnowFilter
    )

    /**
     * Builds and injects the master content filter JavaScript engine into the WebView.
     * Includes debounced MutationObserver, safety fallbacks, and window.__FB_GLASS_UPDATE_CONFIG.
     */
    fun injectFilterEngine(webView: WebView, settings: FilterSettings) {
        val configJson = createConfigJson(settings)
        val script = """
            (function() {
                // Prevent duplicate initialization
                if (window.__FB_GLASS_INITIALIZED) {
                    if (typeof window.__FB_GLASS_UPDATE_CONFIG === 'function') {
                        window.__FB_GLASS_UPDATE_CONFIG($configJson);
                    }
                    return;
                }
                window.__FB_GLASS_INITIALIZED = true;

                var currentConfig = $configJson;

                var adFilterInspector = ${adFilter.getElementInspectorScript()};
                var sponsoredFilterInspector = ${sponsoredPostFilter.getElementInspectorScript()};
                var storiesFilterInspector = ${storiesFilter.getElementInspectorScript()};
                var pymkFilterInspector = ${peopleYouMayKnowFilter.getElementInspectorScript()};

                var adSelectors = ${adFilter.getCssSelectors().joinToString(prefix = "['", separator = "', '", postfix = "']")};
                var sponsoredSelectors = ${sponsoredPostFilter.getCssSelectors().joinToString(prefix = "['", separator = "', '", postfix = "']")};
                var storiesSelectors = ${storiesFilter.getCssSelectors().joinToString(prefix = "['", separator = "', '", postfix = "']")};
                var pymkSelectors = ${peopleYouMayKnowFilter.getCssSelectors().joinToString(prefix = "['", separator = "', '", postfix = "']")};

                // Find nearest feed post container (article / feed unit)
                function findPostContainer(el) {
                    if (!el) return null;
                    var curr = el;
                    var depth = 0;
                    while (curr && curr !== document.body && depth < 10) {
                        var role = curr.getAttribute('role');
                        if (role === 'article' || role === 'feed' || curr.tagName === 'ARTICLE') {
                            return curr;
                        }
                        if (curr.hasAttribute('data-tracking-duration-id') || curr.hasAttribute('data-ft')) {
                            return curr;
                        }
                        curr = curr.parentElement;
                        depth++;
                    }
                    return el;
                }

                function safeHide(el, reason) {
                    if (!el || el.__fb_glass_hidden) return;
                    el.__fb_glass_hidden = true;
                    el.__fb_glass_reason = reason;
                    el.style.setProperty('display', 'none', 'important');
                    el.setAttribute('data-fb-glass-filtered', reason);
                }

                function safeUnhide(el, reason) {
                    if (el && el.__fb_glass_hidden && el.__fb_glass_reason === reason) {
                        el.__fb_glass_hidden = false;
                        el.style.removeProperty('display');
                        el.removeAttribute('data-fb-glass-filtered');
                    }
                }

                function processDocument() {
                    try {
                        // 1. Stories
                        if (currentConfig.hideStories) {
                            for (var i = 0; i < storiesSelectors.length; i++) {
                                var matches = document.querySelectorAll(storiesSelectors[i]);
                                matches.forEach(function(m) { safeHide(m, 'stories'); });
                            }
                        }

                        // 2. People You May Know
                        if (currentConfig.hidePeopleYouMayKnow) {
                            for (var i = 0; i < pymkSelectors.length; i++) {
                                var matches = document.querySelectorAll(pymkSelectors[i]);
                                matches.forEach(function(m) { 
                                    var container = findPostContainer(m);
                                    safeHide(container || m, 'pymk'); 
                                });
                            }
                        }

                        // 3. Direct Ads
                        if (currentConfig.hideAds) {
                            for (var i = 0; i < adSelectors.length; i++) {
                                var matches = document.querySelectorAll(adSelectors[i]);
                                matches.forEach(function(m) {
                                    var container = findPostContainer(m);
                                    safeHide(container || m, 'ad');
                                });
                            }
                        }

                        // 4. Feed posts inspection (Ads & Sponsored)
                        if (currentConfig.hideAds || currentConfig.hideSponsored || currentConfig.hidePeopleYouMayKnow) {
                            var articles = document.querySelectorAll('div[role="article"], article, div[data-tracking-duration-id]');
                            for (var a = 0; a < articles.length; a++) {
                                var item = articles[a];
                                if (item.__fb_glass_hidden) continue;

                                if (currentConfig.hideAds && adFilterInspector(item)) {
                                    safeHide(item, 'ad');
                                    continue;
                                }

                                if (currentConfig.hideSponsored && sponsoredFilterInspector(item)) {
                                    safeHide(item, 'sponsored');
                                    continue;
                                }

                                if (currentConfig.hidePeopleYouMayKnow && pymkFilterInspector(item)) {
                                    safeHide(item, 'pymk');
                                    continue;
                                }
                            }
                        }
                    } catch (err) {
                        console.warn('FB Glass filter warning: ', err);
                    }
                }

                // Debounce to prevent performance degradation during fast scrolling
                var debounceTimeout = null;
                function debouncedProcess() {
                    if (debounceTimeout) clearTimeout(debounceTimeout);
                    debounceTimeout = setTimeout(function() {
                        requestAnimationFrame(processDocument);
                    }, 250);
                }

                // Initial process
                processDocument();

                // MutationObserver on document body to handle lazy-loaded feed units
                if (window.MutationObserver && document.body) {
                    var observer = new MutationObserver(function(mutations) {
                        debouncedProcess();
                    });
                    observer.observe(document.body, { childList: true, subtree: true });
                }

                // Global function called from Kotlin when settings toggle
                window.__FB_GLASS_UPDATE_CONFIG = function(newConfig) {
                    currentConfig = newConfig;
                    // If a filter was disabled, unhide those specific elements
                    if (!newConfig.hideStories) {
                        document.querySelectorAll('[data-fb-glass-filtered="stories"]').forEach(function(el) {
                            safeUnhide(el, 'stories');
                        });
                    }
                    if (!newConfig.hideAds) {
                        document.querySelectorAll('[data-fb-glass-filtered="ad"]').forEach(function(el) {
                            safeUnhide(el, 'ad');
                        });
                    }
                    if (!newConfig.hideSponsored) {
                        document.querySelectorAll('[data-fb-glass-filtered="sponsored"]').forEach(function(el) {
                            safeUnhide(el, 'sponsored');
                        });
                    }
                    if (!newConfig.hidePeopleYouMayKnow) {
                        document.querySelectorAll('[data-fb-glass-filtered="pymk"]').forEach(function(el) {
                            safeUnhide(el, 'pymk');
                        });
                    }
                    debouncedProcess();
                };
            })();
        """.trimIndent()

        webView.evaluateJavascript(script, null)
    }

    /**
     * Updates active filter config in the running WebView without full reload.
     */
    fun updateLiveFilters(webView: WebView, settings: FilterSettings) {
        val configJson = createConfigJson(settings)
        val script = """
            if (typeof window.__FB_GLASS_UPDATE_CONFIG === 'function') {
                window.__FB_GLASS_UPDATE_CONFIG($configJson);
            } else {
                // If not yet injected, inject now
                ${'$'}{""}
            }
        """.trimIndent()
        webView.evaluateJavascript(script) { result ->
            if (result == null || result == "null") {
                injectFilterEngine(webView, settings)
            }
        }
    }

    private fun createConfigJson(settings: FilterSettings): String {
        val json = JSONObject()
        json.put("hideAds", settings.hideAds)
        json.put("hideSponsored", settings.hideSponsored)
        json.put("hideStories", settings.hideStories)
        json.put("hidePeopleYouMayKnow", settings.hidePeopleYouMayKnow)
        return json.toString()
    }
}
