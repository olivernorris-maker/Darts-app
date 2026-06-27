package com.pdcdarts.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class CsvFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // Returns the raw CSV string, or throws on network/HTTP error.
    fun fetch(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "text/csv,text/plain,*/*")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("HTTP ${response.code} for $url")
            }
            return response.body?.string() ?: throw RuntimeException("Empty body for $url")
        }
    }
}
