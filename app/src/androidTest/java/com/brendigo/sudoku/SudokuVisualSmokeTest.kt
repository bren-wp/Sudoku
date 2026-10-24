package com.brendigo.sudoku

import android.graphics.Bitmap
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
        val resolver = rule.activity.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/SudokuQA")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Could not create screenshot in MediaStore")
        try {
            val stream = resolver.openOutputStream(uri)
                ?: error("Could not write screenshot image")
            stream.use {
                check(screenshot.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            val ready = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            check(resolver.update(uri, ready, null, null) == 1)
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
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
