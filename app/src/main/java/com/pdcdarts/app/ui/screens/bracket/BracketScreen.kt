package com.pdcdarts.app.ui.screens.bracket

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdcdarts.app.data.model.DrawEntry
import com.pdcdarts.app.ui.components.*
import com.pdcdarts.app.ui.theme.*

// Bracket layout constants (logical pixels, before zoom)
private const val CARD_W = 220f
private const val CARD_H = 80f
private const val ROUND_GAP = 80f
private const val SLOT_GAP = 24f
private const val CANVAS_PADDING = 40f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BracketScreen(
    tournamentName: String,
    onBack: () -> Unit,
    vm: BracketViewModel = viewModel(),
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
                        text = "$tournamentName — Bracket",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground),
        ) {
            LastUpdatedBar(state.lastUpdatedMs, state.fromCache)

            if (state.error != null) {
                ErrorMessage(message = state.error!!, onRetry = vm::refresh)
                return@Column
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentGreen)
                }
                return@Column
            }

            if (state.draws.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No draw available yet",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                return@Column
            }

            if (state.selectedPlayer != null) {
                PlayerRouteBar(
                    player = state.selectedPlayer!!,
                    draws = state.draws,
                    highlightedMatchIds = state.highlightedMatchIds,
                    onDismiss = { vm.selectPlayer(null) },
                )
            }

            BracketHint()

            BracketCanvas(
                draws = state.draws,
                yPositions = state.matchYPositions,
                highlightedMatchIds = state.highlightedMatchIds,
                selectedPlayer = state.selectedPlayer,
                onPlayerTap = vm::selectPlayer,
            )
        }
    }
}

