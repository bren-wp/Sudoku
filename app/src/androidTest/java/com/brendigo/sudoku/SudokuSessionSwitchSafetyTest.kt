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

/**
 * Failure-injection regression for a full/unavailable disk at the exact
 * moment a session is being replaced. Uses a dedicated preferences file.
 */
@RunWith(AndroidJUnit4::class)
class SudokuSessionSwitchSafetyTest {
    private class RejectingCommitPreferences(
        private val delegate: SharedPreferences,
    ) : SharedPreferences by delegate {
        var rejectCommits = false

        override fun edit(): SharedPreferences.Editor {
            val editor = delegate.edit()
            // Keep the proxy when putString is chained before commit().
            return object : SharedPreferences.Editor by editor {
                override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                    editor.putString(key, value)
                    return this
                }

                override fun commit(): Boolean =
                    if (rejectCommits) false else editor.commit()

                override fun apply() {
                    if (!rejectCommits) editor.apply()
                }
            }
        }
    }

    private fun isolatedState(): Triple<Context, SharedPreferences, RejectingCommitPreferences> {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val backing = base.getSharedPreferences("sudoku_switch_safety_v140_test", Context.MODE_PRIVATE)
        val rejecting = RejectingCommitPreferences(backing)
        val isolated = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = rejecting
        }
        return Triple(isolated, backing, rejecting)
    }

    @Test fun failedCheckpointCannotReplaceUnfinishedPuzzle() = runBlocking {
        val (context, backing, rejecting) = isolatedState()
        check(backing.edit().clear().commit())
        try {
            val controller = SudokuController(context)
            controller.startNew(4, 0)
            val first = requireNotNull(controller.game)
            val cell = first.board.indices.first { first.givens[it] == 0 }
            controller.select(cell)
            controller.enter(first.solution[cell])
            assertTrue(controller.flush())

            val savedBoard = requireNotNull(controller.game).board.clone()
            val originalSolution = requireNotNull(controller.game).solution.clone()
            val played = controller.stats.played

            rejecting.rejectCommits = true
            controller.startNew(4, 1)
            assertArrayEquals(savedBoard, requireNotNull(controller.game).board)
            assertArrayEquals(originalSolution, requireNotNull(controller.game).solution)
            assertEquals(played, controller.stats.played)
            assertTrue(controller.toast.contains("Spremanje napretka"))
            assertFalse(controller.isGenerating)

            controller.startNew(4, 1, daily = true)
            assertArrayEquals(savedBoard, requireNotNull(controller.game).board)
            assertFalse(requireNotNull(controller.game).daily)
            assertEquals(played, controller.stats.played)

            rejecting.rejectCommits = false
            val restarted = SudokuController(context)
            assertArrayEquals(savedBoard, requireNotNull(restarted.game).board)
            assertEquals(played, restarted.stats.played)
        } finally {
            rejecting.rejectCommits = false
            check(backing.edit().clear().commit())
        }
    }

    @Test fun failedCheckpointCannotSwitchFromDailyToRegularGame() = runBlocking {
        val (context, backing, rejecting) = isolatedState()
        check(backing.edit().clear().commit())
        try {
            val controller = SudokuController(context)
            controller.startNew(4, 0)
            assertTrue(controller.flush())
            controller.startNew(4, 1, daily = true)
            assertTrue(controller.flush())
            val daily = requireNotNull(controller.game)
            assertTrue(daily.daily)

            rejecting.rejectCommits = true
            assertFalse(controller.resumeRegularGame())
            assertTrue(requireNotNull(controller.game).daily)
            assertArrayEquals(daily.givens, requireNotNull(controller.game).givens)

            rejecting.rejectCommits = false
            assertTrue(controller.resumeRegularGame())
            assertFalse(requireNotNull(controller.game).daily)
        } finally {
            rejecting.rejectCommits = false
            check(backing.edit().clear().commit())
        }
    }
}
