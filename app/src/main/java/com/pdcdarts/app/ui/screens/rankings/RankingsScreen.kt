package com.pdcdarts.app.ui.screens.rankings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdcdarts.app.data.model.Ranking
import com.pdcdarts.app.ui.components.*
import com.pdcdarts.app.ui.theme.*

@Composable
fun RankingsScreen(vm: RankingsViewModel = viewModel()) {
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
                RankingTypeToggle(
                    types = state.availableTypes,
                    selected = state.selectedType,
                    onSelect = vm::selectType,
                )
            }

            if (state.error != null && state.displayed.isEmpty()) {
                item {
                    ErrorMessage(
                        message = "Could not load rankings.\n${state.error}",
                        onRetry = vm::refresh,
                    )
                }
                return@LazyColumn
            }

            item {
                RankingsTableHeader()
            }

            itemsIndexed(state.displayed) { _, ranking ->
                RankingRow(ranking)
            }

            if (state.displayed.isEmpty() && !state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No rankings data",
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
private fun RankingTypeToggle(types: List<String>, selected: String, onSelect: (String) -> Unit) {
    val displayName = mapOf(
        "OrderOfMerit" to "Order of Merit",
        "ProTour" to "Pro Tour",
        "Womens" to "Women's",
        "Development" to "Development",
    )
    val selectedIndex = types.indexOfFirst { it.equals(selected, ignoreCase = true) }.coerceAtLeast(0)
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = DarkSurface,
        contentColor = AccentGreen,
        edgePadding = 8.dp,
    ) {
        types.forEach { type ->
            Tab(
                selected = type.equals(selected, ignoreCase = true),
                onClick = { onSelect(type) },
                text = {
                    Text(
                        text = displayName[type] ?: type,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}

@Composable
private fun RankingsTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Pos", style = MaterialTheme.typography.labelMedium, color = TextMuted, modifier = Modifier.width(40.dp))
        Text("Player", style = MaterialTheme.typography.labelMedium, color = TextMuted, modifier = Modifier.weight(1f))
        Text("Country", style = MaterialTheme.typography.labelMedium, color = TextMuted, modifier = Modifier.width(60.dp))
        Text(
            "Points",
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted,
            modifier = Modifier.width(70.dp),
            textAlign = TextAlign.End,
        )
        Text(
            "+/-",
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End,
        )
    }
    HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun RankingRow(ranking: Ranking) {
    val changeColor = when {
        ranking.change.startsWith("+") -> AccentGreen
        ranking.change.startsWith("-") -> MaterialTheme.colorScheme.error
        else -> TextMuted
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${ranking.position}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (ranking.position <= 3) AccentAmber else TextSecondary,
            fontWeight = if (ranking.position <= 3) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = ranking.player,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = ranking.country,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.width(60.dp),
        )
        Text(
            text = ranking.points,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(70.dp),
            textAlign = TextAlign.End,
        )
        Text(
            text = ranking.change.ifBlank { "—" },
            style = MaterialTheme.typography.bodySmall,
            color = changeColor,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End,
        )
    }
    HorizontalDivider(
        color = DarkBorder.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}
