package com.example.engine

object VeyoraDevToolsEngine {

    /**
     * Injects network telemetry hooks (`fetch` and `XMLHttpRequest`) and DOM summary helpers
     * so the built-in Developer Tools panel can display live network calls and inspect elements.
     */
    fun buildDevToolsBootstrapScript(): String = """
        (function() {
            if (window.__veyoraDevToolsBootstrapped) return;
            window.__veyoraDevToolsBootstrapped = true;

            // Hook fetch() for DevTools Network Inspector
            const origFetch = window.fetch;
            if (origFetch) {
                window.fetch = async function(...args) {
                    const start = performance.now();
                    const reqUrl = typeof args[0] === 'string' ? args[0] : (args[0] && args[0].url ? args[0].url : '');
                    const method = (args[1] && args[1].method) ? args[1].method : 'GET';
                    try {
                        const response = await origFetch.apply(this, args);
                        const dur = Math.round(performance.now() - start);
                        if (window.VeyoraBridge && window.VeyoraBridge.onJsNetworkLog) {
                            window.VeyoraBridge.onJsNetworkLog(reqUrl, method, 'FETCH', response.status || 200, dur);
                        }
                        return response;
                    } catch (err) {
                        const dur = Math.round(performance.now() - start);
                        if (window.VeyoraBridge && window.VeyoraBridge.onJsNetworkLog) {
                            window.VeyoraBridge.onJsNetworkLog(reqUrl, method, 'FETCH', 0, dur);
                        }
                        throw err;
                    }
                };
            }

            // Hook XMLHttpRequest for DevTools Network Inspector
            const origOpen = XMLHttpRequest.prototype.open;
            const origSend = XMLHttpRequest.prototype.send;
            XMLHttpRequest.prototype.open = function(method, url) {
                this.__veyoraMethod = method || 'GET';
                this.__veyoraUrl = url || '';
                return origOpen.apply(this, arguments);
            };
            XMLHttpRequest.prototype.send = function() {
                const start = performance.now();
                this.addEventListener('loadend', () => {
                    const dur = Math.round(performance.now() - start);
                    if (window.VeyoraBridge && window.VeyoraBridge.onJsNetworkLog) {
                        window.VeyoraBridge.onJsNetworkLog(
                            String(this.__veyoraUrl || ''),
                            String(this.__veyoraMethod || 'GET'),
                            'XHR',
                            this.status || 200,
                            dur
                        );
                    }
                });
                return origSend.apply(this, arguments);
            };

            // Point-and-Tap Element Inspector Overlay
            window.__veyoraSetPointInspect = function(enabled) {
                window.__veyoraInspectMode = enabled;
                let badge = document.getElementById('__veyora_inspect_highlight');
                if (!enabled) {
                    if (badge) badge.remove();
                    return;
                }
                if (!badge) {
                    badge = document.createElement('div');
                    badge.id = '__veyora_inspect_highlight';
                    badge.style.cssText = 'position:fixed;pointer-events:none;z-index:2147483647;border:2px solid #00E5FF;background:rgba(0,229,255,0.16);border-radius:4px;transition:all 0.08s ease;display:none;';
                    const label = document.createElement('div');
                    label.id = '__veyora_inspect_label';
                    label.style.cssText = 'position:absolute;top:-22px;left:0;background:#090D16;color:#00E5FF;font-family:monospace;font-size:10px;padding:2px 6px;border-radius:3px;white-space:nowrap;border:1px solid #00E5FF;';
                    badge.appendChild(label);
                    document.body.appendChild(badge);
                }
            };

            document.addEventListener('click', function(e) {
                if (!window.__veyoraInspectMode) return;
                e.preventDefault();
                e.stopPropagation();
                const el = e.target;
                if (!el || el.id === '__veyora_inspect_highlight') return;

                const rect = el.getBoundingClientRect();
                const badge = document.getElementById('__veyora_inspect_highlight');
                const label = document.getElementById('__veyora_inspect_label');
                if (badge && label) {
                    badge.style.display = 'block';
                    badge.style.left = rect.left + 'px';
                    badge.style.top = rect.top + 'px';
                    badge.style.width = rect.width + 'px';
                    badge.style.height = rect.height + 'px';
                    const tag = el.tagName.toLowerCase();
                    const idStr = el.id ? '#' + el.id : '';
                    const clsStr = (el.className && typeof el.className === 'string')
                        ? '.' + el.className.trim().split(/\s+/).slice(0, 2).join('.')
                        : '';
                    label.textContent = tag + idStr + clsStr + ' (' + Math.round(rect.width) + '×' + Math.round(rect.height) + ')';
                }

                const cs = window.getComputedStyle(el);
                const payload = {
                    tagName: el.tagName.toLowerCase(),
                    id: el.id || '',
                    className: (typeof el.className === 'string') ? el.className : '',
                    dimensions: Math.round(rect.width) + 'px × ' + Math.round(rect.height) + 'px',
                    computedStyles: {
                        display: cs.display,
                        position: cs.position,
                        color: cs.color,
                        backgroundColor: cs.backgroundColor,
                        fontSize: cs.fontSize,
                        fontFamily: cs.fontFamily.split(',')[0],
                        padding: cs.padding,
                        margin: cs.margin,
                        zIndex: cs.zIndex
                    },
                    outerHtml: (el.outerHTML || '').substring(0, 900)
                };
                if (window.VeyoraBridge && window.VeyoraBridge.onElementInspected) {
                    window.VeyoraBridge.onElementInspected(JSON.stringify(payload));
                }
            }, true);
        })();
    """.trimIndent()

    /**
     * Generates a clean DOM tree & page metadata snapshot for the DevTools Elements tab.
     */
    fun buildDomSnapshotScript(): String = """
        (function() {
            function summarizeNode(node, depth) {
                if (!node || depth > 4) return '';
                if (node.nodeType !== 1) return '';
                const tag = node.tagName.toLowerCase();
                if (tag === 'script' || tag === 'style' || tag === 'svg') return '';
                const idPart = node.id ? '#' + node.id : '';
                const clsPart = (node.className && typeof node.className === 'string' && node.className.trim())
                    ? '.' + node.className.trim().split(/\s+/).slice(0, 2).join('.')
                    : '';
                const indent = '  '.repeat(depth);
                let line = indent + '<' + tag + idPart + clsPart + '>\n';
                const children = node.children || [];
                const maxChildren = Math.min(children.length, 12);
                for (let i = 0; i < maxChildren; i++) {
                    line += summarizeNode(children[i], depth + 1);
                }
                if (children.length > maxChildren) {
                    line += indent + '  ... (' + (children.length - maxChildren) + ' more nodes)\n';
                }
                return line;
            }
            const tree = summarizeNode(document.documentElement, 0);
            const totalNodes = document.querySelectorAll('*').length;
            const title = document.title || 'Untitled';
            return JSON.stringify({
                title: title,
                totalNodes: totalNodes,
                tree: tree.substring(0, 4000),
                htmlPreview: (document.documentElement.outerHTML || '').substring(0, 3500)
            });
        })();
    """.trimIndent()
}
