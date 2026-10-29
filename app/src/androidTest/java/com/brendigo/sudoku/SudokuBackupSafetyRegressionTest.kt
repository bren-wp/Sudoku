package com.brendigo.sudoku

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.brendigo.sudoku.data.SudokuBackup
import com.brendigo.sudoku.data.SudokuController
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/** Regression tests for recovery metadata, corruption and legacy v1 archives. */
@RunWith(AndroidJUnit4::class)
class SudokuBackupSafetyRegressionTest {
    private val secret = "offline-secret-regression-2026".toCharArray()

    private fun sandbox(id: String): Pair<Context, SharedPreferences> {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val app = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences =
                super.getSharedPreferences("sudoku_issue41_${id}", mode)
        }
        return app to app.getSharedPreferences("state", Context.MODE_PRIVATE)
    }

    @Test
    fun previewShowsRealStatsAndBothSavedPuzzleTypes() = runBlocking {
        val (context, prefs) = sandbox("preview")
        prefs.edit().clear().commit()
        try {
            val controller = SudokuController(context)
            controller.startNew(4, 0)
            controller.flush()
            var preview = SudokuBackup.preview(SudokuBackup.snapshot(context))
            assertEquals(1, preview.played)
            assertEquals(0, preview.finished)
            assertTrue(preview.savedRegularGame)
            assertFalse(preview.savedDailyGame)

            controller.startNew(6, 1, daily = true)
            controller.flush()
            preview = SudokuBackup.preview(SudokuBackup.snapshot(context))
            assertTrue(preview.savedRegularGame)
            assertTrue(preview.savedDailyGame)
            assertEquals(2, preview.played)
            assertEquals(0, preview.dailyHistoryDays)
        } finally {
            prefs.edit().clear().commit()
        }
    }

    @Test
    fun rejectsDuplicateDailyDatesBadHistoryAndCorruptStatsBeforeAnyWrite() = runBlocking {
        val (context, prefs) = sandbox("corrupt")
        prefs.edit().clear().commit()
        try {
            val controller = SudokuController(context)
            controller.startNew(9, 0)
            assertTrue(controller.flush())
            val previous = SudokuBackup.snapshot(context)
            val stats = JSONObject(requireNotNull(previous["stats"]))

            val duplicate = JSONObject(stats.toString()).apply {
                put("completedDailyDates",
                    JSONArray(listOf("2026-10-01", "2026-10-01")))
            }
            val brokenHistory = JSONObject(stats.toString()).apply {
                put("history", JSONArray().put(JSONObject().apply {
                    put("level", 8)
                    put("size", 5)
                    put("seconds", -20)
                    put("date", "not-a-day")
                }))
            }
            val badDaily = JSONObject(stats.toString()).apply {
                put("lastDaily", "today")
            }
            listOf(duplicate, brokenHistory, badDaily).forEach { invalid ->
                val damaged = previous + mapOf(
                    "stats" to invalid.toString(), "stats_backup" to invalid.toString()
                )
                try {
                    SudokuBackup.restore(context, damaged)
                    fail("Invalid recovery state must never be committed")
                } catch (_: IOException) { }
                try {
                    SudokuBackup.encrypt(damaged, secret.copyOf())
                    fail("Invalid state must never be exported")
                } catch (_: IOException) { }
                assertEquals(previous, SudokuBackup.snapshot(context))
            }
        } finally {
            prefs.edit().clear().commit()
        }
    }

    @Test
    fun legacyVersionOneArchiveWithoutNewStatsFieldsStillRoundTrips() = runBlocking {
        val (source, sourcePrefs) = sandbox("old_source")
        val (target, targetPrefs) = sandbox("old_target")
        sourcePrefs.edit().clear().commit()
        targetPrefs.edit().clear().commit()
        try {
            val original = SudokuController(source)
            original.startNew(4, 1)
            original.flush()
            val snapshot = SudokuBackup.snapshot(source)
            val legacyStats = JSONObject(requireNotNull(snapshot["stats"])).apply {
                remove("completedDailyDates")
                remove("totalPlayedSeconds")
                remove("winsBySize")
            }.toString()
            val legacy = snapshot + mapOf(
                "stats" to legacyStats, "stats_backup" to legacyStats
            )
            val archive = SudokuBackup.encrypt(legacy, secret.copyOf())
            val decoded = SudokuBackup.decrypt(archive, secret.copyOf())
            assertEquals(legacy, decoded)
            SudokuBackup.restore(target, decoded)
            assertEquals(4, requireNotNull(SudokuController(target).game).size)
            assertEquals(1, SudokuController(target).stats.played)
        } finally {
            sourcePrefs.edit().clear().commit()
            targetPrefs.edit().clear().commit()
        }
    }
}
