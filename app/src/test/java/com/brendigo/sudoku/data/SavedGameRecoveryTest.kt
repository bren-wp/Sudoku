package com.brendigo.sudoku.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.time.LocalDate

class SavedGameRecoveryTest {
    private val today = LocalDate.of(2026, 10, 28)

    private fun session(
        daily: Boolean = false,
        day: String = "",
        completed: Boolean = false,
        failed: Boolean = false,
    ): Session = Session(
        size = 4, boxRows = 2, boxColumns = 2, level = 0,
        solution = intArrayOf(1, 2, 3, 4, 3, 4, 1, 2, 2, 1, 4, 3, 4, 3, 2, 1),
        givens = IntArray(16), board = IntArray(16), notes = IntArray(16),
        daily = daily, day = day, completed = completed, failed = failed,
    )

    @Test fun currentSnapshotAndItsUndoTakePrecedence() {
        val primary = session()
        val restored = SavedGameRecovery.choose(
            primary, session(), session(true, today.toString()), today,
        )
        assertSame(primary, restored.session)
        assertEquals("undo", restored.undoKey)
    }

    @Test fun missingPrimaryRestoresUnfinishedRegularGameAndItsUndo() {
        val regular = session()
        val restored = SavedGameRecovery.choose(null, regular, null, today)
        assertSame(regular, restored.session)
        assertEquals("regular_undo", restored.undoKey)
    }

    @Test fun dailyCanBeRecoveredOnlyForToday() {
        val daily = session(daily = true, day = today.toString())
        val restored = SavedGameRecovery.choose(null, null, daily, today)
        assertSame(daily, restored.session)
        assertEquals("daily_undo", restored.undoKey)

        assertNull(SavedGameRecovery.choose(null, null, daily, today.plusDays(1)).session)
    }

    @Test fun completedAndFailedSavesNeverResumeAsActiveGames() {
        val complete = session(completed = true)
        val failed = session(daily = true, day = today.toString(), failed = true)
        val restored = SavedGameRecovery.choose(null, complete, failed, today)
        assertNull(restored.session)
        assertEquals("undo", restored.undoKey)
    }

    @Test fun aMisclassifiedNamedSnapshotCannotOverridePrimary() {
        val mislabeled = session(daily = true, day = today.toString())
        val restored = SavedGameRecovery.choose(null, mislabeled, null, today)
        assertNull(restored.session)
    }

    @Test fun finishedPrimaryDoesNotHideUnfinishedRegularPuzzle() {
        val finishedPrimary = session(completed = true)
        val unfinishedRegular = session()
        val restored = SavedGameRecovery.choose(
            finishedPrimary, unfinishedRegular, null, today,
        )
        assertSame(unfinishedRegular, restored.session)
        assertEquals("regular_undo", restored.undoKey)
    }

    @Test fun expiredDailyPrimaryDoesNotHideCurrentDayChallenge() {
        val expired = session(daily = true, day = today.minusDays(1).toString())
        val current = session(daily = true, day = today.toString())
        val restored = SavedGameRecovery.choose(expired, null, current, today)
        assertSame(current, restored.session)
        assertEquals("daily_undo", restored.undoKey)
    }

    @Test fun completedPrimaryIsPreservedIfNoOtherPuzzleCanResume() {
        val finishedPrimary = session(completed = true)
        val restored = SavedGameRecovery.choose(finishedPrimary, null, null, today)
        assertSame(finishedPrimary, restored.session)
        assertEquals("undo", restored.undoKey)
    }
}
