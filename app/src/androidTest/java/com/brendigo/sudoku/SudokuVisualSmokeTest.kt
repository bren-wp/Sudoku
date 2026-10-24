package com.brendigo.sudoku

import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Real-device UI smoke flow and screenshot capture for side-by-side comparison
 * with the owner's screen references. Tests function, not pixel-perfect similarity.
 */
@RunWith(AndroidJUnit4::class)
class SudokuVisualSmokeTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun waitFor(tag: String, timeoutMs: Long = 25_000) {
        rule.waitUntil(timeoutMillis = timeoutMs) {
            rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun shot(name: String) {
        rule.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val screenshot = instrumentation.uiAutomation.takeScreenshot()
            ?: error("Emulator did not return a screenshot")
        val folder = File(rule.activity.filesDir, "screenshots")
        check(folder.exists() || folder.mkdirs())
        FileOutputStream(File(folder, "$name.png")).use {
            check(screenshot.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
        screenshot.recycle()
    }

    private fun homeTile(title: String) {
        rule.onNodeWithTag("home-$title").performScrollTo().performClick()
        rule.waitForIdle()
    }

    @Test
    fun screenNavigationAndGameplayControls() {
        waitFor("home-nova-igra")
        shot("01-home")

        homeTile("nova-igra")
        waitFor("primary-action")
        shot("02-new-game")

        rule.onNodeWithTag("primary-action").performScrollTo().performClick()
        waitFor("sudoku-board", timeoutMs = 60_000)
        shot("03-game")

        rule.onNodeWithTag("game-settings").performClick()
        waitFor("navigate-back")
        shot("04-settings")

        rule.onNodeWithTag("navigate-back").performClick()
        waitFor("sudoku-board")
        rule.onNodeWithTag("game-back").performClick()
        waitFor("home-nova-igra")

        homeTile("dnevni-izazov")
        waitFor("primary-action")
        shot("05-daily")
        rule.onNodeWithTag("navigate-back").performClick()

        homeTile("statistika")
        shot("06-statistics")
        rule.onNodeWithTag("navigate-back").performClick()

        homeTile("postignuća")
        shot("07-achievements")
        rule.onNodeWithTag("navigate-back").performClick()
        waitFor("home-nova-igra")
    }
}
