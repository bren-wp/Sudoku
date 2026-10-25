package com.brendigo.sudoku

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.semantics.SemanticsProperties
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
        // Android's first-run immersive-mode explanation is an OS overlay,
        // not part of Sudoku. Dismiss it before screen evidence is captured.
        val device = UiDevice.getInstance(instrumentation)
        val immersiveDisclosure = device.findObject(By.text("Got it"))
        if (immersiveDisclosure != null &&
            device.findObject(By.textContains("Viewing full screen")) != null) {
            immersiveDisclosure.click()
            Thread.sleep(450)
        }
        instrumentation.waitForIdleSync()
        check(device.findObject(By.textContains("Viewing full screen")) == null) {
            "The Android immersive disclosure obscures the screenshot"
        }
        var screenshot = instrumentation.uiAutomation.takeScreenshot()
            ?: error("Emulator did not return a screenshot")
        // The launcher may briefly show an Android ANR dialog even when the app
        // itself is healthy. Only dismiss a visible system-owned dialog, then
        // wait and re-capture. Never skip failures inside our own app.
        fun modalSamples(image: Bitmap): Int {
            val points = listOf(
                .35f to .10f, .50f to .10f, .65f to .10f,
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
    fun fourAndSixCellGridModesArePlayable() {
        waitFor("home-nova-igra")
        homeTile("nova-igra")
        rule.onNodeWithTag("size-4").performClick()
        rule.onNodeWithTag("difficulty-1").performClick()
        rule.onNodeWithTag("primary-action").performScrollTo().performClick()
        waitFor("sudoku-board", timeoutMs = 60_000)
        rule.onNodeWithTag("sudoku-cell-15").assertExists()
        rule.onNodeWithTag("number-4").assertExists()
        rule.onNodeWithTag("game-back").performClick()
        waitFor("home-nova-igra")

        homeTile("nova-igra")
        rule.onNodeWithTag("size-6").performClick()
        rule.onNodeWithTag("difficulty-3").performClick()
        rule.onNodeWithTag("primary-action").performScrollTo().performClick()
        waitFor("sudoku-board", timeoutMs = 60_000)
        rule.onNodeWithTag("sudoku-cell-35").assertExists()
        rule.onNodeWithTag("number-6").assertExists()
    }

    @Test
    fun numericEntryUndoNotesAndResumeAreInteractive() {
        waitFor("home-nova-igra")
        homeTile("nova-igra")
        rule.onNodeWithTag("primary-action").performScrollTo().performClick()
        waitFor("sudoku-board", timeoutMs = 60_000)

        rule.onNodeWithTag("game-undo").assertIsNotEnabled()
        rule.onNodeWithTag("game-erase").assertIsNotEnabled()
        rule.onNodeWithTag("number-1").assertIsEnabled()
        val empty = hasContentDescription("prazno polje", substring = true)
        val before = rule.onAllNodes(empty).fetchSemanticsNodes().size
        check(before > 0) { "The generated Sudoku had no editable empty cells" }
        rule.onAllNodes(empty).onFirst().performClick()
        rule.onNodeWithTag("number-1").performClick()
        rule.waitForIdle()
        check(rule.onAllNodes(empty).fetchSemanticsNodes().size == before - 1) {
            "Number entry did not update Sudoku board"
        }

        rule.onNodeWithTag("game-undo").assertIsEnabled().performClick()
        rule.waitForIdle()
        check(rule.onAllNodes(empty).fetchSemanticsNodes().size == before) {
            "Undo did not restore the previous cell"
        }
        rule.onNodeWithTag("game-undo").assertIsNotEnabled()

        rule.onAllNodes(empty).onFirst().performClick()
        rule.onNodeWithTag("game-notes").performClick()
        rule.onNodeWithTag("number-2").performClick()
        rule.waitForIdle()
        check(rule.onAllNodes(empty).fetchSemanticsNodes().size == before) {
            "Notes mode must not fill the selected cell"
        }
        rule.onNodeWithTag("game-erase").assertIsEnabled().performClick()
        rule.onNodeWithTag("game-back").performClick()
        waitFor("resume-game")
        rule.onNodeWithTag("resume-game").performScrollTo().performClick()
        waitFor("sudoku-board")
        rule.onNodeWithTag("game-hint").assertExists()
    }

    @Test
    fun premiumReferenceSectionsAreReachable() {
        waitFor("home-nova-igra")
        rule.onNodeWithTag("brand-footer").assertExists()
        homeTile("postignuća")
        waitFor("screen-postignuća")
        // The four featured achievement rows must be visible at reference size.
        rule.onNodeWithTag("achievement-sudoku-legenda").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("achievement-prva-pobjeda").performScrollTo().performClick()
        rule.onNodeWithTag("achievement-detail").assertExists()
        rule.onNodeWithText("Zatvori").performClick()
        rule.onNodeWithTag("achievements-show-all")
            .performScrollTo().performClick()
        rule.onNodeWithTag("achievement-prvi-korak").assertExists()
        rule.onNodeWithTag("navigate-back").performScrollTo().performClick()
        waitFor("home-nova-igra")

        homeTile("statistika")
        waitFor("screen-statistika")
        // The entire 48dp history header is an accessible click target.
        rule.onNodeWithTag("open-game-history").performScrollTo().performClick()
        rule.onNodeWithText("Povijest igara").assertExists()
        rule.onNodeWithText("Zatvori").performClick()
        rule.onNodeWithTag("stats-achievements").performScrollTo().performClick()
        waitFor("screen-postignuća")
        rule.onNodeWithTag("navigate-back").performScrollTo().performClick()
        waitFor("home-nova-igra")
    }

    @Test
    fun settingsButtonsAreVisibleAndFunctional() {
        waitFor("home-nova-igra")
        homeTile("postavke")
        waitFor("screen-postavke")
        rule.onNodeWithTag("sound-vibration").performScrollTo().assertIsDisplayed()
        // A complete setting row, not just the small thumb, must toggle.
        val darkSetting = rule.onNodeWithTag("setting-tamna-tema")
        darkSetting.assertIsDisplayed()
        val initial = darkSetting.fetchSemanticsNode().config[SemanticsProperties.ToggleableState]
        darkSetting.performClick()
        rule.waitForIdle()
        val toggled = darkSetting.fetchSemanticsNode().config[SemanticsProperties.ToggleableState]
        check(initial != toggled) { "Entire dark-theme row did not toggle" }
        darkSetting.performClick()
        rule.waitForIdle()
        check(darkSetting.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] == initial) {
            "Second row press did not restore dark theme"
        }
        rule.onNodeWithTag("setting-provjera-greške").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("language-settings").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("privacy-policy").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("language-settings").performScrollTo().performClick()
        rule.onNodeWithText("Aplikacija trenutačno podržava hrvatski jezik.").assertExists()
        rule.onNodeWithText("Zatvori").performClick()
        rule.onNodeWithTag("sound-vibration").performScrollTo().performClick()
        rule.onNodeWithText("Zatvori").performClick()
        rule.onNodeWithTag("privacy-policy").performScrollTo().performClick()
        rule.onNodeWithText("Sudoku by Brendigo možeš igrati bez registracije i internetske veze.")
            .assertExists()
        rule.onNodeWithText("Zatvori").performClick()
        rule.onNodeWithTag("navigate-back").performScrollTo().performClick()
        waitFor("home-nova-igra")

        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        check(ctx.packageManager.getApplicationIcon(ctx.packageName) != null) {
            "Launcher adaptive icon is missing"
        }
        val monochromeId = ctx.resources.getIdentifier(
            "ic_sudoku_monochrome", "drawable", ctx.packageName
        )
        check(monochromeId != 0) { "Android themed adaptive icon is missing" }
        val iconDrawable = ctx.getDrawable(monochromeId) ?: error("No themed icon")
        val sample = Bitmap.createBitmap(108, 108, Bitmap.Config.ARGB_8888)
        val output = Canvas(sample)
        iconDrawable.setBounds(0, 0, 108, 108)
        iconDrawable.draw(output)
        check(Color.alpha(sample.getPixel(35, 56)) == 0) {
            "Themed adaptive icon has an opaque background and loses its 7-3-9 design"
        }
        check(Color.alpha(sample.getPixel(54, 54)) > 0) {
            "Center number is missing from the themed icon"
        }
        sample.recycle()
    }

    @Test
    fun screenNavigationAndGameplayControls() {
        waitFor("home-nova-igra")
        // The launch splash is composited for a moment after semantics switch to Home.
        Thread.sleep(1300)
        rule.waitForIdle()
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

        rule.onNodeWithTag("navigate-back").performScrollTo().performClick()
        waitFor("sudoku-board")
        rule.onNodeWithTag("game-back").performClick()
        waitFor("home-nova-igra")

        homeTile("dnevni-izazov")
        waitFor("screen-dnevni-izazov")
        waitFor("primary-action")
        shot("05-daily")
        rule.onNodeWithTag("navigate-back").performScrollTo().performClick()

        homeTile("statistika")
        waitFor("screen-statistika")
        shot("06-statistics")
        rule.onNodeWithTag("navigate-back").performScrollTo().performClick()

        homeTile("postignuća")
        waitFor("screen-postignuća")
        shot("07-achievements")
        rule.onNodeWithTag("navigate-back").performScrollTo().performClick()
        waitFor("home-nova-igra")
    }
}
