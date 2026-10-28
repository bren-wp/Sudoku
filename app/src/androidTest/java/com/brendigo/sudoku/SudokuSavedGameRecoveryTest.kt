package com.brendigo.sudoku

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.brendigo.sudoku.data.SudokuController
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end saved-board recovery with a deliberately isolated preferences
 * file. Never touch the user's actual Sudoku progress or screenshot test data.
 */
@RunWith(AndroidJUnit4::class)
class SudokuSavedGameRecoveryTest {
    @Test
    fun damagedPrimaryRecoversRegularBoardAndMatchingUndoHistory() = runBlocking {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val isolated = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this

            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences =
                super.getSharedPreferences("sudoku_recovery_test_v134", mode)
        }
        val prefs = isolated.getSharedPreferences("ignored", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        try {
            val controller = SudokuController(isolated)
            controller.startNew(size = 4, level = 0)
            val starting = controller.game ?: error("No generated game")
            val cell = starting.board.indices.first { starting.givens[it] == 0 }
            controller.select(cell)
            controller.enter(starting.solution[cell])
            controller.flush()
            val saved = controller.game ?: error("Move was not saved")
            assertNotNull(prefs.getString("regular_game", null))
            assertNotNull(prefs.getString("regular_undo", null))

            // Simulate a single damaged legacy key, not destruction of the
            // separate recoverable snapshot.
            check(prefs.edit().putString("game", "{corrupt").commit())
            val restored = SudokuController(isolated)
            assertArrayEquals(saved.solution, restored.game!!.solution)
            assertArrayEquals(saved.board, restored.game!!.board)
            assertEquals(saved.selected, restored.game!!.selected)
            check(restored.canUndo()) { "Undo history was not restored with its board" }

            restored.undo()
            assertEquals(0, restored.game!!.board[cell])
            restored.flush()
            assertNotNull(prefs.getString("game", null))
        } finally {
            prefs.edit().clear().commit()
        }
    }
}
