package com.brendigo.sudoku.data

import com.brendigo.sudoku.engine.SudokuEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.fail
import org.junit.Test
import java.time.LocalDate

/** Reproduces a local-midnight change without touching an emulator's clock. */
class DailyPuzzleGenerationTest {
    private val firstDay = LocalDate.of(2026, 11, 12)

    @Test fun stableDayCreatesTheExpectedDeterministicPuzzle() = runBlocking {
        val generated = DailyPuzzleGeneration.create(
            4, 2, firstDay, currentDay = { firstDay }
        )
        val expected = SudokuEngine.create(4, 2, firstDay.toEpochDay().toInt())
        assertArrayEquals(expected.givens, requireNotNull(generated).givens)
        assertArrayEquals(expected.solution, generated.solution)
    }

    @Test fun dateChangeDuringGenerationDiscardsYesterdayPuzzle() = runBlocking {
        var clock = firstDay
        val generated = DailyPuzzleGeneration.create(
            4, 1, firstDay,
            currentDay = { clock },
            creator = { size, level, seed ->
                assertEquals(firstDay.toEpochDay().toInt(), seed)
                clock = clock.plusDays(1)
                SudokuEngine.create(size, level, seed)
            },
        )
        assertNull(generated)
    }

    @Test fun cancellationIsNotTransformedIntoAPlayablePuzzle() = runBlocking {
        try {
            DailyPuzzleGeneration.create(
                4, 1, firstDay,
                currentDay = { firstDay },
                creator = { _, _, _ -> throw CancellationException("screen destroyed") },
            )
            fail("Cancelled generation must not return a puzzle.")
        } catch (_: CancellationException) {
            // Structured coroutine cancellation must propagate to the caller.
        }
    }
}
