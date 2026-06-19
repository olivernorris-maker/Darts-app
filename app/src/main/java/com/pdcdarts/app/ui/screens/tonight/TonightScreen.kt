package com.pdcdarts.app.ui.screens.tonight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdcdarts.app.data.model.Match
import com.pdcdarts.app.ui.components.*
import com.pdcdarts.app.ui.theme.*

@Composable
fun TonightScreen(vm: TonightViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    RefreshContainer(
        isRefreshing = state.isLoading,
        onRefresh = vm::refresh,
        modifier = Modifier.fillMaxSize().background(DarkBackground),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                LastUpdatedBar(state.lastUpdatedMs, state.fromCache)
            }

            if (state.error != null && state.todayMatches.isEmpty() && state.upcomingMatches.isEmpty()) {
                item {
                    ErrorMessage(
                        message = "Could not load matches.\n${state.error}",
                        onRetry = vm::refresh,
                    )
                }
                return@LazyColumn
            }

            if (state.todayMatches.isNotEmpty()) {
                item { SectionHeader("Today") }
                state.todayMatches.forEach { (tournament, matches) ->
                    item { TournamentGroupHeader(tournament) }
                    items(matches) { match ->
                        MatchRow(match)
                    }
                }
            }

            if (state.upcomingMatches.isNotEmpty()) {
                item { SectionHeader("Coming Up") }
                state.upcomingMatches.forEach { (tournament, matches) ->
                    item { TournamentGroupHeader(tournament) }
                    items(matches) { match ->
                        MatchRow(match)
                    }
                }
            }

            if (state.todayMatches.isEmpty() && state.upcomingMatches.isEmpty() && !state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No matches scheduled",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentGroupHeader(tournament: String) {
    Text(
        text = tournament,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
    )
}

@Composable
private fun MatchRow(match: Match) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.round,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted,
                    )
                    Spacer(Modifier.height(6.dp))
                    PlayerVsPlayer(match.player1, match.player2, match.winner)
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    StatusChip(match.status)
                    if (match.result.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = match.result,
                            style = MaterialTheme.typography.labelLarge,
                            color = AccentGreen,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatUkDateTime(match.date, match.time),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                if (match.session.isNotBlank() && match.session != "TBD") {
                    Text(
                        text = " · ${match.session}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerVsPlayer(player1: String, player2: String, winner: String) {
    val p1Won = winner.isNotBlank() && winner == player1
    val p2Won = winner.isNotBlank() && winner == player2
    Column {
        Text(
            text = player1,
            style = MaterialTheme.typography.bodyLarge,
            color = if (p1Won) AccentGreen else TextPrimary,
            fontWeight = if (p1Won) FontWeight.SemiBold else FontWeight.Normal,
        )
        Text(
            text = "vs",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.padding(vertical = 2.dp),
        )
        Text(
            text = player2,
            style = MaterialTheme.typography.bodyLarge,
            color = if (p2Won) AccentGreen else TextPrimary,
            fontWeight = if (p2Won) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
