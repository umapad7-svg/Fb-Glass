package com.example.transparency

/**
 * Provides debounced JavaScript MutationObserver code to handle dynamic Facebook infinite feed updates.
 * Identifies container elements, marks processed elements, avoids duplicate work, and prevents memory leaks.
 */
object DomObserver {

    /**
     * Builds the client-side JavaScript observer script.
     */
    fun buildObserverScript(): String {
        return """
            (function() {
                if (window.__FB_GLASS_DOM_OBSERVER_INITIALIZED) {
                    return;
                }
                window.__FB_GLASS_DOM_OBSERVER_INITIALIZED = true;

                ${ElementClassifier.getJsClassifierFunction()}

                function tagContainer(el) {
                    if (!el || el.nodeType !== 1) return;
                    if (el.getAttribute('data-fb-glass-processed') === '1') return;
                    
                    if (isCardContainer(el)) {
                        el.setAttribute('data-fb-glass-processed', '1');
                        el.classList.add('fb-glass-card');
                    }
                }

                function processSubtree(root) {
                    if (!root || root.nodeType !== 1) return;
                    tagContainer(root);

                    var candidates = root.querySelectorAll('article, div[role="article"], div[data-tracking-duration-id], div[data-mcomponent="MCard"]');
                    for (var i = 0; i < candidates.length; i++) {
                        var node = candidates[i];
                        if (node.getAttribute('data-fb-glass-processed') !== '1') {
                            node.setAttribute('data-fb-glass-processed', '1');
                            node.classList.add('fb-glass-card');
                        }
                    }
                }

                // Debounce mechanism to preserve 60fps scrolling performance
                var debounceTimer = null;
                var pendingNodes = [];

                function flushPending() {
                    debounceTimer = null;
                    var nodesToProcess = pendingNodes.slice();
                    pendingNodes = [];
                    for (var i = 0; i < nodesToProcess.length; i++) {
                        try {
                            processSubtree(nodesToProcess[i]);
                        } catch (e) {
                            // Suppress errors during DOM detach
                        }
                    }
                }

                function queueMutation(node) {
                    pendingNodes.push(node);
                    if (!debounceTimer) {
                        debounceTimer = setTimeout(function() {
                            requestAnimationFrame(flushPending);
                        }, 120);
                    }
                }

                if (window.MutationObserver && document.body) {
                    var observer = new MutationObserver(function(mutations) {
                        for (var i = 0; i < mutations.length; i++) {
                            var added = mutations[i].addedNodes;
                            for (var j = 0; j < added.length; j++) {
                                var n = added[j];
                                if (n.nodeType === 1) {
                                    queueMutation(n);
                                }
                            }
                        }
                    });

                    observer.observe(document.body, {
                        childList: true,
                        subtree: true
                    });

                    window.__FB_GLASS_DOM_OBSERVER_INSTANCE = observer;
                }

                // Initial pass on existing content
                if (document.body) {
                    processSubtree(document.body);
                }
            })();
        """.trimIndent()
    }

    /**
     * Script to cleanly disconnect observer when WebView is destroyed.
     */
    fun buildCleanupScript(): String {
        return """
            if (window.__FB_GLASS_DOM_OBSERVER_INSTANCE) {
                try {
                    window.__FB_GLASS_DOM_OBSERVER_INSTANCE.disconnect();
                    window.__FB_GLASS_DOM_OBSERVER_INSTANCE = null;
                    window.__FB_GLASS_DOM_OBSERVER_INITIALIZED = false;
                } catch (e) {}
            }
        """.trimIndent()
    }
}
