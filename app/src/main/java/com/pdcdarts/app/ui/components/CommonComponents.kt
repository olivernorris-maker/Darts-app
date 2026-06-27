package com.pdcdarts.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdcdarts.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val pullState = rememberPullToRefreshState()

    LaunchedEffect(pullState.isRefreshing) {
        if (pullState.isRefreshing) onRefresh()
    }
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) pullState.endRefresh()
    }

    Box(modifier = modifier.nestedScroll(pullState.nestedScrollConnection)) {
        content()
        PullToRefreshContainer(
            state = pullState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = DarkCard,
            contentColor = AccentGreen,
        )
    }
}

@Composable
fun LastUpdatedBar(
    lastUpdatedMs: Long,
    fromCache: Boolean,
    modifier: Modifier = Modifier,
) {
    if (lastUpdatedMs == 0L) return
    val sdf = SimpleDateFormat("d MMM, HH:mm", Locale.UK)
    sdf.timeZone = TimeZone.getTimeZone("Europe/London")
    val label = buildString {
        append("Updated ")
        append(sdf.format(Date(lastUpdatedMs)))
        if (fromCache) append(" · cached")
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        if (fromCache) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
    }
}

@Composable
fun CategoryChip(category: String, modifier: Modifier = Modifier) {
    val color = categoryColor(category)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.18f),
    ) {
        Text(
            text = category,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val (color, label) = when (status.lowercase()) {
        "live" -> AccentGreen to "LIVE"
        "completed" -> TextSecondary to "FT"
        "upcoming" -> AccentAmber to "Soon"
        "scheduled" -> AccentAmber to "Sched."
        else -> TextMuted to status
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.18f),
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = TextMuted,
        letterSpacing = 1.sp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
fun ErrorMessage(message: String, onRetry: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

fun categoryColor(category: String): Color = when (category.lowercase()) {
    "major" -> CategoryMajor
    "protour" -> CategoryProTour
    "eurotour" -> CategoryEuroTour
    "premierleague" -> CategoryPremierLeague
    "modus" -> CategoryModus
    "womens" -> CategoryWomens
    else -> CategoryOther
}

fun formatUkDateTime(date: String, time: String): String {
    if (date.isBlank() || date == "TBD") return "Date TBD"
    return buildString {
        append(date)
        if (time.isNotBlank() && time != "TBD") {
            append(" · ")
            append(time)
            append(" UK")
        }
    }
}
