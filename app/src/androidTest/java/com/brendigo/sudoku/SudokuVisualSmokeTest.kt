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
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
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
        // Compose semantics can settle before the emulator compositor completes a frame.
        Thread.sleep(500)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        var screenshot = instrumentation.uiAutomation.takeScreenshot()
            ?: error("Emulator did not return a screenshot")
        // The launcher may briefly show an Android ANR dialog even when the app
        // itself is healthy. Only dismiss a visible system-owned dialog, then
        // wait and re-capture. Never skip failures inside our own app.
        fun modalSamples(image: Bitmap): Int {
            val points = listOf(
                .35f to .49f, .50f to .45f, .50f to .50f,
                .50f to .55f, .65f to .49f
            )
            return points.count { (rx, ry) ->
                val pixel = image.getPixel(
                    (image.width * rx).toInt(), (image.height * ry).toInt()
                )
                android.graphics.Color.red(pixel) > 208 &&
                    android.graphics.Color.green(pixel) > 208 &&
                    android.graphics.Color.blue(pixel) > 208
            }
        }
        var brightSamples = modalSamples(screenshot)
        if (brightSamples >= 3) {
            val device = UiDevice.getInstance(instrumentation)
            val button = device.findObject(By.text("Close app"))
                ?: device.findObject(By.textContains("Close app"))
                ?: device.findObject(By.text("Wait"))
            check(button != null) {
                "Light system overlay obscures $name, but no system ANR action was found"
            }
            button.click()
            Thread.sleep(900)
            instrumentation.waitForIdleSync()
            screenshot.recycle()
            screenshot = instrumentation.uiAutomation.takeScreenshot()
                ?: error("Emulator did not return a screenshot after dialog")
            brightSamples = modalSamples(screenshot)
        }
        check(brightSamples < 3) {
            "System overlay or launcher ANR still obscures $name: $brightSamples"
        }
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
        waitFor("screen-nova-igra")
        waitFor("primary-action")
        shot("02-new-game")

        rule.onNodeWithTag("primary-action").performScrollTo().performClick()
        waitFor("sudoku-board", timeoutMs = 60_000)
        shot("03-game")

        rule.onNodeWithTag("game-settings").performClick()
        waitFor("screen-postavke")
        waitFor("navigate-back")
        shot("04-settings")

        rule.onNodeWithTag("navigate-back").performClick()
        waitFor("sudoku-board")
        rule.onNodeWithTag("game-back").performClick()
        waitFor("home-nova-igra")

        homeTile("dnevni-izazov")
        waitFor("screen-dnevni-izazov")
        waitFor("primary-action")
        shot("05-daily")
        rule.onNodeWithTag("navigate-back").performClick()

        homeTile("statistika")
        waitFor("screen-statistika")
        shot("06-statistics")
        rule.onNodeWithTag("navigate-back").performClick()

        homeTile("postignuća")
        waitFor("screen-postignuća")
        shot("07-achievements")
        rule.onNodeWithTag("navigate-back").performClick()
        waitFor("home-nova-igra")
    }
}
