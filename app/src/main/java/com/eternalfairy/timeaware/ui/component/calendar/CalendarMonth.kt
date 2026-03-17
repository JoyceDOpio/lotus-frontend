package com.eternalfairy.timeaware.ui.component.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ripple
import com.eternalfairy.timeaware.ui.theme.CALENDAR_DATE_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.CALENDAR_PAST_DATE_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.HEADER_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.SECONDARY_HEADER_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.White
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
private fun WeekDayCell (
    modifier: Modifier = Modifier,
    weekDay: Int
) {
    val text = DayOfWeek.of(weekDay).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())

    Box (
        modifier = modifier
            .aspectRatio(1f)
    ) {
        Text(
            text = text.orEmpty(),
            color = CALENDAR_DATE_TEXT_COLOR,
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun CalendarCell (
    modifier: Modifier = Modifier,
    date: LocalDate? = null,
    isSelected: Boolean = false,
    isToday: Boolean = false,
    onClick: (LocalDate) -> Unit = {},
) {
    val formatter = DateTimeFormatter.ofPattern("d")
    val text = date?.format(formatter)
    val textColor = date?.let { if (isToday) White else {
        if (date.isBefore(LocalDate.now())) CALENDAR_PAST_DATE_TEXT_COLOR else CALENDAR_DATE_TEXT_COLOR
    } } ?: Color.Transparent

    val todaySelectedModifier = Modifier
        .clip(CircleShape)
        .background(HEADER_TEXT_COLOR)

    val todayNotSelectedModifier = Modifier
        .clip(CircleShape)
        .background(SECONDARY_HEADER_TEXT_COLOR)

    val selectedDayModifier = Modifier
        .border(
            width = 1.dp,
            color = SECONDARY_HEADER_TEXT_COLOR,
            shape = CircleShape
        )

    date?.let {
        Box (
            modifier = Modifier
                .aspectRatio(1f)
                .conditional(isSelected && !isToday, selectedDayModifier)
                .conditional(isToday && !isSelected, todayNotSelectedModifier)
                .conditional(isSelected && isToday, todaySelectedModifier)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true),
                    onClick = { onClick(date) }
                )
        ) {
            Text (
                text = text.orEmpty(),
                modifier = Modifier
                    .align(Alignment.Center),
                color = textColor
            )
        }
    } ?: run {
        Box (
            modifier = Modifier
                .aspectRatio(1f)
        ) {
            Text (
                text = text.orEmpty(),
                modifier = Modifier
                    .align(Alignment.Center),
                color = textColor
            )
        }
    }
}

@Composable
fun CalendarGrid (
//    componentHeight: Dp = 320.dp,
//    componentWidth: Dp = 400.dp,
//    paddingStart: Dp = 10.dp,
//    paddingTop: Dp = 5.dp,
//    paddingEnd: Dp = 10.dp,
//    paddingBottom: Dp = 5.dp,
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onSetDate: (LocalDate) -> Unit,
    yearMonth: YearMonth = YearMonth.now()
) {
    fun checkIfLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    val month = yearMonth.month
    val year = yearMonth.year
    val isLeapYear = checkIfLeapYear(year)
    val daysInMonth = month.length(isLeapYear)

    val firstDayOfMonth = LocalDate.of(year, month, 1)
    val lastDayOfMonth = LocalDate.of(year, month, daysInMonth)

    val monthDates = mutableListOf<LocalDate?>()

    // Add days before the start of the given month to fill in the week
    val daysBefore = when (firstDayOfMonth.dayOfWeek) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
    }

    // Add days after the end of the given month to fill in the week
    val daysAfter = when (lastDayOfMonth.dayOfWeek) {
        DayOfWeek.MONDAY -> 6
        DayOfWeek.TUESDAY -> 5
        DayOfWeek.WEDNESDAY -> 4
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 2
        DayOfWeek.SATURDAY -> 1
        DayOfWeek.SUNDAY -> 0
    }

    repeat(daysBefore) {
        monthDates.add(null)
    }

    (1..daysInMonth).forEach { day ->
        monthDates.add(LocalDate.of(year, month, day))
    }

    repeat(daysAfter) {
        monthDates.add(null)
    }

    Column (
        modifier = modifier
            .fillMaxWidth()
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        var dateIndex = 0

        repeat (monthDates.size / 7) {
            Row (
                modifier = modifier
                    .fillMaxWidth()
                    // INFO: Without .weight(1f) the rows stretch vertically
                    .weight(1f)
                ,
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat (NUMBER_OF_DAYS_IN_WEEK) {

                    monthDates[dateIndex]?.let {
                        CalendarCell(
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                            ,
                            date = monthDates[dateIndex],
                            isSelected = monthDates[dateIndex]?.isEqual(selectedDate)
                                ?: false,
                            isToday = monthDates[dateIndex]?.isEqual(LocalDate.now()) ?: false,
                            onClick = onSetDate
                        )
                    } ?: run {
                        CalendarCell(
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        )
                    }

                    dateIndex++
                }
            }
        }
    }
}

