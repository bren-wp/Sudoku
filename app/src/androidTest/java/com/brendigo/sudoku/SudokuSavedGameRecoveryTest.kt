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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.json.JSONObject
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

    @Test
    fun forgedCompletedFlagCannotHideAValidRegularSaveOrFreezeAnUnsolvedBoard() = runBlocking {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val isolated = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences =
                super.getSharedPreferences("sudoku_invalid_completed_recovery_v2", mode)
        }
        val prefs = isolated.getSharedPreferences("ignored", Context.MODE_PRIVATE)
        check(prefs.edit().clear().commit())
        try {
            val controller = SudokuController(isolated)
            controller.startNew(size = 4, level = 0)
            assertFalse(requireNotNull(controller.game).completed)
            assertEquals(true, controller.flush())
            val regularJson = requireNotNull(prefs.getString("regular_game", null))
            val invalid = JSONObject(regularJson).apply {
                put("completed", true)
            }.toString()

            // The primary snapshot claims victory without the solution.
            // A separate, valid regular save must remain resumable.
            check(prefs.edit().putString("game", invalid).commit())
            val recovered = SudokuController(isolated)
            assertNotNull(recovered.game)
            assertFalse(requireNotNull(recovered.game).completed)
            assertArrayEquals(controller.game!!.board, recovered.game!!.board)
            assertEquals(0, recovered.stats.finished)

            // If both snapshots are corrupt, reject rather than displaying
            // an unfinished board as a completed result.
            check(prefs.edit().putString("game", invalid)
                .putString("regular_game", invalid).commit())
            assertNull(SudokuController(isolated).game)
        } finally {
            check(prefs.edit().clear().commit())
        }
    }
}
