package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookmarkEntity
import com.example.data.BrowserExtensionEntity
import com.example.data.BrowserTab
import com.example.data.DownloadItemEntity
import com.example.data.SearchEngine
import com.example.data.VpnServerRegion
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.VeyoraCyan
import com.example.ui.theme.VeyoraEmerald
import com.example.ui.theme.VeyoraIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsSwitcherSheet(
    tabs: List<BrowserTab>,
    activeTabId: String,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onCreateTab: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Open Tabs (${tabs.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onCreateTab(true) },
                        modifier = Modifier.testTag("new_private_tab_button")
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Private")
                    }
                    Button(
                        onClick = { onCreateTab(false) },
                        modifier = Modifier.testTag("new_tab_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New Tab")
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(tabs, key = { it.id }) { tab ->
                    val isSelected = tab.id == activeTabId
                    Card(
                        onClick = { onSelectTab(tab.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (tab.isIncognito) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = VeyoraIndigo
                                        ) {
                                            Text(
                                                "PRIVATE",
                                                fontSize = 9.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = tab.url,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (tab.adsBlockedInTab > 0) {
                                    Text(
                                        text = "🛡️ ${tab.adsBlockedInTab} ads/trackers blocked on this tab",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VeyoraEmerald
                                    )
                                }
                            }

                            IconButton(onClick = { onCloseTab(tab.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close tab"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnProxySelectorSheet(
    vpnEnabled: Boolean,
    selectedRegion: VpnServerRegion,
    onToggleVpn: () -> Unit,
    onSelectRegion: (VpnServerRegion) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnLock,
                        contentDescription = null,
                        tint = VeyoraCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "VPN & Proxy Shield",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (vpnEnabled) {
                                "Connected • Masked IP: ${selectedRegion.virtualIp}"
                            } else {
                                "Disconnected • Select a server to encrypt & mask IP"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (vpnEnabled) VeyoraEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = vpnEnabled,
                    onCheckedChange = { onToggleVpn() },
                    modifier = Modifier.testTag("sheet_vpn_switch")
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(VpnServerRegion.entries) { region ->
                    val isSelected = region == selectedRegion
                    Surface(
                        onClick = { onSelectRegion(region) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) VeyoraCyan else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("vpn_region_item_${region.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = region.flagEmoji, fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = region.countryName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${region.cityName} • IP: ${region.virtualIp}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = VeyoraEmerald.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "${region.basePingMs} ms",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VeyoraEmerald,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = VeyoraCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionsStoreSheet(
    extensions: List<BrowserExtensionEntity>,
    onToggleExtension: (BrowserExtensionEntity) -> Unit,
    onAddCustomExtension: (String, String, String, String, String, String, String) -> Unit,
    onDeleteExtension: (String) -> Unit,
    onOpenChromeWebStoreInBrowser: () -> Unit,
    onDismiss: () -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var extName by remember { mutableStateOf("") }
    var extDesc by remember { mutableStateOf("") }
    var extStoreUrl by remember { mutableStateOf("") }
    var extCss by remember { mutableStateOf("") }
    var extJs by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = VeyoraIndigo,
                        modifier = Modifier.size(26.dp)
                    )
                    Column {
                        Text(
                            text = "Extensions & Chrome Web Store",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage built-in add-ons, Chrome Web Store imports & custom JS/CSS",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action Buttons: Open Chrome Web Store or Add Custom Extension
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showAddForm = !showAddForm },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_custom_extension_form_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (showAddForm) "Hide Input Form" else "Custom Extension Input")
                }

                OutlinedButton(
                    onClick = onOpenChromeWebStoreInBrowser,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_chrome_web_store_button")
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Chrome Web Store")
                }
            }

            if (showAddForm) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Install Custom Extension or Chrome Web Store Add-on",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = extName,
                            onValueChange = { extName = it },
                            label = { Text("Extension Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_ext_name_input")
                        )
                        OutlinedTextField(
                            value = extStoreUrl,
                            onValueChange = { extStoreUrl = it },
                            label = { Text("Chrome Web Store URL / ID (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = extDesc,
                            onValueChange = { extDesc = it },
                            label = { Text("Description") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = extCss,
                            onValueChange = { extCss = it },
                            label = { Text("Custom CSS Rules (e.g. .popup { display:none !important; })") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = extJs,
                            onValueChange = { extJs = it },
                            label = { Text("Custom JS Script (e.g. console.log('Extension loaded'))") },
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_ext_js_input")
                        )
                        Button(
                            onClick = {
                                if (extName.isNotBlank()) {
                                    onAddCustomExtension(
                                        extName,
                                        extDesc,
                                        "*://*/*",
                                        extCss,
                                        extJs,
                                        if (extStoreUrl.isNotBlank()) "Chrome Web Store" else "Custom Script",
                                        extStoreUrl
                                    )
                                    extName = ""
                                    extDesc = ""
                                    extStoreUrl = ""
                                    extCss = ""
                                    extJs = ""
                                    showAddForm = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_custom_extension_button")
                        ) {
                            Text("Install & Enable Extension")
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(extensions, key = { it.id }) { ext ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (ext.isEnabled) VeyoraIndigo.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = VeyoraIndigo.copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = ext.sourceType.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = VeyoraIndigo,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "v${ext.version}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = ext.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (ext.sourceType != "Built-in") {
                                        IconButton(onClick = { onDeleteExtension(ext.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove extension",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = ext.isEnabled,
                                        onCheckedChange = { onToggleExtension(ext) }
                                    )
                                }
                            }

                            Text(
                                text = ext.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksAndDownloadsSheet(
    bookmarks: List<BookmarkEntity>,
    downloads: List<DownloadItemEntity>,
    initialTab: Int = 0,
    onOpenUrl: (String) -> Unit,
    onDeleteBookmark: (Int) -> Unit,
    onDeleteDownload: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Bookmarks (${bookmarks.size})") },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Downloads (${downloads.size})") },
                    icon = { Icon(Icons.Default.Download, contentDescription = null) }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (bookmarks.isEmpty()) {
                        item {
                            Text(
                                text = "No saved bookmarks yet. Tap the Bookmark icon in the address bar on any webpage to save it.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(bookmarks, key = { it.id }) { bm ->
                            Card(
                                onClick = { onOpenUrl(bm.url) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = bm.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = bm.url,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(onClick = { onDeleteBookmark(bm.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete bookmark")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (downloads.isEmpty()) {
                        item {
                            Text(
                                text = "No downloads yet. Any file downloaded during browsing appears here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(downloads, key = { it.id }) { dl ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dl.fileName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${dl.sizeBytes / 1024} KB • ${dl.status}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = VeyoraEmerald
                                        )
                                    }
                                    IconButton(onClick = { onDeleteDownload(dl.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove download")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAndProfileSheet(
    isDarkMode: Boolean,
    adBlockEnabled: Boolean,
    vpnEnabled: Boolean,
    selectedVpnRegion: VpnServerRegion,
    selectedSearchEngine: SearchEngine,
    isDesktopMode: Boolean,
    isReaderMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onToggleAdBlock: (Boolean) -> Unit,
    onOpenVpnSheet: () -> Unit,
    onSelectSearchEngine: (SearchEngine) -> Unit,
    onToggleDesktopMode: () -> Unit,
    onToggleReaderMode: () -> Unit,
    onOpenExtensions: () -> Unit,
    onOpenArchitectureGuide: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                Text(
                    text = "Veyora Browser Settings & Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // 1. Block ads & pop-ups Toggle
            item {
                SettingToggleRow(
                    icon = Icons.Default.Security,
                    title = "Block ads & pop-ups",
                    subtitle = "Ad Blocked: Cleaner pages, Faster browsing (Prothom Alo + Global)",
                    checked = adBlockEnabled,
                    onCheckedChange = onToggleAdBlock,
                    testTag = "settings_ad_block_switch"
                )
            }

            // 2. Dark / Light Theme Toggle
            item {
                SettingToggleRow(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode Interface",
                    subtitle = if (isDarkMode) "Obsidian Cyber-Shield Dark active" else "Daylight Crisp Light active",
                    checked = isDarkMode,
                    onCheckedChange = { onToggleDarkMode() },
                    testTag = "settings_dark_mode_switch"
                )
            }

            // 3. Desktop Site & Clean Reader Mode
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = onToggleDesktopMode,
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDesktopMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.DesktopWindows, contentDescription = null)
                            Column {
                                Text("Desktop Site", style = MaterialTheme.typography.labelLarge)
                                Text(if (isDesktopMode) "Enabled" else "Mobile View", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Surface(
                        onClick = onToggleReaderMode,
                        shape = RoundedCornerShape(14.dp),
                        color = if (isReaderMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null)
                            Column {
                                Text("Reader Mode", style = MaterialTheme.typography.labelLarge)
                                Text(if (isReaderMode) "Distraction-Free" else "Standard", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // 4. Default Search Engine Customization
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Default Search Engine",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        SearchEngine.entries.forEach { engine ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectSearchEngine(engine) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(engine.badgeColorHex)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = engine.shortCode,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(text = engine.displayName, style = MaterialTheme.typography.bodyMedium)
                                }
                                RadioButton(
                                    selected = engine == selectedSearchEngine,
                                    onClick = { onSelectSearchEngine(engine) }
                                )
                            }
                        }
                    }
                }
            }

            // 5. Quick Links to VPN Region, Extensions & Build Blueprint
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenVpnSheet,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("VPN (${selectedVpnRegion.countryCode})")
                    }
                    OutlinedButton(
                        onClick = onOpenExtensions,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Extension, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Extensions")
                    }
                }
            }

            item {
                Button(
                    onClick = onOpenArchitectureGuide,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_open_blueprint_button")
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Modular Code Snippets & Build Guide")
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchitectureBlueprintSheet(
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                Text(
                    text = "Veyora Browser — Modular Architecture & Build Guide",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Complete implementation overview for <1MB core WebView engine, AdShield, VPN headers, and DevTools.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                BlueprintCard(
                    title = "1. Network & DOM Ad-Blocker (Prothom Alo + Global)",
                    code = """
// WebViewClient.shouldInterceptRequest (Socket-level 204 Zero-Byte Blocker)
override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
    val url = request?.url?.toString() ?: return null
    if (adBlockEnabled && VeyoraAdBlocker.evaluateRequest(url).isBlocked) {
        return VeyoraAdBlocker.createEmptyResourceResponse() // 204 No Content
    }
    return super.shouldInterceptRequest(view, request)
}

// MutationObserver DOM Cleaner for News Portals (Prothom Alo DFP & AdSense slots)
const adSelectors = ['ins.adsbygoogle', '[id^="div-gpt-ad"]', '.dfp-ad', '[class*="ad-slot"]'];
new MutationObserver(() => {
    adSelectors.forEach(sel => document.querySelectorAll(sel).forEach(el => el.remove()));
}).observe(document.documentElement, { childList: true, subtree: true });
                    """.trimIndent()
                )
            }

            item {
                BlueprintCard(
                    title = "2. VPN & Proxy Regional Header + WebRTC Shield",
                    code = """
// Regional Proxy Headers in WebView.loadUrl(url, headers)
val headers = mapOf(
    "X-Forwarded-For" to region.virtualIp,
    "CF-IPCountry" to region.countryCode,
    "Accept-Language" to region.acceptLanguage,
    "DNT" to "1", "Sec-GPC" to "1"
)
webView.loadUrl(targetUrl, headers)
                    """.trimIndent()
                )
            }

            item {
                BlueprintCard(
                    title = "3. How to Build & Optimize APK (<1MB Core Target)",
                    code = """
# 1. Compile Debug APK in AI Studio / Gradle CLI:
gradle assembleDebug

# 2. Build R8-Minified Release APK / App Bundle:
gradle assembleRelease

# Key APK Size Optimizations Applied:
# - Uses system Android WebView (Chromium engine shared with OS = 0 MB engine bloat)
# - Unused heavy SDKs commented out in app/build.gradle.kts
# - Vector drawables & Canvas graphics instead of bulky raster bitmaps
                    """.trimIndent()
                )
            }
        }
    }
}

@Composable
private fun BlueprintCard(title: String, code: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = VeyoraCyan, fontWeight = FontWeight.Bold)
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Text(
                text = code,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = Color(0xFFE2E8F0)
            )
        }
    }
}
