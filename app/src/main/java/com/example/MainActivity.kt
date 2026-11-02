package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ArchitectureBlueprintSheet
import com.example.ui.components.BookmarksAndDownloadsSheet
import com.example.ui.components.BrowserTopNavigationBar
import com.example.ui.components.DevToolsDrawerPanel
import com.example.ui.components.ExtensionsStoreSheet
import com.example.ui.components.HomepageDashboard
import com.example.ui.components.SettingsAndProfileSheet
import com.example.ui.components.TabsSwitcherSheet
import com.example.ui.components.VeyoraWebViewContainer
import com.example.ui.components.VpnProxySelectorSheet
import com.example.ui.components.WebViewController
import com.example.ui.theme.VeyoraBrowserTheme
import com.example.ui.theme.VeyoraCyan
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialIntentUrl = intent?.dataString
        setContent {
            val viewModel: BrowserViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(initialIntentUrl) {
                if (!initialIntentUrl.isNullOrBlank()) {
                    viewModel.navigateToInput(initialIntentUrl)
                }
            }

            VeyoraBrowserTheme(darkTheme = uiState.isDarkMode) {
                VeyoraBrowserApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun VeyoraBrowserApp(viewModel: BrowserViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shortcuts by viewModel.shortcuts.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val extensions by viewModel.extensions.collectAsStateWithLifecycle()

    val webViewController = remember { WebViewController() }
    val activeTab = uiState.activeTab
    var addressBarText by remember(activeTab.url) {
        mutableStateOf(if (activeTab.url == "veyora://home") "" else activeTab.url)
    }

    val isBookmarked = remember(bookmarks, activeTab.url) {
        bookmarks.any { it.url == activeTab.url }
    }

    // System back button handling
    BackHandler(enabled = uiState.devToolsVisible || !uiState.isHomeVisible) {
        when {
            uiState.devToolsVisible -> viewModel.toggleDevTools()
            activeTab.canGoBack -> webViewController.goBack()
            !uiState.isHomeVisible -> viewModel.navigateHome()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            BrowserTopNavigationBar(
                urlText = addressBarText,
                onUrlTextChange = { addressBarText = it },
                onSubmitUrlOrSearch = { viewModel.navigateToInput(it) },
                canGoBack = activeTab.canGoBack,
                canGoForward = activeTab.canGoForward,
                isLoading = activeTab.isLoading,
                loadProgress = activeTab.progress,
                isHomeVisible = uiState.isHomeVisible,
                adBlockEnabled = uiState.adBlockEnabled,
                adsBlockedInTab = activeTab.adsBlockedInTab,
                totalAdsBlocked = uiState.totalAdsBlocked,
                isBookmarked = isBookmarked,
                vpnEnabled = uiState.vpnEnabled,
                selectedVpnRegion = uiState.selectedVpnRegion,
                selectedSearchEngine = uiState.selectedSearchEngine,
                isDesktopMode = activeTab.isDesktopMode,
                isReaderMode = activeTab.isReaderMode,
                isDarkMode = uiState.isDarkMode,
                openTabsCount = uiState.tabs.size,
                activeExtensionsCount = extensions.count { it.isEnabled },
                adBlockedNotification = uiState.adBlockedNotification,
                onBackClick = {
                    if (activeTab.canGoBack) {
                        webViewController.goBack()
                    } else if (!uiState.isHomeVisible) {
                        viewModel.navigateHome()
                    }
                },
                onForwardClick = {
                    if (activeTab.canGoForward) {
                        webViewController.goForward()
                    }
                },
                onRefreshClick = {
                    if (uiState.isHomeVisible && addressBarText.isNotBlank()) {
                        viewModel.navigateToInput(addressBarText)
                    } else {
                        viewModel.triggerReload()
                    }
                },
                onHomeClick = { viewModel.navigateHome() },
                onNewTabClick = { incognito -> viewModel.createNewTab(isIncognito = incognito) },
                onOpenTabsClick = { viewModel.openSheet(ActiveSheet.TABS) },
                onToggleAdBlock = { viewModel.setAdBlockEnabled(it) },
                onToggleBookmark = { viewModel.toggleBookmarkCurrentPage() },
                onDownloadCurrentPage = {
                    val pageTitle = activeTab.title.replace("[^a-zA-Z0-9._-]".toRegex(), "_").ifBlank { "webpage" }
                    viewModel.recordDownload(
                        fileName = "${pageTitle}.html",
                        url = activeTab.url,
                        mimeType = "text/html",
                        sizeBytes = 148_400L
                    )
                },
                onOpenBookmarksSheet = { viewModel.openSheet(ActiveSheet.BOOKMARKS_DOWNLOADS) },
                onOpenDownloadsSheet = { viewModel.openSheet(ActiveSheet.DOWNLOADS_ONLY) },
                onOpenExtensionsSheet = { viewModel.openSheet(ActiveSheet.EXTENSIONS) },
                onOpenVpnSheet = { viewModel.openSheet(ActiveSheet.VPN_PROXY) },
                onToggleDesktopMode = { viewModel.toggleDesktopMode() },
                onToggleReaderMode = { viewModel.toggleReaderMode() },
                onToggleDevTools = {
                    if (uiState.isHomeVisible) {
                        viewModel.navigateToInput("veyora://adtest")
                    }
                    viewModel.toggleDevTools()
                },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onOpenSettingsSheet = { viewModel.openSheet(ActiveSheet.SETTINGS_PROFILE) },
                onDismissNotification = { viewModel.dismissAdBlockedBanner() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // 1. Home
                NavigationBarItem(
                    selected = uiState.isHomeVisible && uiState.activeSheet == ActiveSheet.NONE && !uiState.devToolsVisible,
                    onClick = { viewModel.navigateHome() },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("nav_home_button")
                )

                // 2. Tabs
                NavigationBarItem(
                    selected = uiState.activeSheet == ActiveSheet.TABS,
                    onClick = { viewModel.openSheet(ActiveSheet.TABS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("${uiState.tabs.size}")
                                }
                            }
                        ) {
                            Icon(Icons.Default.Tab, contentDescription = "Tabs")
                        }
                    },
                    label = { Text("Tabs") },
                    modifier = Modifier.testTag("nav_tabs_button")
                )

                // 3. Developer Tools (Inspect Mode)
                NavigationBarItem(
                    selected = uiState.devToolsVisible,
                    onClick = {
                        if (uiState.isHomeVisible) {
                            viewModel.navigateToInput("veyora://adtest")
                        }
                        viewModel.toggleDevTools()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "Inspect Mode",
                            tint = if (uiState.devToolsVisible) VeyoraCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = { Text("Inspect") },
                    modifier = Modifier.testTag("nav_inspect_button")
                )

                // 4. Bookmarks & Downloads
                NavigationBarItem(
                    selected = uiState.activeSheet == ActiveSheet.BOOKMARKS_DOWNLOADS || uiState.activeSheet == ActiveSheet.DOWNLOADS_ONLY,
                    onClick = { viewModel.openSheet(ActiveSheet.BOOKMARKS_DOWNLOADS) },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks & Downloads") },
                    label = { Text("Saved") },
                    modifier = Modifier.testTag("nav_bookmarks_button")
                )

                // 5. Settings & Profile
                NavigationBarItem(
                    selected = uiState.activeSheet == ActiveSheet.SETTINGS_PROFILE,
                    onClick = { viewModel.openSheet(ActiveSheet.SETTINGS_PROFILE) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings & Menu") },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_settings_button")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Content Area (Homepage Dashboard OR Live WebView)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (uiState.devToolsVisible) 0.54f else 1f)
            ) {
                if (uiState.isHomeVisible) {
                    HomepageDashboard(
                        shortcuts = shortcuts,
                        selectedSearchEngine = uiState.selectedSearchEngine,
                        selectedNewsCategory = uiState.selectedNewsCategory,
                        liveHeadlines = uiState.liveHeadlines,
                        showAllHeadlines = uiState.showAllHeadlines,
                        adBlockEnabled = uiState.adBlockEnabled,
                        totalAdsBlocked = uiState.totalAdsBlocked,
                        estimatedDataSavedBytes = uiState.estimatedDataSavedBytes,
                        vpnEnabled = uiState.vpnEnabled,
                        selectedVpnRegion = uiState.selectedVpnRegion,
                        onNavigateToInput = { viewModel.navigateToInput(it) },
                        onSelectSearchEngine = { viewModel.selectSearchEngine(it) },
                        onSelectNewsCategory = { viewModel.selectNewsCategory(it) },
                        onToggleShowAllHeadlines = { viewModel.toggleShowAllHeadlines() },
                        onAddShortcut = { title, url -> viewModel.addQuickShortcut(title, url) },
                        onDeleteShortcut = { id -> viewModel.deleteQuickShortcut(id) }
                    )
                } else {
                    VeyoraWebViewContainer(
                        url = activeTab.url,
                        adBlockEnabled = uiState.adBlockEnabled,
                        vpnEnabled = uiState.vpnEnabled,
                        vpnRegion = uiState.selectedVpnRegion,
                        isDesktopMode = activeTab.isDesktopMode,
                        isReaderMode = activeTab.isReaderMode,
                        pointInspectActive = uiState.pointInspectActive,
                        reloadTrigger = uiState.reloadTrigger,
                        pendingJsCommand = uiState.pendingJsCommand,
                        enabledExtensions = extensions,
                        controller = webViewController,
                        onPageStarted = { url, title -> viewModel.onPageStarted(url, title) },
                        onPageProgress = { progress -> viewModel.onPageProgress(progress) },
                        onPageFinished = { url, title, back, forward ->
                            viewModel.onPageFinished(url, title, back, forward)
                        },
                        onAdBlocked = { adUrl, reason, savedBytes, isPopup ->
                            viewModel.recordBlockedAd(adUrl, reason, savedBytes, isPopup)
                        },
                        onDomAdsRemoved = { count, detail ->
                            viewModel.recordDomAdsRemoved(count, detail)
                        },
                        onAllowedNetworkRequest = { reqUrl, method, type, status, dur ->
                            viewModel.recordAllowedNetworkRequest(reqUrl, method, type, status, dur)
                        },
                        onConsoleLog = { level, msg, src, line ->
                            viewModel.appendConsoleLog(level, msg, src, line)
                        },
                        onDomSnapshot = { json -> viewModel.onDomSnapshotReceived(json) },
                        onElementInspected = { json -> viewModel.onElementInspected(json) },
                        onJsCommandConsumed = { viewModel.consumePendingJsCommand() },
                        onDownloadRequested = { fileName, dlUrl, mime, size ->
                            viewModel.recordDownload(fileName, dlUrl, mime, size)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Built-in Developer Tools Split-Screen Panel (Inspect Mode)
            if (uiState.devToolsVisible) {
                DevToolsDrawerPanel(
                    activeTab = uiState.activeDevToolsTab,
                    pointInspectActive = uiState.pointInspectActive,
                    domTreeSnapshot = uiState.domTreeSnapshot,
                    htmlSourcePreview = uiState.htmlSourcePreview,
                    domNodeCount = uiState.domNodeCount,
                    inspectedElement = uiState.inspectedElement,
                    consoleLogs = uiState.consoleLogs,
                    networkLogs = uiState.networkLogs,
                    vpnEnabled = uiState.vpnEnabled,
                    vpnRegion = uiState.selectedVpnRegion,
                    adBlockEnabled = uiState.adBlockEnabled,
                    onSelectTab = { viewModel.selectDevToolsTab(it) },
                    onTogglePointInspect = { viewModel.togglePointInspect() },
                    onRefreshDom = {
                        webViewController.refreshDomSnapshot { raw ->
                            viewModel.onDomSnapshotReceived(raw)
                        }
                    },
                    onExecuteJs = { expr -> viewModel.executeConsoleJavaScript(expr) },
                    onClearConsole = { viewModel.clearConsoleLogs() },
                    onClearNetwork = { viewModel.clearNetworkLogs() },
                    onClose = { viewModel.toggleDevTools() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.46f)
                )
            }
        }

        // Modal Bottom Sheets
        when (uiState.activeSheet) {
            ActiveSheet.TABS -> {
                TabsSwitcherSheet(
                    tabs = uiState.tabs,
                    activeTabId = uiState.activeTabId,
                    onSelectTab = { viewModel.selectTab(it) },
                    onCloseTab = { viewModel.closeTab(it) },
                    onCreateTab = { incognito -> viewModel.createNewTab(isIncognito = incognito) },
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.VPN_PROXY -> {
                VpnProxySelectorSheet(
                    vpnEnabled = uiState.vpnEnabled,
                    selectedRegion = uiState.selectedVpnRegion,
                    onToggleVpn = { viewModel.toggleVpn() },
                    onSelectRegion = { region -> viewModel.selectVpnRegion(region) },
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.EXTENSIONS -> {
                ExtensionsStoreSheet(
                    extensions = extensions,
                    onToggleExtension = { viewModel.toggleExtension(it) },
                    onAddCustomExtension = { name, desc, match, css, js, source, url ->
                        viewModel.addCustomExtension(name, desc, match, css, js, source, url)
                    },
                    onDeleteExtension = { viewModel.deleteExtension(it) },
                    onOpenChromeWebStoreInBrowser = {
                        viewModel.navigateToInput("https://chromewebstore.google.com")
                    },
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.BOOKMARKS_DOWNLOADS -> {
                BookmarksAndDownloadsSheet(
                    bookmarks = bookmarks,
                    downloads = downloads,
                    initialTab = 0,
                    onOpenUrl = { viewModel.navigateToInput(it) },
                    onDeleteBookmark = { viewModel.deleteBookmark(it) },
                    onDeleteDownload = { viewModel.deleteDownload(it) },
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.DOWNLOADS_ONLY -> {
                BookmarksAndDownloadsSheet(
                    bookmarks = bookmarks,
                    downloads = downloads,
                    initialTab = 1,
                    onOpenUrl = { viewModel.navigateToInput(it) },
                    onDeleteBookmark = { viewModel.deleteBookmark(it) },
                    onDeleteDownload = { viewModel.deleteDownload(it) },
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.SETTINGS_PROFILE -> {
                SettingsAndProfileSheet(
                    isDarkMode = uiState.isDarkMode,
                    adBlockEnabled = uiState.adBlockEnabled,
                    vpnEnabled = uiState.vpnEnabled,
                    selectedVpnRegion = uiState.selectedVpnRegion,
                    selectedSearchEngine = uiState.selectedSearchEngine,
                    isDesktopMode = activeTab.isDesktopMode,
                    isReaderMode = activeTab.isReaderMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onToggleAdBlock = { viewModel.setAdBlockEnabled(it) },
                    onOpenVpnSheet = { viewModel.openSheet(ActiveSheet.VPN_PROXY) },
                    onSelectSearchEngine = { viewModel.selectSearchEngine(it) },
                    onToggleDesktopMode = { viewModel.toggleDesktopMode() },
                    onToggleReaderMode = { viewModel.toggleReaderMode() },
                    onOpenExtensions = { viewModel.openSheet(ActiveSheet.EXTENSIONS) },
                    onOpenArchitectureGuide = { viewModel.openSheet(ActiveSheet.ARCHITECTURE_GUIDE) },
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.ARCHITECTURE_GUIDE -> {
                ArchitectureBlueprintSheet(
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            ActiveSheet.NONE -> {}
        }
    }
}
