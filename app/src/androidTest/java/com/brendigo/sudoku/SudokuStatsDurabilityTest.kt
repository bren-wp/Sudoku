package com.brendigo.sudoku

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.brendigo.sudoku.data.SudokuController
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests a real SharedPreferences transaction, never the test device's progress.
 * The same preference editor must contain both the completed board and points.
 */
@RunWith(AndroidJUnit4::class)
class SudokuStatsDurabilityTest {
    @Test
    fun victoryAndStatisticsAreCommittedTogetherAndRecoverFromBadPrimary() = runBlocking {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val isolated = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences =
                super.getSharedPreferences("sudoku_stats_atomic_v135_test", mode)
        }
        val prefs = isolated.getSharedPreferences("ignored", Context.MODE_PRIVATE)
        check(prefs.edit().clear().commit())
        try {
            val controller = SudokuController(isolated)
            controller.startNew(4, 0)
            val original = requireNotNull(controller.game)
            assertEquals(1, controller.stats.played)
            assertEquals(0, controller.stats.finished)
            repeat(10) { controller.tick() }
            assertEquals(10L, controller.stats.totalPlayedSeconds)
            for (index in original.board.indices) {
                if (original.givens[index] != 0) continue
                controller.select(index)
                controller.enter(original.solution[index])
            }
            assertTrue(requireNotNull(controller.game).completed)
            assertEquals(1, controller.stats.finished)
            controller.flush()
            assertEquals(1, JSONObject(prefs.getString("stats", "{}")!!).getInt("finished"))
            assertEquals(1, JSONObject(prefs.getString("stats_backup", "{}")!!).getInt("finished"))
            assertTrue(JSONObject(prefs.getString("game", "{}")!!).getBoolean("completed"))
            assertTrue(JSONObject(prefs.getString("regular_game", "{}")!!).getBoolean("completed"))

            val restored = SudokuController(isolated)
            assertEquals(1, restored.stats.finished)
            assertEquals(1, restored.stats.played)
            assertEquals(10L, restored.stats.totalPlayedSeconds)
            assertEquals(10L, restored.stats.totalSeconds)
            assertTrue(requireNotNull(restored.game).completed)

            check(prefs.edit().putString("stats", "{malformed").commit())
            val recovered = SudokuController(isolated)
            assertEquals(1, recovered.stats.finished)
            assertEquals(1, recovered.stats.played)
            assertTrue(requireNotNull(recovered.game).completed)
            // Completion is immutable: reopening the saved result cannot grant
            // another score or a second award for the same game.
            recovered.enter(1)
            assertEquals(1, recovered.stats.finished)
        } finally {
            check(prefs.edit().clear().commit())
        }
    }
}
