package com.example.engine

import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

object VeyoraAdBlocker {

    // High-confidence ad, pop-up, and tracker domains (including South Asian & global news portal ad networks)
    private val blockedDomains = setOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "googletagservices.com",
        "pagead2.googlesyndication.com",
        "securepubads.g.doubleclick.net",
        "tpc.googlesyndication.com",
        "adnxs.com",
        "adsrvr.org",
        "criteo.com",
        "criteo.net",
        "taboola.com",
        "outbrain.com",
        "mgid.com",
        "revcontent.com",
        "popads.net",
        "popcash.net",
        "propellerads.com",
        "adsterra.com",
        "exoclick.com",
        "juicyads.com",
        "rubiconproject.com",
        "pubmatic.com",
        "openx.net",
        "indexexchange.com",
        "casalemedia.com",
        "amazon-adsystem.com",
        "moatads.com",
        "scorecardresearch.com",
        "quantserve.com",
        "zedo.com",
        "adform.net",
        "smartadserver.com",
        "teads.tv",
        "sharethrough.com",
        "triplelift.com",
        "33across.com",
        "sovrn.com",
        "bidswitch.net",
        "adroll.com",
        "hotjar.com",
        "mouseflow.com",
        "clarity.ms",
        "connect.facebook.net/en_US/fbevents.js"
    )

    // URL path/query keywords common in ad slots, trackers, and popunders
    private val blockedUrlPatterns = listOf(
        "/pagead/",
        "/adframe",
        "/adbanner",
        "/ads/banner",
        "/ads/pop",
        "/popunder",
        "/adserver",
        "/dfp_ads",
        "/sponsored-banner",
        "adsbygoogle.js",
        "gpt.js",
        "pubads_impl",
        "show_ads.js",
        "ad_status.js",
        "prebid.js",
        "taboola.js",
        "outbrain.js"
    )

    data class BlockDecision(
        val isBlocked: Boolean,
        val reason: String = "",
        val estimatedSavedBytes: Long = 0L
    )

    fun evaluateRequest(url: String): BlockDecision {
        val lowerUrl = url.lowercase()
        val host = try {
            Uri.parse(lowerUrl).host ?: ""
        } catch (_: Exception) {
            ""
        }

        if (host.isNotEmpty()) {
            for (domain in blockedDomains) {
                if (host == domain || host.endsWith(".$domain")) {
                    return BlockDecision(
                        isBlocked = true,
                        reason = "Blocked Ad/Tracker Host ($domain)",
                        estimatedSavedBytes = 38_400L
                    )
                }
            }
        }

        for (pattern in blockedUrlPatterns) {
            if (lowerUrl.contains(pattern)) {
                return BlockDecision(
                    isBlocked = true,
                    reason = "Blocked Ad Script Pattern ($pattern)",
                    estimatedSavedBytes = 29_600L
                )
            }
        }

        return BlockDecision(isBlocked = false)
    }

    fun createEmptyResourceResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            204,
            "No Content (Blocked by Veyora Shield)",
            mapOf(
                "Access-Control-Allow-Origin" to "*",
                "Cache-Control" to "no-store"
            ),
            ByteArrayInputStream(ByteArray(0))
        )
    }

    /**
     * Injected JavaScript + CSS specifically engineered for news portals (such as Prothom Alo)
     * and heavy content websites. Removes in-article DFP slots, sticky banners, pop-up overlays,
     * and collapses leftover whitespace so paragraphs flow seamlessly.
     */
    fun buildCosmeticAdBlockerScript(): String = """
        (function() {
            if (window.__veyoraAdShieldActive) return;
            window.__veyoraAdShieldActive = true;

            // Block intrusive window.open pop-ups
            const originalWindowOpen = window.open;
            window.open = function(url, name, specs) {
                console.warn('[Veyora AdShield] Blocked pop-up window request:', url);
                if (window.VeyoraBridge && window.VeyoraBridge.onDomAdsRemoved) {
                    window.VeyoraBridge.onDomAdsRemoved(1, 'Pop-up window blocked');
                }
                return null;
            };

            const adSelectors = [
                'ins.adsbygoogle',
                '[id^="div-gpt-ad"]',
                '[id*="google_ads"]',
                '[class*="dfp-ad"]',
                '[class*="ad-slot"]',
                '[class*="ad-wrapper"]',
                '[class*="ad-container"]',
                '[class*="ad-banner"]',
                '[class*="ad-box"]',
                '[class*="top-ad"]',
                '[class*="bottom-sticky-ad"]',
                '[class*="in-article-ad"]',
                '[class*="prothomalo-ad"]',
                '[data-ad-slot]',
                '[data-google-query-id]',
                'iframe[src*="doubleclick.net"]',
                'iframe[src*="googlesyndication.com"]',
                'iframe[id*="google_ads"]',
                '.taboola-widget',
                '.OUTBRAIN',
                '.mgid-widget',
                '.sponsored-box',
                '.ad-placeholder'
            ];

            const styleId = 'veyora-adshield-style';
            if (!document.getElementById(styleId)) {
                const style = document.createElement('style');
                style.id = styleId;
                style.textContent = adSelectors.join(',\n') + ` {
                    display: none !important;
                    visibility: hidden !important;
                    height: 0px !important;
                    min-height: 0px !important;
                    max-height: 0px !important;
                    margin: 0px !important;
                    padding: 0px !important;
                    border: none !important;
                    overflow: hidden !important;
                    pointer-events: none !important;
                }`;
                (document.head || document.documentElement).appendChild(style);
            }

            let totalRemovedInPage = 0;

            function sweepDomAds() {
                let newlyRemoved = 0;
                adSelectors.forEach(selector => {
                    try {
                        const nodes = document.querySelectorAll(selector);
                        nodes.forEach(node => {
                            if (node && node.getAttribute('data-veyora-cleaned') !== '1') {
                                node.setAttribute('data-veyora-cleaned', '1');
                                node.style.setProperty('display', 'none', 'important');
                                node.style.setProperty('height', '0px', 'important');
                                // Also collapse empty parent wrapper if it only held an ad label
                                const parent = node.parentElement;
                                if (parent && parent.innerText && parent.innerText.trim().length <= 18) {
                                    const txt = parent.innerText.trim().toLowerCase();
                                    if (txt === 'advertisement' || txt === 'sponsored' || txt === 'বিজ্ঞাপন' || txt === 'ad') {
                                        parent.style.setProperty('display', 'none', 'important');
                                    }
                                }
                                node.remove();
                                newlyRemoved++;
                            }
                        });
                    } catch (e) {}
                });

                if (newlyRemoved > 0) {
                    totalRemovedInPage += newlyRemoved;
                    if (window.VeyoraBridge && window.VeyoraBridge.onDomAdsRemoved) {
                        window.VeyoraBridge.onDomAdsRemoved(newlyRemoved, 'In-article DOM ads stripped');
                    }
                }
            }

            sweepDomAds();
            const observer = new MutationObserver(() => sweepDomAds());
            observer.observe(document.documentElement || document.body, {
                childList: true,
                subtree: true
            });
            setTimeout(sweepDomAds, 800);
            setTimeout(sweepDomAds, 2200);
        })();
    """.trimIndent()

    /**
     * Built-in News Portal Ad-Stress Test Sandbox (`veyora://adtest`)
     * Lets users immediately test Prothom Alo / news portal in-article ad stripping,
     * pop-up blocking, VPN virtual IP header inspection, and DevTools DOM/Console inspection.
     */
    fun buildAdStressTestHtml(
        adBlockEnabled: Boolean,
        vpnEnabled: Boolean,
        vpnRegionName: String,
        vpnVirtualIp: String
    ): String = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>Prothom Alo &amp; News Portal Ad-Shield Benchmark</title>
            <style>
                :root {
                    --bg: #0f172a;
                    --card: #1e293b;
                    --text: #f8fafc;
                    --muted: #94a3b8;
                    --accent: #00e5ff;
                    --danger: #ef4444;
                    --success: #10b981;
                }
                body {
                    margin: 0;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    background: var(--bg);
                    color: var(--text);
                    line-height: 1.68;
                    padding: 16px;
                }
                .masthead {
                    border-bottom: 2px solid #dc2626;
                    padding-bottom: 12px;
                    margin-bottom: 16px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .masthead-brand {
                    font-size: 22px;
                    font-weight: 800;
                    letter-spacing: -0.5px;
                    color: #f8fafc;
                }
                .masthead-brand span { color: #dc2626; }
                .badge {
                    background: rgba(0, 229, 255, 0.14);
                    color: var(--accent);
                    border: 1px solid rgba(0, 229, 255, 0.35);
                    padding: 4px 10px;
                    border-radius: 999px;
                    font-size: 11px;
                    font-weight: 700;
                }
                .status-strip {
                    background: var(--card);
                    border-radius: 12px;
                    padding: 12px 14px;
                    margin-bottom: 18px;
                    font-size: 13px;
                    border-left: 4px solid ${if (adBlockEnabled) "#10b981" else "#ef4444"};
                }
                article h1 {
                    font-size: 23px;
                    line-height: 1.3;
                    margin: 0 0 10px 0;
                }
                .meta {
                    color: var(--muted);
                    font-size: 12px;
                    margin-bottom: 16px;
                }
                p {
                    margin: 0 0 16px 0;
                    font-size: 15px;
                    color: #e2e8f0;
                }
                /* Simulated intrusive ads that get stripped when Veyora AdShield is ON */
                .dfp-ad, .in-article-ad, .ad-banner, .bottom-sticky-ad {
                    background: linear-gradient(135deg, #450a0a, #7f1d1d);
                    border: 2px dashed #f87171;
                    border-radius: 12px;
                    padding: 18px;
                    margin: 18px 0;
                    text-align: center;
                    color: #fecaca;
                }
                .ad-tag {
                    font-size: 10px;
                    text-transform: uppercase;
                    letter-spacing: 1.2px;
                    background: #ef4444;
                    color: white;
                    padding: 2px 8px;
                    border-radius: 4px;
                    display: inline-block;
                    margin-bottom: 6px;
                }
                .btn-row {
                    display: flex;
                    gap: 10px;
                    flex-wrap: wrap;
                    margin-top: 16px;
                }
                button {
                    background: #0284c7;
                    color: white;
                    border: none;
                    border-radius: 8px;
                    padding: 10px 14px;
                    font-weight: 600;
                    font-size: 13px;
                    cursor: pointer;
                }
            </style>
        </head>
        <body>
            <div class="masthead">
                <div class="masthead-brand">PROTHOM<span>ALO</span> • NEWS LAB</div>
                <div class="badge">${if (adBlockEnabled) "AD-SHIELD ACTIVE" else "ADS UNFILTERED"}</div>
            </div>

            <div class="status-strip" id="telemetry-box">
                <strong>Veyora Live Engine:</strong>
                Ad Blocker is <b>${if (adBlockEnabled) "ENABLED (Clean Reader)" else "DISABLED (Showing Intrusive Ads)"}</b> •
                VPN Tunnel: <b>${if (vpnEnabled) "$vpnRegionName ($vpnVirtualIp)" else "Direct Connection"}</b>
            </div>

            <!-- Simulated Top Billboard DFP Ad -->
            <div id="div-gpt-ad-top-billboard" class="dfp-ad" data-ad-slot="prothomalo_top_970x250">
                <div class="ad-tag">বিজ্ঞাপন • Advertisement</div>
                <div><strong>Intrusive Top Billboard Banner (970×250)</strong></div>
                <div style="font-size:12px;">DoubleClick / GoogleSyndication Heavy Video Ad Slot (420 KB)</div>
            </div>

            <article>
                <h1>Next-Generation Lightweight Mobile Browsers Transform News Reading Speed by 340%</h1>
                <div class="meta">Dhaka Bureau • Published 5 mins ago • Clean Web Benchmark</div>

                <p>
                    Modern readers visiting high-traffic portals such as Prothom Alo often download megabytes of
                    third-party tracking scripts, auto-playing video banners, and floating interstitials before the
                    first paragraph of news even finishes rendering.
                </p>

                <!-- Simulated In-Article Ad #1 -->
                <div class="in-article-ad" data-ad-slot="in_article_slot_1">
                    <div class="ad-tag">Sponsored In-Article Banner</div>
                    <div><strong>🔥 Flash Sale Pop-Under &amp; Tracker Widget</strong></div>
                    <div style="font-size:12px;">This mid-article ad disrupts reading flow when Ad Blocker is turned off.</div>
                </div>

                <p>
                    With Veyora Browser's multi-layered WebView interception engine, network requests to known ad
                    servers are terminated at the socket layer with a zero-byte 204 response, while a DOM MutationObserver
                    collapses empty ad containers in real time.
                </p>

                <!-- Simulated In-Article Ad #2 -->
                <ins class="adsbygoogle ad-banner" style="display:block" data-ad-client="ca-pub-999999">
                    <div class="ad-tag">Google Syndication Slot</div>
                    <div><strong>Auto-Expand Interstitial Ad Unit</strong></div>
                </ins>

                <p>
                    Use the toggle switch in the Veyora toolbar or Settings menu ("Block ads &amp; pop-ups") to compare
                    this page with Ad Shield ON versus OFF, or tap the <strong>Inspect</strong> tab below to inspect elements,
                    run Console JavaScript, and view blocked network requests!
                </p>
            </article>

            <div class="btn-row">
                <button onclick="window.open('https://popads.net/fake-popup', '_blank')">
                    Test Pop-up Blocker
                </button>
                <button onclick="console.log('[Benchmark] Article DOM nodes:', document.querySelectorAll('*').length)">
                    Log DOM Count to Console
                </button>
                <button onclick="location.href='https://www.prothomalo.com'">
                    Open Live ProthomAlo.com
                </button>
            </div>

            <script>
                console.info('[News Portal Benchmark] Page loaded. AdShield=${adBlockEnabled}, VPN=${vpnRegionName}');
            </script>
        </body>
        </html>
    """.trimIndent()
}
