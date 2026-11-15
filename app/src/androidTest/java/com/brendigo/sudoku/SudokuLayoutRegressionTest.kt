package com.brendigo.sudoku

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
