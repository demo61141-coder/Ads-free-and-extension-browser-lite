package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Message
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.BrowserExtensionEntity
import com.example.data.ConsoleLogLevel
import com.example.data.VpnServerRegion
import com.example.engine.VeyoraAdBlocker
import com.example.engine.VeyoraDevToolsEngine
import com.example.engine.VeyoraVpnProxyEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VeyoraJsBridge(
    private val scope: CoroutineScope,
    private val onDomAdsRemovedCallback: (Int, String) -> Unit,
    private val onJsNetworkCallback: (String, String, String, Int, Int) -> Unit,
    private val onElementInspectedCallback: (String) -> Unit
) {
    @JavascriptInterface
    fun onDomAdsRemoved(count: Int, reason: String) {
        scope.launch(Dispatchers.Main) {
            onDomAdsRemovedCallback(count, reason)
        }
    }

    @JavascriptInterface
    fun onJsNetworkLog(url: String, method: String, type: String, status: Int, durationMs: Int) {
        scope.launch(Dispatchers.Main) {
            onJsNetworkCallback(url, method, type, status, durationMs)
        }
    }

    @JavascriptInterface
    fun onElementInspected(payloadJson: String) {
        scope.launch(Dispatchers.Main) {
            onElementInspectedCallback(payloadJson)
        }
    }
}

class WebViewController {
    var webView: WebView? = null

