package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "quick_shortcuts")
data class QuickShortcutEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val iconLetter: String,
    val colorHex: Long,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val url: String,
    val mimeType: String,
    val sizeBytes: Long,
    val status: String = "Completed",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "extensions")
data class BrowserExtensionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val version: String,
    val description: String,
    val matchPattern: String = "*://*/*",
    val jsCode: String = "",
    val cssCode: String = "",
    val sourceType: String = "Built-in", // "Built-in", "Chrome Web Store", "Custom Script"
    val storeUrl: String = "",
    val isEnabled: Boolean = true
)

enum class SearchEngine(
    val displayName: String,
    val shortCode: String,
    val queryTemplate: String,
    val homeUrl: String,
    val badgeColorHex: Long
) {
    GOOGLE(
        displayName = "Google",
        shortCode = "G",
        queryTemplate = "https://www.google.com/search?q=%s",
        homeUrl = "https://www.google.com",
        badgeColorHex = 0xFF4285F4
    ),
    DUCKDUCKGO(
        displayName = "DuckDuckGo",
        shortCode = "DDG",
        queryTemplate = "https://duckduckgo.com/?q=%s",
        homeUrl = "https://duckduckgo.com",
        badgeColorHex = 0xFFDE5833
    ),
    BING(
        displayName = "Microsoft Bing",
        shortCode = "B",
        queryTemplate = "https://www.bing.com/search?q=%s",
        homeUrl = "https://www.bing.com",
        badgeColorHex = 0xFF008373
    ),
    YAHOO(
        displayName = "Yahoo Search",
        shortCode = "Y!",
        queryTemplate = "https://search.yahoo.com/search?p=%s",
        homeUrl = "https://search.yahoo.com",
        badgeColorHex = 0xFF6001D2
    ),
    BRAVE(
        displayName = "Brave Search",
        shortCode = "BRV",
        queryTemplate = "https://search.brave.com/search?q=%s",
        homeUrl = "https://search.brave.com",
        badgeColorHex = 0xFFFB542B
    )
}

enum class VpnServerRegion(
    val id: String,
    val countryName: String,
    val cityName: String,
    val flagEmoji: String,
    val virtualIp: String,
    val countryCode: String,
    val acceptLanguage: String,
    val basePingMs: Int
) {
    AUTO(
        id = "auto",
        countryName = "Auto / Best Location",
        cityName = "Cloudflare Edge Anycast",
        flagEmoji = "🌐",
        virtualIp = "104.16.248.249",
        countryCode = "US",
        acceptLanguage = "en-US,en;q=0.9",
        basePingMs = 14
    ),
    UNITED_STATES(
        id = "us",
        countryName = "United States",
        cityName = "New York • Ultra Relay",
        flagEmoji = "🇺🇸",
        virtualIp = "198.51.100.42",
        countryCode = "US",
        acceptLanguage = "en-US,en;q=0.9",
        basePingMs = 34
    ),
    GERMANY(
        id = "de",
        countryName = "Germany",
        cityName = "Frankfurt • Privacy Core",
        flagEmoji = "🇩🇪",
        virtualIp = "185.220.101.14",
        countryCode = "DE",
        acceptLanguage = "de-DE,de;q=0.9,en;q=0.8",
        basePingMs = 45
    ),
    UNITED_KINGDOM(
        id = "uk",
        countryName = "United Kingdom",
        cityName = "London • Docklands Node",
        flagEmoji = "🇬🇧",
        virtualIp = "192.0.2.188",
        countryCode = "GB",
        acceptLanguage = "en-GB,en;q=0.9",
        basePingMs = 41
    ),
    CANADA(
        id = "ca",
        countryName = "Canada",
        cityName = "Toronto • Shield North",
        flagEmoji = "🇨🇦",
        virtualIp = "203.0.113.77",
        countryCode = "CA",
        acceptLanguage = "en-CA,en;q=0.9,fr-CA;q=0.8",
        basePingMs = 39
    ),
    SINGAPORE(
        id = "sg",
        countryName = "Singapore",
        cityName = "Jurong East • Asia Fast",
        flagEmoji = "🇸🇬",
        virtualIp = "172.67.142.90",
        countryCode = "SG",
        acceptLanguage = "en-SG,en;q=0.9",
        basePingMs = 21
    )
}

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Tab",
    val url: String = "veyora://home",
    val isIncognito: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val adsBlockedInTab: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isReaderMode: Boolean = false
)

enum class ConsoleLogLevel { LOG, INFO, WARN, ERROR, RESULT }

data class DevToolsConsoleLog(
    val id: String = UUID.randomUUID().toString(),
    val level: ConsoleLogLevel,
    val message: String,
    val sourceId: String = "console",
    val lineNumber: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

data class DevToolsNetworkLog(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val host: String,
    val method: String = "GET",
    val resourceType: String, // "DOC", "SCRIPT", "STYLE", "IMG", "XHR", "AD_BLOCKED"
    val status: Int,
    val isBlockedAd: Boolean = false,
    val blockedReason: String = "",
    val durationMs: Int = 18,
    val timestamp: Long = System.currentTimeMillis()
)

data class InspectedElementInfo(
    val tagName: String = "body",
    val id: String = "",
    val className: String = "",
    val dimensions: String = "0 × 0",
    val computedStyles: Map<String, String> = emptyMap(),
    val attributes: Map<String, String> = emptyMap(),
    val outerHtmlSnippet: String = "Tap any element on the page while Point-Inspect is active, or press 'Refresh DOM' to inspect the document tree."
)