@Composable
private fun BracketHint() {
    Text(
        text = "Pinch to zoom · drag to pan · tap a player to trace their route",
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@Composable
private fun PlayerRouteBar(
    player: String,
    draws: List<DrawEntry>,
    highlightedMatchIds: Set<String>,
    onDismiss: () -> Unit,
) {
    val route = draws
        .filter { it.matchId in highlightedMatchIds }
        .sortedBy { it.roundOrder }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.15f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player,
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                route.forEach { draw ->
                    val opponent = when {
                        draw.slotTop.equals(player, ignoreCase = true) -> draw.slotBottom
                        draw.slotBottom.equals(player, ignoreCase = true) -> draw.slotTop
                        else -> "TBD"
                    }
                    val dateInfo = if (draw.scheduledDate != "TBD" && draw.scheduledDate.isNotBlank()) {
                        "  ${draw.scheduledDate} ${draw.scheduledTime} UK"
                    } else ""
                    Text(
                        text = "• ${draw.round}: vs $opponent$dateInfo",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
            TextButton(onClick = onDismiss) {
                Text("Clear", color = AccentBlue)
            }
        }
    }
}

@Composable
private fun BracketCanvas(
    draws: List<DrawEntry>,
    yPositions: Map<String, Float>,
    highlightedMatchIds: Set<String>,
    selectedPlayer: String?,
    onPlayerTap: (String?) -> Unit,
) {
    if (draws.isEmpty()) return

    val byRound = draws.groupBy { it.roundOrder }.toSortedMap()
    val minY = yPositions.values.minOrNull() ?: 0f

    // Precompute card bounding boxes in logical space for tap detection.
    // Recomputed only when draws/yPositions change (not on scale/offset changes).
    data class CardHit(val draw: DrawEntry, val lx: Float, val ly: Float)
    val cardHits: List<CardHit> = remember(draws, yPositions) {
        draws.map { match ->
            val roundOrder = match.roundOrder
            val lx = CANVAS_PADDING + (roundOrder - 1) * (CARD_W + ROUND_GAP)
            val yIdx = (yPositions[match.matchId] ?: 0f) - minY
            val ly = CANVAS_PADDING + yIdx * (CARD_H + SLOT_GAP)
            CardHit(match, lx, ly)
        }
    }

    var scale by remember { mutableFloatStateOf(0.85f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(0.2f, 4f)
        scale = newScale
        offset += panChange
    }

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .transformable(transformState)
            .pointerInput(cardHits, scale, offset) {
                detectTapGestures { tap ->
                    // Convert screen tap to logical coordinates.
                    // withTransform in the Canvas does: translate(offset) then scale(scale, scale, Offset.Zero)
                    // so: screenPos = offset + logPos * scale → logPos = (screenPos - offset) / scale
                    val logX = (tap.x - offset.x) / scale
                    val logY = (tap.y - offset.y) / scale

                    var hit: String? = null
                    for (card in cardHits) {
                        if (logX in card.lx..(card.lx + CARD_W) && logY in card.ly..(card.ly + CARD_H)) {
                            val midY = card.ly + CARD_H / 2f
                            val slot = if (logY < midY) card.draw.slotTop else card.draw.slotBottom
                            if (slot.isNotBlank() && slot != "TBD" && !slot.startsWith("Winner of")) {
                                hit = slot
                            }
                            break
                        }
                    }
                    onPlayerTap(hit)
                }
            },
    ) {
        withTransform(
            transformBlock = {
                translate(offset.x, offset.y)
                scale(scale, scale, Offset.Zero)
            },
        ) {
            drawBracket(
                byRound = byRound,
                yPositions = yPositions,
                minY = minY,
                highlightedMatchIds = highlightedMatchIds,
                selectedPlayer = selectedPlayer,
                textMeasurer = textMeasurer,
            )
        }
    }
}

private fun DrawScope.drawBracket(
    byRound: Map<Int, List<DrawEntry>>,
    yPositions: Map<String, Float>,
    minY: Float,
    highlightedMatchIds: Set<String>,
    selectedPlayer: String?,
    textMeasurer: TextMeasurer,
) {
    val matchById = byRound.values.flatten().associateBy { it.matchId }
    val maxRound = byRound.keys.maxOrNull() ?: 1

    // Pass 1: connector lines (drawn first so cards appear on top)
    for ((roundOrder, roundMatches) in byRound) {
        for (match in roundMatches) {
            if (match.feedsIntoMatchId.isBlank()) continue
            val nextMatch = matchById[match.feedsIntoMatchId] ?: continue

            val x1 = CANVAS_PADDING + (roundOrder - 1) * (CARD_W + ROUND_GAP) + CARD_W
            val y1 = CANVAS_PADDING + ((yPositions[match.matchId] ?: 0f) - minY) * (CARD_H + SLOT_GAP) + CARD_H / 2f
            val x2 = CANVAS_PADDING + roundOrder * (CARD_W + ROUND_GAP)
            val y2 = CANVAS_PADDING + ((yPositions[nextMatch.matchId] ?: 0f) - minY) * (CARD_H + SLOT_GAP) + CARD_H / 2f

            val isHighlighted = match.matchId in highlightedMatchIds && nextMatch.matchId in highlightedMatchIds
            val lineColor = if (isHighlighted) AccentBlue.copy(alpha = 0.8f) else Color(0xFF333344)
            val lineWidth = if (isHighlighted) 3f else 1.5f

            val midX = x1 + ROUND_GAP / 2f
            drawLine(lineColor, Offset(x1, y1), Offset(midX, y1), lineWidth)
            drawLine(lineColor, Offset(midX, y1), Offset(midX, y2), lineWidth)
            drawLine(lineColor, Offset(midX, y2), Offset(x2, y2), lineWidth)
        }
    }

    // Pass 2: match cards + round labels
    for ((roundOrder, roundMatches) in byRound) {
        val cardX = CANVAS_PADDING + (roundOrder - 1) * (CARD_W + ROUND_GAP)
        val roundName = roundMatches.firstOrNull()?.round ?: "R$roundOrder"
        val labelStyle = SpanStyle(color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        val labelText = textMeasurer.measure(
            buildAnnotatedString { withStyle(labelStyle) { append(roundName.uppercase()) } }
        )
        drawText(
            labelText,
            topLeft = Offset(cardX + (CARD_W - labelText.size.width) / 2f, CANVAS_PADDING - 24f),
        )

        for (match in roundMatches) {
            val yIdx = (yPositions[match.matchId] ?: 0f) - minY
            val cardY = CANVAS_PADDING + yIdx * (CARD_H + SLOT_GAP)
            val isHighlighted = match.matchId in highlightedMatchIds
            val isFinal = roundOrder == maxRound
            drawMatchCard(
                match = match,
                x = cardX,
                y = cardY,
                isHighlighted = isHighlighted,
                selectedPlayer = selectedPlayer,
                textMeasurer = textMeasurer,
                isFinal = isFinal,
            )
        }
    }
}

private fun DrawScope.drawMatchCard(
    match: DrawEntry,
    x: Float,
    y: Float,
    isHighlighted: Boolean,
    selectedPlayer: String?,
    textMeasurer: TextMeasurer,
    isFinal: Boolean,
) {
    val cardBg = if (isHighlighted) Color(0xFF192638) else Color(0xFF1C1C28)
    val borderCol = when {
        isHighlighted -> AccentBlue.copy(alpha = 0.7f)
        match.winner.isNotBlank() -> Color(0xFF254525)
        else -> Color(0xFF2A2A3A)
    }

    drawRoundRect(color = cardBg, topLeft = Offset(x, y), size = Size(CARD_W, CARD_H), cornerRadius = CornerRadius(10f))
    drawRoundRect(color = borderCol, topLeft = Offset(x, y), size = Size(CARD_W, CARD_H), cornerRadius = CornerRadius(10f), style = Stroke(1.5f))
    drawLine(Color(0xFF2A2A3A), Offset(x + 8f, y + CARD_H / 2f), Offset(x + CARD_W - 8f, y + CARD_H / 2f), 1f)

    val topWon = match.winner.isNotBlank() && match.winner.equals(match.slotTop, ignoreCase = true)
    val botWon = match.winner.isNotBlank() && match.winner.equals(match.slotBottom, ignoreCase = true)

    fun slotTextColor(slot: String, won: Boolean): Color = when {
        won -> AccentGreen
        selectedPlayer != null && slot.equals(selectedPlayer, ignoreCase = true) -> AccentBlue
        slot.startsWith("Winner of") || slot.isBlank() || slot == "TBD" -> TextMuted
        else -> TextPrimary
    }

    val slotH = CARD_H / 2f
    val pad = 10f

    fun drawSlot(label: String, color: Color, slotY: Float, bold: Boolean) {
        val clipped = if (label.length > 25) label.take(24) + "…" else label
        val style = SpanStyle(color = color, fontSize = 12.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        val measured = textMeasurer.measure(buildAnnotatedString { withStyle(style) { append(clipped) } })
        drawText(measured, topLeft = Offset(x + pad, slotY + (slotH - measured.size.height) / 2f))
    }

    drawSlot(match.slotTop.ifBlank { "TBD" }, slotTextColor(match.slotTop, topWon), y, topWon)
    drawSlot(match.slotBottom.ifBlank { "TBD" }, slotTextColor(match.slotBottom, botWon), y + slotH, botWon)

    // Winner indicator dot on the right edge
    if (match.winner.isNotBlank()) {
        val dotY = if (topWon) y + slotH / 2f else y + slotH + slotH / 2f
        drawCircle(AccentGreen, radius = 4f, center = Offset(x + CARD_W - 12f, dotY))
    }

    // Session / date info below the card (small)
    val dateLabel = when {
        match.scheduledDate.isNotBlank() && match.scheduledDate != "TBD" ->
            "${match.scheduledDate}${if (match.scheduledTime.isNotBlank() && match.scheduledTime != "TBD") " · ${match.scheduledTime}" else ""}"
        else -> ""
    }
    if (dateLabel.isNotBlank()) {
        val infoStyle = SpanStyle(color = TextMuted, fontSize = 9.sp)
        val measured = textMeasurer.measure(buildAnnotatedString { withStyle(infoStyle) { append(dateLabel) } })
        drawText(measured, topLeft = Offset(x + (CARD_W - measured.size.width) / 2f, y + CARD_H + 4f))
    }
}
