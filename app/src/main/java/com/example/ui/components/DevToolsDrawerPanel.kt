package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConsoleLogLevel
import com.example.data.DevToolsConsoleLog
import com.example.data.DevToolsNetworkLog
import com.example.data.InspectedElementInfo
import com.example.data.VpnServerRegion
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.VeyoraAmber
import com.example.ui.theme.VeyoraCoral
import com.example.ui.theme.VeyoraCyan
import com.example.ui.theme.VeyoraEmerald

@Composable
fun DevToolsDrawerPanel(
    activeTab: Int,
    pointInspectActive: Boolean,
    domTreeSnapshot: String,
    htmlSourcePreview: String,
    domNodeCount: Int,
    inspectedElement: InspectedElementInfo,
    consoleLogs: List<DevToolsConsoleLog>,
    networkLogs: List<DevToolsNetworkLog>,
    vpnEnabled: Boolean,
    vpnRegion: VpnServerRegion,
    adBlockEnabled: Boolean,
    onSelectTab: (Int) -> Unit,
    onTogglePointInspect: () -> Unit,
    onRefreshDom: () -> Unit,
    onExecuteJs: (String) -> Unit,
    onClearConsole: () -> Unit,
    onClearNetwork: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0B101E),
        contentColor = Color(0xFFF1F5F9),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        tonalElevation = 10.dp,
        shadowElevation = 14.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = VeyoraCyan.copy(alpha = 0.4f),
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            )
            .testTag("devtools_drawer_panel")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111827))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = VeyoraCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Veyora DevTools",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = VeyoraCyan.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = "$domNodeCount DOM nodes",
                            style = MaterialTheme.typography.labelSmall,
                            color = VeyoraCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Point-and-Tap Element Inspector Toggle Button
                    Surface(
                        onClick = onTogglePointInspect,
                        shape = RoundedCornerShape(50),
                        color = if (pointInspectActive) VeyoraCyan else Color(0xFF1E293B),
                        modifier = Modifier.testTag("devtools_point_inspect_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = "Point Inspect",
                                tint = if (pointInspectActive) Color(0xFF090D16) else VeyoraCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (pointInspectActive) "Inspecting Tap…" else "Tap-Inspect",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (pointInspectActive) Color(0xFF090D16) else VeyoraCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefreshDom,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh DOM",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("devtools_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close DevTools",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // DevTools Tabs: Elements | Console | Network | Info
            val tabTitles = listOf(
                "Elements",
                "Console (${consoleLogs.size})",
                "Network (${networkLogs.size})",
                "Security"
            )
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color(0xFF0F172A),
                contentColor = VeyoraCyan
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { onSelectTab(index) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.testTag("devtools_tab_$index")
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (activeTab) {
                    0 -> ElementsInspectorView(
                        inspectedElement = inspectedElement,
                        domTreeSnapshot = domTreeSnapshot,
                        htmlSourcePreview = htmlSourcePreview,
                        pointInspectActive = pointInspectActive
                    )
                    1 -> ConsoleInspectorView(
                        logs = consoleLogs,
                        onExecuteJs = onExecuteJs,
                        onClear = onClearConsole
                    )
                    2 -> NetworkInspectorView(
                        logs = networkLogs,
                        onClear = onClearNetwork
                    )
                    3 -> SecurityEnvInspectorView(
                        vpnEnabled = vpnEnabled,
                        vpnRegion = vpnRegion,
                        adBlockEnabled = adBlockEnabled,
                        domNodeCount = domNodeCount
                    )
                }
            }
        }
    }
}

