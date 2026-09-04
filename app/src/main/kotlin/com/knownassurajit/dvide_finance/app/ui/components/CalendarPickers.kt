package com.knownassurajit.dvide_finance.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

fun weekStartDayOfWeek(weekStartDay: Int): DayOfWeek = when (weekStartDay) {
    Calendar.SUNDAY -> DayOfWeek.SUNDAY
    Calendar.SATURDAY -> DayOfWeek.SATURDAY
    else -> DayOfWeek.MONDAY
}

private fun weekdayLabels(weekStart: DayOfWeek): List<String> =
    (0..6).map { weekStart.plus(it.toLong()).getDisplayName(TextStyle.NARROW, Locale.getDefault()) }

private fun leadingBlanks(month: YearMonth, weekStart: DayOfWeek): Int {
    val first = month.atDay(1).dayOfWeek
    return (first.value - weekStart.value + 7) % 7
}

@Composable
fun PaydayCalendar(
    payday: Int,
    onPaydayChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    weekStartDay: Int = Calendar.MONDAY,
) {
    val month = remember { YearMonth.now() }
    val weekStart = weekStartDayOfWeek(weekStartDay)
    val selected = payday.coerceIn(1, 31)
    val length = month.lengthOfMonth()
    val blanks = leadingBlanks(month, weekStart)
    val cells = blanks + 31

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("onboard_payday_slider"),
        verticalArrangement = Arrangement.spacedBy(DvideDimens.tight),
    ) {
        Text(
            text = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                " · tap a day",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        WeekdayHeader(weekStart)
        CalendarGrid(cellCount = cells) { index ->
            if (index < blanks) {
                Box(modifier = Modifier.aspectRatio(1f))
            } else {
                val day = index - blanks + 1
                val inMonth = day <= length
                DayCell(
                    label = day.toString(),
                    selected = day == selected,
                    inRange = false,
                    muted = !inMonth,
                    onClick = { onPaydayChange(day) },
                )
            }
        }
    }
}

@Composable
fun CycleRangeCalendar(
    start: LocalDate?,
    end: LocalDate?,
    onRangeChange: (start: LocalDate, end: LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    weekStartDay: Int = Calendar.MONDAY,
) {
    var visibleMonth by remember(start) {
        mutableStateOf(YearMonth.from(start ?: LocalDate.now()))
    }
    val weekStart = weekStartDayOfWeek(weekStartDay)
    val blanks = leadingBlanks(visibleMonth, weekStart)
    val length = visibleMonth.lengthOfMonth()
    val cells = blanks + length

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DvideDimens.tight),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { visibleMonth = visibleMonth.minusMonths(1) }) {
                Icon(CwIcons.Back, contentDescription = "Previous month")
            }
            Text(
                text = visibleMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                    " ${visibleMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = { visibleMonth = visibleMonth.plusMonths(1) }) {
                Icon(CwIcons.ChevronRight, contentDescription = "Next month")
            }
        }
        WeekdayHeader(weekStart)
        CalendarGrid(cellCount = cells) { index ->
            if (index < blanks) {
                Box(modifier = Modifier.aspectRatio(1f))
            } else {
                val day = index - blanks + 1
                val date = visibleMonth.atDay(day)
                val isStart = start != null && date == start
                val isEnd = end != null && date == end
                val inRange = start != null && end != null &&
                    !date.isBefore(start) && !date.isAfter(end)
                DayCell(
                    label = day.toString(),
                    selected = isStart || isEnd,
                    inRange = inRange && !isStart && !isEnd,
                    rangeStart = isStart && end != null,
                    rangeEnd = isEnd && start != null && start != end,
                    onClick = {
                        val next = when {
                            start == null || end != null -> date to null
                            date.isBefore(start) -> date to null
                            else -> start to date
                        }
                        onRangeChange(next.first, next.second)
                    },
                )
            }
        }
        Text(
            text = when {
                start != null && end != null -> "Window ${start.dayOfMonth}–${end.dayOfMonth}"
                start != null -> "Start ${start.dayOfMonth} · tap an end date"
                else -> "Tap a start date, then an end date"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WeekdayHeader(weekStart: DayOfWeek) {
    Row(modifier = Modifier.fillMaxWidth()) {
        weekdayLabels(weekStart).forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    cellCount: Int,
    cell: @Composable (index: Int) -> Unit,
) {
    val rows = (cellCount + 6) / 7
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val index = row * 7 + col
                    Box(modifier = Modifier.weight(1f)) {
                        if (index < cellCount) cell(index)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    label: String,
    selected: Boolean,
    inRange: Boolean,
    muted: Boolean = false,
    rangeStart: Boolean = false,
    rangeEnd: Boolean = false,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = when {
        rangeStart -> RoundedCornerShape(topStart = 999.dp, bottomStart = 999.dp)
        rangeEnd -> RoundedCornerShape(topEnd = 999.dp, bottomEnd = 999.dp)
        inRange -> RoundedCornerShape(0.dp)
        else -> CircleShape
    }
    val bg = when {
        selected -> scheme.primary
        inRange -> scheme.primaryContainer
        else -> androidx.compose.ui.graphics.Color.Transparent
    }
    val fg = when {
        selected -> scheme.onPrimary
        muted -> scheme.onSurfaceVariant.copy(alpha = 0.55f)
        else -> scheme.onSurface
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(2.dp)
            .clip(shape)
            .background(bg)
            .semantics {
                this.selected = selected
                contentDescription = "Day $label"
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = fg,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
