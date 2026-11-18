package com.brendigo.sudoku

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.brendigo.sudoku.ui.SectionTitle
import com.brendigo.sudoku.ui.SolidAction
import com.brendigo.sudoku.ui.SudokuTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Native geometry regression tests for the two highest-priority screens.
 * These read real Compose semantics and never modify player achievements.
 */
@RunWith(AndroidJUnit4::class)
class SudokuLayoutRegressionTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun openPage(homeTile: String) {
        rule.waitUntil(20_000) {
            rule.onAllNodesWithTag("home-$homeTile").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("home-$homeTile").performScrollTo().performClick()
    }

    @Test
    fun all24AchievementDescriptionsRemainAboveTheirProgressBars() {
        openPage("postignuća")
        rule.onNodeWithTag("achievements-show-all").performScrollTo().performClick()
        val slugs = listOf(
            "prva-pobjeda", "sedam-dana-zaredom", "majstor-logike",
            "bez-greške", "vrući-niz", "brzinac", "rješavač-izazova",
            "prvi-korak", "redoviti-igrač", "iskusni-igrač", "stotka",
            "besprijekoran-niz", "samostalni-mislilac", "prvi-izazov",
            "dnevna-rutina", "deset-sati-logike", "strpljivi-mislilac",
            "ekspert", "velemajstor", "mala-mreža", "srednja-mreža",
            "klasična-mreža", "zlatni-niz", "sudoku-legenda",
        )
        check(slugs.size == 24)
        val tolerance = rule.activity.resources.displayMetrics.density
        for (slug in slugs) {
            val card = rule.onNodeWithTag("achievement-$slug")
            card.performScrollTo().assertIsDisplayed()
            val bounds = card.fetchSemanticsNode().boundsInRoot
            val description = rule.onNodeWithTag(
                "achievement-description-$slug", useUnmergedTree = true
            ).fetchSemanticsNode().boundsInRoot
            val progress = rule.onNodeWithTag(
                "achievement-progress-$slug", useUnmergedTree = true
            ).fetchSemanticsNode().boundsInRoot
            check(description.width > 0f && description.height > 0f &&
                progress.width > 0f && progress.height > 0f &&
                description.top >= bounds.top - tolerance &&
                description.bottom <= progress.top + tolerance &&
                progress.bottom <= bounds.bottom + tolerance) {
                "Achievement $slug overlaps/crops its description or progress: " +
                    "card=$bounds, description=$description, progress=$progress"
            }
        }
    }

    @Test
    fun commonHeadingsAndActionsDoNotCropAtAccessibilityFontScale() {
        rule.waitUntil(20_000) {
            rule.onAllNodesWithTag("home-nova-igra").fetchSemanticsNodes().isNotEmpty()
        }
        val density = rule.activity.resources.displayMetrics.density
        rule.runOnUiThread {
            rule.activity.setContent {
                CompositionLocalProvider(LocalDensity provides Density(density, 1.8f)) {
                    SudokuTheme {
                        Column(Modifier.fillMaxSize().padding(20.dp)) {
                            SectionTitle(
                                title = "Dnevni izazov", description = "Današnji izazov",
                                onBack = {}, centered = true,
                            )
                            SolidAction(
                                title = "Pokreni dnevni izazov i osvoji nagradu",
                                onClick = {},
                                modifier = Modifier.fillMaxWidth(),
                                tag = "accessible-primary-action",
                            )
                        }
                    }
                }
            }
        }
        rule.waitForIdle()
        val header = rule.onNodeWithTag("screen-dnevni-izazov")
            .fetchSemanticsNode().boundsInRoot
        val back = rule.onNodeWithTag("navigate-back")
            .fetchSemanticsNode().boundsInRoot
        val button = rule.onNodeWithTag("accessible-primary-action")
            .fetchSemanticsNode().boundsInRoot
        val label = rule.onNodeWithText(
            "Pokreni dnevni izazov i osvoji nagradu", useUnmergedTree = true
        ).fetchSemanticsNode().boundsInRoot
        val maxWidth = rule.activity.resources.displayMetrics.widthPixels
        val slack = density * 2
        check(header.width > 0f && header.left >= -slack &&
            header.right <= maxWidth + slack &&
            back.right <= header.left + slack) {
            "Large-text centered title overlaps back or clips: $header, back=$back"
        }
        check(button.width > 0f && button.height >= density * 48f &&
            label.width > 0f && label.height > 0f &&
            label.left >= button.left - slack &&
            label.right <= button.right + slack &&
            label.top >= button.top - slack &&
            label.bottom <= button.bottom + slack) {
            "Large-font CTA label is clipped: button=$button, label=$label"
        }
    }

    @Test
    fun statisticsLabelsFitChartAndAchievementPreviewStaysAboveFooter() {
        openPage("statistika")
        val chart = rule.onNodeWithTag("stats-difficulty-chart")
        chart.performScrollTo().assertIsDisplayed()
        val chartBounds = chart.fetchSemanticsNode().boundsInRoot
        val tolerance = rule.activity.resources.displayMetrics.density
        val labels = (0..3).map { index ->
            rule.onNodeWithTag(
                "stats-chart-label-$index", useUnmergedTree = true
            ).fetchSemanticsNode().boundsInRoot
        }
        check(labels.all {
            it.width > 0f && it.height > 0f &&
                it.left >= chartBounds.left - tolerance &&
                it.right <= chartBounds.right + tolerance &&
                it.top >= chartBounds.top - tolerance &&
                it.bottom <= chartBounds.bottom + tolerance
        }) {
            "Statistics x-axis labels are clipped outside the chart: $labels / $chartBounds"
        }
        labels.zipWithNext().forEach { (left, right) ->
            check(left.right <= right.left + tolerance) {
                "Statistics chart labels overlap horizontally: $left / $right"
            }
        }
        val achievements = rule.onNodeWithTag("stats-achievements")
        achievements.performScrollTo().assertIsDisplayed()
        val panel = achievements.fetchSemanticsNode().boundsInRoot
        val footer = rule.onNodeWithTag("brand-footer")
            .fetchSemanticsNode().boundsInRoot
        check(panel.width > 0f && panel.height > 0f &&
            panel.bottom <= footer.top + tolerance) {
            "Statistics achievements panel intersects the fixed footer: $panel / $footer"
        }
    }
}