    fun goBack() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        }
    }

    fun goForward() {
        if (webView?.canGoForward() == true) {
            webView?.goForward()
        }
    }

    fun reload() {
        webView?.reload()
    }

    fun refreshDomSnapshot(onResult: (String) -> Unit) {
        webView?.evaluateJavascript(VeyoraDevToolsEngine.buildDomSnapshotScript()) { raw ->
            if (raw != null && raw != "null") {
                onResult(raw)
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VeyoraWebViewContainer(
    url: String,
    adBlockEnabled: Boolean,
    vpnEnabled: Boolean,
    vpnRegion: VpnServerRegion,
    isDesktopMode: Boolean,
    isReaderMode: Boolean,
    pointInspectActive: Boolean,
    reloadTrigger: Int,
    pendingJsCommand: String?,
    enabledExtensions: List<BrowserExtensionEntity>,
    controller: WebViewController,
    onPageStarted: (String, String?) -> Unit,
    onPageProgress: (Int) -> Unit,
    onPageFinished: (String, String?, Boolean, Boolean) -> Unit,
    onAdBlocked: (String, String, Long, Boolean) -> Unit,
    onDomAdsRemoved: (Int, String) -> Unit,
    onAllowedNetworkRequest: (String, String, String, Int, Int) -> Unit,
    onConsoleLog: (ConsoleLogLevel, String, String, Int) -> Unit,
    onDomSnapshot: (String) -> Unit,
    onElementInspected: (String) -> Unit,
    onJsCommandConsumed: () -> Unit,
    onDownloadRequested: (String, String, String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mainScope = remember { CoroutineScope(Dispatchers.Main) }

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            WebView.setWebContentsDebuggingEnabled(true)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                loadsImagesAutomatically = true
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                useWideViewPort = true
                loadWithOverviewMode = true
                // Enable multi-window interception so our WebChromeClient can block pop-up windows
                setSupportMultipleWindows(true)
                javaScriptCanOpenWindowsAutomatically = false
            }

            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)

            addJavascriptInterface(
                VeyoraJsBridge(
                    scope = mainScope,
                    onDomAdsRemovedCallback = onDomAdsRemoved,
                    onJsNetworkCallback = onAllowedNetworkRequest,
                    onElementInspectedCallback = onElementInspected
                ),
                "VeyoraBridge"
            )

            setDownloadListener(DownloadListener { downloadUrl, _, contentDisposition, mimeType, contentLength ->
                val fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimeType)
                onDownloadRequested(
                    fileName,
                    downloadUrl,
                    mimeType ?: "application/octet-stream",
                    if (contentLength > 0) contentLength else 128_000L
                )
            })
        }
    }

    DisposableEffect(webView) {
        controller.webView = webView
        onDispose {
            controller.webView = null
        }
    }

    // Keep WebViewClient and WebChromeClient updated with latest settings
    LaunchedEffect(adBlockEnabled, vpnEnabled, vpnRegion, enabledExtensions, pointInspectActive, isReaderMode) {
        webView.webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val reqUrl = request?.url?.toString() ?: return null
                val method = request.method ?: "GET"

                if (adBlockEnabled) {
                    val decision = VeyoraAdBlocker.evaluateRequest(reqUrl)
                    if (decision.isBlocked) {
                        mainScope.launch {
                            onAdBlocked(reqUrl, decision.reason, decision.estimatedSavedBytes, false)
                        }
                        return VeyoraAdBlocker.createEmptyResourceResponse()
                    }
                }

                // Record allowed request for DevTools Network Inspector
                val pathLower = reqUrl.lowercase()
                val resType = when {
                    request.isForMainFrame -> "DOC"
                    pathLower.contains(".js") -> "SCRIPT"
                    pathLower.contains(".css") -> "STYLE"
                    pathLower.contains(".png") || pathLower.contains(".jpg") ||
                        pathLower.contains(".webp") || pathLower.contains(".svg") -> "IMG"
                    else -> "RES"
                }
                mainScope.launch {
                    onAllowedNetworkRequest(reqUrl, method, resType, 200, 19)
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, pageUrl: String?, favicon: Bitmap?) {
                super.onPageStarted(view, pageUrl, favicon)
                val safeUrl = pageUrl ?: ""
                onPageStarted(safeUrl, view?.title)

                // Inject VPN privacy script & AdBlocker cosmetic filter early
                val vpnScript = VeyoraVpnProxyEngine.buildVpnPrivacyScript(vpnEnabled, vpnRegion)
                if (vpnScript.isNotEmpty()) {
                    view?.evaluateJavascript(vpnScript, null)
                }
                if (adBlockEnabled) {
                    view?.evaluateJavascript(VeyoraAdBlocker.buildCosmeticAdBlockerScript(), null)
                }
            }

            override fun onPageFinished(view: WebView?, pageUrl: String?) {
                super.onPageFinished(view, pageUrl)
                val safeUrl = pageUrl ?: ""

                // 1. Re-apply cosmetic AdBlocker for dynamic news portals (e.g. Prothom Alo)
                if (adBlockEnabled) {
                    view?.evaluateJavascript(VeyoraAdBlocker.buildCosmeticAdBlockerScript(), null)
                }

                // 2. Inject DevTools hooks & Point-Inspect state
                view?.evaluateJavascript(VeyoraDevToolsEngine.buildDevToolsBootstrapScript(), null)
                view?.evaluateJavascript(
                    "if(window.__veyoraSetPointInspect) window.__veyoraSetPointInspect($pointInspectActive);",
                    null
                )

                // 3. Inject all enabled user & Chrome Web Store extensions
                enabledExtensions.filter { it.isEnabled }.forEach { ext ->
                    if (ext.cssCode.isNotBlank()) {
                        val escapedCss = ext.cssCode
                            .replace("\\", "\\\\")
                            .replace("`", "\\`")
                            .replace("$", "\\$")
                        val cssInjector = """
                            (function() {
                                const id = 'veyora-ext-${ext.id}';
                                if (!document.getElementById(id)) {
                                    const s = document.createElement('style');
                                    s.id = id;
                                    s.textContent = `$escapedCss`;
                                    (document.head || document.documentElement).appendChild(s);
                                }
                            })();
                        """.trimIndent()
                        view?.evaluateJavascript(cssInjector, null)
                    }
                    if (ext.jsCode.isNotBlank()) {
                        view?.evaluateJavascript(ext.jsCode, null)
                    }
                }

                // 4. Optional Clean Reader typography mode
                if (isReaderMode) {
                    val readerCss = """
                        (function() {
                            const s = document.createElement('style');
                            s.id = 'veyora-reader-mode';
                            s.textContent = 'body { max-width: 720px !important; margin: 0 auto !important; padding: 18px !important; font-size: 18px !important; line-height: 1.75 !important; } header, nav, footer, aside { display: none !important; }';
                            document.head.appendChild(s);
                        })();
                    """.trimIndent()
                    view?.evaluateJavascript(readerCss, null)
                }

                // 5. Capture DOM Tree snapshot for DevTools Elements tab
                view?.evaluateJavascript(VeyoraDevToolsEngine.buildDomSnapshotScript()) { raw ->
                    if (raw != null && raw != "null") {
                        onDomSnapshot(raw)
                    }
                }

                onPageFinished(
                    safeUrl,
                    view?.title,
                    view?.canGoBack() ?: false,
                    view?.canGoForward() ?: false
                )
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onPageProgress(newProgress)
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                if (consoleMessage != null) {
                    val level = when (consoleMessage.messageLevel()) {
                        ConsoleMessage.MessageLevel.ERROR -> ConsoleLogLevel.ERROR
                        ConsoleMessage.MessageLevel.WARNING -> ConsoleLogLevel.WARN
                        ConsoleMessage.MessageLevel.LOG -> ConsoleLogLevel.LOG
                        else -> ConsoleLogLevel.INFO
                    }
                    onConsoleLog(
                        level,
                        consoleMessage.message() ?: "",
                        consoleMessage.sourceId() ?: "page",
                        consoleMessage.lineNumber()
                    )
                }
                return true
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                if (adBlockEnabled && !isUserGesture) {
                    onAdBlocked("popup://blocked-window", "Blocked Automatic Pop-up Window", 45_000L, true)
                    return false
                }
                return false
            }
        }
    }

    // Update Point-Inspect mode immediately when toggled
    LaunchedEffect(pointInspectActive) {
        webView.evaluateJavascript(
            "if(window.__veyoraSetPointInspect) window.__veyoraSetPointInspect($pointInspectActive);",
            null
        )
    }

    // Update Desktop User-Agent when toggled
    LaunchedEffect(isDesktopMode) {
        webView.settings.userAgentString = if (isDesktopMode) {
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Veyora/1.0"
        } else {
            "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36 Veyora/1.0"
        }
    }

    // Execute live JS commands from DevTools Console REPL
    LaunchedEffect(pendingJsCommand) {
        if (!pendingJsCommand.isNullOrBlank()) {
            webView.evaluateJavascript(pendingJsCommand) { result ->
                onConsoleLog(
                    ConsoleLogLevel.RESULT,
                    "← ${result ?: "undefined"}",
                    "eval",
                    1
                )
            }
            onJsCommandConsumed()
        }
    }

    // Load target URL or built-in News Portal Ad-Blocker Benchmark Sandbox
    LaunchedEffect(url, reloadTrigger) {
        if (url == "veyora://home") return@LaunchedEffect
        if (url == "veyora://adtest") {
            val html = VeyoraAdBlocker.buildAdStressTestHtml(
                adBlockEnabled = adBlockEnabled,
                vpnEnabled = vpnEnabled,
                vpnRegionName = vpnRegion.countryName,
                vpnVirtualIp = vpnRegion.virtualIp
            )
            webView.loadDataWithBaseURL(
                "https://www.prothomalo.com/sandbox-benchmark",
                html,
                "text/html",
                "UTF-8",
                null
            )
        } else {
            val headers = VeyoraVpnProxyEngine.buildTunnelHeaders(vpnEnabled, vpnRegion)
            webView.loadUrl(url, headers)
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}
