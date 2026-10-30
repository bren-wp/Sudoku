package com.brendigo.sudoku

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import com.brendigo.sudoku.ui.BrandHeader
import com.brendigo.sudoku.ui.SudokuTheme
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

    @Test
    fun captureRealSplashScreenForReferenceQA() {
        // The real Compose splash exists for only 850ms on launch. Bring that
        // same screen back after Home so screenshots are deterministic, without
        // changing release behavior or substituting a mocked screenshot.
        waitFor("home-nova-igra")
        rule.runOnUiThread {
            val controllerField = MainActivity::class.java.getDeclaredField("sudoku")
            controllerField.isAccessible = true
            val controller = controllerField.get(rule.activity)
                as com.brendigo.sudoku.data.SudokuController
            controller.go(com.brendigo.sudoku.data.Screen.SPLASH)
        }
        waitFor("splash-screen")
        // The loader remains a visible, accessible Compose element even when
        // system animation duration scales are set to zero by the emulator.
        rule.onNodeWithTag("splash-loading").assertIsDisplayed()
        shot("08-splash")
    }

    /**
     * Splash branding must remain real, readable Compose UI at increased font
     * scale on both 941x1672 and 720x1280 emulator viewports. This protects
     * geometric accessibility; an image-level 1:1 comparison remains separate.
     */
    @Test
    fun splashBrandDoesNotOverlapLoadingIndicatorAtLargeFontScale() {
        waitFor("home-nova-igra")
        val displayDensity = rule.activity.resources.displayMetrics.density
        rule.runOnUiThread {
            rule.activity.setContent {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalDensity provides
                        androidx.compose.ui.unit.Density(displayDensity, 1.8f)
                ) {
                    SudokuTheme {
                        Box(Modifier.fillMaxSize()) {
                            com.brendigo.sudoku.ui.SplashPage()
                        }
                    }
                }
            }
        }
        rule.waitForIdle()
        val screen = rule.onNodeWithTag("splash-screen").fetchSemanticsNode().boundsInRoot
        val brand = rule.onNodeWithTag("splash-brand").fetchSemanticsNode().boundsInRoot
        val logo = rule.onNodeWithTag("splash-logo").fetchSemanticsNode().boundsInRoot
        val title = rule.onNodeWithTag("splash-title").fetchSemanticsNode().boundsInRoot
        val byline = rule.onNodeWithTag("splash-byline").fetchSemanticsNode().boundsInRoot
        val tagline = rule.onNodeWithTag("splash-tagline").fetchSemanticsNode().boundsInRoot
        val loading = rule.onNodeWithTag("splash-loading").fetchSemanticsNode().boundsInRoot
        val slack = displayDensity * 2f
        check(brand.left >= screen.left - slack &&
            brand.right <= screen.right + slack) {
            "Splash branding overflowed the available width"
        }
        check(title.left >= screen.left - slack &&
            title.right <= screen.right + slack) {
            "Splash title overflowed at 1.8x system font scale"
        }
        check(logo.bottom <= title.top + slack &&
            title.bottom <= byline.top + slack &&
            byline.bottom <= tagline.top + slack) {
            "Splash logo, title, byline and tagline overlap"
        }
        check(tagline.bottom <= brand.bottom + slack &&
            brand.bottom + 8f * displayDensity < loading.top) {
            "Splash branding overlaps the native loading indicator"
        }
        check(loading.bottom <= screen.bottom + slack) {
            "Splash loading indicator is clipped at the bottom"
        }
        rule.onNodeWithTag("splash-loading").assertIsDisplayed()
    }

    @Test
    fun android12SystemSplashUsesSudokuBrandedIcon() {
        if (android.os.Build.VERSION.SDK_INT < 31) return
        val icon = android.util.TypedValue()
        check(rule.activity.theme.resolveAttribute(
            android.R.attr.windowSplashScreenAnimatedIcon, icon, true
        )) { "Android 12+ system splash icon is not configured" }
        check(icon.resourceId == R.drawable.ic_sudoku) {
            "System launch splash does not use the branded 7-3-9 icon"
        }
        val background = android.util.TypedValue()
        check(rule.activity.theme.resolveAttribute(
            android.R.attr.windowSplashScreenBackground, background, true
        )) { "Android 12+ system splash background is missing" }
        check(background.data == rule.activity.getColor(R.color.launcher_background)) {
            "System launch splash and adaptive icon backgrounds do not match"
        }
    }

    @Test
    fun productionAppRemainsOfflineAndDisablesAndroidBackup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val app = context.applicationInfo
        val info = context.packageManager.getPackageInfo(
            context.packageName, android.content.pm.PackageManager.GET_PERMISSIONS
        )
        check("android.permission.INTERNET" !in (info.requestedPermissions ?: emptyArray())) {
            "Sudoku must not unexpectedly request Internet access"
        }
        check(app.flags and android.content.pm.ApplicationInfo.FLAG_ALLOW_BACKUP == 0) {
            "Offline Sudoku history must not be automatically backed up"
        }
        check(app.flags and android.content.pm.ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC == 0) {
            "Cleartext traffic must remain disabled"
        }
    }

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

    /**
     * Check native New Game card geometry independently at 941x1672 and
     * 720x1280. It must use real selectable Compose elements, never a
     * reference bitmap or invisible click target laid over a screenshot.
     */
    @Test
    fun newGameCardGeometryRespectsBothEmulatorViewports() {
        waitFor("home-nova-igra")
        homeTile("nova-igra")
        waitFor("screen-nova-igra")
        rule.onNodeWithTag("new-game-title").assertExists()
        rule.onNodeWithTag("new-game-subtitle").assertExists()
        rule.onNodeWithTag("new-game-difficulty-list").assertExists()
        val density = rule.activity.resources.displayMetrics.density
        val viewportWidth = rule.activity.resources.displayMetrics.widthPixels.toFloat()
        val cardWidth = mutableListOf<Float>()
        for (index in 0..5) {
            val node = rule.onNodeWithTag("difficulty-$index")
            node.performScrollTo().assertIsDisplayed()
            val bounds = node.fetchSemanticsNode().boundsInRoot
            check(bounds.left >= -density * 2f &&
                bounds.right <= viewportWidth + density * 2f &&
                bounds.height >= density * 72f) {
                "New Game difficulty card $index clipped or too short: $bounds"
            }
            cardWidth.add(bounds.width)
        }
        check(cardWidth.maxOrNull()!! - cardWidth.minOrNull()!! <= density * 3f) {
            "New Game card widths must remain consistent on the same viewport"
        }
        val action = rule.onNodeWithTag("primary-action")
        action.performScrollTo().assertIsDisplayed()
        val actionBounds = action.fetchSemanticsNode().boundsInRoot
        check(actionBounds.left >= -density &&
            actionBounds.right <= viewportWidth + density &&
            actionBounds.height >= density * 48f) {
            "New Game CTA is clipped or below the minimum 48dp target: $actionBounds"
        }
        rule.onNodeWithTag("new-game-size-header").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("size-9").assertIsSelected()
        rule.onNodeWithTag("difficulty-2").assertIsSelected()
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

    /**
     * The live 9×9 game must preserve the owner's square board, four aligned
     * tools and a centered 5+4 keypad on both emulated device sizes. This
     * verifies actual Compose hit regions, not decorative screenshot pixels.
     */
    @Test
    fun gameplayReferenceGeometryKeepsBoardToolsAndNineKeysAligned() {
        waitFor("home-nova-igra")
        homeTile("nova-igra")
        rule.onNodeWithTag("size-9").assertIsSelected()
        rule.onNodeWithTag("primary-action").performScrollTo().performClick()
        waitFor("sudoku-board", timeoutMs = 60_000)
        val dp = rule.activity.resources.displayMetrics.density
        val viewportWidth = rule.activity.resources.displayMetrics.widthPixels.toFloat()
        val board = rule.onNodeWithTag("sudoku-board")
        board.performScrollTo().assertIsDisplayed()
        val boardRect = board.fetchSemanticsNode().boundsInRoot
        check(kotlin.math.abs(boardRect.width - boardRect.height) <= 3f * dp &&
            boardRect.left >= -dp && boardRect.right <= viewportWidth + dp) {
            "Real Sudoku board must be square and fully fit the viewport: $boardRect"
        }
        (0..80).forEach { index ->
            rule.onNodeWithTag("sudoku-cell-$index").assertExists()
        }
        val firstCell = rule.onNodeWithTag("sudoku-cell-0").fetchSemanticsNode().boundsInRoot
        val secondCell = rule.onNodeWithTag("sudoku-cell-1").fetchSemanticsNode().boundsInRoot
        val nextRow = rule.onNodeWithTag("sudoku-cell-9").fetchSemanticsNode().boundsInRoot
        check(firstCell.width > 0f && firstCell.height > 0f &&
            kotlin.math.abs(firstCell.width - firstCell.height) < 2f * dp &&
            kotlin.math.abs(firstCell.top - secondCell.top) <= dp &&
            firstCell.right <= secondCell.left + dp &&
            nextRow.top >= firstCell.bottom - dp) {
            "Grid cell geometry is inconsistent: $firstCell / $secondCell / $nextRow"
        }

        val tools = rule.onNodeWithTag("game-tool-row")
        tools.performScrollTo().assertIsDisplayed()
        val toolsRect = tools.fetchSemanticsNode().boundsInRoot
        val buttons = listOf("game-notes", "game-hint", "game-undo", "game-erase")
            .map { rule.onNodeWithTag(it).fetchSemanticsNode().boundsInRoot }
        buttons.zipWithNext().forEach { (left, right) ->
            check(left.right <= right.left + dp &&
                kotlin.math.abs(left.top - right.top) <= dp) {
                "Gameplay toolbar overlaps or misaligns its buttons"
            }
        }
        check(buttons.first().left >= toolsRect.left - dp &&
            buttons.last().right <= toolsRect.right + dp &&
            buttons.all { it.height >= 48f * dp }) {
            "The real gameplay tools do not fit their full 48dp hit targets"
        }

        val panel = rule.onNodeWithTag("game-number-panel")
        panel.performScrollTo().assertIsDisplayed()
        val panelRect = panel.fetchSemanticsNode().boundsInRoot
        val keys = (1..9).map { value ->
            rule.onNodeWithTag("number-$value").fetchSemanticsNode().boundsInRoot
        }
        val firstRow = keys.take(5)
        val secondRow = keys.drop(5)
        val keyWidth = keys.first().width
        check(keys.all {
            it.width > 0f && it.height >= 48f * dp &&
                kotlin.math.abs(it.width - keyWidth) < dp &&
                it.left >= panelRect.left - dp &&
                it.right <= panelRect.right + dp
        }) { "Gameplay keypad has clipped or differently sized native buttons" }
        firstRow.zipWithNext().forEach { (left, right) ->
            check(left.right <= right.left + dp &&
                kotlin.math.abs(left.top - right.top) <= dp) {
                "Top keypad row must remain five non-overlapping horizontal keys"
            }
        }
        secondRow.zipWithNext().forEach { (left, right) ->
            check(left.right <= right.left + dp &&
                kotlin.math.abs(left.top - right.top) <= dp) {
                "Bottom keypad row must remain four non-overlapping horizontal keys"
            }
        }
        check(secondRow.first().top >= firstRow.first().bottom - dp &&
            secondRow.first().left > firstRow.first().left &&
            secondRow.last().right < firstRow.last().right) {
            "Native 5+4 Sudoku keypad must remain vertically ordered and centered"
        }
    }

    @Test
    fun gamePauseConcealsGridAndResumeRestoresControls() {
        waitFor("home-nova-igra")
        homeTile("nova-igra")
        rule.onNodeWithTag("primary-action").performScrollTo().performClick()
        waitFor("sudoku-board", timeoutMs = 60_000)

        // HUD values and interaction surfaces stay present on both emulator
        // resolutions, while the standard two-row keypad remains usable.
        rule.onNodeWithTag("game-hud").assertExists()
        rule.onNodeWithTag("game-time").assertExists()
        rule.onNodeWithTag("game-progress").assertExists()
        rule.onNodeWithTag("game-mistakes").assertExists()
        rule.onNodeWithText("Srednja težina").assertExists()
        rule.onNodeWithText("praznih polja", substring = true).assertExists()
        val boardBounds = rule.onNodeWithTag("sudoku-board")
            .fetchSemanticsNode().boundsInRoot
        val metrics = rule.activity.resources.displayMetrics
        if (metrics.widthPixels in 930..950 && metrics.heightPixels in 1660..1690) {
            check(boardBounds.top in 230f..275f) {
                "Reference 941×1672 Sudoku board starts too low/high: " +
                    "top=${boardBounds.top}px, expected near the reference's 250px"
            }
        }
        val noteCard = rule.onNodeWithTag("game-notes")
        noteCard.assertIsNotSelected()
        rule.onNodeWithTag("game-notes-status", useUnmergedTree = true).assertExists()
        rule.onNodeWithText("Isključeno", useUnmergedTree = true).assertExists()
        val notesBounds = noteCard.fetchSemanticsNode().boundsInRoot
        val statusBounds = rule.onNodeWithTag("game-notes-status", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        check(statusBounds.top >= notesBounds.top - 1f &&
            statusBounds.bottom <= notesBounds.bottom + 1f) {
            "Reference Notes label/toggle escapes its card: status=$statusBounds, card=$notesBounds"
        }
        val hintBounds = rule.onNodeWithTag("game-hint").fetchSemanticsNode().boundsInRoot
        val eraseBounds = rule.onNodeWithTag("game-erase").fetchSemanticsNode().boundsInRoot
        check(notesBounds.right <= hintBounds.left + 1f &&
            notesBounds.top == hintBounds.top &&
            eraseBounds.right > hintBounds.right) {
            "Gameplay tool cards overlap or lose their reference row order"
        }
        noteCard.performClick()
        noteCard.assertIsSelected()
        rule.onNodeWithText("Uključeno", useUnmergedTree = true).assertExists()
        noteCard.performClick()
        noteCard.assertIsNotSelected()
        rule.onNodeWithText("Isključeno", useUnmergedTree = true).assertExists()
        rule.onNodeWithTag("sudoku-cell-0").assertExists()

        rule.onNodeWithTag("game-pause").performClick()
        rule.onNodeWithText("Igra je pauzirana").assertExists()
        rule.onNodeWithTag("sudoku-board-paused").assertExists()
        rule.onNodeWithTag("sudoku-cell-0").assertDoesNotExist()
        rule.onNodeWithTag("number-1").assertIsNotEnabled()
        rule.onNodeWithTag("game-hint").assertIsNotEnabled()
        rule.onNodeWithText("▶ Nastavi").performClick()
        rule.onNodeWithTag("sudoku-board-paused").assertDoesNotExist()
        rule.onNodeWithTag("sudoku-cell-0").assertExists()
        rule.onNodeWithTag("number-1").assertIsEnabled()
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
        val dailyTile = rule.onNodeWithTag("home-dnevni-izazov")
        dailyTile.assertExists()
        val dailyBounds = dailyTile.fetchSemanticsNode().boundsInRoot
        val density = rule.activity.resources.displayMetrics.density
        check(largeBounds.right <= dailyBounds.left + density &&
            kotlin.math.abs(largeBounds.top - dailyBounds.top) <= density * 2f &&
            kotlin.math.abs(largeBounds.bottom - dailyBounds.bottom) <= density * 2f) {
            "Home primary cards must remain a level, non-overlapping reference pair: " +
                "$largeBounds and $dailyBounds"
        }
        rule.onNodeWithTag("home-postignuća").assertExists()
        rule.onNodeWithTag("home-postavke").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("home-slogan").assertExists()
        // Each UI smoke test can inherit an unfinished session and its 190dp
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
        val weekRow = rule.onNodeWithTag("home-week-days")
            .fetchSemanticsNode().boundsInRoot
        val weekMarkers = (0..6).map { index ->
            rule.onNodeWithTag("home-weekday-$index")
                .fetchSemanticsNode().boundsInRoot
        }
        check(weekMarkers.size == 7 &&
            weekMarkers.all { marker ->
                marker.width > 0f && marker.height > 0f &&
                    marker.left >= streakBounds.left - 1f &&
                    marker.right <= streakBounds.right + 1f &&
                    marker.top >= weekRow.top - density &&
                    marker.bottom <= weekRow.bottom + density
            }) {
            "Each weekday must remain a visible and bounded real UI element"
        }
        weekMarkers.zipWithNext().forEach { (first, second) ->
            check(first.right <= second.left + density) {
                "Home weekly progress markers overlap or change order: $first / $second"
            }
        }
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
        val secondCard = rule.onNodeWithTag("achievement-sedam-dana-zaredom")
            .fetchSemanticsNode().boundsInRoot
        val thirdCard = rule.onNodeWithTag("achievement-majstor-logike")
            .fetchSemanticsNode().boundsInRoot
        val pxTolerance = rule.activity.resources.displayMetrics.density * 2f
        check(
            firstCard.right <= secondCard.left + pxTolerance &&
                kotlin.math.abs(firstCard.top - secondCard.top) <= pxTolerance &&
                kotlin.math.abs(firstCard.bottom - secondCard.bottom) <= pxTolerance &&
                thirdCard.top >= firstCard.bottom - pxTolerance
        ) {
            "Achievement cards must keep 2-column aligned rows: " +
                "first=$firstCard, second=$secondCard, third=$thirdCard"
        }
        rule.onNodeWithTag("achievement-summary").assertExists()
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
        // Verify that the four metric cards and chart pair match the
        // reference hierarchy without overlapping on either emulator width.
        val chart = rule.onNodeWithTag("stats-difficulty-chart")
            .fetchSemanticsNode().boundsInRoot
        val ring = rule.onNodeWithTag("stats-completion-ring")
            .fetchSemanticsNode().boundsInRoot
        val statsDensity = rule.activity.resources.displayMetrics.density
        if (screenWidthDp >= 410f) {
            check(chart.right <= ring.left + statsDensity &&
                kotlin.math.abs(chart.top - ring.top) <= statsDensity * 2f &&
                kotlin.math.abs(chart.bottom - ring.bottom) <= statsDensity * 3f) {
                "Reference Stats chart and ring must share one level row: $chart, $ring"
            }
        } else {
            check(chart.bottom <= ring.top + statsDensity) {
                "Compact Stats chart and ring must stack without overlap: $chart, $ring"
            }
        }
        rule.onNodeWithTag("stats-history-panel").assertExists()
        rule.onNodeWithTag("stats-achievements").assertExists()
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

    /**
     * All four real award previews must have consistent native geometry and
     * dynamic progress even when every achievement remains locked. The
     * reference width uses four columns; compact widths use two 2-card rows.
     */
    /**
     * All 24 native achievement cards remain accessible after expanding
     * and rotating. Only the stable achievement title is saved for the
     * dialog; points and progress stay backed by the actual controller.
     */
    @Test
    fun achievementsExpandedListAndDetailsSurviveActivityRecreation() {
        waitFor("home-postignuća")
        homeTile("postignuća")
        waitFor("screen-postignuća")
        rule.onNodeWithTag(
            "achievement-progress-prva-pobjeda", useUnmergedTree = true
        ).assertExists()
        rule.onNodeWithTag(
            "achievement-progress-sudoku-legenda", useUnmergedTree = true
        ).assertExists()
        val showAll = rule.onNodeWithTag("achievements-show-all")
        showAll.performScrollTo().performClick()
        rule.onNodeWithText("Prikaži istaknuta postignuća").assertExists()
        rule.onNodeWithTag("achievement-zlatni-niz").assertExists()
        rule.onNodeWithTag("achievement-mala-mreža").assertExists()
        rule.onNodeWithTag("achievement-zlatni-niz").performScrollTo().performClick()
        rule.onNodeWithTag("achievement-detail").assertExists()
        rule.activityRule.scenario.recreate()
        waitFor("screen-postignuća")
        rule.onNodeWithTag("achievement-detail").assertExists()
        rule.onNodeWithText("Zatvori").performClick()
        rule.onNodeWithTag("achievement-zlatni-niz").assertExists()
        rule.onNodeWithTag(
            "achievement-progress-zlatni-niz", useUnmergedTree = true
        ).assertExists()
        rule.onNodeWithTag("achievements-show-all").performScrollTo()
        rule.onNodeWithText("Prikaži istaknuta postignuća").assertExists()
    }

    @Test
    fun statisticsAwardPreviewGeometryWorksAcrossBothViewports() {
        waitFor("home-nova-igra")
        homeTile("statistika")
        waitFor("screen-statistika")
        rule.onNodeWithTag("stats-achievements").performScrollTo()
            .assertIsDisplayed()
        rule.onNodeWithTag("stats-completion-percent", useUnmergedTree = true)
            .assertExists()
        val cards = (0..3).map { index ->
            val node = rule.onNodeWithTag(
                "stats-award-preview-$index", useUnmergedTree = true
            )
            node.assertExists()
            node.fetchSemanticsNode().boundsInRoot
        }
        val density = rule.activity.resources.displayMetrics.density
        val widthDp = rule.activity.resources.displayMetrics.widthPixels / density
        check(cards.all { it.width > 0f && it.height >= 70f * density }) {
            "Each real Statistics achievement preview must be readable: $cards"
        }
        if (widthDp < 410f) {
            check(
                cards[0].right <= cards[1].left + density &&
                cards[2].right <= cards[3].left + density &&
                cards[0].bottom <= cards[2].top + density &&
                cards[1].bottom <= cards[3].top + density
            ) { "Compact Statistics previews must be a non-overlapping 2x2 grid: $cards" }
        } else {
            cards.zipWithNext().forEach { (left, right) ->
                check(
                    left.right <= right.left + density &&
                    kotlin.math.abs(left.top - right.top) <= density * 2f &&
                    kotlin.math.abs(left.bottom - right.bottom) <= density * 2f
                ) { "Reference Statistics previews must align on one row: $cards" }
            }
        }
        rule.onNodeWithTag("stats-history-panel").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("open-game-history").performClick()
        rule.onNodeWithText("Povijest igara").assertExists()
        rule.onNodeWithText("Zatvori").performClick()
    }

    /**
     * Day counts/dates are always real device/user data. Verify the layout
     * instead of changing completed days just to reproduce an illustration.
     */
    @Test
    fun dailyWeekdayGeometryAndLeaderboardRemainAccessible() {
        waitFor("home-nova-igra")
        homeTile("dnevni-izazov")
        waitFor("screen-dnevni-izazov")
        val density = rule.activity.resources.displayMetrics.density
        val width = rule.activity.resources.displayMetrics.widthPixels.toFloat()
        val week = rule.onNodeWithTag("daily-weekly-streak")
        week.performScrollTo().assertIsDisplayed()
        val row = week.fetchSemanticsNode().boundsInRoot
        val days = (0..6).map { i ->
            rule.onNodeWithTag("daily-weekday-$i")
                .fetchSemanticsNode().boundsInRoot
        }
        check(days.size == 7 && days.all {
            it.width > 0f && it.height > 0f &&
                it.left >= row.left - density &&
                it.right <= row.right + density &&
                it.left >= -density &&
                it.right <= width + density
        }) { "Seven real calendar days must fit inside the Daily timeline" }
        days.zipWithNext().forEach { (left, right) ->
            check(left.right <= right.left + density &&
                kotlin.math.abs(left.top - right.top) <= density * 2f) {
                "Daily week markers are out of order or overlap: $left / $right"
            }
        }
        rule.onNodeWithTag("daily-reward-emblem", useUnmergedTree = true)
            .assertExists()
        val action = rule.onNodeWithTag("daily-leaderboard")
        action.performScrollTo().assertIsDisplayed()
        val bounds = action.fetchSemanticsNode().boundsInRoot
        check(bounds.height >= 48f * density - 1f &&
            bounds.left >= -density && bounds.right <= width + density) {
            "Offline leaderboard action is clipped or smaller than 48dp: $bounds"
        }
        action.performClick()
        rule.onNodeWithText("Osobna rang-lista").assertExists()
        rule.onNodeWithText(
            "Offline rezultati na ovom uređaju. Nema globalne rang-liste."
        ).assertExists()
        rule.onNodeWithText("Zatvori").performClick()
    }

    @Test
    fun dailyAndAchievementReferencePanelsRemainInteractive() {
        waitFor("home-nova-igra")
        homeTile("dnevni-izazov")
        waitFor("screen-dnevni-izazov")
        rule.onNodeWithTag("daily-summary").assertExists()
        rule.onNodeWithTag("daily-hero").assertExists()
        val rewardBounds = rule.onNodeWithTag("daily-reward-metric")
            .fetchSemanticsNode().boundsInRoot
        val difficultyBounds = rule.onNodeWithTag("daily-difficulty-metric")
            .fetchSemanticsNode().boundsInRoot
        val timeBounds = rule.onNodeWithTag("daily-time-metric")
            .fetchSemanticsNode().boundsInRoot
        val compactWidthDp = rule.activity.resources.displayMetrics.widthPixels /
            rule.activity.resources.displayMetrics.density
        if (compactWidthDp < 410f) {
            check(difficultyBounds.right <= timeBounds.left + 1f &&
                rewardBounds.top >= difficultyBounds.bottom - 1f) {
                "Compact Daily metrics must form 2+1 rows: " +
                    "difficulty=$difficultyBounds, time=$timeBounds, reward=$rewardBounds"
            }
        } else {
            check(difficultyBounds.right <= timeBounds.left + 1f &&
                timeBounds.right <= rewardBounds.left + 1f) {
                "Reference Daily metrics must preserve their three-column layout"
            }
        }
        rule.onNodeWithTag("daily-weekly-streak").performScrollTo().assertIsDisplayed()
        listOf("PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED").forEach { day ->
            rule.onAllNodes(hasContentDescription("$day:", substring = true))
                .onFirst().assertExists()
        }
        val dailyTop = rule.onNodeWithTag("daily-summary")
            .fetchSemanticsNode().boundsInRoot
        val heroTop = rule.onNodeWithTag("daily-hero")
            .fetchSemanticsNode().boundsInRoot
        check(dailyTop.bottom <= heroTop.top + 1f) {
            "Daily Challenge header must not overlap the hero"
        }
        val actualDensity = rule.activity.resources.displayMetrics.density
        val panel = rule.onNodeWithTag("daily-streak-panel")
            .fetchSemanticsNode().boundsInRoot
        check(heroTop.bottom <= panel.top + actualDensity) {
            "Daily hero overlaps week timeline"
        }
        // In the compact viewport, Compose's scroll container intentionally
        // leaves offscreen nodes unplaced (0x0 bounds). Scroll to each action
        // before asserting its visibility instead of comparing stale bounds.
        rule.onNodeWithTag("primary-action").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("daily-leaderboard").performScrollTo().assertIsDisplayed()
        // Metric bounds above were captured before scrolling; do not compare
        // them against later scroll-offset hero bounds on compact viewports.
        // The 2+1 vs 3-column assertions already validate these layouts.
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
    fun savedDailyChallengeIsClearlyMarkedAndResumesWithoutReset() {
        waitFor("home-nova-igra")
        homeTile("dnevni-izazov")
        waitFor("screen-dnevni-izazov")
        rule.onNodeWithTag("primary-action").performScrollTo().performClick()
        waitFor("sudoku-board", timeoutMs = 60_000)
        // Toggle Notes deliberately: if a new puzzle is generated on Resume,
        // its default Notes mode would be disabled.
        val notes = rule.onNodeWithTag("game-notes")
        if (!notes.fetchSemanticsNode().config[SemanticsProperties.Selected]) {
            notes.performClick()
        }
        notes.assertIsSelected()
        rule.onNodeWithTag("game-back").performClick()
        waitFor("home-nova-igra")
        // Verify disk-backed resume, not only the still-alive controller.
        rule.activityRule.scenario.recreate()
        waitFor("home-nova-igra")
        homeTile("dnevni-izazov")
        waitFor("screen-dnevni-izazov")
        rule.onNodeWithText("Nastavi današnji izazov", substring = true).assertExists()
        rule.onNodeWithTag("primary-action").performScrollTo().performClick()
        waitFor("sudoku-board", timeoutMs = 60_000)
        rule.onNodeWithTag("game-notes").assertIsSelected()
        rule.onNodeWithTag("number-1").assertIsEnabled()
    }

    @Test
    fun allSevenSettingsPersistAfterActivityRecreation() {
        waitFor("home-nova-igra")
        homeTile("postavke")
        waitFor("screen-postavke")

        // Toggle each switch via its full accessible row, not a visual thumb.
        val mainToggles = listOf(
            "setting-tamna-tema", "setting-animacije",
            "setting-automatske-bilješke", "setting-provjera-greške",
            "setting-isticanje-brojeva",
        )
        val original = mainToggles.associateWith { tag ->
            rule.onNodeWithTag(tag).performScrollTo()
                .fetchSemanticsNode().config[SemanticsProperties.ToggleableState]
        }
        mainToggles.forEach { tag ->
            val toggle = rule.onNodeWithTag(tag)
            toggle.performScrollTo().performClick()
            rule.waitForIdle()
            check(toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] !=
                original.getValue(tag)) { "Switch $tag did not respond to full-row tap" }
        }

        rule.onNodeWithTag("sound-vibration").performScrollTo().performClick()
        val modalToggles = listOf("setting-zvuk", "setting-vibracija")
        val modalOriginal = modalToggles.associateWith { tag ->
            rule.onNodeWithTag(tag)
                .fetchSemanticsNode().config[SemanticsProperties.ToggleableState]
        }
        modalToggles.forEach { tag -> rule.onNodeWithTag(tag).performClick() }

        // Recreate while the sound dialog is still visible: the screen,
        // modal and both disk-backed option changes must all survive.
        rule.activityRule.scenario.recreate()
        waitFor("screen-postavke")
        modalToggles.forEach { tag ->
            val toggle = rule.onNodeWithTag(tag)
            check(toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] !=
                modalOriginal.getValue(tag)) { "$tag was not saved or modal state was lost" }
            toggle.performClick() // Restore device-specific original preference.
            rule.waitForIdle()
            check(toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                modalOriginal.getValue(tag)) { "$tag failed to restore initial state" }
        }
        rule.onNodeWithText("Zatvori").performClick()

        mainToggles.forEach { tag ->
            val toggle = rule.onNodeWithTag(tag)
            toggle.performScrollTo()
            check(toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] !=
                original.getValue(tag)) { "Preference $tag did not persist after recreation" }
            toggle.performClick() // Revert settings; tests must not alter user defaults.
            rule.waitForIdle()
            check(toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                original.getValue(tag)) { "Preference $tag did not restore initial state" }
        }
        rule.onNodeWithTag("privacy-policy").performScrollTo().performClick()
        rule.onNodeWithText("Sudoku by Brendigo možeš igrati bez registracije i internetske veze.")
            .assertExists()
        rule.onNodeWithText("Zatvori").performClick()
    }

    @Test
    fun gameSettingsReturnToSamePuzzleAfterRecreation() {
        waitFor("home-nova-igra")
        homeTile("nova-igra")
        rule.onNodeWithTag("primary-action").performScrollTo().performClick()
        waitFor("sudoku-board", timeoutMs = 60_000)
        rule.onNodeWithTag("game-settings").performClick()
        waitFor("screen-postavke")

        rule.activityRule.scenario.recreate()
        waitFor("screen-postavke")
        rule.onNodeWithTag("navigate-back").performScrollTo().performClick()
        waitFor("sudoku-board")
        rule.onNodeWithTag("sudoku-cell-80").assertExists()
        rule.onNodeWithTag("number-9").assertExists()
    }

    /**
     * The five Settings panels remain real accessible controls. Check the
     * tappable row, native dropdown value and release version at both emulator
     * viewports instead of using the owner's flat reference as a fake UI.
     */
    @Test
    fun settingsReferenceRowsKeepTouchTargetsAndCorrectContent() {
        waitFor("home-nova-igra")
        homeTile("postavke")
        waitFor("screen-postavke")
        val density = rule.activity.resources.displayMetrics.density
        val screenWidth = rule.activity.resources.displayMetrics.widthPixels.toFloat()

        val switches = listOf(
            "setting-tamna-tema",
            "setting-animacije",
            "setting-automatske-bilješke",
            "setting-provjera-greške",
            "setting-isticanje-brojeva",
        )
        switches.forEach { tag ->
            val node = rule.onNodeWithTag(tag)
            node.performScrollTo().assertIsDisplayed()
            val bounds = node.fetchSemanticsNode().boundsInRoot
            check(bounds.height >= 48f * density - 1f &&
                bounds.left >= -density &&
                bounds.right <= screenWidth + density) {
                "Settings switch $tag is too short or clipped: $bounds"
            }
            check(node.fetchSemanticsNode().config.contains(
                SemanticsProperties.ToggleableState
            )) { "Settings switch $tag must remain natively toggleable" }
        }

        val language = rule.onNodeWithTag("language-settings")
        language.performScrollTo().assertIsDisplayed()
        val languagePanel = language.fetchSemanticsNode().boundsInRoot
        val value = rule.onNodeWithTag(
            "settings-language-value", useUnmergedTree = true
        ).fetchSemanticsNode().boundsInRoot
        check(value.width > 0f && value.height > 0f &&
            value.left >= languagePanel.left - density &&
            value.right <= languagePanel.right + density &&
            value.top >= languagePanel.top - density &&
            value.bottom <= languagePanel.bottom + density) {
            "Croatian language selector must fit in the blue language panel"
        }
        rule.onNodeWithText("Hrvatski", useUnmergedTree = true).assertExists()

        rule.onNodeWithTag("settings-version").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText(
            "Verzija " + BuildConfig.VERSION_NAME, useUnmergedTree = true
        ).assertExists()
        rule.onNodeWithTag("privacy-policy").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("about-app").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("settings-backup").assertExists()
    }

    @Test
    fun settingsButtonsAreVisibleAndFunctional() {
        waitFor("home-nova-igra")
        homeTile("postavke")
        waitFor("screen-postavke")
        rule.onNodeWithText("PRILAGODI IGRU SVOJIM ŽELJAMA").assertExists()
        // The reference viewport shows the first two groups in sequence.
        // Offscreen scrollable children may legitimately have 0x0 geometry on
        // compact devices, so do not compare bounds across scroll offsets.
        val appearance = rule.onNodeWithTag("settings-appearance")
            .fetchSemanticsNode().boundsInRoot
        val gamePanel = rule.onNodeWithTag("settings-game")
            .fetchSemanticsNode().boundsInRoot
        val tolerance = rule.activity.resources.displayMetrics.density * 2f
        check(appearance.bottom <= gamePanel.top + tolerance &&
            kotlin.math.abs(appearance.left - gamePanel.left) <= tolerance &&
            kotlin.math.abs(appearance.right - gamePanel.right) <= tolerance) {
            "Settings first two groups must be ordered and aligned"
        }
        listOf("sound-vibration", "language-settings", "settings-about")
            .forEach { tag ->
                rule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
                val visibleBounds = rule.onNodeWithTag(tag)
                    .fetchSemanticsNode().boundsInRoot
                check(visibleBounds.width > 0f &&
                    kotlin.math.abs(visibleBounds.width - appearance.width) <= tolerance) {
                    "Settings panel $tag must align with other full-width panels"
                }
            }
        rule.onNodeWithTag("settings-backup").assertExists()
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
        val privacyBounds = privacyRow.fetchSemanticsNode().boundsInRoot
        val privacyBottom = privacyBounds.bottom
        // WCAG/Android usability: keep a real 48dp touch target even if
        // reference artwork renders a visually smaller row.
        val minimumTouchPx = 48f * rule.activity.resources.displayMetrics.density
        check(privacyBounds.bottom - privacyBounds.top + 0.5f >= minimumTouchPx) {
            "Settings privacy action touch height is smaller than 48dp"
        }
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
        rule.onNodeWithTag("about-app").performScrollTo().performClick()
        rule.onNodeWithText("Treniraj mozak svaki dan.").assertExists()
        rule.onNodeWithText("Zatvori").performClick()
        rule.onNodeWithTag("privacy-policy").performScrollTo().performClick()
        rule.onNodeWithText("Sudoku by Brendigo možeš igrati bez registracije i internetske veze.")
            .assertExists()
        rule.onNodeWithText("Zatvori").performClick()

        // Both offline backup actions must be genuinely interactive,
        // not decorative UI or an action that writes files automatically.
        rule.onNodeWithTag("backup-export").performScrollTo().performClick()
        rule.onNodeWithText("Izvezi sigurnosnu kopiju").assertExists()
        rule.onNodeWithText("Odustani").performClick()
        rule.onNodeWithTag("backup-import").performScrollTo().performClick()
        rule.onNodeWithText("Uvezi sigurnosnu kopiju").assertExists()
        rule.onNodeWithText("Odustani").performClick()

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

    /**
     * QA-only render of the REAL in-app Compose header. The test places the
     * branded Compose component on a black canvas, crops its actual pixels,
     * and centers the crop inside a 1254px square to compare with the owner's
     * standalone wordmark artwork. No application preferences are modified.
     */
    @Test
    fun captureActualComposeWordmarkForTenReferenceQa() {
        waitFor("home-nova-igra")
        rule.runOnUiThread {
            rule.activity.setContent {
                SudokuTheme {
                    Box(Modifier.fillMaxSize().background(ComposeColor.Black),
                        contentAlignment = Alignment.Center) {
                        BrandHeader(Modifier.fillMaxWidth())
                    }
                }
            }
        }
        rule.waitForIdle()
        Thread.sleep(350)
        val shot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            ?: error("No real Compose logo screenshot")
        try {
            var minX = shot.width
            var minY = shot.height
            var maxX = -1
            var maxY = -1
            for (y in shot.height / 4 until shot.height * 3 / 4 step 2) {
                for (x in 0 until shot.width step 2) {
                    val c = shot.getPixel(x, y)
                    if (Color.red(c) > 48 || Color.green(c) > 48 ||
                        Color.blue(c) > 84) {
                        minX = minOf(minX, x)
                        minY = minOf(minY, y)
                        maxX = maxOf(maxX, x)
                        maxY = maxOf(maxY, y)
                    }
                }
            }
            check(maxX - minX > 100 && maxY - minY > 30) {
                "Could not find original Compose brand header on screenshot"
            }
            val crop = Bitmap.createBitmap(
                shot, minX, minY, maxX - minX + 1, maxY - minY + 1
            )
            try {
                // A screen screenshot is inherently opaque. Make the QA-only
                // black studio backdrop transparent before resizing; the
                // owner's 1254px wordmark reference has an alpha channel.
                // The actual 7-3-9 tile stays opaque (its dark blue is NOT black).
                val transparent = crop.copy(Bitmap.Config.ARGB_8888, true)
                    ?: error("Cannot allocate transparent wordmark QA image")
                try {
                    val pixels = IntArray(crop.width * crop.height)
                    transparent.getPixels(pixels, 0, crop.width, 0, 0,
                        crop.width, crop.height)
                    for (index in pixels.indices) {
                        val c = pixels[index]
                        if (Color.red(c) <= 4 && Color.green(c) <= 4 &&
                            Color.blue(c) <= 4) pixels[index] = Color.TRANSPARENT
                    }
                    transparent.setPixels(pixels, 0, crop.width, 0, 0,
                        crop.width, crop.height)
                    val width = 1160
                    val height = (transparent.height.toFloat() /
                        transparent.width * width).toInt()
                    check(height in 50..1100) {
                        "Invalid Compose wordmark aspect ratio"
                    }
                    val scaled = Bitmap.createScaledBitmap(
                        transparent, width, height, true
                    )
                try {
                    val full = Bitmap.createBitmap(1254, 1254, Bitmap.Config.ARGB_8888)
                    try {
                        val canvas = Canvas(full)
                        // Keep outside-logo pixels transparent, not black.
                        canvas.drawBitmap(scaled, (1254 - width) / 2f,
                            (1254 - height) / 2f, Paint(Paint.FILTER_BITMAP_FLAG))
                        saveBrandingQaPng("09-wordmark.png", full)
                    } finally { full.recycle() }
                } finally { scaled.recycle() }
                } finally { transparent.recycle() }
            } finally { crop.recycle() }
        } finally { shot.recycle() }
    }

    private fun saveBrandingQaPng(name: String, bitmap: Bitmap) {
        val resolver = rule.activity.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/SudokuQA")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Cannot create $name")
        try {
            resolver.openOutputStream(uri)?.use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
            } ?: error("Cannot write $name")
            check(resolver.update(uri, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null) == 1)
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    /**
     * Native Android launcher resource rendered at the original square
     * reference resolution. This is NOT a copy of the owner's PNG; it exercises
     * the actual adaptive icon packaged in the installed APK.
     */
    @Test
    fun captureNativeLauncherForTenReferenceQa() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val launcher = context.packageManager.getApplicationIcon(context.packageName)
        check(launcher is android.graphics.drawable.AdaptiveIconDrawable) {
            "Android launcher icon is not the expected adaptive resource"
        }
        // Reference 10 is transparent around the original icon. Rendering on
        // an opaque black canvas creates a *false* mismatch over the entire
        // transparent region and hides Android's actual adaptive mask.
        val bitmap = Bitmap.createBitmap(1254, 1254, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap) // newly allocated ARGB_8888 is transparent
            launcher.setBounds(0, 0, 1254, 1254)
            launcher.draw(canvas)
            check(Color.alpha(bitmap.getPixel(0, 0)) == 0) {
                "Adaptive launcher background incorrectly extends past its mask"
            }
            check(Color.alpha(bitmap.getPixel(627, 627)) > 0) {
                "Adaptive launcher has no visible center"
            }
            saveBrandingQaPng("10-launcher-icon.png", bitmap)
        } finally {
            bitmap.recycle()
        }

        // Monochrome is a separate Android 13+ themed-icon asset: compare its
        // alpha mask independently instead of tinting the RGB reference art.
        val monochrome = context.getDrawable(R.drawable.ic_sudoku_monochrome)
            ?: error("Missing monochrome Sudoku launcher resource")
        val monoBitmap = Bitmap.createBitmap(1254, 1254, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(monoBitmap)
            monochrome.setBounds(0, 0, 1254, 1254)
            monochrome.draw(canvas)
            check(Color.alpha(monoBitmap.getPixel(0, 0)) == 0) {
                "Themed icon must preserve transparent corners"
            }
            check(Color.alpha(monoBitmap.getPixel(627, 627)) > 0) {
                "Themed icon lost the center digit"
            }
            saveBrandingQaPng("11-launcher-monochrome.png", monoBitmap)
        } finally {
            monoBitmap.recycle()
        }
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
        rule.onNodeWithTag("game-number-panel").assertExists()
        val gameBoard = rule.onNodeWithTag("sudoku-board").fetchSemanticsNode().boundsInRoot
        val numPanel = rule.onNodeWithTag("game-number-panel").fetchSemanticsNode().boundsInRoot
        val firstNumber = rule.onNodeWithTag("number-1").fetchSemanticsNode().boundsInRoot
        val ninthNumber = rule.onNodeWithTag("number-9").fetchSemanticsNode().boundsInRoot
        val epsilon = rule.activity.resources.displayMetrics.density * 2f
        check(numPanel.top >= gameBoard.bottom - epsilon &&
            firstNumber.left >= numPanel.left - epsilon &&
            ninthNumber.right <= numPanel.right + epsilon) {
            "Gameplay board/keypad must stay ordered and within keypad frame: " +
                "$gameBoard, $numPanel, $firstNumber, $ninthNumber"
        }
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
