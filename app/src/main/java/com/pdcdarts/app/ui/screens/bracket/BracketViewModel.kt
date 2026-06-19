package com.pdcdarts.app.ui.screens.bracket

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdcdarts.app.data.model.DrawEntry
import com.pdcdarts.app.data.repository.DartsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BracketUiState(
    val draws: List<DrawEntry> = emptyList(),
    val rounds: List<String> = emptyList(),          // ordered round names
    val matchYPositions: Map<String, Float> = emptyMap(),
    val selectedPlayer: String? = null,
    val highlightedMatchIds: Set<String> = emptySet(),
    val lastUpdatedMs: Long = 0L,
    val fromCache: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class BracketViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = DartsRepository(app)

    private val _state = MutableStateFlow(BracketUiState(isLoading = true))
    val state: StateFlow<BracketUiState> = _state

    private var tournamentName: String = ""

    fun load(name: String, forceRefresh: Boolean = false) {
        tournamentName = name
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val result = repo.getDraw(forceRefresh)
                val draws = result.data.filter { it.tournament.equals(name, ignoreCase = true) }
                val yPositions = calculateYPositions(draws)
                val rounds = draws.sortedBy { it.roundOrder }.map { it.round }.distinct()

                _state.value = BracketUiState(
                    draws = draws,
                    rounds = rounds,
                    matchYPositions = yPositions,
                    lastUpdatedMs = result.lastUpdatedMs,
                    fromCache = result.fromCache,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load bracket",
                )
            }
        }
    }

    fun refresh() = load(tournamentName, forceRefresh = true)

    fun selectPlayer(player: String?) {
        if (player == null) {
            _state.value = _state.value.copy(selectedPlayer = null, highlightedMatchIds = emptySet())
            return
        }
        // If same player tapped again, deselect
        if (_state.value.selectedPlayer == player) {
            _state.value = _state.value.copy(selectedPlayer = null, highlightedMatchIds = emptySet())
            return
        }

        val draws = _state.value.draws
        val matchById = draws.associateBy { it.matchId }

        // Build set of matchIds where this player appears (as a slot or winner)
        val highlighted = mutableSetOf<String>()
        for (draw in draws) {
            if (playerInvolved(player, draw)) {
                highlighted.add(draw.matchId)
                // Follow forward through feedsIntoMatchId
                var current = draw
                while (current.feedsIntoMatchId.isNotBlank()) {
                    val next = matchById[current.feedsIntoMatchId] ?: break
                    highlighted.add(next.matchId)
                    current = next
                }
            }
        }

        _state.value = _state.value.copy(
            selectedPlayer = player,
            highlightedMatchIds = highlighted,
        )
    }

    private fun playerInvolved(player: String, draw: DrawEntry): Boolean {
        return draw.slotTop.equals(player, ignoreCase = true) ||
                draw.slotBottom.equals(player, ignoreCase = true) ||
                draw.winner.equals(player, ignoreCase = true)
    }

    // Assign a floating Y index to each match so the bracket renders correctly.
    // Round 1 matches are placed sequentially; later rounds are centred between their feeders.
    private fun calculateYPositions(draws: List<DrawEntry>): Map<String, Float> {
        if (draws.isEmpty()) return emptyMap()

        // Build feeder map: matchId -> list of matchIds that feed into it
        val feeders = mutableMapOf<String, MutableList<String>>()
        for (draw in draws) {
            if (draw.feedsIntoMatchId.isNotBlank()) {
                feeders.getOrPut(draw.feedsIntoMatchId) { mutableListOf() }.add(draw.matchId)
            }
        }

        val maxRound = draws.maxOf { it.roundOrder }
        val byRound = draws.groupBy { it.roundOrder }
        val yPositions = mutableMapOf<String, Float>()

        // Seed round 1 matches sequentially
        val round1 = byRound[1] ?: draws.filter { it.roundOrder == draws.minOf { d -> d.roundOrder } }
        round1.forEachIndexed { index, match ->
            yPositions[match.matchId] = index.toFloat()
        }

        // Process rounds 2..max
        for (r in 2..maxRound) {
            val roundMatches = byRound[r] ?: continue
            for (match in roundMatches) {
                val matchFeeders = feeders[match.matchId]?.mapNotNull { yPositions[it] } ?: emptyList()
                yPositions[match.matchId] = if (matchFeeders.isNotEmpty()) {
                    matchFeeders.average().toFloat()
                } else {
                    // No known feeders: fall back to sequential positioning within this round
                    roundMatches.indexOf(match).toFloat() * (1 shl (r - 1)).toFloat()
                }
            }
        }

        return yPositions
    }
}