@Composable
private fun ElementsInspectorView(
    inspectedElement: InspectedElementInfo,
    domTreeSnapshot: String,
    htmlSourcePreview: String,
    pointInspectActive: Boolean
) {
    var viewMode by remember { mutableIntStateOf(0) } // 0: Inspected + DOM Tree, 1: Raw HTML

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (pointInspectActive) {
                    "👆 Tap any element on the webpage above to inspect its styles & HTML"
                } else {
                    "Element & DOM Structure"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (pointInspectActive) VeyoraCyan else Color(0xFF94A3B8),
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChipButton(
                    label = "DOM Tree",
                    selected = viewMode == 0,
                    onClick = { viewMode = 0 }
                )
                FilterChipButton(
                    label = "HTML Source",
                    selected = viewMode == 1,
                    onClick = { viewMode = 1 }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Selected Element Box
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val selectorText = buildString {
                                append("<")
                                append(inspectedElement.tagName)
                                if (inspectedElement.id.isNotBlank()) append("#${inspectedElement.id}")
                                if (inspectedElement.className.isNotBlank()) {
                                    append(".")
                                    append(
                                        inspectedElement.className.trim()
                                            .split("\\s+".toRegex())
                                            .take(2)
                                            .joinToString(".")
                                    )
                                }
                                append(">")
                            }
                            Text(
                                text = selectorText,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = VeyoraCyan
                            )
                            Text(
                                text = inspectedElement.dimensions,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                color = VeyoraEmerald
                            )
                        }

                        if (inspectedElement.computedStyles.isNotEmpty()) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Text(
                                text = "Computed CSS Styles:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                            val styleSummary = inspectedElement.computedStyles.entries
                                .joinToString(" • ") { "${it.key}: ${it.value}" }
                            Text(
                                text = styleSummary,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Text(
                            text = inspectedElement.outerHtmlSnippet,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Full DOM Hierarchy or Raw HTML Source
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF090D16),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (viewMode == 0) "LIVE DOM HIERARCHY" else "RAW DOCUMENT OUTERHTML",
                            style = MaterialTheme.typography.labelSmall,
                            color = VeyoraCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (viewMode == 0) domTreeSnapshot else htmlSourcePreview.ifBlank { domTreeSnapshot },
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsoleInspectorView(
    logs: List<DevToolsConsoleLog>,
    onExecuteJs: (String) -> Unit,
    onClear: () -> Unit
) {
    var jsInput by remember { mutableStateOf("") }
    val quickSnippets = listOf(
        "document.title",
        "document.querySelectorAll('*').length",
        "navigator.userAgent",
        "window.location.href",
        "performance.now()"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Snippet Chips + Clear Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickSnippets.forEach { snippet ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF1E293B),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onExecuteJs(snippet) }
                    ) {
                        Text(
                            text = snippet,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            color = VeyoraCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onClear,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Console",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Console Log Stream
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF090D16))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(logs, key = { it.id }) { item ->
                val color = when (item.level) {
                    ConsoleLogLevel.ERROR -> VeyoraCoral
                    ConsoleLogLevel.WARN -> VeyoraAmber
                    ConsoleLogLevel.RESULT -> VeyoraEmerald
                    ConsoleLogLevel.INFO -> VeyoraCyan
                    ConsoleLogLevel.LOG -> Color(0xFFE2E8F0)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.level.name.take(3),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = item.message,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Live JavaScript REPL Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = jsInput,
                onValueChange = { jsInput = it },
                placeholder = {
                    Text(
                        "Run JavaScript on page (e.g. document.body.style.zoom='1.1')",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (jsInput.isNotBlank()) {
                            onExecuteJs(jsInput)
                            jsInput = ""
                        }
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("devtools_console_input")
            )

            Button(
                onClick = {
                    if (jsInput.isNotBlank()) {
                        onExecuteJs(jsInput)
                        jsInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VeyoraCyan),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                modifier = Modifier.testTag("devtools_console_run_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Execute JS",
                    tint = Color(0xFF090D16),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun NetworkInspectorView(
    logs: List<DevToolsNetworkLog>,
    onClear: () -> Unit
) {
    var filterMode by remember { mutableIntStateOf(0) } // 0: All, 1: Blocked Ads, 2: Allowed

    val filtered = remember(logs, filterMode) {
        when (filterMode) {
            1 -> logs.filter { it.isBlockedAd }
            2 -> logs.filter { !it.isBlockedAd }
            else -> logs
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChipButton(
                    label = "All (${logs.size})",
                    selected = filterMode == 0,
                    onClick = { filterMode = 0 }
                )
                FilterChipButton(
                    label = "Blocked Ads (${logs.count { it.isBlockedAd }})",
                    selected = filterMode == 1,
                    onClick = { filterMode = 1 }
                )
                FilterChipButton(
                    label = "Allowed (${logs.count { !it.isBlockedAd }})",
                    selected = filterMode == 2,
                    onClick = { filterMode = 2 }
                )
            }

            IconButton(
                onClick = onClear,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Network Logs",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF090D16))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = "No network entries matching this filter yet. Navigate to any site to inspect requests.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                items(filtered, key = { it.id }) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF111827))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (entry.isBlockedAd) {
                                VeyoraCoral.copy(alpha = 0.2f)
                            } else {
                                VeyoraEmerald.copy(alpha = 0.18f)
                            }
                        ) {
                            Text(
                                text = if (entry.isBlockedAd) "BLOCKED" else "${entry.status} ${entry.resourceType}",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (entry.isBlockedAd) VeyoraCoral else VeyoraEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.url,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp,
                                color = Color(0xFFE2E8F0),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (entry.isBlockedAd && entry.blockedReason.isNotBlank()) {
                                Text(
                                    text = entry.blockedReason,
                                    fontSize = 10.sp,
                                    color = VeyoraCoral
                                )
                            }
                        }

                        Text(
                            text = "${entry.durationMs}ms",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityEnvInspectorView(
    vpnEnabled: Boolean,
    vpnRegion: VpnServerRegion,
    adBlockEnabled: Boolean,
    domNodeCount: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            EnvRow("AdShield Interceptor", if (adBlockEnabled) "Active (Socket 204 + DOM MutationObserver)" else "Paused")
        }
        item {
            EnvRow("VPN Proxy Tunnel", if (vpnEnabled) "${vpnRegion.countryName} (${vpnRegion.virtualIp})" else "Direct Device IP")
        }
        item {
            EnvRow("WebRTC IP Leak Protection", if (vpnEnabled) "Enforced (ICE Relay Only)" else "Standard")
        }
        item {
            EnvRow("Third-Party Cookies", "Blocked by Default")
        }
        item {
            EnvRow("Active Page DOM Nodes", "$domNodeCount elements")
        }
    }
}

@Composable
private fun EnvRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF111827))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = VeyoraCyan,
            fontFamily = JetBrainsMonoFamily
        )
    }
}

@Composable
private fun FilterChipButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) VeyoraCyan else Color(0xFF1E293B)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Color(0xFF090D16) else Color(0xFFCBD5E1),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
