package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SearchEngine
import com.example.data.VpnServerRegion
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.VeyoraCyan
import com.example.ui.theme.VeyoraEmerald
import com.example.ui.theme.VeyoraIndigo

/**
 * Chrome-style Browser Top Navigation Bar with:
 * - Back, Forward, Refresh buttons
 * - URL / Search Omnibox input field
 * - Open Tabs counter button
 * - Chrome-style Three-Dot (`⋮`) Overflow Menu containing Ad Blocker, Extensions,
 *   Downloads, Desktop Site, Bookmarks, VPN & Proxy, Inspect Mode (DevTools), and Settings!
 */
@Composable
fun BrowserTopNavigationBar(
    urlText: String,
    onUrlTextChange: (String) -> Unit,
    onSubmitUrlOrSearch: (String) -> Unit,
    canGoBack: Boolean,
    canGoForward: Boolean,
    isLoading: Boolean,
    loadProgress: Int,
    isHomeVisible: Boolean,
    adBlockEnabled: Boolean,
    adsBlockedInTab: Int,
    totalAdsBlocked: Int,
    isBookmarked: Boolean,
    vpnEnabled: Boolean,
    selectedVpnRegion: VpnServerRegion,
    selectedSearchEngine: SearchEngine,
    isDesktopMode: Boolean,
    isReaderMode: Boolean,
    isDarkMode: Boolean,
    openTabsCount: Int,
    activeExtensionsCount: Int,
    adBlockedNotification: String?,
    onBackClick: () -> Unit,
    onForwardClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onHomeClick: () -> Unit,
    onNewTabClick: (Boolean) -> Unit,
    onOpenTabsClick: () -> Unit,
    onToggleAdBlock: (Boolean) -> Unit,
    onToggleBookmark: () -> Unit,
    onDownloadCurrentPage: () -> Unit,
    onOpenBookmarksSheet: () -> Unit,
    onOpenDownloadsSheet: () -> Unit,
    onOpenExtensionsSheet: () -> Unit,
    onOpenVpnSheet: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onToggleReaderMode: () -> Unit,
    onToggleDevTools: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onOpenSettingsSheet: () -> Unit,
    onDismissNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var showThreeDotMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 1. Back Button
            IconButton(
                onClick = onBackClick,
                enabled = canGoBack || !isHomeVisible,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("toolbar_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate Back",
                    tint = if (canGoBack || !isHomeVisible) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            // 2. Forward Button
            IconButton(
                onClick = onForwardClick,
                enabled = canGoForward,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("toolbar_forward_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Navigate Forward",
                    tint = if (canGoForward) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            // 3. Refresh Button
            IconButton(
                onClick = onRefreshClick,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("toolbar_refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Page",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 4. URL / Search Text Input Field
            OutlinedTextField(
                value = urlText,
                onValueChange = onUrlTextChange,
                placeholder = {
                    Text(
                        text = "Search ${selectedSearchEngine.displayName} or type URL",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Surface(
                        onClick = { onToggleAdBlock(!adBlockEnabled) },
                        shape = CircleShape,
                        color = if (adBlockEnabled) {
                            VeyoraEmerald.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .testTag("omnibox_ad_shield_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Ad Blocker Status",
                                tint = if (adBlockEnabled) VeyoraEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            if (adsBlockedInTab > 0) {
                                Text(
                                    text = "$adsBlockedInTab",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VeyoraEmerald
                                )
                            }
                        }
                    }
                },
                trailingIcon = {
                    if (urlText.isNotEmpty()) {
                        IconButton(
                            onClick = { onUrlTextChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear URL",
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        focusManager.clearFocus()
                        onSubmitUrlOrSearch(urlText)
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("top_omnibox_input")
            )

            // 5. Chrome-style Tab Counter Box
            Surface(
                onClick = onOpenTabsClick,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(32.dp)
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .testTag("toolbar_tabs_counter_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$openTabsCount",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 6. Chrome-style Three-Dot (⋮) Overflow Menu Button
            Box {
                IconButton(
                    onClick = { showThreeDotMenu = true },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("chrome_three_dot_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options (Chrome 3-dot menu)",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Chrome-Style Three-Dot Popup Menu containing ALL Features
                DropdownMenu(
                    expanded = showThreeDotMenu,
                    onDismissRequest = { showThreeDotMenu = false },
                    modifier = Modifier
                        .width(295.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .testTag("chrome_three_dot_dropdown")
                ) {
                    // Top Quick-Action Icon Strip (Forward, Reload, Bookmark, Download, Home)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                showThreeDotMenu = false
                                onForwardClick()
                            },
                            enabled = canGoForward
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Forward"
                            )
                        }
                        IconButton(
                            onClick = {
                                showThreeDotMenu = false
                                onToggleBookmark()
                            }
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark page",
                                tint = if (isBookmarked) VeyoraCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                showThreeDotMenu = false
                                onDownloadCurrentPage()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download page"
                            )
                        }
                        IconButton(
                            onClick = {
                                showThreeDotMenu = false
                                onHomeClick()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        }
                        IconButton(
                            onClick = {
                                showThreeDotMenu = false
                                onRefreshClick()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload"
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // New Tab & New Incognito Tab
                    DropdownMenuItem(
                        text = { Text("New tab") },
                        leadingIcon = { Icon(Icons.Default.AddBox, contentDescription = null) },
                        onClick = {
                            showThreeDotMenu = false
                            onNewTabClick(false)
                        },
                        modifier = Modifier.testTag("menu_item_new_tab")
                    )

                    DropdownMenuItem(
                        text = { Text("New Incognito tab") },
                        leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = VeyoraIndigo) },
                        onClick = {
                            showThreeDotMenu = false
                            onNewTabClick(true)
                        },
                        modifier = Modifier.testTag("menu_item_incognito_tab")
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Block ads & pop-ups (Ad Blocker) inside 3-dot menu
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "Block ads & pop-ups",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (adBlockEnabled) "Active • $totalAdsBlocked ads blocked"
                                    else "Off • Tap to enable AdShield",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (adBlockEnabled) VeyoraEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (adBlockEnabled) VeyoraEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            Switch(
                                checked = adBlockEnabled,
                                onCheckedChange = { onToggleAdBlock(it) }
                            )
                        },
                        onClick = { onToggleAdBlock(!adBlockEnabled) },
                        modifier = Modifier.testTag("menu_item_ad_blocker")
                    )

                    // Extensions & Chrome Web Store inside 3-dot menu
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("Extensions", fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "$activeExtensionsCount active • Chrome Web Store & Custom JS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = null,
                                tint = VeyoraIndigo
                            )
                        },
                        onClick = {
                            showThreeDotMenu = false
                            onOpenExtensionsSheet()
                        },
                        modifier = Modifier.testTag("menu_item_extensions")
                    )

                    // Downloads inside 3-dot menu
                    DropdownMenuItem(
                        text = { Text("Downloads") },
                        leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                        onClick = {
                            showThreeDotMenu = false
                            onOpenDownloadsSheet()
                        },
                        modifier = Modifier.testTag("menu_item_downloads")
                    )

                    // Bookmarks inside 3-dot menu
                    DropdownMenuItem(
                        text = { Text("Bookmarks") },
                        leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                        onClick = {
                            showThreeDotMenu = false
                            onOpenBookmarksSheet()
                        },
                        modifier = Modifier.testTag("menu_item_bookmarks")
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Desktop site (with Chrome-style Checkbox!)
                    DropdownMenuItem(
                        text = { Text("Desktop site", fontWeight = FontWeight.Medium) },
                        leadingIcon = { Icon(Icons.Default.DesktopWindows, contentDescription = null) },
                        trailingIcon = {
                            Checkbox(
                                checked = isDesktopMode,
                                onCheckedChange = {
                                    showThreeDotMenu = false
                                    onToggleDesktopMode()
                                }
                            )
                        },
                        onClick = {
                            showThreeDotMenu = false
                            onToggleDesktopMode()
                        },
                        modifier = Modifier.testTag("menu_item_desktop_site")
                    )

                    // VPN & Proxy Region Switcher inside 3-dot menu
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("VPN & Proxy Shield", fontWeight = FontWeight.Medium)
                                Text(
                                    text = if (vpnEnabled) {
                                        "${selectedVpnRegion.flagEmoji} ${selectedVpnRegion.countryName} (${selectedVpnRegion.virtualIp})"
                                    } else {
                                        "Disconnected • Tap to choose server"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (vpnEnabled) VeyoraCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.VpnLock,
                                contentDescription = null,
                                tint = if (vpnEnabled) VeyoraCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = {
                            showThreeDotMenu = false
                            onOpenVpnSheet()
                        },
                        modifier = Modifier.testTag("menu_item_vpn_proxy")
                    )

                    // Developer Tools (Inspect Mode) inside 3-dot menu
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("Developer Tools (Inspect)")
                                Text(
                                    text = "DOM Tree, Console & Network",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = VeyoraCyan
                            )
                        },
                        onClick = {
                            showThreeDotMenu = false
                            onToggleDevTools()
                        },
                        modifier = Modifier.testTag("menu_item_devtools")
                    )

                    // Reader Mode inside 3-dot menu
                    DropdownMenuItem(
                        text = { Text("Clean Reader mode") },
                        leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                        trailingIcon = {
                            Checkbox(
                                checked = isReaderMode,
                                onCheckedChange = {
                                    showThreeDotMenu = false
                                    onToggleReaderMode()
                                }
                            )
                        },
                        onClick = {
                            showThreeDotMenu = false
                            onToggleReaderMode()
                        }
                    )

                    // Dark Mode inside 3-dot menu
                    DropdownMenuItem(
                        text = { Text("Dark theme") },
                        leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                        trailingIcon = {
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { onToggleDarkMode() }
                            )
                        },
                        onClick = { onToggleDarkMode() }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Settings & Search Engine Customization
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("Settings & Search Engine")
                                Text(
                                    text = "Engine: ${selectedSearchEngine.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        onClick = {
                            showThreeDotMenu = false
                            onOpenSettingsSheet()
                        },
                        modifier = Modifier.testTag("menu_item_settings")
                    )
                }
            }
        }

        // Loading Progress Indicator
        if (isLoading) {
            LinearProgressIndicator(
                progress = { (loadProgress / 100f).coerceIn(0.05f, 1f) },
                color = VeyoraCyan,
                trackColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
            )
        }

        // Ad Blocked Visual Notification Banner ("Ad Blocked: Cleaner pages, Faster browsing")
        AnimatedVisibility(
            visible = adBlockedNotification != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = Color(0xFF064E3B),
                contentColor = Color(0xFFA7F3D0),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ad_blocked_notification_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = VeyoraEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = adBlockedNotification ?: "Ad Blocked: Cleaner pages, Faster browsing",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = onDismissNotification,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss notification",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
