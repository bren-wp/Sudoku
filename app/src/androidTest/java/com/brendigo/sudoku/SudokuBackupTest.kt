package com.brendigo.sudoku

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.brendigo.sudoku.data.SudokuBackup
import com.brendigo.sudoku.data.SudokuController
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/** Fully isolated offline encrypted-backup roundtrip and destructive-import guard. */
@RunWith(AndroidJUnit4::class)
class SudokuBackupTest {
    private val password = "Very-long-local-password-2026".toCharArray()

    private fun sandbox(id: String): Pair<Context, SharedPreferences> {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val isolated = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences =
                super.getSharedPreferences("sudoku_backup_v136_${id}", mode)
        }
        return isolated to isolated.getSharedPreferences("state", Context.MODE_PRIVATE)
    }

    @Test
    fun encryptedRoundtripPreservesGameStatsNotesUndoAndPrivacy() = runBlocking {
        val (source, sourcePrefs) = sandbox("source")
        val (target, targetPrefs) = sandbox("target")
        sourcePrefs.edit().clear().commit()
        targetPrefs.edit().clear().commit()
        try {
            val original = SudokuController(source)
            original.startNew(4, 2)
            val session = requireNotNull(original.game)
            val cell = session.board.indices.first { session.givens[it] == 0 }
            original.select(cell)
            original.toggleNotes()
            original.enter(2)
            original.toggleNotes()
            original.enter(session.solution[cell])
            original.flush()
            val before = requireNotNull(original.game)
            assertEquals(1, original.stats.played)
            val data = SudokuBackup.snapshot(source)
            val bytes = SudokuBackup.encrypt(data, password.copyOf())
            assertFalse(String(bytes, Charsets.ISO_8859_1).contains("\"solution\""))
            assertFalse(String(bytes, Charsets.ISO_8859_1).contains("regular_game"))
            val recovered = SudokuBackup.decrypt(bytes, password.copyOf())
            assertEquals(data, recovered)
            SudokuBackup.restore(target, recovered)
            val restored = SudokuController(target)
            assertArrayEquals(before.board, requireNotNull(restored.game).board)
            assertArrayEquals(before.solution, requireNotNull(restored.game).solution)
            assertEquals(1, restored.stats.played)
            assertTrue(restored.canUndo())
            restored.undo()
            assertEquals(0, requireNotNull(restored.game).board[cell])
        } finally {
            sourcePrefs.edit().clear().commit()
            targetPrefs.edit().clear().commit()
        }
    }

    @Test
    fun restoredDailyProgressDoesNotAwardTheSameChallengeTwice() = runBlocking {
        val (source, sourcePrefs) = sandbox("daily_source")
        val (target, targetPrefs) = sandbox("daily_target")
        sourcePrefs.edit().clear().commit()
        targetPrefs.edit().clear().commit()
        try {
            val original = SudokuController(source)
            original.startNew(4, 0, daily = true)
            val game = requireNotNull(original.game)
            for (index in game.board.indices) {
                if (game.givens[index] == 0) {
                    original.select(index)
                    original.enter(game.solution[index])
                }
            }
            assertTrue(requireNotNull(original.game).completed)
            assertEquals(1, original.stats.dailyWins)
            original.flush()
            val encrypted = SudokuBackup.encrypt(
                SudokuBackup.snapshot(source), password.copyOf()
            )
            val validated = SudokuBackup.decrypt(encrypted, password.copyOf())
            SudokuBackup.restore(target, validated)
            val restored = SudokuController(target)
            assertTrue(restored.dailyAlreadyCompleted())
            assertEquals(1, restored.stats.dailyWins)
            assertEquals(50, restored.stats.dailyPoints)
            val beforePlayed = restored.stats.played
            restored.startNew(4, 0, daily = true)
            assertEquals(1, restored.stats.dailyWins)
            assertEquals(beforePlayed, restored.stats.played)
        } finally {
            sourcePrefs.edit().clear().commit()
            targetPrefs.edit().clear().commit()
        }
    }


    @Test
    fun emptyOrMismatchedArchiveCannotEraseExistingProgress() = runBlocking {
        val (context, prefs) = sandbox("reject_empty")
        prefs.edit().clear().commit()
        try {
            val controller = SudokuController(context)
            controller.startNew(4, 0)
            controller.flush()
            val baseline = SudokuBackup.snapshot(context)
            val validStats = requireNotNull(baseline["stats"])
            val invalid = listOf(
                emptyMap(),
                mapOf("options" to "{}"),
                mapOf("stats" to "{}"),
                mapOf("stats" to validStats, "stats_backup" to """{"played":0,"finished":0}"""),
            )
            invalid.forEach { values ->
                try {
                    SudokuBackup.restore(context, values)
                    fail("Invalid backup must never erase current game")
                } catch (_: IOException) {
                    // Reject before any preference edit.
                }
                try {
                    SudokuBackup.encrypt(values, password.copyOf())
                    fail("Invalid backup must never be exported")
                } catch (_: IOException) {
                    // A valid key must not authorize a destructive empty archive.
                }
                assertEquals(baseline, SudokuBackup.snapshot(context))
            }
        } finally {
            prefs.edit().clear().commit()
        }
    }

    @Test
    fun oldActivityFlushCannotOverwriteRestoredGame() = runBlocking {
        val (source, sourcePrefs) = sandbox("lock_source")
        val (target, targetPrefs) = sandbox("lock_target")
        sourcePrefs.edit().clear().commit()
        targetPrefs.edit().clear().commit()
        try {
            val newProgress = SudokuController(source)
            newProgress.startNew(6, 0)
            newProgress.flush()
            val expected = SudokuBackup.snapshot(source)

            val outgoing = SudokuController(target)
            outgoing.startNew(4, 1)
            outgoing.flush()
            assertTrue(outgoing.prepareForBackupRestore())
            SudokuBackup.restore(target, expected)
            // A delayed onStop callback must not write outgoing 4x4 over 6x6.
            assertTrue(outgoing.flush())
            assertEquals(expected, SudokuBackup.snapshot(target))
            assertEquals(6, requireNotNull(SudokuController(target).game).size)
        } finally {
            sourcePrefs.edit().clear().commit()
            targetPrefs.edit().clear().commit()
        }
    }


    @Test
    fun failedDiskCommitRollsBackPreviousLocalSnapshot() = runBlocking {
        val (base, prefs) = sandbox("rollback")
        prefs.edit().clear().commit()
        try {
            val previousController = SudokuController(base)
            previousController.startNew(4, 1)
            previousController.flush()
            val original = SudokuBackup.snapshot(base)

            val (source, sourcePrefs) = sandbox("rollback_source")
            sourcePrefs.edit().clear().commit()
            try {
                val sourceController = SudokuController(source)
                sourceController.startNew(9, 0)
                sourceController.flush()
                val candidate = SudokuBackup.snapshot(source)
                assertNotEquals(original, candidate)

                // Simulate a commit that updates the in-memory preferences,
                // but reports a disk write failure to the import caller.
                var firstCommit = true
                val failing = object : ContextWrapper(base) {
                    override fun getApplicationContext(): Context = this
                    override fun getSharedPreferences(name: String?, mode: Int):
                        SharedPreferences = object : SharedPreferences by prefs {
                        override fun edit(): SharedPreferences.Editor {
                            val underlying = prefs.edit()
                            return object : SharedPreferences.Editor by underlying {
                                override fun commit(): Boolean {
                                    val result = underlying.commit()
                                    return if (firstCommit) {
                                        firstCommit = false
                                        false
                                    } else result
                                }
                            }
                        }
                    }
                }
                try {
                    SudokuBackup.restore(failing, candidate)
                    fail("Failed commit must not report a successful import")
                } catch (_: IOException) {
                    // The original snapshot must survive the failed import.
                }
                assertEquals(original, SudokuBackup.snapshot(base))
                assertEquals(4, requireNotNull(SudokuController(base).game).size)
            } finally {
                sourcePrefs.edit().clear().commit()
            }
        } finally {
            prefs.edit().clear().commit()
        }
    }

    @Test
    fun wrongPasswordTamperingAndBadDataNeverReplaceExistingProgress() = runBlocking {
        val (target, prefs) = sandbox("invalid")
        prefs.edit().clear().commit()
        try {
            val controller = SudokuController(target)
            controller.startNew(6, 1)
            controller.flush()
            val original = SudokuBackup.snapshot(target)
            val encrypted = SudokuBackup.encrypt(original, password.copyOf())
            val changed = encrypted.clone()
            changed[changed.lastIndex] = (changed.last().toInt() xor 1).toByte()

            for (invalid in listOf(changed, encrypted.copyOfRange(1, encrypted.size))) {
                try {
                    SudokuBackup.decrypt(invalid, password.copyOf())
                    fail("Invalid or tampered archive must be rejected")
                } catch (_: IOException) {
                    // Decryption is authenticated and never mutates preferences.
                }
            }
            try {
                SudokuBackup.decrypt(encrypted, "Definitely-wrong-password".toCharArray())
                fail("Wrong password must be rejected")
            } catch (_: IOException) {
                // Expected, and data stays intact.
            }
            try {
                SudokuBackup.encrypt(mapOf("game" to "{bad"), password.copyOf())
                fail("Invalid game snapshot must not be exported")
            } catch (_: IOException) {
                // Import and export share strict state validation.
            }
            assertEquals(original, SudokuBackup.snapshot(target))
            val after = SudokuController(target)
            assertEquals(1, after.stats.played)
            assertArrayEquals(requireNotNull(controller.game).board, requireNotNull(after.game).board)
        } finally {
            prefs.edit().clear().commit()
        }
    }
}
