package com.pdcdarts.app.data.local

import android.content.Context
import java.io.File

class CacheManager(context: Context) {

    private val cacheDir = File(context.filesDir, "csv_cache").also { it.mkdirs() }

    fun save(key: String, csv: String) {
        File(cacheDir, "$key.csv").writeText(csv, Charsets.UTF_8)
        File(cacheDir, "$key.ts").writeText(System.currentTimeMillis().toString())
    }

    fun load(key: String): String? {
        val file = File(cacheDir, "$key.csv")
        return if (file.exists()) file.readText(Charsets.UTF_8) else null
    }

    fun lastUpdatedMs(key: String): Long {
        val ts = File(cacheDir, "$key.ts")
        return if (ts.exists()) ts.readText().toLongOrNull() ?: 0L else 0L
    }

    fun hasFreshCache(key: String, maxAgeMs: Long): Boolean {
        val age = System.currentTimeMillis() - lastUpdatedMs(key)
        return age < maxAgeMs && File(cacheDir, "$key.csv").exists()
    }

    companion object {
        const val KEY_CALENDAR = "calendar"
        const val KEY_MATCHES = "matches"
        const val KEY_DRAW = "draw"
        const val KEY_RANKINGS = "rankings"
        const val KEY_COMPETITIONS = "competitions"
    }
}
