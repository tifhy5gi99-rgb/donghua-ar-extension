package com.donghuaar

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class DonghuaARProvider : MainAPI() {
    override var mainUrl = "https://donghua-ar.me"
    override var name = "Donghua AR"
    override val hasMainPage = true
    override var lang = "ar"
    override val hasQuickSearch = true
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(TvType.Anime, TvType.Movie)

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = app.get(mainUrl).document
        val items = doc.select("div.col-md-2.col-sm-4.col-xs-6").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, items)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h3")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("img")?.attr("src")
        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/?s=$query").document
        return doc.select("div.col-md-2.col-sm-4.col-xs-6").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1")?.text() ?: ""
        val poster = doc.selectFirst("div.poster img")?.attr("src")
        val description = doc.selectFirst("div.content")?.text()
        
        val episodes = doc.select("div.episodes-list a").map {
            newEpisode(it.attr("href")) {
                this.name = it.text()
            }
        }

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster
            this.plot = description
            addEpisodes(TvType.Anime, episodes)
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val doc = app.get(data).document
        val iframe = doc.selectFirst("iframe")?.attr("src") ?: return false
        
        callback.invoke(
            ExtractorLink(
                "DonghuaAR",
                "Main Server",
                iframe,
                "",
                Qualities.Unknown.value,
                isM3u8 = iframe.contains("m3u8")
            )
        )
        return true
    }
}
