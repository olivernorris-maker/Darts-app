package com.pdcdarts.app.ui.screens.tournament

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdcdarts.app.data.model.Competition
import com.pdcdarts.app.data.model.DrawEntry
import com.pdcdarts.app.data.model.Tournament
import com.pdcdarts.app.data.repository.DartsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TournamentDetailUiState(
    val tournament: Tournament? = null,
    val competition: Competition? = null,
    val drawEntries: List<DrawEntry> = emptyList(),
    val lastUpdatedMs: Long = 0L,
    val fromCache: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class TournamentDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = DartsRepository(app)

    private val _state = MutableStateFlow(TournamentDetailUiState(isLoading = true))
    val state: StateFlow<TournamentDetailUiState> = _state

    private var tournamentName: String = ""

    fun load(name: String, forceRefresh: Boolean = false) {
        tournamentName = name
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val calResult = repo.getCalendar(forceRefresh)
                val compResult = repo.getCompetitions(forceRefresh)
                val drawResult = repo.getDraw(forceRefresh)

                val tournament = calResult.data.firstOrNull {
                    it.name.equals(name, ignoreCase = true)
                }
                val competition = compResult.data.firstOrNull {
                    it.tournament.equals(name, ignoreCase = true)
                }
                val draws = drawResult.data.filter {
                    it.tournament.equals(name, ignoreCase = true)
                }

                _state.value = TournamentDetailUiState(
                    tournament = tournament,
                    competition = competition,
                    drawEntries = draws,
                    lastUpdatedMs = maxOf(calResult.lastUpdatedMs, compResult.lastUpdatedMs, drawResult.lastUpdatedMs),
                    fromCache = calResult.fromCache && compResult.fromCache && drawResult.fromCache,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load tournament",
                )
            }
        }
    }

    fun refresh() = load(tournamentName, forceRefresh = true)
}
