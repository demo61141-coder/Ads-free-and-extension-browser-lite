package com.example.data

import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val dao: BrowserDao) {

    val shortcuts: Flow<List<QuickShortcutEntity>> = dao.getAllShortcuts()
    val bookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val downloads: Flow<List<DownloadItemEntity>> = dao.getAllDownloads()
    val extensions: Flow<List<BrowserExtensionEntity>> = dao.getAllExtensions()

    suspend fun ensureSeeded() {
        if (dao.getShortcutCount() == 0) {
            val defaults = listOf(
                QuickShortcutEntity(
                    title = "Google",
                    url = "https://www.google.com",
                    iconLetter = "G",
                    colorHex = 0xFF4285F4,
                    isDefault = true,
                    sortOrder = 1
                ),
                QuickShortcutEntity(
                    title = "YouTube",
                    url = "https://m.youtube.com",
                    iconLetter = "▶",
                    colorHex = 0xFFFF0000,
                    isDefault = true,
                    sortOrder = 2
                ),
                QuickShortcutEntity(
                    title = "Wikipedia",
                    url = "https://en.m.wikipedia.org",
                    iconLetter = "W",
                    colorHex = 0xFF334155,
                    isDefault = true,
                    sortOrder = 3
                ),
                QuickShortcutEntity(
                    title = "Facebook",
                    url = "https://m.facebook.com",
                    iconLetter = "f",
                    colorHex = 0xFF1877F2,
                    isDefault = true,
                    sortOrder = 4
                ),
                QuickShortcutEntity(
                    title = "Reddit",
                    url = "https://www.reddit.com",
                    iconLetter = "R",
                    colorHex = 0xFFFF4500,
                    isDefault = true,
                    sortOrder = 5
                ),
                QuickShortcutEntity(
                    title = "Instagram",
                    url = "https://www.instagram.com",
                    iconLetter = "IG",
                    colorHex = 0xFFE1306C,
                    isDefault = true,
                    sortOrder = 6
                ),
                QuickShortcutEntity(
                    title = "Prothom Alo",
                    url = "https://www.prothomalo.com",
                    iconLetter = "P",
                    colorHex = 0xFFDC2626,
                    isDefault = true,
                    sortOrder = 7
                )
            )
            dao.insertShortcuts(defaults)

            // Seed helpful default bookmarks
            dao.insertBookmark(
                BookmarkEntity(
                    title = "Prothom Alo (Ad-Free News Test)",
                    url = "https://www.prothomalo.com",
                    category = "News"
                )
            )
            dao.insertBookmark(
                BookmarkEntity(
                    title = "Veyora Ad-Blocker Stress Sandbox",
                    url = "veyora://adtest",
                    category = "Testing"
                )
            )
            dao.insertBookmark(
                BookmarkEntity(
                    title = "Wikipedia — Free Encyclopedia",
                    url = "https://en.m.wikipedia.org",
                    category = "Reference"
                )
            )
            dao.insertBookmark(
                BookmarkEntity(
                    title = "Chrome Web Store",
                    url = "https://chromewebstore.google.com",
                    category = "Extensions"
                )
            )
        }

        if (dao.getExtensionCount() == 0) {
            val defaultExtensions = listOf(
                BrowserExtensionEntity(
                    id = "ext_news_clean_reader",
                    name = "News Portal Clean Reader (Prothom Alo + Global)",
                    version = "2.4.0",
                    description = "Strips in-article banner ads, floating video pop-ups, and collapses empty DFP ad containers on Prothom Alo and news sites.",
                    matchPattern = "*://*/*",
                    cssCode = """
                        .dfp-ad, .ad-box, .ad-container, .advertisement, [id*="google_ads"],
                        ins.adsbygoogle, [class*="ad-slot"], [class*="sponsored-content"],
                        .sticky-ad, .bottom-ad-banner, .interstitial-ad {
                            display: none !important;
                            height: 0 !important;
                            min-height: 0 !important;
                            margin: 0 !important;
                            padding: 0 !important;
                            overflow: hidden !important;
                        }
                    """.trimIndent(),
                    jsCode = """
                        (function() {
                            const selectors = [
                                '[id*="google_ads"]', 'ins.adsbygoogle', '.dfp-ad',
                                '[class*="ad-wrapper"]', '[class*="ad-container"]',
                                '[data-ad-slot]', 'iframe[src*="doubleclick"]',
                                'iframe[src*="googlesyndication"]'
                            ];
                            function cleanNewsAds() {
                                selectors.forEach(sel => {
                                    document.querySelectorAll(sel).forEach(el => el.remove());
                                });
                            }
                            cleanNewsAds();
                        })();
                    """.trimIndent(),
                    sourceType = "Built-in",
                    isEnabled = true
                ),
                BrowserExtensionEntity(
                    id = "ext_cookie_annoyance_blocker",
                    name = "Cookie & GDPR Popup Auto-Dismiss",
                    version = "1.8.2",
                    description = "Automatically hides intrusive cookie consent banners and newsletter overlay modals.",
                    matchPattern = "*://*/*",
                    cssCode = """
                        [id*="cookie-banner"], [class*="cookie-consent"], [class*="gdpr-banner"],
                        #onetrust-consent-sdk, .cc-window, .fc-consent-root {
                            display: none !important;
                        }
                    """.trimIndent(),
                    jsCode = """
                        console.info('[Veyora Ext] Cookie & GDPR Auto-Dismiss active');
                    """.trimIndent(),
                    sourceType = "Chrome Web Store",
                    storeUrl = "https://chromewebstore.google.com/detail/idontcareaboutcookies",
                    isEnabled = true
                ),
                BrowserExtensionEntity(
                    id = "ext_dark_reader_lite",
                    name = "Midnight OLED Force Dark Mode",
                    version = "4.9.1",
                    description = "Applies smart CSS color inversion to bright websites while preserving images and videos.",
                    matchPattern = "*://*/*",
                    cssCode = """
                        html {
                            filter: invert(0.92) hue-rotate(180deg) !important;
                            background: #0d1117 !important;
                        }
                        img, video, iframe, canvas, svg {
                            filter: invert(1) hue-rotate(180deg) !important;
                        }
                    """.trimIndent(),
                    jsCode = "console.info('[Veyora Ext] Midnight OLED Force Dark applied');",
                    sourceType = "Chrome Web Store",
                    storeUrl = "https://chromewebstore.google.com/detail/dark-reader",
                    isEnabled = false
                ),
                BrowserExtensionEntity(
                    id = "ext_copy_unlocker",
                    name = "Right-Click & Text Selection Unlocker",
                    version = "1.3.0",
                    description = "Re-enables text copying, context menus, and selection on restricted news articles.",
                    matchPattern = "*://*/*",
                    cssCode = """
                        *, body, article, p, span, div {
                            -webkit-user-select: text !important;
                            user-select: text !important;
                        }
                    """.trimIndent(),
                    jsCode = """
                        (function() {
                            ['copy', 'cut', 'contextmenu', 'selectstart'].forEach(evt => {
                                window.addEventListener(evt, e => e.stopPropagation(), true);
                            });
                        })();
                    """.trimIndent(),
                    sourceType = "Built-in",
                    isEnabled = true
                )
            )
            dao.insertExtensions(defaultExtensions)
        }
    }

    suspend fun addShortcut(title: String, url: String, colorHex: Long) {
        val cleanTitle = title.trim().ifEmpty { "Site" }
        val iconLetter = cleanTitle.take(2).uppercase()
        dao.insertShortcut(
            QuickShortcutEntity(
                title = cleanTitle,
                url = url.trim(),
                iconLetter = iconLetter,
                colorHex = colorHex,
                isDefault = false,
                sortOrder = 99
            )
        )
    }

    suspend fun deleteShortcut(id: Int) = dao.deleteShortcutById(id)

    suspend fun addBookmark(title: String, url: String, category: String = "Saved") {
        dao.insertBookmark(BookmarkEntity(title = title, url = url, category = category))
    }

    suspend fun deleteBookmark(id: Int) = dao.deleteBookmarkById(id)

    suspend fun addDownload(fileName: String, url: String, mimeType: String, sizeBytes: Long) {
        dao.insertDownload(
            DownloadItemEntity(
                fileName = fileName,
                url = url,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
                status = "Completed"
            )
        )
    }

    suspend fun deleteDownload(id: Int) = dao.deleteDownloadById(id)

    suspend fun clearDownloads() = dao.clearAllDownloads()

    suspend fun toggleExtension(extension: BrowserExtensionEntity) {
        dao.updateExtension(extension.copy(isEnabled = !extension.isEnabled))
    }

    suspend fun addCustomExtension(
        name: String,
        description: String,
        matchPattern: String,
        cssCode: String,
        jsCode: String,
        sourceType: String,
        storeUrl: String
    ) {
        dao.insertExtension(
            BrowserExtensionEntity(
                name = name.trim(),
                version = "1.0.0",
                description = description.trim().ifEmpty { "Custom user extension" },
                matchPattern = matchPattern.trim().ifEmpty { "*://*/*" },
                cssCode = cssCode,
                jsCode = jsCode,
                sourceType = sourceType,
                storeUrl = storeUrl.trim(),
                isEnabled = true
            )
        )
    }

    suspend fun deleteExtension(id: String) = dao.deleteExtensionById(id)
}
