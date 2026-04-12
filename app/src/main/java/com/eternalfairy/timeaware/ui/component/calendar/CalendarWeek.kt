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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ripple
import com.eternalfairy.timeaware.ui.data.UserInput
import com.eternalfairy.timeaware.ui.theme.CALENDAR_DATE_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.HEADER_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.SECONDARY_HEADER_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.White
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class ListDirection {
    START,
    END
}

const val NUMBER_OF_DAYS_IN_WEEK = 7

@Composable
fun CalendarWeek(
//    componentHeight: Dp = 90.dp,
    componentWidth: Dp = 400.dp,
    paddingStart: Dp = 10.dp,
    paddingTop: Dp = 5.dp,
    paddingEnd: Dp = 10.dp,
    paddingBottom: Dp = 5.dp,
    userInput: UserInput,
//    onSetDate: (OffsetDateTime) -> Unit
    onSetDate: (LocalDate) -> Unit
) {
    var circledDate by remember { mutableStateOf(userInput.selectedDate) }
    // The index of the circled date in a week
    var circledDateIndex by remember { mutableIntStateOf(0) }
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("d")
    val daysOfWeek = mutableListOf<String>()
    for (dayOfWeek in DayOfWeek.entries) {
        val localizedDayName = dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())

        daysOfWeek += localizedDayName
    }
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

    fun getWeek (date: LocalDate = LocalDate.now()) : List<LocalDate> {
        // Gives the first day of the week (Monday) in which the given day is
        val firstDayOfTheWeek = date.with(DayOfWeek.MONDAY)
        val day = firstDayOfTheWeek
        val week = List<LocalDate>(
            size = 7,
            init = { index ->
                day.plusDays(index.toLong())
            }
        )

        return week
    }

    fun getNextWeek (date: LocalDate = LocalDate.now()) : List<LocalDate> {
        return getWeek(date.plusWeeks(1))
    }

    fun getPreviousWeek (date: LocalDate = LocalDate.now()) : List<LocalDate> {
        return getWeek(date.minusWeeks(1))
    }

    var weeks by remember { mutableStateOf(listOf(getWeek(date = userInput.selectedDate))) }

    fun loadMore (direction: ListDirection) : Unit {
        // Append more data to the beginning of the list
        if (direction == ListDirection.START) {
            val week = getPreviousWeek(weeks[0][0])
            val newWeeks = List(
                size = (weeks.size + 1),
                init = { index ->
                    if (index == 0) {
                        week
                    } else {
                        weeks[index - 1]
                    }
                }
            )
            weeks = newWeeks
        }
        // Append more data to the end of the list
        else if (direction == ListDirection.END) {
            val week = getNextWeek(weeks[weeks.size - 1][0])
            val newWeeks = List(
                size = (weeks.size + 1),
                init = { index ->
                    if (index == weeks.size) {
                        week
                    } else {
                        weeks[index]
                    }
                }
            )
            weeks = newWeeks
        }
    }

    fun setInitialCircledDateIndex () {
        weeks.forEachIndexed { weekIndex, week ->
            week.forEachIndexed { dayIndex, day ->
                if (day == userInput.selectedDate) {
                    circledDateIndex = dayIndex
                }
            }
        }
    }

    fun setCircledDateOnScroll () {
        val visibleItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()
        if (visibleItem != null) {
            val date = weeks[visibleItem.index][circledDateIndex]
            circledDate = date
        }
    }

    fun setCircledDateOnClick (index: Int, date: LocalDate) {
        circledDateIndex = index
        circledDate = date

        onSetDate(date)
    }

    LaunchedEffect(true) {
        setInitialCircledDateIndex()
    }

    // TODO: Add a button to scroll to today

    // Set the new circled date when user scrolls
    LaunchedEffect(key1 = listState.firstVisibleItemIndex) {
        setCircledDateOnScroll()
    }

    // Load more if scrolled to end or start of the list
    LaunchedEffect(key1 = reachedListStart, key2 = reachedListEnd) {
        if (reachedListStart) loadMore(ListDirection.START)
        if (reachedListEnd) loadMore(ListDirection.END)
    }

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

    Column (
        modifier = Modifier
            .padding(
                start = paddingStart, top = paddingTop, end = paddingEnd, bottom = paddingBottom
            )
            .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
            .width(componentWidth)
            .background(COMPONENT_BACKGROUND_COLOR)
    ) {
        // Circled date
        Row (
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
            ,
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text (
                text = circledDate.format(DateTimeFormatter.ofPattern("d. MMMM yyyy")),
                textAlign = TextAlign.Center,
                color = HEADER_TEXT_COLOR,
                fontSize = 18.sp
            )
        }

        // Weekday names
        Row (
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(NUMBER_OF_DAYS_IN_WEEK) { iteration ->
                Box (
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(5.dp)
                ) {
                    Text (
                        text = daysOfWeek[iteration],
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
        }

        // Week days
        LazyRow (
            modifier = Modifier
                .fillMaxWidth(),
            state = listState,
            flingBehavior = flingBehaviour
        ) {
            items(items = weeks, key = { it }) { week ->
                Row (
                    modifier = Modifier
                        .fillParentMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(NUMBER_OF_DAYS_IN_WEEK) { iteration ->
                        Box (
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(5.dp)
                                .conditional(week[iteration] == today, todaySelectedModifier)
                                .conditional(iteration != circledDateIndex && week[iteration] == today, todayNotSelectedModifier)
                                .conditional(iteration == circledDateIndex && week[iteration] != today, selectedDayModifier)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = {
                                        setCircledDateOnClick(
                                            index = iteration,
                                            date = week[iteration]
                                        )
                                    }
                                )
                        ) {
                            Text (
                                text = week[iteration].format(formatter),
                                modifier = Modifier
                                    .align(Alignment.Center),
                                color = if (week[iteration] == today) White else CALENDAR_DATE_TEXT_COLOR
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.conditional (condition: Boolean, modifier: Modifier) : Modifier {
    return if (condition) {
        then(modifier)
    } else {
        this
    }
}