@Composable
fun CalendarMonth (
    componentHeight: Dp = 320.dp,
    componentWidth: Dp = 400.dp,
    paddingStart: Dp = 10.dp,
    paddingTop: Dp = 5.dp,
    paddingEnd: Dp = 10.dp,
    paddingBottom: Dp = 5.dp,
    selectedDate: LocalDate,
    onSetDate: (LocalDate) -> Unit
) {
    val listState = rememberLazyListState()
    val flingBehaviour = rememberSnapFlingBehavior(
        lazyListState = listState,
        snapPosition = SnapPosition.Center
    )
    val reachedListEnd by remember {
        derivedStateOf {
            // Check if the visible item is the last item in the list
            val visibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            visibleItem?.index == listState.layoutInfo.totalItemsCount - 1
        }
    }
    val reachedListStart by remember {
        derivedStateOf {
            // Check if the visible item is the first item in the list
            val visibleItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()
            visibleItem?.index == 0
        }
    }

    fun getNextYearMonth (yearMonth: YearMonth = YearMonth.now()) : YearMonth {
        return yearMonth.plusMonths(1)
    }

    fun getPreviousYearMonth (yearMonth: YearMonth = YearMonth.now()) : YearMonth {
        return yearMonth.minusMonths(1)
    }

    var yearMonths by remember { mutableStateOf(listOf(YearMonth.now())) }

    fun loadMore (direction: ListDirection) : Unit {
        // Append more data to the beginning of the list
        if (direction == ListDirection.START) {
            val yearMonth = getPreviousYearMonth(yearMonths[0])
            val newYearMonths = List(
                size = (yearMonths.size + 1),
                init = { index ->
                    if (index == 0) {
                        yearMonth
                    } else {
                        yearMonths[index - 1]
                    }
                }
            )
            yearMonths = newYearMonths
        }
        // Append more data to the end of the list
        else if (direction == ListDirection.END) {
            val yearMonth = getNextYearMonth(yearMonths[yearMonths.size - 1])
            val newYearMonths = List(
                size = (yearMonths.size + 1),
                init = { index ->
                    if (index == yearMonths.size) {
                        yearMonth
                    } else {
                        yearMonths[index]
                    }
                }
            )
            yearMonths = newYearMonths
        }
    }

    // Load more if scrolled to end or start of the list
    LaunchedEffect(key1 = reachedListStart, key2 = reachedListEnd) {
        if (reachedListStart) loadMore(ListDirection.START)
        if (reachedListEnd) loadMore(ListDirection.END)
    }

    Column (
        modifier = Modifier
            .padding(
                start = paddingStart,
                top = paddingTop,
                end = paddingEnd,
                bottom = paddingBottom
            )
            .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
            .width(componentWidth)
            .height(componentHeight)
            .background(COMPONENT_BACKGROUND_COLOR)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Month name
        Row (
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
            ,
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text (
                text = yearMonths[if (listState.layoutInfo.totalItemsCount == 1) 0 else listState.firstVisibleItemIndex].format(DateTimeFormatter.ofPattern("MMMM yyyy")),
//                text = yearMonths[listState.layoutInfo.].format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                textAlign = TextAlign.Center,
                color = HEADER_TEXT_COLOR,
                fontSize = 20.sp
            )
        }

        // Weekday names
        Row (
            modifier = Modifier
            ,
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(NUMBER_OF_DAYS_IN_WEEK) { weekDay ->
                WeekDayCell(
                    modifier = Modifier.weight(1f),
                    weekDay = weekDay + 1
                )
            }
        }

        // Dates
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
            ,
            state = listState,
            flingBehavior = flingBehaviour
        ) {
            items(items = yearMonths, key = { it }) { yearMonth ->
                CalendarGrid(
                    modifier = Modifier
                        .weight(1f)
                        // INFO: If I don't set .fillParentMaxWidth(), the ContainerGrid will fill the LazyRow only partially and the given month will overlap with a part of the next month
                        .fillParentMaxWidth()
                    ,
                    selectedDate = selectedDate,
                    onSetDate = onSetDate,
                    yearMonth = yearMonth
                )
            }
        }
    }
}