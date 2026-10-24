package com.brendigo.sudoku.data

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DailyProgressTest {
    @Test fun duplicateDateCannotEarnSecondReward() {
        val day = LocalDate.of(2026, 10, 24)
        val first = DailyProgress.complete(day, "", 0, 0, emptyList())
        assertTrue(first.awarded)
        val second = DailyProgress.complete(day, first.lastDay, first.currentStreak,
            first.bestStreak, first.completedDays)
        assertFalse(second.awarded)
        assertEquals(1, second.completedDays.size)
    }

    @Test fun consecutiveDaysIncreaseCurrentAndBestStreak() {
        val older = LocalDate.of(2026, 10, 23)
        val outcome = DailyProgress.complete(older.plusDays(1),
            older.toString(), 6, 6, listOf(older.toString()))
        assertTrue(outcome.awarded)
        assertEquals(7, outcome.currentStreak)
        assertEquals(7, outcome.bestStreak)
    }

    @Test fun lateFinishDoesNotClaimNextDayOrOverwriteRecentStreak() {
        val yesterday = LocalDate.of(2026, 10, 23)
        val today = yesterday.plusDays(1)
        val result = DailyProgress.complete(yesterday, today.toString(), 3, 5,
            listOf(today.toString()))
        assertTrue(result.awarded)
        assertEquals(today.toString(), result.lastDay)
        assertEquals(3, result.currentStreak)
        assertEquals(5, result.bestStreak)
        assertEquals(2, result.completedDays.size)
    }

    @Test fun skippedDayResetsCurrentStreakButRetainsRecord() {
        val old = LocalDate.of(2026, 10, 20)
        val updated = DailyProgress.complete(old.plusDays(4), old.toString(), 8, 8,
            listOf(old.toString()))
        assertEquals(1, updated.currentStreak)
        assertEquals(8, updated.bestStreak)
    }

    @Test fun previousDaysRemainClaimableIndependentlyOfCurrentDate() {
        val day = LocalDate.of(2026, 10, 23)
        val yesterday = DailyProgress.complete(day, "", 0, 0, emptyList())
        val today = DailyProgress.complete(day.plusDays(1), yesterday.lastDay,
            yesterday.currentStreak, yesterday.bestStreak, yesterday.completedDays)
        assertTrue(today.awarded)
        assertEquals(2, today.completedDays.size)
        assertEquals(2, today.currentStreak)
    }
}
