package me.donghuaar

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class DonghuaArProvider : MainAPI() {
    override var mainUrl = "https://donghua-ar.me"
    override var name = "Donghua Realm"
    override val supportedTypes = setOf(TvType.TvSeries, TvType.Movie)
    override var lang = "ar"
    override val hasMainPage = true

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(mainUrl).document
        val home = mutableListOf<HomePageList>()
        
        document.select("section, div.mt-12").forEach { section ->
            val title = section.selectFirst("h2")?.text() ?: ""
            val items = section.select("a[href^='/series/'], a[href^='/movies/']").mapNotNull {
                it.toSearchResult()
            }
            if (items.isNotEmpty()) {
                home.add(HomePageList(title, items))
            }
        }
        
        return HomePageResponse(home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h3")?.text() ?: return null
        val href = this.attr("href")
        val posterUrl = this.selectFirst("img")?.attr("src")
        
        return if (href.contains("/series/")) {
            newTvSeriesSearchResponse(title, fixUrl(href)) {
                this.posterUrl = posterUrl
            }
        } else {
            newMovieSearchResponse(title, fixUrl(href)) {
                this.posterUrl = posterUrl
            }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/genres?search=$query").document
        return document.select("a[href^='/series/'], a[href^='/movies/']").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1")?.text() ?: ""
        val poster = document.selectFirst("img[alt='$title']")?.attr("src")
        val description = document.selectFirst(".story, p")?.text()
        
        val episodes = document.select("a[href^='/watch/']").map {
            val href = it.attr("href")
            val name = it.text()
            Episode(fixUrl(href), name)
        }

        return if (url.contains("/series/")) {
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.plot = description
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = description
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        
        // البحث عن روابط التحميل المباشرة أو روابط المشغل
        document.select("a[href*='gofile.io']").forEach {
            val link = it.attr("href")
            callback.invoke(
                ExtractorLink(
                    this.name,
                    "Direct",
                    link,
                    "",
                    Qualities.P1080.value,
                    true
                )
            )
        }
        
        return true
    }
}