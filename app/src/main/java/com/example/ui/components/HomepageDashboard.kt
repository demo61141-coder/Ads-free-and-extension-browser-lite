package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NewsArticleItem
import com.example.data.NewsCategory
import com.example.data.NewsHeadlinesRepository
import com.example.data.QuickShortcutEntity
import com.example.data.SearchEngine
import com.example.data.VpnServerRegion
import com.example.ui.theme.VeyoraCyan
import com.example.ui.theme.VeyoraEmerald

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun HomepageDashboard(
    shortcuts: List<QuickShortcutEntity>,
    selectedSearchEngine: SearchEngine,
    selectedNewsCategory: NewsCategory,
    liveHeadlines: List<NewsArticleItem>,
    showAllHeadlines: Boolean,
    adBlockEnabled: Boolean,
    totalAdsBlocked: Int,
    estimatedDataSavedBytes: Long,
    vpnEnabled: Boolean,
    selectedVpnRegion: VpnServerRegion,
    onNavigateToInput: (String) -> Unit,
    onSelectSearchEngine: (SearchEngine) -> Unit,
    onSelectNewsCategory: (NewsCategory) -> Unit,
    onToggleShowAllHeadlines: () -> Unit,
    onAddShortcut: (String, String) -> Unit,
    onDeleteShortcut: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddShortcutDialog by remember { mutableStateOf(false) }
    var showEngineSelector by remember { mutableStateOf(false) }

    val articlesForCategory = remember(selectedNewsCategory, liveHeadlines) {
        NewsHeadlinesRepository.getArticlesForCategory(selectedNewsCategory, liveHeadlines)
    }
    val featuredHeroArticles = remember(articlesForCategory) {
        articlesForCategory.filter { it.isFeaturedHero }.ifEmpty { articlesForCategory.take(2) }
    }
    val standardArticles = remember(articlesForCategory, showAllHeadlines) {
        val list = articlesForCategory
        if (showAllHeadlines) list else list.take(5)
    }

    val savedKb = estimatedDataSavedBytes / 1024
    val savedFormatted = if (savedKb >= 1024) {
        String.format("%.1f MB", savedKb / 1024f)
    } else {
        "$savedKb KB"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Lightweight (<1MB) & Headline Badges Row (from Reference Screenshots)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Headline" Pill Badge (Screenshot 1)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Headline",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Headline",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "500+ publishers",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // "Lightweight (<1MB)" Pill Badge (Screenshot 2)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Lightweight",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Lightweight",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Size <1MB • $savedFormatted saved",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. Smart Search & URL Input Box (matching Screenshot 2 rounded search bar)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search Engine Pill Selector
                        Surface(
                            shape = CircleShape,
                            color = Color(selectedSearchEngine.badgeColorHex).copy(alpha = 0.18f),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { showEngineSelector = !showEngineSelector }
                                .testTag("search_engine_selector_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = selectedSearchEngine.shortCode,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(selectedSearchEngine.badgeColorHex),
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Select Search Engine",
                                    tint = Color(selectedSearchEngine.badgeColorHex),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "Search ${selectedSearchEngine.displayName} or type URL…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    if (searchQuery.isNotBlank()) {
                                        onNavigateToInput(searchQuery)
                                    }
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_search_input")
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    onNavigateToInput(searchQuery)
                                } else {
                                    onNavigateToInput(selectedSearchEngine.homeUrl)
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("home_search_go_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Go",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Inline Search Engine Selector Row
                AnimatedVisibility(
                    visible = showEngineSelector,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SearchEngine.entries.forEach { engine ->
                            val isSelected = engine == selectedSearchEngine
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) {
                                    Color(engine.badgeColorHex)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        onSelectSearchEngine(engine)
                                        showEngineSelector = false
                                    }
                                    .testTag("engine_option_${engine.name.lowercase()}")
                            ) {
                                Text(
                                    text = engine.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Access Shortcuts Grid (Google, YouTube, Wikipedia, Facebook, Reddit, Instagram, Prothom Alo + Add)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Access",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (adBlockEnabled) VeyoraEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (adBlockEnabled) "$totalAdsBlocked Ads Blocked" else "AdShield Off",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (adBlockEnabled) VeyoraEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (vpnEnabled) {
                            Text(
                                text = "• ${selectedVpnRegion.flagEmoji} ${selectedVpnRegion.countryCode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = VeyoraCyan
                            )
                        }
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 4
                ) {
                    shortcuts.forEach { shortcut ->
                        QuickAccessTile(
                            shortcut = shortcut,
                            onClick = { onNavigateToInput(shortcut.url) },
                            onDelete = if (!shortcut.isDefault) {
                                { onDeleteShortcut(shortcut.id) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AddShortcutTile(
                        onClick = { showAddShortcutDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Daily Headlines Category Tabs (matching Screenshot 2: "For you", "Politics", "Football", "Entertainment", etc.)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Daily Headlines",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "From over 500 leading publishers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Horizontal Category Tabs Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NewsCategory.entries.forEach { category ->
                        val isSelected = category == selectedNewsCategory
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectNewsCategory(category) }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                .testTag("news_category_tab_${category.name.lowercase()}")
                        ) {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onBackground
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(if (isSelected) 28.dp else 0.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isSelected) Color(0xFFEF4444) else Color.Transparent
                                    )
                            )
                        }
                    }
                }
            }
        }

        // 5. Featured Headline Cards (Screenshot 1 stacked visual cards with Red Publisher Badge & Time)
        items(featuredHeroArticles, key = { "hero_${it.id}" }) { article ->
            FeaturedHeadlineOverlayCard(
                article = article,
                onClick = { onNavigateToInput(article.url) }
            )
        }

        // 6. "See More »" Header Bar (from Screenshot 2)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onToggleShowAllHeadlines() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showAllHeadlines) "Showing All Top Stories" else "See More »",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Ad-Free Reader Enabled",
                    style = MaterialTheme.typography.labelSmall,
                    color = VeyoraEmerald
                )
            }
        }

        // 7. Compact News Article List Items with Thumbnail (Screenshot 2 lower list)
        items(standardArticles, key = { "list_${it.id}" }) { article ->
            CompactHeadlineRowCard(
                article = article,
                onClick = { onNavigateToInput(article.url) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showAddShortcutDialog) {
        AddQuickShortcutDialog(
            onDismiss = { showAddShortcutDialog = false },
            onConfirm = { title, url ->
                onAddShortcut(title, url)
                showAddShortcutDialog = false
            }
        )
    }
}

/**
 * Matches Screenshot 1 & 2 Featured News Card:
 * Rich visual backdrop, red publisher badge (`#DC2626`) + timestamp (`2h`), and bold white headline overlay.
 */
@Composable
private fun FeaturedHeadlineOverlayCard(
    article: NewsArticleItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .testTag("featured_headline_card_${article.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(article.cardColorStartHex),
                            Color(article.cardColorEndHex),
                            Color(0xFF090D16)
                        )
                    )
                )
        ) {
            // Subtle decorative editorial globe/wave artwork
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = size.height * 0.65f,
                    center = Offset(size.width * 0.85f, size.height * 0.25f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = size.height * 0.4f,
                    center = Offset(size.width * 0.85f, size.height * 0.25f)
                )
            }

            // Bottom dark scrim for crisp text legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.72f)
                            )
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Red Publisher Badge + Timestamp (exact Screenshot 1 style)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFDC2626)
                        ) {
                            Text(
                                text = article.publisherBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = article.timeAgo,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Bold White Headline
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Matches Screenshot 2 lower news list:
 * Headline + Publisher meta on the left, rounded visual thumbnail on the right.
 */
@Composable
private fun CompactHeadlineRowCard(
    article: NewsArticleItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compact_headline_card_${article.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFDC2626).copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = article.publisherBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "• ${article.timeAgo}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Thumbnail box on right
            Box(
                modifier = Modifier
                    .size(width = 86.dp, height = 68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(article.cardColorStartHex),
                                Color(article.cardColorEndHex)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Newspaper,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickAccessTile(
    shortcut: QuickShortcutEntity,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onDelete?.invoke() }
            )
            .testTag("shortcut_tile_${shortcut.title.lowercase().replace(" ", "_")}")
    ) {
        Box(modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)) {
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove shortcut",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(shortcut.colorHex).copy(alpha = 0.18f))
                        .border(1.dp, Color(shortcut.colorHex).copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = shortcut.iconLetter,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(shortcut.colorHex),
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = shortcut.title,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AddShortcutTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("add_shortcut_button")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Shortcut",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Add",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AddQuickShortcutDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Quick Access Shortcut") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Site Title (e.g. GitHub, BBC)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shortcut_title_input")
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Website URL (e.g. github.com)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shortcut_url_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && url.isNotBlank()) {
                        onConfirm(title, url)
                    }
                },
                modifier = Modifier.testTag("shortcut_confirm_button")
            ) {
                Text("Add Shortcut")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
