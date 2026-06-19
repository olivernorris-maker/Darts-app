package com.pdcdarts.app.ui.screens.results

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdcdarts.app.data.model.Match
import com.pdcdarts.app.data.repository.DartsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ResultsUiState(
    val completedMatches: List<Match> = emptyList(),
    val lastUpdatedMs: Long = 0L,
    val fromCache: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class ResultsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = DartsRepository(app)

    private val _state = MutableStateFlow(ResultsUiState(isLoading = true))
    val state: StateFlow<ResultsUiState> = _state

    init { load() }

    fun refresh() = load(forceRefresh = true)

    private fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val result = repo.getMatches(forceRefresh)
                val completed = result.data
                    .filter { it.status.lowercase() == "completed" }
                    .reversed() // most recent first (assumes sheet is chronological)

                _state.value = ResultsUiState(
                    completedMatches = completed,
                    lastUpdatedMs = result.lastUpdatedMs,
                    fromCache = result.fromCache,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load results",
                )
            }
        }
    }
}
