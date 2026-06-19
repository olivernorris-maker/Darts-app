package com.pdcdarts.app.data.model

data class Tournament(
    val name: String,
    val category: String,   // Major / ProTour / EuroTour / PremierLeague / Modus / Womens / Other
    val startDate: String,
    val endDate: String,
    val venue: String,
    val city: String,
    val country: String,
    val status: String,     // Upcoming / Live / Completed
    val notes: String,
)

data class Match(
    val tournament: String,
    val round: String,
    val player1: String,
    val player2: String,
    val date: String,
    val time: String,
    val session: String,    // Afternoon / Evening
    val matchId: String,
    val status: String,     // Scheduled / Completed
    val result: String,
    val winner: String,
)

data class DrawEntry(
    val tournament: String,
    val round: String,
    val roundOrder: Int,    // 1 = first round, ascending to final
    val matchId: String,
    val slotTop: String,    // player name, "TBD", or "Winner of [MatchID]"
    val slotBottom: String,
    val scheduledDate: String,
    val scheduledTime: String,
    val session: String,
    val winner: String,
    val feedsIntoMatchId: String,
)

data class Ranking(
    val rankingType: String, // OrderOfMerit / ProTour / Womens / Development
    val position: Int,
    val player: String,
    val country: String,
    val points: String,
    val change: String,
)

data class Competition(
    val tournament: String,
    val format: String,
    val field: String,
    val prizeFund: String,
    val defendingChampion: String,
    val dates: String,
    val venue: String,
    val broadcast: String,
    val notes: String,
)

// Wraps a loaded dataset with its fetch timestamp for "last updated" display
data class DataResult<T>(
    val data: List<T>,
    val lastUpdatedMs: Long,
    val fromCache: Boolean = false,
)
