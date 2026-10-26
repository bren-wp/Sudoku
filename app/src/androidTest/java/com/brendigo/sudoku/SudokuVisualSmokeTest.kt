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
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
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
        rule.onNodeWithTag("size-9").assertIsSelected()
        rule.onNodeWithTag("size-4").assertIsNotSelected()
        rule.onNodeWithTag("difficulty-2").assertIsSelected()
        val size9 = rule.onNodeWithTag("size-9").fetchSemanticsNode().boundsInRoot
        val size4 = rule.onNodeWithTag("size-4").fetchSemanticsNode().boundsInRoot
        val backBounds = rule.onNodeWithTag("navigate-back").fetchSemanticsNode().boundsInRoot
        val sizeHeader = rule.onNodeWithTag("new-game-size-header")
            .fetchSemanticsNode().boundsInRoot
        val selector = rule.onNodeWithTag("new-game-size-selector")
            .fetchSemanticsNode().boundsInRoot
        check(backBounds.right <= selector.left + 1f &&
            selector.right <= sizeHeader.right + 1f &&
            size9.left >= selector.left - 1f &&
            size4.right <= selector.right + 1f) {
            "New Game size selection overflows header or overlaps Back: " +
                "back=$backBounds, selector=$selector, header=$sizeHeader"
        }
        val selectorWidthDp = (size4.right - size9.left) /
            rule.activity.resources.displayMetrics.density
        check(selectorWidthDp <= 210f) {
            "New Game size selector unexpectedly stretched to $selectorWidthDp dp"
        }
        val mainAction = rule.onNodeWithTag("primary-action").fetchSemanticsNode().boundsInRoot
        val difficulty = rule.onNodeWithTag("difficulty-2").fetchSemanticsNode().boundsInRoot
        check(mainAction.width < difficulty.width * .86f) {
            "New Game primary CTA must be narrower than difficulty cards"
        }
        (0..5).forEach { index ->
            rule.onNodeWithTag("difficulty-$index").assertExists()
        }
        rule.onNodeWithTag("difficulty-5").performScrollTo().performClick()
        rule.onNodeWithTag("difficulty-5").assertIsSelected()
        rule.onNodeWithTag("difficulty-2").performScrollTo().performClick()
        rule.onNodeWithTag("difficulty-2").assertIsSelected()
        rule.onNodeWithTag("size-4").performScrollTo().performClick()
        rule.onNodeWithTag("size-4").assertIsSelected()
        rule.onNodeWithTag("difficulty-1").performClick()
        rule.onNodeWithTag("difficulty-1").assertIsSelected()
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
    fun newGameSetupSurvivesActivityRecreation() {
        waitFor("home-nova-igra")
        homeTile("nova-igra")
        rule.onNodeWithTag("size-6").performClick()
        rule.onNodeWithTag("difficulty-5").performScrollTo().performClick()
        rule.onNodeWithTag("size-6").assertIsSelected()
        rule.onNodeWithTag("difficulty-5").assertIsSelected()

        // Activity recreation simulates Android rotations and resource changes.
        // Both the current screen and unstarted 6x6/Majstor selections must survive.
        rule.activityRule.scenario.recreate()
        waitFor("screen-nova-igra")
        rule.onNodeWithTag("size-6").assertIsSelected()
        rule.onNodeWithTag("difficulty-5").assertIsSelected()
        rule.onNodeWithTag("size-9").assertIsNotSelected()
        rule.onNodeWithTag("difficulty-2").assertIsNotSelected()

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
        // Both keypad rows use the same key width; the four-key row is centered.
        val firstKey = rule.onNodeWithTag("number-1").fetchSemanticsNode().boundsInRoot
        val fifthKey = rule.onNodeWithTag("number-5").fetchSemanticsNode().boundsInRoot
        val sixthKey = rule.onNodeWithTag("number-6").fetchSemanticsNode().boundsInRoot
        val ninthKey = rule.onNodeWithTag("number-9").fetchSemanticsNode().boundsInRoot
        check(kotlin.math.abs(firstKey.width - sixthKey.width) < 5f) {
            "Second keypad row has different-sized keys"
        }
        check(sixthKey.left > firstKey.left && ninthKey.right < fifthKey.right) {
            "Four numeric keys must be centered under the first five"
        }
        val empty = hasContentDescription("prazno polje", substring = true)
        val before = rule.onAllNodes(empty).fetchSemanticsNodes().size
        check(before > 0) { "The generated Sudoku had no editable empty cells" }
        rule.onAllNodes(empty).onFirst().performClick()
        rule.onAllNodes(empty).onFirst().assertIsSelected()
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
        rule.onAllNodes(hasContentDescription("bilješke 2", substring = true))
            .onFirst().assertExists()
        rule.onNodeWithTag("game-erase").assertIsEnabled().performClick()
        rule.onNodeWithTag("game-back").performClick()
        waitFor("resume-game")
        val resume = rule.onNodeWithTag("resume-game")
        resume.performScrollTo().assertIsDisplayed()
        val resumeHeightDp = resume.fetchSemanticsNode().boundsInRoot.height /
            rule.activity.resources.displayMetrics.density
        check(resumeHeightDp >= 166f) {
            "Resume hero card is too short for the reference layout: $resumeHeightDp dp"
        }
        resume.performClick()
        waitFor("sudoku-board")
        rule.onNodeWithTag("game-hint").assertExists()
    }

    @Test
    fun premiumReferenceSectionsAreReachable() {
        waitFor("home-nova-igra")
        rule.onNodeWithTag("brand-footer").assertExists()
        val primaryHome = rule.onNodeWithTag("home-nova-igra")
        val compactHome = rule.onNodeWithTag("home-statistika")
        val largeBounds = primaryHome.fetchSemanticsNode().boundsInRoot
        val smallBounds = compactHome.fetchSemanticsNode().boundsInRoot
        check(largeBounds.height > smallBounds.height) {
            "Primary home cards must be taller than compact navigation cards"
        }
        rule.onNodeWithTag("home-dnevni-izazov").assertExists()
        rule.onNodeWithTag("home-postignuća").assertExists()
        rule.onNodeWithTag("home-postavke").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("home-slogan").assertExists()
        // Each UI smoke test can inherit an unfinished session and its 174dp
        // resume banner. Scroll the Settings card into view before measuring
        // layout bounds; off-viewport semantics coordinates can be clipped.
        val awardsBounds = rule.onNodeWithTag("home-postignuća")
            .fetchSemanticsNode().boundsInRoot
        val settingsBounds = rule.onNodeWithTag("home-postavke")
            .fetchSemanticsNode().boundsInRoot
        val statsBounds = compactHome.fetchSemanticsNode().boundsInRoot
        val screenWidthDp = rule.activity.resources.displayMetrics.widthPixels /
            rule.activity.resources.displayMetrics.density
        // Reference width retains the original three-column navigation.
        // The 720px emulator gets a 2+1 layout with no overlapping cards.
        if (screenWidthDp < 410f) {
            check(statsBounds.right <= awardsBounds.left + 1f &&
                settingsBounds.top >= statsBounds.bottom - 1f) {
                "Compact Home navigation geometry: width=${screenWidthDp}dp, " +
                    "stats=$statsBounds, awards=$awardsBounds, settings=$settingsBounds"
            }
            rule.onNodeWithTag("home-week-days").assertExists()
        } else {
            check(statsBounds.right <= awardsBounds.left + 1f &&
                awardsBounds.right <= settingsBounds.left + 1f) {
                "Reference Home navigation must preserve all three columns"
            }
        }
        rule.onNodeWithTag("home-streak").performScrollTo().assertExists()
        val streakBounds = rule.onNodeWithTag("home-streak")
            .fetchSemanticsNode().boundsInRoot
        val mondayMarker = rule.onAllNodes(
            hasContentDescription("Ponedjeljak:", substring = true)
        ).onFirst().fetchSemanticsNode().boundsInRoot
        check(mondayMarker.left >= streakBounds.left - 1f &&
            mondayMarker.right <= streakBounds.right + 1f &&
            mondayMarker.bottom <= streakBounds.bottom + 1f) {
            "Monday progress marker must remain inside the Home streak panel"
        }
        rule.onNodeWithText("Sljedeća", substring = true).assertExists()
        rule.onAllNodes(hasContentDescription("Ponedjeljak:", substring = true))
            .onFirst().assertExists()
        homeTile("postignuća")
        waitFor("screen-postignuća")
        // The four featured achievement rows must be visible at reference size.
        // Content-sized compact cards must contain their real description text.
        val firstCard = rule.onNodeWithTag("achievement-prva-pobjeda")
            .fetchSemanticsNode().boundsInRoot
        val firstDescription = rule.onNodeWithTag(
            "achievement-description-prva-pobjeda", useUnmergedTree = true
        ).fetchSemanticsNode().boundsInRoot
        check(firstDescription.top >= firstCard.top - 1f &&
            firstDescription.bottom <= firstCard.bottom + 1f) {
            "Achievement description escapes its card on this viewport"
        }
        val viewportWidthDp = rule.activity.resources.displayMetrics.widthPixels /
            rule.activity.resources.displayMetrics.density
        if (viewportWidthDp < 410f) {
            val minHeightPx = 180f * rule.activity.resources.displayMetrics.density
            check(firstCard.height >= minHeightPx) {
                "Compact achievement card too short: ${firstCard.height}px"
            }
        }
        rule.onNodeWithTag("achievement-sudoku-legenda").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("achievement-prva-pobjeda").performScrollTo().performClick()
        rule.onNodeWithTag("achievement-detail").assertExists()
        rule.onNodeWithText("Zatvori").performClick()
        val allAwards = rule.onNodeWithTag("achievements-show-all")
        allAwards.performScrollTo()
        val lastButtonBottom = allAwards.fetchSemanticsNode().boundsInRoot.bottom
        val footerTop = rule.onNodeWithTag("brand-footer")
            .fetchSemanticsNode().boundsInRoot.top
        check(lastButtonBottom <= footerTop + 1f) {
            "Achievements button overlaps fixed footer: $lastButtonBottom > $footerTop"
        }
        allAwards.performClick()
        val expectedAwards = listOf(
            "prva-pobjeda", "sedam-dana-zaredom", "majstor-logike",
            "bez-greške", "vrući-niz", "brzinac", "rješavač-izazova",
            "prvi-korak", "redoviti-igrač", "iskusni-igrač", "stotka",
            "besprijekoran-niz", "samostalni-mislilac", "prvi-izazov",
            "dnevna-rutina", "deset-sati-logike", "strpljivi-mislilac",
            "ekspert", "velemajstor", "mala-mreža", "srednja-mreža",
            "klasična-mreža", "zlatni-niz", "sudoku-legenda"
        )
        check(expectedAwards.size == 24)
        expectedAwards.forEach { slug ->
            rule.onNodeWithTag("achievement-$slug").assertExists()
        }
        rule.onNodeWithTag("navigate-back").performScrollTo().performClick()
        waitFor("home-nova-igra")

        homeTile("statistika")
        waitFor("screen-statistika")
        rule.onNodeWithText("TVOJ SUDOKU PUT U BROJKAMA").assertExists()
        rule.onNodeWithText("Prekinute igre").assertExists()
        rule.onNodeWithText("Riješene igre po težini").assertExists()
        rule.onNodeWithText("Postotak dovršenosti").assertExists()
        // Verify both metric rows remain separate and tappable even at 720x1280.
        listOf("played", "best", "total", "streak").forEach { id ->
            rule.onNodeWithTag("stat-tile-$id").assertExists()
        }
        val playedTile = rule.onNodeWithTag("stat-tile-played")
            .fetchSemanticsNode().boundsInRoot
        val bestTile = rule.onNodeWithTag("stat-tile-best")
            .fetchSemanticsNode().boundsInRoot
        val totalTile = rule.onNodeWithTag("stat-tile-total")
            .fetchSemanticsNode().boundsInRoot
        val streakTile = rule.onNodeWithTag("stat-tile-streak")
            .fetchSemanticsNode().boundsInRoot
        check(playedTile.right <= bestTile.left + 1f &&
            totalTile.right <= streakTile.left + 1f &&
            playedTile.bottom <= totalTile.top + 1f) {
            "Statistics metric cards overlap on a compact viewport"
        }
        rule.onNodeWithTag("stat-tile-best").performClick()
        rule.onNodeWithText("Najbrže vrijeme dovršene Sudoku partije.").assertExists()
        rule.onNodeWithText("Zatvori").performClick()
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
    fun dailyAndAchievementReferencePanelsRemainInteractive() {
        waitFor("home-nova-igra")
        homeTile("dnevni-izazov")
        waitFor("screen-dnevni-izazov")
        rule.onNodeWithTag("daily-summary").assertExists()
        rule.onNodeWithTag("daily-hero").assertExists()
        val dailyTop = rule.onNodeWithTag("daily-summary")
            .fetchSemanticsNode().boundsInRoot
        val heroTop = rule.onNodeWithTag("daily-hero")
            .fetchSemanticsNode().boundsInRoot
        check(dailyTop.bottom <= heroTop.top + 1f) {
            "Daily Challenge header must not overlap the hero"
        }
        rule.onNodeWithText("Zlatna značka").assertExists()
        rule.onNodeWithTag("daily-leaderboard").performScrollTo().performClick()
        rule.onNodeWithText("Osobna rang-lista").assertExists()
        rule.onNodeWithText("Zatvori").performClick()
        rule.onNodeWithTag("navigate-back").performScrollTo().performClick()
        waitFor("home-nova-igra")

        homeTile("postignuća")
        waitFor("screen-postignuća")
        val summary = rule.onNodeWithTag("achievement-summary")
        summary.assertExists()
        val firstRow = rule.onNodeWithTag("achievement-prva-pobjeda")
            .fetchSemanticsNode().boundsInRoot
        val secondColumn = rule.onNodeWithTag("achievement-sedam-dana-zaredom")
            .fetchSemanticsNode().boundsInRoot
        val finalRow = rule.onNodeWithTag("achievement-sudoku-legenda")
            .fetchSemanticsNode().boundsInRoot
        val summaryBounds = summary.fetchSemanticsNode().boundsInRoot
        check(firstRow.top >= summaryBounds.bottom) {
            "First achievement card overlaps the score summary"
        }
        check(kotlin.math.abs(firstRow.top - secondColumn.top) <= 1f) {
            "First two achievement cards do not share a row baseline"
        }
        check(firstRow.height > finalRow.height) {
            "Reference achievement hierarchy requires compact lower feature rows"
        }
        val award = rule.onNodeWithTag("achievement-prva-pobjeda")
        val heightInPixels = award.fetchSemanticsNode().boundsInRoot.height
        val minHeightInPixels = 145f * rule.activity.resources.displayMetrics.density
        check(heightInPixels >= minHeightInPixels) {
            "Achievement card height $heightInPixels px smaller than $minHeightInPixels px"
        }
        award.performScrollTo().performClick()
        rule.onNodeWithTag("achievement-detail").assertExists()
    }

    @Test
    fun settingsButtonsAreVisibleAndFunctional() {
        waitFor("home-nova-igra")
        homeTile("postavke")
        waitFor("screen-postavke")
        rule.onNodeWithText("PRILAGODI IGRU SVOJIM ŽELJAMA").assertExists()
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
        val privacyRow = rule.onNodeWithTag("privacy-policy")
        privacyRow.performScrollTo().assertIsDisplayed()
        val privacyBottom = privacyRow.fetchSemanticsNode().boundsInRoot.bottom
        val footerAt = rule.onNodeWithTag("brand-footer")
            .fetchSemanticsNode().boundsInRoot.top
        check(privacyBottom <= footerAt + 1f) {
            "Settings privacy action must not be obscured by the footer"
        }
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
