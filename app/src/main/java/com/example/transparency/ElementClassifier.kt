package com.example.transparency

/**
 * Centralized selectors and classification rules for Facebook mobile web (m.facebook.com) DOM elements.
 * Separates background/container elements from content (text, media, buttons, inputs).
 */
object ElementClassifier {

    /**
     * Root background containers that should be completely transparent when transparency > 0.
     */
    val ROOT_CONTAINER_SELECTORS = listOf(
        "html",
        "body",
        "#root",
        "#viewport",
        "#objects_container",
        "#m_newsfeed_stream",
        ".m-timeline-body",
        "div[data-mcomponent=\"MContainer\"]",
        "div[data-pagelet=\"root\"]",
        "div[role=\"main\"]"
    )

    /**
     * Feed post cards, story wrappers, and content units.
     */
    val CARD_CONTAINER_SELECTORS = listOf(
        "article",
        "div[role=\"article\"]",
        "div[data-mcomponent=\"MCard\"]",
        "div[data-tracking-duration-id]",
        "div[data-ft]",
        "div[data-pagelet*=\"FeedUnit\"]",
        "div[data-sigil*=\"m-feed-unit\"]",
        ".storyStream > div",
        ".fb-glass-card"
    )

    /**
     * Headers, app bars, and sticky top/bottom navigation surfaces.
     */
    val HEADER_NAV_SELECTORS = listOf(
        "header",
        "div[role=\"banner\"]",
        "div[data-sigil=\"m-header\"]",
        "div[data-pagelet=\"MHeader\"]",
        "#header",
        "nav[role=\"navigation\"]"
    )

    /**
     * Comment boxes and reply bubbles.
     */
    val COMMENT_CONTAINER_SELECTORS = listOf(
        "div[data-sigil*=\"comment\"]",
        "div[role=\"comment\"]",
        ".comment_list > div",
        "div[data-pagelet*=\"Comment\"]"
    )

    /**
     * Flyouts, dialogs, popovers, and bottom sheets.
     */
    val OVERLAY_CONTAINER_SELECTORS = listOf(
        "div[role=\"dialog\"]",
        "div[data-sigil*=\"flyout\"]",
        "div[data-sigil*=\"overlay\"]",
        "div[data-pagelet*=\"Modal\"]"
    )

    /**
     * Elements that MUST NEVER be made translucent, preserving full opacity and interactivity.
     */
    val PROTECTED_CONTENT_SELECTORS = listOf(
        "img",
        "video",
        "svg",
        "canvas",
        "picture",
        "button",
        "[role=\"button\"]",
        "input",
        "textarea",
        "select",
        "[role=\"img\"]",
        "i.img",
        "span[data-sigil*=\"like\"]",
        "span[data-sigil*=\"reaction\"]"
    )

    /**
     * JavaScript helper function injected into WebView to inspect if a newly added DOM node
     * is a post/card container without touching protected interactive elements.
     */
    fun getJsClassifierFunction(): String {
        return """
            function isCardContainer(el) {
                if (!el || el.nodeType !== 1) return false;
                var tag = el.tagName.toLowerCase();
                if (tag === 'img' || tag === 'video' || tag === 'svg' || tag === 'button' || tag === 'input' || tag === 'textarea') {
                    return false;
                }
                var role = el.getAttribute('role');
                if (role === 'article') return true;
                if (tag === 'article') return true;
                if (el.hasAttribute('data-tracking-duration-id') || el.hasAttribute('data-ft')) return true;
                var pagelet = el.getAttribute('data-pagelet') || '';
                if (pagelet.indexOf('FeedUnit') !== -1) return true;
                var sigil = el.getAttribute('data-sigil') || '';
                if (sigil.indexOf('m-feed-unit') !== -1) return true;
                var mcomp = el.getAttribute('data-mcomponent') || '';
                if (mcomp === 'MCard') return true;
                return false;
            }
        """.trimIndent()
    }
}
