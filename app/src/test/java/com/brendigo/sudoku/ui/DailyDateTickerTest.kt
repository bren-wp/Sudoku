package com.brendigo.sudoku.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class DailyDateTickerTest {
    private val zagreb = ZoneId.of("Europe/Zagreb")

    @Test fun nearMidnightSchedulesARefreshWithoutWaitingAnotherMinute() {
        val justBeforeMidnight = ZonedDateTime.of(
            2026, 10, 25, 23, 59, 59, 0, zagreb
        )
        assertEquals(1_000L, DailyDateTicker.delayUntilDayRefresh(justBeforeMidnight))
    }

    @Test fun normalDayCapsRefreshIntervalForClockAndTimezoneChanges() {
        val noon = ZonedDateTime.of(2026, 10, 25, 12, 0, 0, 0, zagreb)
        assertEquals(60_000L, DailyDateTicker.delayUntilDayRefresh(noon))
    }

    @Test fun daylightSavingTransitionsStillUseLocalCalendarMidnight() {
        val beforeClocksMoveBack = ZonedDateTime.of(
            2026, 10, 25, 1, 30, 0, 0, zagreb
        )
        assertEquals(60_000L, DailyDateTicker.delayUntilDayRefresh(beforeClocksMoveBack))
        val afterTransition = ZonedDateTime.of(
            2026, 10, 25, 23, 59, 58, 0, zagreb
        )
        assertEquals(2_000L, DailyDateTicker.delayUntilDayRefresh(afterTransition))
    }

    @Test fun crossingToTheNewYearUsesTheNewLocalDate() {
        val lastSecond = ZonedDateTime.of(2026, 12, 31, 23, 59, 59, 0, zagreb)
        assertEquals(1_000L, DailyDateTicker.delayUntilDayRefresh(lastSecond))
    }
}
