package com.pdcdarts.app.config

private const val SHEET_ID = "1S3TkT8gcO4ojWVh913NuxRpnFXwBEaqenrS0qgSnUNg"
private const val BASE = "https://docs.google.com/spreadsheets/d/$SHEET_ID/gviz/tq?tqx=out:csv&sheet="

object CsvUrls {

    // Tab: Calendar
    // Columns: Tournament, Category, StartDate, EndDate, Venue, City, Country, Status, Notes
    const val CALENDAR = "${BASE}Calendar"

    // Tab: Matches
    // Columns: Tournament, Round, Player1, Player2, Date, Time, Session, MatchID, Status, Result, Winner
    const val MATCHES = "${BASE}Matches"

    // Tab: Draw
    // Columns: Tournament, Round, RoundOrder, MatchID, SlotTop, SlotBottom,
    //          ScheduledDate, ScheduledTime, Session, Winner, FeedsIntoMatchID
    const val DRAW = "${BASE}Draw"

    // Tab: Rankings
    // Columns: RankingType, Position, Player, Country, Points, Change
    const val RANKINGS = "${BASE}Rankings"

    // Tab: Competitions
    // Columns: Tournament, Format, Field, PrizeFund, DefendingChampion, Dates, Venue, Broadcast, Notes
    const val COMPETITIONS = "${BASE}Competitions"

    // How long cached data is considered fresh before triggering a background refresh (ms)
    const val CACHE_MAX_AGE_MS = 15 * 60 * 1000L // 15 minutes
}
