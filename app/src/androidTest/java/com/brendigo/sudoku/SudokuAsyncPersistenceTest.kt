package com.brendigo.sudoku

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.brendigo.sudoku.data.SudokuController
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/** Actual Android SharedPreferences, with a slow filesystem write injected. */
@RunWith(AndroidJUnit4::class)
class SudokuAsyncPersistenceTest {
    private class SlowPreferences(private val delegate: SharedPreferences) :
        SharedPreferences by delegate {
        val waitForCommit = AtomicBoolean(false)
        val enteredCommit = CountDownLatch(1)
        val releaseCommit = CountDownLatch(1)
        val wroteOnMain = AtomicBoolean(false)
        val commits = AtomicInteger(0)

        override fun edit(): SharedPreferences.Editor {
            val editor = delegate.edit()
            return object : SharedPreferences.Editor by editor {
                override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                    editor.putString(key, value)
                    return this
                }

                override fun commit(): Boolean {
                    commits.incrementAndGet()
                    if (Looper.myLooper() == Looper.getMainLooper()) wroteOnMain.set(true)
                    if (waitForCommit.compareAndSet(true, false)) {
                        enteredCommit.countDown()
                        if (!releaseCommit.await(5, TimeUnit.SECONDS)) return false
                    }
                    return editor.commit()
                }
            }
        }
    }

    @Test fun slowCommitNeverBlocksMainAndLatestMoveWins() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val base = instrumentation.targetContext
        val backing = base.getSharedPreferences("sudoku_async_disk_v1066_test", Context.MODE_PRIVATE)
        check(backing.edit().clear().commit())
        val slow = SlowPreferences(backing)
        val isolated = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = slow
        }
        try {
            val controller = SudokuController(isolated)
            controller.startNew(4, 0)
            assertTrue(controller.flush())
            val puzzle = requireNotNull(controller.game)
            val cells = puzzle.board.indices.filter { puzzle.givens[it] == 0 }.take(2)
            assertEquals(2, cells.size)

            slow.waitForCommit.set(true)
            instrumentation.runOnMainSync {
                controller.select(cells[0])
                controller.enter(puzzle.solution[cells[0]])
            }
            assertTrue("IO commit did not start", slow.enteredCommit.await(3, TimeUnit.SECONDS))
            // A commit is intentionally blocked in the background. If the UI
            // thread did the write, runOnMainSync would not return.
            instrumentation.runOnMainSync {
                controller.select(cells[1])
                controller.enter(puzzle.solution[cells[1]])
            }
            // Only an actual Activity recreation may opt into the in-process
            // pending snapshot, never a fresh validation/recovery controller.
            val recreated = SudokuController(isolated, restorePendingInProcess = true)
            assertEquals(puzzle.solution[cells[0]], requireNotNull(recreated.game).board[cells[0]])
            assertEquals(puzzle.solution[cells[1]], requireNotNull(recreated.game).board[cells[1]])
            slow.releaseCommit.countDown()
            assertTrue("Final durable checkpoint failed", controller.flush())
            assertFalse("A disk commit ran on the Android main thread", slow.wroteOnMain.get())

            val restored = SudokuController(isolated)
            assertEquals(puzzle.solution[cells[0]], requireNotNull(restored.game).board[cells[0]])
            assertEquals(puzzle.solution[cells[1]], requireNotNull(restored.game).board[cells[1]])
            val saved = JSONObject(requireNotNull(backing.getString("game", null)))
            val stats = JSONObject(requireNotNull(backing.getString("stats", null)))
            assertEquals(requireNotNull(restored.game).completed, saved.getBoolean("completed"))
            assertEquals(controller.stats.finished, stats.getInt("finished"))
        } finally {
            slow.releaseCommit.countDown()
            check(backing.edit().clear().commit())
        }
    }
    @Test fun rapidAutoSavesAreCoalescedWithoutLosingTheFinalSnapshot() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val base = instrumentation.targetContext
        val backing = base.getSharedPreferences("sudoku_write_coalescing_v1068_test", Context.MODE_PRIVATE)
        check(backing.edit().clear().commit())
        val slow = SlowPreferences(backing)
        val isolated = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = slow
        }
        try {
            val controller = SudokuController(isolated)
            controller.startNew(4, 0)
            assertTrue(controller.flush())
            val previousCommits = slow.commits.get()
            slow.waitForCommit.set(true)
            instrumentation.runOnMainSync { controller.toggleNotes() }
            assertTrue("First write never reached IO", slow.enteredCommit.await(3, TimeUnit.SECONDS))
            // Fifty rapid UI interactions while the first fsync is held.
            // Coalescing must keep at most one ordinary queued auto-save.
            instrumentation.runOnMainSync {
                repeat(50) { controller.toggleNotes() }
            }
            slow.releaseCommit.countDown()
            assertTrue(controller.flush())
            val delta = slow.commits.get() - previousCommits
            assertTrue("Too many writes for a burst of UI updates: $delta", delta in 2..3)
            assertFalse(slow.wroteOnMain.get())
            assertTrue(requireNotNull(SudokuController(isolated).game).notesMode)
        } finally {
            slow.releaseCommit.countDown()
            check(backing.edit().clear().commit())
        }
    }

}
