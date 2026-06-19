package com.pdcdarts.app.data.parser

import com.pdcdarts.app.data.model.*

// RFC-4180-compatible single-line CSV parser.
// Does not handle multi-line quoted fields (not needed for this use-case).
internal fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val c = line[i]
        when {
            c == '"' && !inQuotes -> inQuotes = true
            c == '"' && inQuotes -> {
                if (i + 1 < line.length && line[i + 1] == '"') {
                    current.append('"')
                    i++
                } else {
                    inQuotes = false
                }
            }
            c == ',' && !inQuotes -> {
                result.add(current.toString().trim())
                current.clear()
            }
            else -> current.append(c)
        }
        i++
    }
    result.add(current.toString().trim())
    return result
}

// Returns "" if index is out of bounds, so parsers never throw on short rows.
private fun List<String>.safeGet(index: Int) = getOrElse(index) { "" }

private fun String.orTbd() = ifBlank { "TBD" }

object CsvParser {

    fun parseTournaments(csv: String): List<Tournament> {
        val lines = csv.lines()
        return lines.drop(1).filter { it.isNotBlank() }.mapNotNull { line ->
            val cols = parseCsvLine(line)
            if (cols.size < 2) return@mapNotNull null
            Tournament(
                name = cols.safeGet(0).orTbd(),
                category = cols.safeGet(1).orTbd(),
                startDate = cols.safeGet(2).orTbd(),
                endDate = cols.safeGet(3).orTbd(),
                venue = cols.safeGet(4).orTbd(),
                city = cols.safeGet(5).orTbd(),
                country = cols.safeGet(6).orTbd(),
                status = cols.safeGet(7).orTbd(),
                notes = cols.safeGet(8),
            )
        }
    }

    fun parseMatches(csv: String): List<Match> {
        val lines = csv.lines()
        return lines.drop(1).filter { it.isNotBlank() }.mapNotNull { line ->
            val cols = parseCsvLine(line)
            if (cols.size < 2) return@mapNotNull null
            Match(
                tournament = cols.safeGet(0).orTbd(),
                round = cols.safeGet(1).orTbd(),
                player1 = cols.safeGet(2).orTbd(),
                player2 = cols.safeGet(3).orTbd(),
                date = cols.safeGet(4).orTbd(),
                time = cols.safeGet(5).orTbd(),
                session = cols.safeGet(6).orTbd(),
                matchId = cols.safeGet(7),
                status = cols.safeGet(8).orTbd(),
                result = cols.safeGet(9),
                winner = cols.safeGet(10),
            )
        }
    }

    fun parseDrawEntries(csv: String): List<DrawEntry> {
        val lines = csv.lines()
        return lines.drop(1).filter { it.isNotBlank() }.mapNotNull { line ->
            val cols = parseCsvLine(line)
            if (cols.size < 2) return@mapNotNull null
            DrawEntry(
                tournament = cols.safeGet(0).orTbd(),
                round = cols.safeGet(1).orTbd(),
                roundOrder = cols.safeGet(2).toIntOrNull() ?: 1,
                matchId = cols.safeGet(3),
                slotTop = cols.safeGet(4).orTbd(),
                slotBottom = cols.safeGet(5).orTbd(),
                scheduledDate = cols.safeGet(6).orTbd(),
                scheduledTime = cols.safeGet(7).orTbd(),
                session = cols.safeGet(8).orTbd(),
                winner = cols.safeGet(9),
                feedsIntoMatchId = cols.safeGet(10),
            )
        }
    }

    fun parseRankings(csv: String): List<Ranking> {
        val lines = csv.lines()
        return lines.drop(1).filter { it.isNotBlank() }.mapNotNull { line ->
            val cols = parseCsvLine(line)
            if (cols.size < 2) return@mapNotNull null
            Ranking(
                rankingType = cols.safeGet(0).orTbd(),
                position = cols.safeGet(1).toIntOrNull() ?: 0,
                player = cols.safeGet(2).orTbd(),
                country = cols.safeGet(3).orTbd(),
                points = cols.safeGet(4).orTbd(),
                change = cols.safeGet(5),
            )
        }
    }

    fun parseCompetitions(csv: String): List<Competition> {
        val lines = csv.lines()
        return lines.drop(1).filter { it.isNotBlank() }.mapNotNull { line ->
            val cols = parseCsvLine(line)
            if (cols.size < 2) return@mapNotNull null
            Competition(
                tournament = cols.safeGet(0).orTbd(),
                format = cols.safeGet(1).orTbd(),
                field = cols.safeGet(2).orTbd(),
                prizeFund = cols.safeGet(3).orTbd(),
                defendingChampion = cols.safeGet(4).orTbd(),
                dates = cols.safeGet(5).orTbd(),
                venue = cols.safeGet(6).orTbd(),
                broadcast = cols.safeGet(7).orTbd(),
                notes = cols.safeGet(8),
            )
        }
    }
}
