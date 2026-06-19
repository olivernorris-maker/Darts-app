package com.pdcdarts.app.data.repository

import android.content.Context
import com.pdcdarts.app.config.CsvUrls
import com.pdcdarts.app.data.local.CacheManager
import com.pdcdarts.app.data.model.*
import com.pdcdarts.app.data.parser.CsvParser
import com.pdcdarts.app.data.remote.CsvFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DartsRepository(context: Context) {

    private val fetcher = CsvFetcher()
    private val cache = CacheManager(context)

    // Generic fetch-or-cache logic:
    // 1. If fresh cache exists and forceRefresh=false, return cache.
    // 2. Otherwise fetch from network; on failure fall back to stale cache.
    // 3. If nothing is available, throw.
    private suspend fun fetchCsv(
        key: String,
        url: String,
        forceRefresh: Boolean,
    ): Pair<String, Boolean> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cache.hasFreshCache(key, CsvUrls.CACHE_MAX_AGE_MS)) {
            return@withContext cache.load(key)!! to true
        }
        try {
            val csv = fetcher.fetch(url)
            cache.save(key, csv)
            csv to false
        } catch (e: Exception) {
            val cached = cache.load(key)
            if (cached != null) cached to true
            else throw e
        }
    }

    suspend fun getCalendar(forceRefresh: Boolean = false): DataResult<Tournament> {
        val (csv, fromCache) = fetchCsv(CacheManager.KEY_CALENDAR, CsvUrls.CALENDAR, forceRefresh)
        return DataResult(
            data = CsvParser.parseTournaments(csv),
            lastUpdatedMs = cache.lastUpdatedMs(CacheManager.KEY_CALENDAR),
            fromCache = fromCache,
        )
    }

    suspend fun getMatches(forceRefresh: Boolean = false): DataResult<Match> {
        val (csv, fromCache) = fetchCsv(CacheManager.KEY_MATCHES, CsvUrls.MATCHES, forceRefresh)
        return DataResult(
            data = CsvParser.parseMatches(csv),
            lastUpdatedMs = cache.lastUpdatedMs(CacheManager.KEY_MATCHES),
            fromCache = fromCache,
        )
    }

    suspend fun getDraw(forceRefresh: Boolean = false): DataResult<DrawEntry> {
        val (csv, fromCache) = fetchCsv(CacheManager.KEY_DRAW, CsvUrls.DRAW, forceRefresh)
        return DataResult(
            data = CsvParser.parseDrawEntries(csv),
            lastUpdatedMs = cache.lastUpdatedMs(CacheManager.KEY_DRAW),
            fromCache = fromCache,
        )
    }

    suspend fun getRankings(forceRefresh: Boolean = false): DataResult<Ranking> {
        val (csv, fromCache) = fetchCsv(CacheManager.KEY_RANKINGS, CsvUrls.RANKINGS, forceRefresh)
        return DataResult(
            data = CsvParser.parseRankings(csv),
            lastUpdatedMs = cache.lastUpdatedMs(CacheManager.KEY_RANKINGS),
            fromCache = fromCache,
        )
    }

    suspend fun getCompetitions(forceRefresh: Boolean = false): DataResult<Competition> {
        val (csv, fromCache) = fetchCsv(CacheManager.KEY_COMPETITIONS, CsvUrls.COMPETITIONS, forceRefresh)
        return DataResult(
            data = CsvParser.parseCompetitions(csv),
            lastUpdatedMs = cache.lastUpdatedMs(CacheManager.KEY_COMPETITIONS),
            fromCache = fromCache,
        )
    }
}
