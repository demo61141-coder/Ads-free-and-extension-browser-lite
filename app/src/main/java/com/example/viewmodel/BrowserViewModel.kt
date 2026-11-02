package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BookmarkEntity
import com.example.data.BrowserDatabase
import com.example.data.BrowserExtensionEntity
import com.example.data.BrowserRepository
import com.example.data.BrowserTab
import com.example.data.ConsoleLogLevel
import com.example.data.DevToolsConsoleLog
import com.example.data.DevToolsNetworkLog
import com.example.data.DownloadItemEntity
import com.example.data.InspectedElementInfo
import com.example.data.NewsArticleItem
import com.example.data.NewsCategory
import com.example.data.NewsHeadlinesRepository
import com.example.data.QuickShortcutEntity
import com.example.data.SearchEngine
import com.example.data.VpnServerRegion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URLEncoder

enum class ActiveSheet {
    NONE,
    TABS,
    BOOKMARKS_DOWNLOADS,
    DOWNLOADS_ONLY,
    VPN_PROXY,
    EXTENSIONS,
    SETTINGS_PROFILE,
    ARCHITECTURE_GUIDE
}

data class BrowserUiState(
    val isDarkMode: Boolean = true,
    val adBlockEnabled: Boolean = true,
    val totalAdsBlocked: Int = 14,
    val totalPopupsBlocked: Int = 3,
    val estimatedDataSavedBytes: Long = 542_000L,
    val adBlockedNotification: String? = null,
    val vpnEnabled: Boolean = false,
    val selectedVpnRegion: VpnServerRegion = VpnServerRegion.AUTO,
    val vpnConnectedSinceMs: Long = 0L,
    val selectedSearchEngine: SearchEngine = SearchEngine.GOOGLE,
    val selectedNewsCategory: NewsCategory = NewsCategory.FOR_YOU,
    val liveHeadlines: List<NewsArticleItem> = emptyList(),
    val showAllHeadlines: Boolean = false,
    val tabs: List<BrowserTab> = listOf(BrowserTab(id = "tab_main", title = "Home", url = "veyora://home")),
    val activeTabId: String = "tab_main",
    val devToolsVisible: Boolean = false,
    val activeDevToolsTab: Int = 0, // 0: Elements, 1: Console, 2: Network, 3: Info
    val pointInspectActive: Boolean = false,
    val domTreeSnapshot: String = "<html>\n  <head>\n  <body>\n    <!-- Load a web page to inspect live DOM -->",
    val htmlSourcePreview: String = "",
    val domNodeCount: Int = 0,
    val inspectedElement: InspectedElementInfo = InspectedElementInfo(),
    val consoleLogs: List<DevToolsConsoleLog> = listOf(
        DevToolsConsoleLog(
            level = ConsoleLogLevel.INFO,
            message = "[Veyora Engine] WebView v130 + Multi-Layer AdShield initialized.",
            sourceId = "veyora://core"
        )
    ),
    val networkLogs: List<DevToolsNetworkLog> = emptyList(),
    val activeSheet: ActiveSheet = ActiveSheet.NONE,
    val pendingJsCommand: String? = null,
    val reloadTrigger: Int = 0
) {
    val activeTab: BrowserTab
        get() = tabs.find { it.id == activeTabId } ?: tabs.first()

    val isHomeVisible: Boolean
        get() = activeTab.url == "veyora://home"
}

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BrowserRepository = BrowserRepository(
        BrowserDatabase.getInstance(application).browserDao()
    )

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    val shortcuts: StateFlow<List<QuickShortcutEntity>> = repository.shortcuts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadItemEntity>> = repository.downloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val extensions: StateFlow<List<BrowserExtensionEntity>> = repository.extensions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var notificationJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            val live = NewsHeadlinesRepository.fetchLiveHeadlines()
            if (live.isNotEmpty()) {
                _uiState.update { it.copy(liveHeadlines = live) }
            }
        }
    }

    fun selectNewsCategory(category: NewsCategory) {
        _uiState.update { it.copy(selectedNewsCategory = category) }
    }

    fun toggleShowAllHeadlines() {
        _uiState.update { it.copy(showAllHeadlines = !it.showAllHeadlines) }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun setAdBlockEnabled(enabled: Boolean) {
        _uiState.update { it.copy(adBlockEnabled = enabled, reloadTrigger = it.reloadTrigger + 1) }
        if (enabled) {
            showAdBlockedBanner("Ad Blocked: Cleaner pages, Faster browsing")
        } else {
            showAdBlockedBanner("Ad Blocker Paused: Ads & pop-ups allowed on pages")
        }
    }

    fun showAdBlockedBanner(message: String = "Ad Blocked: Cleaner pages, Faster browsing") {
        notificationJob?.cancel()
        _uiState.update { it.copy(adBlockedNotification = message) }
        notificationJob = viewModelScope.launch {
            delay(3200)
            _uiState.update { it.copy(adBlockedNotification = null) }
        }
    }

    fun dismissAdBlockedBanner() {
        notificationJob?.cancel()
        _uiState.update { it.copy(adBlockedNotification = null) }
    }

    fun toggleVpn() {
        _uiState.update { state ->
            val next = !state.vpnEnabled
            state.copy(
                vpnEnabled = next,
                vpnConnectedSinceMs = if (next) System.currentTimeMillis() else 0L,
                reloadTrigger = state.reloadTrigger + 1
            )
        }
    }

    fun selectVpnRegion(region: VpnServerRegion) {
        _uiState.update { state ->
            state.copy(
                selectedVpnRegion = region,
                vpnEnabled = true,
                vpnConnectedSinceMs = if (state.vpnConnectedSinceMs == 0L) System.currentTimeMillis() else state.vpnConnectedSinceMs,
                reloadTrigger = state.reloadTrigger + 1
            )
        }
    }

    fun selectSearchEngine(engine: SearchEngine) {
        _uiState.update { it.copy(selectedSearchEngine = engine) }
    }

    fun openSheet(sheet: ActiveSheet) {
        _uiState.update { it.copy(activeSheet = sheet) }
    }

    fun closeSheet() {
        _uiState.update { it.copy(activeSheet = ActiveSheet.NONE) }
    }

    fun toggleDevTools() {
        _uiState.update { state ->
            val nextVisible = !state.devToolsVisible
            state.copy(
                devToolsVisible = nextVisible,
                pointInspectActive = if (!nextVisible) false else state.pointInspectActive,
                activeSheet = ActiveSheet.NONE
            )
        }
    }

    fun selectDevToolsTab(index: Int) {
        _uiState.update { it.copy(activeDevToolsTab = index) }
    }

    fun togglePointInspect() {
        _uiState.update { it.copy(pointInspectActive = !it.pointInspectActive) }
    }

    /**
     * Converts raw omnibox input (search query, domain name, full URL, or `veyora://` internal route)
     * into a normalized URL and loads it in the active tab.
     */
    fun navigateToInput(rawInput: String) {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return

        val resolvedUrl = when {
            trimmed.startsWith("veyora://", ignoreCase = true) -> trimmed.lowercase()
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            trimmed.contains(" ") -> {
                val encoded = URLEncoder.encode(trimmed, "UTF-8")
                String.format(_uiState.value.selectedSearchEngine.queryTemplate, encoded)
            }
            Patterns.WEB_URL.matcher(trimmed).matches() ||
                (trimmed.contains(".") && !trimmed.endsWith(".")) -> {
                "https://$trimmed"
            }
            else -> {
                val encoded = URLEncoder.encode(trimmed, "UTF-8")
                String.format(_uiState.value.selectedSearchEngine.queryTemplate, encoded)
            }
        }

        updateActiveTab { tab ->
            tab.copy(
                url = resolvedUrl,
                title = if (resolvedUrl == "veyora://home") "Home"
                else if (resolvedUrl == "veyora://adtest") "News Portal Ad-Shield Benchmark"
                else trimmed,
                isLoading = resolvedUrl != "veyora://home",
                adsBlockedInTab = 0
            )
        }
        _uiState.update { it.copy(activeSheet = ActiveSheet.NONE) }
    }

    fun navigateHome() {
        updateActiveTab { tab ->
            tab.copy(
                url = "veyora://home",
                title = "Home",
                isLoading = false,
                progress = 0,
                pointInspectReset = true
            )
        }
        _uiState.update { it.copy(pointInspectActive = false, activeSheet = ActiveSheet.NONE) }
    }

    private fun BrowserTab.copy(
        url: String = this.url,
        title: String = this.title,
        isLoading: Boolean = this.isLoading,
        progress: Int = this.progress,
        pointInspectReset: Boolean = false
    ): BrowserTab = this.copy(
        url = url,
        title = title,
        isLoading = isLoading,
        progress = progress
    )

    fun createNewTab(url: String = "veyora://home", isIncognito: Boolean = false) {
        val newTab = BrowserTab(
            title = if (isIncognito) "Private Tab" else "New Tab",
            url = url,
            isIncognito = isIncognito
        )
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + newTab,
                activeTabId = newTab.id,
                activeSheet = ActiveSheet.NONE
            )
        }
    }

    fun selectTab(tabId: String) {
        _uiState.update { state ->
            if (state.tabs.any { it.id == tabId }) {
                state.copy(activeTabId = tabId, activeSheet = ActiveSheet.NONE)
            } else state
        }
    }

    fun closeTab(tabId: String) {
        _uiState.update { state ->
            val remaining = state.tabs.filterNot { it.id == tabId }
            if (remaining.isEmpty()) {
                val fresh = BrowserTab(title = "Home", url = "veyora://home")
                state.copy(tabs = listOf(fresh), activeTabId = fresh.id)
            } else {
                val nextActive = if (state.activeTabId == tabId) remaining.last().id else state.activeTabId
                state.copy(tabs = remaining, activeTabId = nextActive)
            }
        }
    }

    fun toggleDesktopMode() {
        updateActiveTab { it.copy(isDesktopMode = !it.isDesktopMode) }
        _uiState.update { it.copy(reloadTrigger = it.reloadTrigger + 1) }
    }

    fun toggleReaderMode() {
        updateActiveTab { it.copy(isReaderMode = !it.isReaderMode) }
        _uiState.update { it.copy(reloadTrigger = it.reloadTrigger + 1) }
    }

    fun triggerReload() {
        _uiState.update { it.copy(reloadTrigger = it.reloadTrigger + 1) }
    }

    fun onPageStarted(url: String, title: String?) {
        updateActiveTab { tab ->
            tab.copy(
                url = url,
                title = title?.takeIf { it.isNotBlank() } ?: tab.title,
                isLoading = true,
                progress = 15
            )
        }
    }

    fun onPageProgress(progress: Int) {
        updateActiveTab { tab ->
            tab.copy(
                progress = progress,
                isLoading = progress < 100
            )
        }
    }

    fun onPageFinished(url: String, title: String?, canGoBack: Boolean, canGoForward: Boolean) {
        updateActiveTab { tab ->
            tab.copy(
                url = url,
                title = title?.takeIf { it.isNotBlank() } ?: tab.title,
                isLoading = false,
                progress = 100,
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }
    }

    fun recordBlockedAd(url: String, reason: String, savedBytes: Long, isPopup: Boolean = false) {
        val host = try {
            Uri.parse(url).host ?: "ad-network"
        } catch (_: Exception) {
            "ad-network"
        }
        val netLog = DevToolsNetworkLog(
            url = url,
            host = host,
            method = "GET",
            resourceType = "AD-BLOCKED",
            status = 204,
            isBlockedAd = true,
            blockedReason = reason,
            durationMs = 1
        )
        _uiState.update { state ->
            val currentTabAds = state.activeTab.adsBlockedInTab
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) tab.copy(adsBlockedInTab = tab.adsBlockedInTab + 1)
                else tab
            }
            state.copy(
                totalAdsBlocked = state.totalAdsBlocked + 1,
                totalPopupsBlocked = if (isPopup) state.totalPopupsBlocked + 1 else state.totalPopupsBlocked,
                estimatedDataSavedBytes = state.estimatedDataSavedBytes + savedBytes,
                tabs = updatedTabs,
                networkLogs = (listOf(netLog) + state.networkLogs).take(120)
            )
        }
        // Show the visual notification banner when the first batch of ads on a page is blocked
        if (_uiState.value.activeTab.adsBlockedInTab == 1) {
            showAdBlockedBanner("Ad Blocked: Cleaner pages, Faster browsing")
        }
    }

    fun recordDomAdsRemoved(count: Int, detail: String) {
        if (count <= 0) return
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) tab.copy(adsBlockedInTab = tab.adsBlockedInTab + count)
                else tab
            }
            state.copy(
                totalAdsBlocked = state.totalAdsBlocked + count,
                estimatedDataSavedBytes = state.estimatedDataSavedBytes + (count * 24_000L),
                tabs = updatedTabs,
                consoleLogs = (state.consoleLogs + DevToolsConsoleLog(
                    level = ConsoleLogLevel.INFO,
                    message = "[Veyora AdShield] $detail ($count elements removed)",
                    sourceId = "adshield.js"
                )).takeLast(120)
            )
        }
        showAdBlockedBanner("Ad Blocked: Cleaner pages, Faster browsing ($count in-page ads removed)")
    }

    fun recordAllowedNetworkRequest(url: String, method: String, type: String, status: Int = 200, durationMs: Int = 24) {
        if (url.startsWith("data:") || url.startsWith("blob:")) return
        val host = try {
            Uri.parse(url).host ?: "local"
        } catch (_: Exception) {
            "local"
        }
        val log = DevToolsNetworkLog(
            url = url,
            host = host,
            method = method,
            resourceType = type,
            status = status,
            isBlockedAd = false,
            durationMs = durationMs
        )
        _uiState.update { state ->
            state.copy(networkLogs = (listOf(log) + state.networkLogs).take(120))
        }
    }

    fun clearNetworkLogs() {
        _uiState.update { it.copy(networkLogs = emptyList()) }
    }

    fun appendConsoleLog(level: ConsoleLogLevel, message: String, sourceId: String = "page", line: Int = 1) {
        _uiState.update { state ->
            state.copy(
                consoleLogs = (state.consoleLogs + DevToolsConsoleLog(
                    level = level,
                    message = message,
                    sourceId = sourceId,
                    lineNumber = line
                )).takeLast(150)
            )
        }
    }

    fun clearConsoleLogs() {
        _uiState.update { it.copy(consoleLogs = emptyList()) }
    }

    fun executeConsoleJavaScript(jsExpression: String) {
        val trimmed = jsExpression.trim()
        if (trimmed.isEmpty()) return
        appendConsoleLog(ConsoleLogLevel.LOG, "> $trimmed", "repl")
        _uiState.update { it.copy(pendingJsCommand = trimmed) }
    }

    fun consumePendingJsCommand() {
        _uiState.update { it.copy(pendingJsCommand = null) }
    }

    fun onDomSnapshotReceived(rawJson: String) {
        try {
            val cleaned = if (rawJson.startsWith("\"") && rawJson.endsWith("\"")) {
                org.json.JSONTokener(rawJson).nextValue().toString()
            } else rawJson
            val obj = JSONObject(cleaned)
            val tree = obj.optString("tree", "")
            val html = obj.optString("htmlPreview", "")
            val count = obj.optInt("totalNodes", 0)
            _uiState.update { state ->
                state.copy(
                    domTreeSnapshot = tree.ifBlank { state.domTreeSnapshot },
                    htmlSourcePreview = html,
                    domNodeCount = count
                )
            }
        } catch (_: Exception) {}
    }

    fun onElementInspected(rawJson: String) {
        try {
            val obj = JSONObject(rawJson)
            val stylesObj = obj.optJSONObject("computedStyles")
            val stylesMap = mutableMapOf<String, String>()
            if (stylesObj != null) {
                val keys = stylesObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    stylesMap[k] = stylesObj.optString(k, "")
                }
            }
            val info = InspectedElementInfo(
                tagName = obj.optString("tagName", "div"),
                id = obj.optString("id", ""),
                className = obj.optString("className", ""),
                dimensions = obj.optString("dimensions", ""),
                computedStyles = stylesMap,
                outerHtmlSnippet = obj.optString("outerHtml", "")
            )
            _uiState.update { state ->
                state.copy(
                    inspectedElement = info,
                    devToolsVisible = true,
                    activeDevToolsTab = 0
                )
            }
        } catch (_: Exception) {}
    }

    // Room Persistence Actions
    fun addQuickShortcut(title: String, url: String, colorHex: Long = 0xFF00B8D4) {
        val normalizedUrl = if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("veyora://")) {
            url.trim()
        } else {
            "https://${url.trim()}"
        }
        viewModelScope.launch {
            repository.addShortcut(title, normalizedUrl, colorHex)
        }
    }

    fun deleteQuickShortcut(id: Int) {
        viewModelScope.launch {
            repository.deleteShortcut(id)
        }
    }

    fun toggleBookmarkCurrentPage() {
        val current = _uiState.value.activeTab
        if (current.url == "veyora://home") return
        val existing = bookmarks.value.find { it.url == current.url }
        viewModelScope.launch {
            if (existing != null) {
                repository.deleteBookmark(existing.id)
            } else {
                repository.addBookmark(title = current.title, url = current.url)
            }
        }
    }

    fun addBookmark(title: String, url: String) {
        viewModelScope.launch {
            repository.addBookmark(title, url)
        }
    }

    fun deleteBookmark(id: Int) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
        }
    }

    fun recordDownload(fileName: String, url: String, mimeType: String, sizeBytes: Long) {
        viewModelScope.launch {
            repository.addDownload(fileName, url, mimeType, sizeBytes)
        }
        showAdBlockedBanner("Download Saved: $fileName")
    }

    fun deleteDownload(id: Int) {
        viewModelScope.launch {
            repository.deleteDownload(id)
        }
    }

    fun toggleExtension(extension: BrowserExtensionEntity) {
        viewModelScope.launch {
            repository.toggleExtension(extension)
            _uiState.update { it.copy(reloadTrigger = it.reloadTrigger + 1) }
        }
    }

    fun addCustomExtension(
        name: String,
        description: String,
        matchPattern: String,
        cssCode: String,
        jsCode: String,
        sourceType: String,
        storeUrl: String
    ) {
        viewModelScope.launch {
            repository.addCustomExtension(
                name = name,
                description = description,
                matchPattern = matchPattern,
                cssCode = cssCode,
                jsCode = jsCode,
                sourceType = sourceType,
                storeUrl = storeUrl
            )
            _uiState.update { it.copy(reloadTrigger = it.reloadTrigger + 1) }
        }
    }

    fun deleteExtension(id: String) {
        viewModelScope.launch {
            repository.deleteExtension(id)
            _uiState.update { it.copy(reloadTrigger = it.reloadTrigger + 1) }
        }
    }

    private fun updateActiveTab(transform: (BrowserTab) -> BrowserTab) {
        _uiState.update { state ->
            val updated = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) transform(tab) else tab
            }
            state.copy(tabs = updated)
        }
    }
}
