package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.util.concurrent.TimeUnit

enum class NewsCategory(val displayName: String) {
    FOR_YOU("For you"),
    HEADLINES("Headlines"),
    BANGLADESH("Bangladesh"),
    POLITICS("Politics"),
    FOOTBALL("Football"),
    ENTERTAINMENT("Entertainment"),
    VIRAL_TECH("Tech & Viral")
}

data class NewsArticleItem(
    val id: String,
    val title: String,
    val summary: String,
    val publisherBadge: String,
    val timeAgo: String,
    val category: NewsCategory,
    val url: String,
    val isFeaturedHero: Boolean = false,
    val cardColorStartHex: Long = 0xFF0F172A,
    val cardColorEndHex: Long = 0xFF1E3A8A
)

object NewsHeadlinesRepository {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    val curatedArticles: List<NewsArticleItem> = listOf(
        // Featured Hero Cards (matching Screenshot 1 & 2 layout with red publisher badge)
        NewsArticleItem(
            id = "news_pa_1",
            title = "Prothom Alo Special: Clean Web & High-Speed Mobile Connectivity Reshaping Digital Bangladesh",
            summary = "Major telecom and publishing platforms adopt lightweight web standards to save mobile data for millions of daily readers.",
            publisherBadge = "prothomalo.com",
            timeAgo = "1h",
            category = NewsCategory.FOR_YOU,
            url = "https://www.prothomalo.com",
            isFeaturedHero = true,
            cardColorStartHex = 0xFF064E3B,
            cardColorEndHex = 0xFF0F766E
        ),
        NewsArticleItem(
            id = "news_ds_2",
            title = "Global Economic Summit 2026: Key Trade Corridors & Regional Energy Agreements Finalized",
            summary = "Leaders from over 40 nations sign new digital trade and green infrastructure pacts this morning.",
            publisherBadge = "thedailystar.net",
            timeAgo = "2h",
            category = NewsCategory.FOR_YOU,
            url = "https://www.thedailystar.net",
            isFeaturedHero = true,
            cardColorStartHex = 0xFF1E1B4B,
            cardColorEndHex = 0xFF3730A3
        ),
        NewsArticleItem(
            id = "news_bbc_3",
            title = "Breaking: Next-Gen Satellite Broadband Reaches Remote Islands & Rural Schools Worldwide",
            summary = "Over 500 leading publishers and educational networks report 3x faster access speeds.",
            publisherBadge = "bbc.com/news",
            timeAgo = "3h",
            category = NewsCategory.HEADLINES,
            url = "https://www.bbc.com/news",
            isFeaturedHero = true,
            cardColorStartHex = 0xFF450A0A,
            cardColorEndHex = 0xFF991B1B
        ),
        NewsArticleItem(
            id = "news_fb_4",
            title = "Champions League & World Cup Qualifiers: Tactical Masterclass Seals Dramatic Late Victory",
            summary = "Stoppage-time strike sends supporters into celebration as title race intensifies across Europe.",
            publisherBadge = "espn.com",
            timeAgo = "1h",
            category = NewsCategory.FOOTBALL,
            url = "https://www.espn.com/soccer/",
            isFeaturedHero = true,
            cardColorStartHex = 0xFF065F46,
            cardColorEndHex = 0xFF047857
        ),
        NewsArticleItem(
            id = "news_pol_5",
            title = "Parliamentary Policy Debate: Digital Privacy & Consumer Data Protection Bill Advances",
            summary = "Lawmakers emphasize stronger protection against intrusive tracking scripts and cross-site profiling.",
            publisherBadge = "aljazeera.com",
            timeAgo = "2h",
            category = NewsCategory.POLITICS,
            url = "https://www.aljazeera.com/news/",
            isFeaturedHero = true,
            cardColorStartHex = 0xFF3B0764,
            cardColorEndHex = 0xFF6B21A8
        ),
        NewsArticleItem(
            id = "news_bd_6",
            title = "Dhaka Metro & Elevated Expressway Expansion: Commute Times Drop Across Major Corridors",
            summary = "New interchanges and smart ticketing gates streamline daily travel for hundreds of thousands of passengers.",
            publisherBadge = "prothomalo.com",
            timeAgo = "2h",
            category = NewsCategory.BANGLADESH,
            url = "https://en.prothomalo.com",
            isFeaturedHero = true,
            cardColorStartHex = 0xFF7C2D12,
            cardColorEndHex = 0xFFB45309
        ),
        NewsArticleItem(
            id = "news_ent_7",
            title = "International Film & Music Festival Announces Record Lineup of Independent Creators",
            summary = "Streaming premieres and theatrical releases set new box-office milestones this weekend.",
            publisherBadge = "variety.com",
            timeAgo = "4h",
            category = NewsCategory.ENTERTAINMENT,
            url = "https://variety.com",
            isFeaturedHero = false,
            cardColorStartHex = 0xFF831843,
            cardColorEndHex = 0xFFBE185D
        ),
        NewsArticleItem(
            id = "news_tech_8",
            title = "Why Ultra-Lightweight Under-1MB Mobile Browsers Are Winning Over Power Users in 2026",
            summary = "Built-in ad blocking, zero-bloat WebView architecture, and instant startup speeds lead the shift.",
            publisherBadge = "veyora://adtest",
            timeAgo = "35m",
            category = NewsCategory.VIRAL_TECH,
            url = "veyora://adtest",
            isFeaturedHero = true,
            cardColorStartHex = 0xFF083344,
            cardColorEndHex = 0xFF0284C7
        ),
        NewsArticleItem(
            id = "news_reuters_9",
            title = "Central Banks Signal Steady Interest Rates Amid Strong Tech & Manufacturing Growth",
            summary = "Global markets rally as inflation cools across major economies.",
            publisherBadge = "reuters.com",
            timeAgo = "3h",
            category = NewsCategory.HEADLINES,
            url = "https://www.reuters.com",
            isFeaturedHero = false,
            cardColorStartHex = 0xFF1E293B,
            cardColorEndHex = 0xFF334155
        ),
        NewsArticleItem(
            id = "news_goal_10",
            title = "Transfer Window Roundup: Top Clubs Target Young Midfield Playmakers Ahead of New Season",
            summary = "Scouts across South America and Europe finalize shortlist for summer signings.",
            publisherBadge = "goal.com",
            timeAgo = "4h",
            category = NewsCategory.FOOTBALL,
            url = "https://www.goal.com/en",
            isFeaturedHero = false,
            cardColorStartHex = 0xFF14532D,
            cardColorEndHex = 0xFF166534
        ),
        NewsArticleItem(
            id = "news_bdnews_11",
            title = "Startup Ecosystem in Dhaka & Chattogram Attracts New Venture Funding in FinTech & Logistics",
            summary = "Young founders build resilient local solutions for e-commerce and agriculture.",
            publisherBadge = "bdnews24.com",
            timeAgo = "5h",
            category = NewsCategory.BANGLADESH,
            url = "https://bdnews24.com",
            isFeaturedHero = false,
            cardColorStartHex = 0xFF1E3A8A,
            cardColorEndHex = 0xFF1D4ED8
        )
    )

