package com.brendigo.sudoku

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.brendigo.sudoku.data.SudokuController
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Regression for the Erase button with auto-generated pencil candidates. */
@RunWith(AndroidJUnit4::class)
class SudokuAutoNotesEraseTest {
    @Test fun emptyCellWithAutoNotesCannotCreatePhantomUndoMove() = runBlocking {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val backing = base.getSharedPreferences(
            "sudoku_auto_notes_erase_v1071_test", Context.MODE_PRIVATE
        )
        check(backing.edit().clear().commit())
        val context = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = backing
        }
        try {
            val controller = SudokuController(context)
            controller.updateOptions(controller.options.copy(autoNotes = true))
            controller.startNew(4, 0)
            val puzzle = requireNotNull(controller.game)
            val cell = puzzle.board.indices.first {
                puzzle.isEditable(it) && puzzle.notes[it] != 0
            }
            controller.select(cell)
            val initial = requireNotNull(controller.game)
            val originalMask = initial.notes[cell]
            assertFalse("Erasing automatic candidates cannot change the board", controller.canErase())
            assertFalse(controller.canUndo())

            controller.erase()
            val afterNoOp = requireNotNull(controller.game)
            assertEquals(originalMask, afterNoOp.notes[cell])
            assertEquals(0, afterNoOp.board[cell])
            assertFalse("A no-op must not push Undo history", controller.canUndo())

            controller.enter(puzzle.solution[cell])
            assertTrue("A filled editable cell can be erased", controller.canErase())
            controller.erase()
            assertEquals(0, requireNotNull(controller.game).board[cell])
            assertEquals(originalMask,
                requireNotNull(controller.game).notes[cell])
            assertTrue(controller.canUndo())
            controller.undo()
            assertEquals(puzzle.solution[cell],
                requireNotNull(controller.game).board[cell])
            assertTrue(controller.flush())

            val restarted = SudokuController(context)
            assertTrue(restarted.options.autoNotes)
            assertEquals(puzzle.solution[cell], requireNotNull(restarted.game).board[cell])
        } finally {
            check(backing.edit().clear().commit())
        }
    }

    @Test fun manualNotesCanStillBeErasedWithAutoNotesDisabled() = runBlocking {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val backing = base.getSharedPreferences(
            "sudoku_manual_notes_erase_v1071_test", Context.MODE_PRIVATE
        )
        check(backing.edit().clear().commit())
        val context = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = backing
        }
        try {
            val controller = SudokuController(context)
            controller.startNew(4, 0)
            val puzzle = requireNotNull(controller.game)
            val cell = puzzle.board.indices.first { puzzle.isEditable(it) }
            controller.select(cell)
            controller.toggleNotes()
            controller.enter(puzzle.solution[cell])
            assertEquals(0, requireNotNull(controller.game).board[cell])
            assertTrue(controller.canErase())
            controller.erase()
            assertEquals(0, requireNotNull(controller.game).notes[cell])
            assertFalse(controller.canErase())
            assertTrue(controller.flush())
        } finally {
            check(backing.edit().clear().commit())
        }
    }
}
