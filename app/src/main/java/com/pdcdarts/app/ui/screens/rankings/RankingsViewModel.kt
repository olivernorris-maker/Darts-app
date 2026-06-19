package com.pdcdarts.app.ui.screens.rankings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdcdarts.app.data.model.Ranking
import com.pdcdarts.app.data.repository.DartsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RankingsUiState(
    val displayed: List<Ranking> = emptyList(),
    val selectedType: String = "OrderOfMerit",
    val availableTypes: List<String> = listOf("OrderOfMerit", "ProTour", "Womens", "Development"),
    val lastUpdatedMs: Long = 0L,
    val fromCache: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class RankingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = DartsRepository(app)
    private var allRankings: List<Ranking> = emptyList()

    private val _state = MutableStateFlow(RankingsUiState(isLoading = true))
    val state: StateFlow<RankingsUiState> = _state

    init { load() }

    fun refresh() = load(forceRefresh = true)

    fun selectType(type: String) {
        _state.value = _state.value.copy(selectedType = type)
        applyFilter()
    }

    private fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val result = repo.getRankings(forceRefresh)
                allRankings = result.data
                val types = result.data.map { it.rankingType }.distinct()
                _state.value = _state.value.copy(
                    availableTypes = types.ifEmpty { listOf("OrderOfMerit", "ProTour", "Womens") },
                    lastUpdatedMs = result.lastUpdatedMs,
                    fromCache = result.fromCache,
                    isLoading = false,
                )
                applyFilter()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load rankings",
                )
            }
        }
    }

    private fun applyFilter() {
        val type = _state.value.selectedType
        _state.value = _state.value.copy(
            displayed = allRankings
                .filter { it.rankingType.equals(type, ignoreCase = true) }
                .sortedBy { it.position },
        )
    }
}
