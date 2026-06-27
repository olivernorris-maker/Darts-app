package com.pdcdarts.app.ui.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.pdcdarts.app.ui.screens.tonight.PlayerVsPlayer
import com.pdcdarts.app.ui.theme.*

@Composable
fun ResultsScreen(vm: ResultsViewModel = viewModel()) {
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

            if (state.error != null && state.completedMatches.isEmpty()) {
                item {
                    ErrorMessage(message = "Could not load results.\n${state.error}", onRetry = vm::refresh)
                }
                return@LazyColumn
            }

            if (state.completedMatches.isEmpty() && !state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("No completed matches yet", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                return@LazyColumn
            }

            // Group by tournament for display
            val grouped = state.completedMatches.groupBy { it.tournament }
            grouped.forEach { (tournament, matches) ->
                item {
                    SectionHeader(tournament)
                }
                itemsIndexed(matches) { _, match ->
                    ResultCard(match)
                }
            }
        }
    }
}

@Composable
private fun ResultCard(match: Match) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = match.round,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                )
                Spacer(Modifier.height(6.dp))
                PlayerVsPlayer(match.player1, match.player2, match.winner)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatUkDateTime(match.date, match.time),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
            if (match.result.isNotBlank()) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = match.result,
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold,
                    )
                    if (match.winner.isNotBlank()) {
                        Text(
                            text = match.winner,
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentGreen,
                        )
                    }
                }
            }
        }
    }
}
