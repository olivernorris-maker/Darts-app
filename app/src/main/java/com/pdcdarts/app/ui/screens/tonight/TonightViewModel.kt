package com.pdcdarts.app.ui.screens.tonight

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdcdarts.app.data.model.Match
import com.pdcdarts.app.data.repository.DartsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class TonightUiState(
    val todayMatches: Map<String, List<Match>> = emptyMap(),   // tournament -> matches
    val upcomingMatches: Map<String, List<Match>> = emptyMap(),
    val lastUpdatedMs: Long = 0L,
    val fromCache: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class TonightViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = DartsRepository(app)

    private val _state = MutableStateFlow(TonightUiState(isLoading = true))
    val state: StateFlow<TonightUiState> = _state

    init { load() }

    fun refresh() = load(forceRefresh = true)

    private fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val result = repo.getMatches(forceRefresh)
                val today = todayString()

                val scheduled = result.data.filter { it.status.lowercase() != "completed" }
                val (todayList, soonList) = scheduled.partition { normaliseDate(it.date) == today }

                val sortedSoon = soonList
                    .sortedWith(compareBy({ normaliseDate(it.date) }, { it.time }))
                    .take(20)

                _state.value = TonightUiState(
                    todayMatches = todayList.groupBy { it.tournament },
                    upcomingMatches = sortedSoon.groupBy { it.tournament },
                    lastUpdatedMs = result.lastUpdatedMs,
                    fromCache = result.fromCache,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load matches",
                )
            }
        }
    }

    private fun todayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.UK)
        sdf.timeZone = TimeZone.getTimeZone("Europe/London")
        return sdf.format(Date())
    }

    // Tries to normalise various date formats to yyyy-MM-dd for comparison.
    private fun normaliseDate(date: String): String {
        if (date.isBlank() || date == "TBD") return ""
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.UK),
            SimpleDateFormat("dd/MM/yyyy", Locale.UK),
            SimpleDateFormat("d MMM yyyy", Locale.UK),
            SimpleDateFormat("dd MMM yyyy", Locale.UK),
        )
        for (fmt in formats) {
            fmt.timeZone = TimeZone.getTimeZone("Europe/London")
            try {
                val target = SimpleDateFormat("yyyy-MM-dd", Locale.UK).also {
                    it.timeZone = TimeZone.getTimeZone("Europe/London")
                }
                return target.format(fmt.parse(date) ?: continue)
            } catch (ignored: Exception) {}
        }
        return date
    }
}
