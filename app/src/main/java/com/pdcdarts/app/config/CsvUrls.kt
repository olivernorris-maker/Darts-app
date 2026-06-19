package com.pdcdarts.app.config

/**
 * CSV URLs for each Google Sheets tab published to the web.
 *
 * To get a URL: In Google Sheets, go to File → Share → Publish to web,
 * select the tab and "Comma-separated values (.csv)", then click Publish.
 * Paste each URL below. The URL typically looks like:
 * https://docs.google.com/spreadsheets/d/YOUR_SHEET_ID/export?format=csv&gid=TAB_GID
 */
object CsvUrls {

    // Tab: Calendar
    // Columns: Tournament, Category, StartDate, EndDate, Venue, City, Country, Status, Notes
    const val CALENDAR = "https://docs.google.com/spreadsheets/d/REPLACE_ME/export?format=csv&gid=0"

    // Tab: Matches
    // Columns: Tournament, Round, Player1, Player2, Date, Time, Session, MatchID, Status, Result, Winner
    const val MATCHES = "https://docs.google.com/spreadsheets/d/REPLACE_ME/export?format=csv&gid=1"

    // Tab: Draw
    // Columns: Tournament, Round, RoundOrder, MatchID, SlotTop, SlotBottom,
    //          ScheduledDate, ScheduledTime, Session, Winner, FeedsIntoMatchID
    const val DRAW = "https://docs.google.com/spreadsheets/d/REPLACE_ME/export?format=csv&gid=2"

    // Tab: Rankings
    // Columns: RankingType, Position, Player, Country, Points, Change
    const val RANKINGS = "https://docs.google.com/spreadsheets/d/REPLACE_ME/export?format=csv&gid=3"

    // Tab: Competitions
    // Columns: Tournament, Format, Field, PrizeFund, DefendingChampion, Dates, Venue, Broadcast, Notes
    const val COMPETITIONS = "https://docs.google.com/spreadsheets/d/REPLACE_ME/export?format=csv&gid=4"

    // How long cached data is considered fresh before triggering a background refresh (ms)
    const val CACHE_MAX_AGE_MS = 15 * 60 * 1000L // 15 minutes
}