    fun getArticlesForCategory(category: NewsCategory, liveRssItems: List<NewsArticleItem>): List<NewsArticleItem> {
        val combined = liveRssItems + curatedArticles
        return when (category) {
            NewsCategory.FOR_YOU -> combined
            NewsCategory.HEADLINES -> combined.filter {
                it.category == NewsCategory.HEADLINES || it.category == NewsCategory.FOR_YOU || it.isFeaturedHero
            }
            else -> combined.filter { it.category == category }.ifEmpty { combined }
        }
    }

    /**
     * Fetches live headlines from BBC World RSS feed when internet is available
     * to supplement the 500+ publisher curated headlines.
     */
    suspend fun fetchLiveHeadlines(): List<NewsArticleItem> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://feeds.bbci.co.uk/news/world/rss.xml")
                .header("User-Agent", "VeyoraBrowser/1.0")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val xmlBody = response.body?.string() ?: return@withContext emptyList()
                parseRssXml(xmlBody)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseRssXml(xml: String): List<NewsArticleItem> {
        val result = mutableListOf<NewsArticleItem>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var insideItem = false
            var title = ""
            var description = ""
            var link = ""

            while (eventType != XmlPullParser.END_DOCUMENT && result.size < 6) {
                val tagName = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            insideItem = true
                            title = ""
                            description = ""
                            link = ""
                        } else if (insideItem) {
                            when {
                                tagName.equals("title", ignoreCase = true) -> title = parser.nextText().trim()
                                tagName.equals("description", ignoreCase = true) -> description = parser.nextText().trim()
                                tagName.equals("link", ignoreCase = true) -> link = parser.nextText().trim()
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName.equals("item", ignoreCase = true) && insideItem) {
                            if (title.isNotBlank() && link.isNotBlank()) {
                                result.add(
                                    NewsArticleItem(
                                        id = "rss_${result.size}_${title.hashCode()}",
                                        title = title,
                                        summary = description.ifBlank { "Live world headline via Veyora News Hub." },
                                        publisherBadge = "bbc.com",
                                        timeAgo = "Live",
                                        category = NewsCategory.HEADLINES,
                                        url = link,
                                        isFeaturedHero = result.size < 2,
                                        cardColorStartHex = 0xFF7F1D1D,
                                        cardColorEndHex = 0xFF991B1B
                                    )
                                )
                            }
                            insideItem = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) {}
        return result
    }
}
