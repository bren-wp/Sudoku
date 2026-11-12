package com.brendigo.sudoku

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.brendigo.sudoku.data.SudokuController
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A session switch must be indivisible even while waiting for the off-main
 * thread disk checkpoint. These tests use real Android preferences and hold
 * a commit open, rather than relying on timing of the Sudoku generator.
 */
@RunWith(AndroidJUnit4::class)
class SudokuTransitionSerializationTest {
    private class HeldPreferences(private val delegate: SharedPreferences) :
        SharedPreferences by delegate {
        val blockNextCommit = AtomicBoolean(false)
        var commitStarted = CountDownLatch(1)
        var allowCommit = CountDownLatch(1)

        override fun edit(): SharedPreferences.Editor {
            val editor = delegate.edit()
            return object : SharedPreferences.Editor by editor {
                override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                    editor.putString(key, value)
                    return this
                }

                override fun commit(): Boolean {
                    if (blockNextCommit.compareAndSet(true, false)) {
                        commitStarted.countDown()
                        if (!allowCommit.await(15, TimeUnit.SECONDS)) return false
                    }
                    return editor.commit()
                }
            }
        }
    }

    private data class Fixture(
        val context: Context,
        val backing: SharedPreferences,
        val slow: HeldPreferences,
    )

    private fun fixture(): Fixture {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val backing = base.getSharedPreferences(
            "sudoku_transition_serialization_v1070_test", Context.MODE_PRIVATE
        )
        check(backing.edit().clear().commit())
        val slow = HeldPreferences(backing)
        val isolated = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = slow
        }
        return Fixture(isolated, backing, slow)
    }

    @Test fun dailySwitchBlocksCompetingNewGameAndResume() = runBlocking {
        val (context, backing, slow) = fixture()
        try {
            val controller = SudokuController(context)
            controller.startNew(4, 0)
            assertTrue(controller.flush())
            assertEquals(1, controller.stats.played)

            slow.blockNextCommit.set(true)
            val dailySwitch = async { controller.startNew(4, 1, daily = true) }
            yield()
            assertTrue("Daily checkpoint not reached", slow.commitStarted.await(10, TimeUnit.SECONDS))
            assertTrue("Transition must lock before waiting for disk", controller.isGenerating)

            // Both actions are ignored while the daily checkpoint is held.
            controller.startNew(4, 2)
            assertFalse(controller.resumeRegularGame())
            assertEquals(1, controller.stats.played)

            slow.allowCommit.countDown()
            withTimeout(20_000) { dailySwitch.await() }
            assertFalse(controller.isGenerating)
            assertTrue(requireNotNull(controller.game).daily)
            assertEquals(2, controller.stats.played)
            assertTrue(controller.flush())

            val restored = SudokuController(context)
            assertTrue(requireNotNull(restored.game).daily)
            assertEquals(2, restored.stats.played)
        } finally {
            slow.allowCommit.countDown()
            check(backing.edit().clear().commit())
        }
    }

    @Test fun resumeSwitchBlocksCompetingNewGameBeforeCheckpointFinishes() = runBlocking {
        val (context, backing, slow) = fixture()
        try {
            val controller = SudokuController(context)
            controller.startNew(4, 0)
            assertTrue(controller.flush())
            controller.startNew(4, 1, daily = true)
            assertTrue(controller.flush())
            assertTrue(requireNotNull(controller.game).daily)
            assertEquals(2, controller.stats.played)

            slow.blockNextCommit.set(true)
            val resume = async { controller.resumeRegularGame() }
            yield()
            assertTrue("Resume checkpoint not reached", slow.commitStarted.await(10, TimeUnit.SECONDS))
            assertTrue("Resume must claim transition guard", controller.isGenerating)

            controller.startNew(4, 5)
            assertFalse(controller.resumeRegularGame())
            assertTrue(requireNotNull(controller.game).daily)

            slow.allowCommit.countDown()
            assertTrue(withTimeout(20_000) { resume.await() })
            assertFalse(controller.isGenerating)
            assertFalse(requireNotNull(controller.game).daily)
            assertEquals(2, controller.stats.played)
            assertTrue(controller.flush())

            val restored = SudokuController(context)
            assertEquals(2, restored.stats.played)
        } finally {
            slow.allowCommit.countDown()
            check(backing.edit().clear().commit())
        }
    }
}
