package com.brendigo.sudoku.data

import com.brendigo.sudoku.engine.SudokuEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Daily puzzles are tied to the date at the moment generation began.
 * A clock or time-zone change during a suspended generator must never
 * label an old seeded puzzle as the newly selected day's challenge.
 *
 * The current-day provider and creator are injected for deterministic tests.
 * The production creator remains offline and runs on Dispatchers.Default.
 */
internal object DailyPuzzleGeneration {
    suspend fun create(
        size: Int,
        level: Int,
        day: LocalDate,
        currentDay: () -> LocalDate = { LocalDate.now() },
        creator: suspend (Int, Int, Int) -> SudokuEngine.Puzzle = { grid, difficulty, seed ->
            withContext(Dispatchers.Default) {
                SudokuEngine.create(grid, difficulty, seed)
            }
        },
    ): SudokuEngine.Puzzle? {
        val puzzle = creator(size, level, day.toEpochDay().toInt())
        return puzzle.takeIf { isCurrentDay(day, currentDay) }
    }

    /** Reusable guard: callers must recheck after any later suspendable IO. */
    fun isCurrentDay(
        puzzleDay: LocalDate,
        currentDay: () -> LocalDate = { LocalDate.now() },
    ): Boolean = currentDay() == puzzleDay
}
