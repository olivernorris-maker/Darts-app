package com.pdcdarts.app.ui.screens.tournament

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdcdarts.app.data.model.Competition
import com.pdcdarts.app.data.model.Tournament
import com.pdcdarts.app.ui.components.*
import com.pdcdarts.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailScreen(
    tournamentName: String,
    onBack: () -> Unit,
    onViewBracket: (String) -> Unit,
    vm: TournamentDetailViewModel = viewModel(),
) {
    LaunchedEffect(tournamentName) {
        vm.load(tournamentName)
    }

    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.tournament?.name ?: tournamentName,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
            )
        },
    ) { innerPadding ->
        RefreshContainer(
            isRefreshing = state.isLoading,
            onRefresh = vm::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    LastUpdatedBar(state.lastUpdatedMs, state.fromCache)
                }

                if (state.error != null) {
                    item {
                        ErrorMessage(message = state.error!!, onRetry = vm::refresh)
                    }
                    return@LazyColumn
                }

                state.tournament?.let { tournament ->
                    item {
                        TournamentHeaderCard(tournament)
                    }
                }

                state.competition?.let { comp ->
                    item { SectionHeader("Competition Info") }
                    item { CompetitionInfoCard(comp) }
                }

                if (state.drawEntries.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { onViewBracket(tournamentName) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        ) {
                            Icon(Icons.Default.AccountTree, contentDescription = null, tint = DarkBackground)
                            Spacer(Modifier.width(8.dp))
                            Text("View Bracket", color = DarkBackground, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentHeaderCard(tournament: Tournament) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChip(tournament.category)
                StatusChip(tournament.status)
            }
            InfoRow("Venue", listOf(tournament.venue, tournament.city, tournament.country)
                .filter { it.isNotBlank() && it != "TBD" }.joinToString(", ").ifBlank { "TBD" })
            val dateRange = when {
                tournament.startDate == tournament.endDate -> tournament.startDate
                tournament.endDate.isBlank() || tournament.endDate == "TBD" -> tournament.startDate
                else -> "${tournament.startDate} – ${tournament.endDate}"
            }
            InfoRow("Dates", dateRange)
            if (tournament.notes.isNotBlank()) {
                InfoRow("Notes", tournament.notes)
            }
        }
    }
}

@Composable
private fun CompetitionInfoCard(comp: Competition) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (comp.format.isNotBlank() && comp.format != "TBD") InfoRow("Format", comp.format)
            if (comp.field.isNotBlank() && comp.field != "TBD") InfoRow("Field", comp.field)
            if (comp.prizeFund.isNotBlank() && comp.prizeFund != "TBD") InfoRow("Prize Fund", comp.prizeFund)
            if (comp.defendingChampion.isNotBlank() && comp.defendingChampion != "TBD") InfoRow("Defending Champion", comp.defendingChampion)
            if (comp.broadcast.isNotBlank() && comp.broadcast != "TBD") InfoRow("Broadcast", comp.broadcast)
            if (comp.dates.isNotBlank() && comp.dates != "TBD") InfoRow("Dates", comp.dates)
            if (comp.venue.isNotBlank() && comp.venue != "TBD") InfoRow("Venue", comp.venue)
            if (comp.notes.isNotBlank()) InfoRow("Notes", comp.notes)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.width(140.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}
