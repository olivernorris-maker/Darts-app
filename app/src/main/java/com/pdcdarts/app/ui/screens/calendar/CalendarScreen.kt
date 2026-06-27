package com.pdcdarts.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdcdarts.app.data.model.Tournament
import com.pdcdarts.app.ui.components.*
import com.pdcdarts.app.ui.theme.*

@Composable
fun CalendarScreen(
    onTournamentClick: (String) -> Unit,
    vm: CalendarViewModel = viewModel(),
) {
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

            item {
                CategoryFilterRow(
                    categories = state.allCategories,
                    selected = state.selectedCategory,
                    onSelect = vm::selectCategory,
                )
            }

            if (state.error != null && state.upcoming.isEmpty() && state.completed.isEmpty()) {
                item {
                    ErrorMessage(message = "Could not load calendar.\n${state.error}", onRetry = vm::refresh)
                }
                return@LazyColumn
            }

            if (state.upcoming.isNotEmpty()) {
                item { SectionHeader("Upcoming") }
                items(state.upcoming) { t ->
                    TournamentCard(tournament = t, onClick = { onTournamentClick(t.name) })
                }
            }

            if (state.completed.isNotEmpty()) {
                item { SectionHeader("Completed") }
                items(state.completed) { t ->
                    TournamentCard(tournament = t, onClick = { onTournamentClick(t.name) })
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { cat ->
            val isSelected = cat == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(cat) },
                label = { Text(cat) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentGreen.copy(alpha = 0.2f),
                    selectedLabelColor = AccentGreen,
                    containerColor = DarkCard,
                    labelColor = TextSecondary,
                ),
            )
        }
    }
}

@Composable
private fun TournamentCard(tournament: Tournament, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CategoryChip(tournament.category)
                    StatusChip(tournament.status)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = tournament.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                if (tournament.venue != "TBD" || tournament.city != "TBD") {
                    Text(
                        text = listOf(tournament.venue, tournament.city, tournament.country)
                            .filter { it.isNotBlank() && it != "TBD" }
                            .joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                Spacer(Modifier.height(2.dp))
                val dateRange = when {
                    tournament.startDate == tournament.endDate -> tournament.startDate
                    tournament.endDate == "TBD" -> tournament.startDate
                    else -> "${tournament.startDate} – ${tournament.endDate}"
                }
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
                if (tournament.notes.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = tournament.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open tournament",
                tint = TextMuted,
            )
        }
    }
}
