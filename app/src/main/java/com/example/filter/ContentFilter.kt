package com.example.filter

/**
 * Interface representing an individual modular Facebook content filter.
 */
interface ContentFilter {
    val id: String
    val name: String
    val description: String

    /**
     * CSS selectors known to target this type of content safely.
     */
    fun getCssSelectors(): List<String>

    /**
     * Custom DOM inspection and matching JavaScript logic (evaluated per post container).
     * Returns true if the element matches the filter condition and should be hidden.
     */
    fun getElementInspectorScript(): String
}

/**
 * Filter targeting commercial advertisement banners and explicit ads.
 */
class AdFilter : ContentFilter {
    override val id: String = "ad_filter"
    override val name: String = "Hide Ads"
    override val description: String = "Attempts to remove advertisements displayed in your Facebook feed."

    override fun getCssSelectors(): List<String> = listOf(
        "div[data-ad-preview]",
        "div[data-ad-rendering-role]",
        "div[id^='feed_subtitle_'] a[href*='/ads/']",
        "div[id^='feed_subtitle_'] a[href*='facebook.com/ads']",
        "div[data-pagelet*='Ad']",
        "div[data-pagelet*='Sponsored']",
        "aside[role='complementary'] div[data-pagelet*='RightRail']"
    )

    override fun getElementInspectorScript(): String = """
        function(el) {
            // Check direct ad attributes
            if (el.hasAttribute('data-ad-preview') || el.hasAttribute('data-ad-rendering-role')) return true;
            // Check ad links
            var adLink = el.querySelector('a[href*="/ads/about"], a[href*="facebook.com/ads/"], a[href*="/ads/preferences"]');
            if (adLink) return true;
            // Check aria labels
            var aria = el.getAttribute('aria-label') || '';
            if (/^(advertisement|anuncio|publicité|werbung|publicidad)$/i.test(aria.trim())) return true;
            return false;
        }
    """.trimIndent()
}

/**
 * Filter targeting feed posts marked as sponsored.
 */
class SponsoredPostFilter : ContentFilter {
    override val id: String = "sponsored_filter"
    override val name: String = "Hide Sponsored Posts"
    override val description: String = "Attempts to remove posts marked as sponsored."

    override fun getCssSelectors(): List<String> = listOf(
        "div[aria-label='Sponsored']",
        "div[aria-label='Patrocinado']",
        "div[aria-label='Publicidad']",
        "span[aria-label='Sponsored']",
        "a[aria-label='Sponsored']",
        "a[href*='notif_t=ad']"
    )

    override fun getElementInspectorScript(): String = """
        function(el) {
            // Check aria-label directly or in sub-headers
            var spans = el.querySelectorAll('span, a, div[role="button"]');
            for (var i = 0; i < spans.length; i++) {
                var s = spans[i];
                var aria = s.getAttribute('aria-label') || '';
                if (/^(sponsored|patrocinado|publicidad|gesponsert|commandité)$/i.test(aria.trim())) {
                    return true;
                }
                // Check if textContent is explicitly 'Sponsored' or separated characters e.g. S p o n s o r e d
                var txt = s.textContent.trim();
                if (txt.length >= 9 && txt.length <= 25) {
                    var cleaned = txt.replace(/\s+/g, '').toLowerCase();
                    if (cleaned === 'sponsored' || cleaned === 'patrocinado' || cleaned === 'publicidad') {
                        return true;
                    }
                }
            }
            return false;
        }
    """.trimIndent()
}

/**
 * Filter targeting Stories reels/tray at top of feed.
 */
class StoriesFilter : ContentFilter {
    override val id: String = "stories_filter"
    override val name: String = "Hide Stories"
    override val description: String = "Optionally hide the Stories section from the Facebook feed."

    override fun getCssSelectors(): List<String> = listOf(
        "div[aria-label='Stories']",
        "div[aria-label='Historias']",
        "div[data-pagelet='Stories']",
        "div[data-pagelet='StoryFeed']",
        "div[data-pagelet='StoriesTray']",
        "div[id*='stories_tray']",
        "div[role='region'][aria-label*='Stories' i]"
    )

    override fun getElementInspectorScript(): String = """
        function(el) {
            var label = el.getAttribute('aria-label') || '';
            if (/^(stories|historias|story feed)$/i.test(label.trim())) return true;
            if (el.getAttribute('data-pagelet') === 'Stories') return true;
            return false;
        }
    """.trimIndent()
}

/**
 * Filter targeting "People You May Know" cards and widgets.
 */
class PeopleYouMayKnowFilter : ContentFilter {
    override val id: String = "pymk_filter"
    override val name: String = "Hide People You May Know"
    override val description: String = "Hide recommendation cards and friend suggestions."

    override fun getCssSelectors(): List<String> = listOf(
        "div[aria-label*='People You May Know' i]",
        "div[aria-label*='Personas que quizá conozcas' i]",
        "div[data-pagelet*='PeopleYouMayKnow' i]",
        "div[data-pagelet*='PYMK' i]"
    )

    override fun getElementInspectorScript(): String = """
        function(el) {
            var label = (el.getAttribute('aria-label') || '').toLowerCase();
            if (label.indexOf('people you may know') !== -1 || label.indexOf('personas que quiz') !== -1) {
                return true;
            }
            // Check headings
            var h = el.querySelectorAll('h3, h4, span[role="heading"]');
            for (var i = 0; i < h.length; i++) {
                var text = (h[i].textContent || '').toLowerCase();
                if (text.indexOf('people you may know') !== -1 || text.indexOf('personas que quiz') !== -1) {
                    return true;
                }
            }
            return false;
        }
    """.trimIndent()
}
