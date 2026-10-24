package com.brendigo.sudoku.data

import java.time.LocalDate

/** Calendar-day identity for offline daily rewards, independent from device clock at finish. */
object DailyProgress {
    data class Result(
        val awarded: Boolean,
        val lastDay: String,
        val currentStreak: Int,
        val bestStreak: Int,
        val completedDays: List<String>,
    )

    fun complete(
        puzzleDay: LocalDate,
        lastDay: String,
        currentStreak: Int,
        bestStreak: Int,
        completedDays: List<String>,
    ): Result {
        val key = puzzleDay.toString()
        val alreadyWon = key in completedDays || key == lastDay
        if (alreadyWon) return Result(false, lastDay, currentStreak,
            bestStreak, completedDays)

        val previous = runCatching { LocalDate.parse(lastDay) }.getOrNull()
        val isNewest = previous == null || puzzleDay.isAfter(previous)
        val updatedStreak = if (isNewest) {
            if (previous == puzzleDay.minusDays(1)) currentStreak + 1 else 1
        } else currentStreak
        return Result(
            awarded = true,
            lastDay = if (isNewest) key else lastDay,
            currentStreak = updatedStreak,
            bestStreak = maxOf(bestStreak, updatedStreak),
            completedDays = (completedDays + key).distinct().takeLast(4000),
        )
    }
}
