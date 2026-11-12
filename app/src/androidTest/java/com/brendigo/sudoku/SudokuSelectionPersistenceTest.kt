package com.brendigo.sudoku

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.brendigo.sudoku.data.SudokuController
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A cell-selection change must survive a fresh controller, not only an
 * Activity recreation reading the in-process pending-state queue.
 */
@RunWith(AndroidJUnit4::class)
class SudokuSelectionPersistenceTest {
    @Test fun selectedCellPersistsAndPausedSelectionCannotMutateTheBoard() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("sudoku_state_v1", Context.MODE_PRIVATE)
        // Other instrumentation tests share the application's preferences.
        // Clean both ends to avoid test-order dependence.
        check(prefs.edit().clear().commit())
        try {
            val controller = SudokuController(context)
            controller.startNew(size = 4, level = 0)
            assertTrue(controller.flush())

            val starting = requireNotNull(controller.game)
            val target = starting.board.indices.first {
                starting.isEditable(it) && it != starting.selected
            }
            controller.select(target)
            assertEquals(target, requireNotNull(controller.game).selected)
            assertTrue("Selection checkpoint failed", controller.flush())

            val restored = SudokuController(context)
            assertEquals(target, requireNotNull(restored.game).selected)
            restored.togglePause()
            val other = starting.board.indices.first { it != target }
            restored.select(other)
            assertEquals("Paused selection changed unexpectedly", target,
                requireNotNull(restored.game).selected)
            assertTrue(restored.flush())
            assertEquals(target, requireNotNull(SudokuController(context).game).selected)
        } finally {
            check(prefs.edit().clear().commit())
        }
    }
}
