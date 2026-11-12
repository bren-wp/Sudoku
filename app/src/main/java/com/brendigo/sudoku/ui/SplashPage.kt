package com.brendigo.sudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SplashPage() {
    // Preserve the 941x1672 reference composition at the default font scale.
    // Smaller or accessibility-scaled displays use the same *real* Compose
    // content, with guarded typography instead of a cropped screenshot.
    BoxWithConstraints(Modifier.fillMaxSize().testTag("splash-screen")) {
        val landscape = maxWidth > maxHeight
        val veryNarrow = maxWidth < 310.dp
        val compact = maxHeight < 700.dp || maxWidth < 385.dp
        val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
        // On a 240x400dp phone with enlarged accessible text, reserve a
        // compact region for branding above the native loading indicator.
        // The reference-size, default-font composition is unchanged.
        val tightAccessibility = fontScale > 1.35f && maxHeight < 500.dp
        // Keep the rendered wordmark within a safe horizontal footprint when
        // the system's accessible font scale is unusually large.
        val typeFactor = minOf(1f, 1.35f / fontScale)
        val idealHeadingSp = ((when {
            landscape -> 48
            veryNarrow -> 42
            compact -> 53
            else -> 79
        }) * typeFactor).toInt().coerceAtLeast(28)
        // "Sudoku" must remain on one line even on 240dp devices with
        // 1.8x accessible text. Otherwise a wrapped heading pushes the
        // tagline into the native loader. Normal reference typography is
        // unchanged; only constrained width + high font scale is capped.
        val titleWidthDp = (maxWidth.value - 40f).coerceAtLeast(0f)
        val maxSingleLineSp = (titleWidthDp / (4.25f * fontScale))
            .toInt().coerceAtLeast(20)
        val headingSp = minOf(idealHeadingSp, maxSingleLineSp)
        val tagline = if (compact || fontScale > 1.35f)
            "TRENIRAJ MOZAK SVAKI DAN"
        else "T R E N I R A J   M O Z A K   S V A K I   D A N"

        Column(
            Modifier.align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = maxHeight * when {
                    tightAccessibility -> .035f
                    landscape -> .055f
                    compact -> .19f
                    // On the 941 x 1672 reference the 7-3-9 emblem starts
                    // 8px lower. Adjust the parent layout, not the drawn logo:
                    // offsets would make the semantics bounds diverge.
                    else -> .260f
                })
                .testTag("splash-brand"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // The owner's 941px artwork has a concentrated cyan halo
            // extending behind the lower-left edge of its rounded 7-3-9
            // emblem. Paint only decorative light: the logo is still an
            // actual scaled Compose 3x3 grid, never a pasted screenshot.
            Box(
                Modifier.testTag("splash-logo")
                    .drawBehind {
                        val center = Offset(size.width * .34f, size.height * .70f)
                        val glowRadius = size.width * .79f
                        drawCircle(
                            Brush.radialGradient(
                                listOf(
                                    Ink.cyan.copy(alpha = .54f),
                                    Ink.blue.copy(alpha = .19f),
                                    Color.Transparent,
                                ),
                                center = center, radius = glowRadius,
                            ),
                            radius = glowRadius,
                            center = center,
                        )
                        // The 941x1672 source has a second, tighter cyan
                        // bloom behind the selected centre tile. Preserve
                        // the live Compose logo and illuminate it separately.
                        val tileGlow = Offset(size.width * .52f, size.height * .54f)
                        val tileRadius = size.width * .45f
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Ink.cyan.copy(alpha = .19f),
                                    Ink.blue.copy(alpha = .09f),
                                    Color.Transparent,
                                ),
                                center = tileGlow, radius = tileRadius,
                            ),
                            center = tileGlow, radius = tileRadius,
                        )
                    },
            ) {
                SudokuLogo(when {
                    tightAccessibility -> 64.dp
                    landscape -> 70.dp
                    veryNarrow -> 80.dp
                    compact -> 92.dp
                    else -> 124.dp
                }, glowing = true)
            }
            Spacer(Modifier.height(if (tightAccessibility || landscape) 6.dp else if (compact) 13.dp else 13.dp))
            AccentText("Sudoku", headingSp, Modifier.testTag("splash-title"))
            Text(
                "by Brendigo",
                color = Ink.muted,
                letterSpacing = if (fontScale > 1.35f) 2.sp else 5.sp,
                fontSize = if (landscape || veryNarrow) 12.sp
                else if (compact) 13.sp else 16.sp,
                modifier = Modifier.testTag("splash-byline"),
                maxLines = 1,
            )
            Spacer(Modifier.height(if (tightAccessibility || landscape) 6.dp else if (compact) 12.dp else 11.dp))
            Text(
                tagline,
                color = Ink.cyan,
                fontSize = if (compact) 9.sp else 15.sp,
                letterSpacing = if (compact || fontScale > 1.35f) 1.sp
                else androidx.compose.ui.unit.TextUnit.Unspecified,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth().testTag("splash-tagline"),
            )
        }
        // A deterministic loading arc stays visible when Android animations
        // are disabled and remains vertically separated from brand content.
        Canvas(
            Modifier.align(Alignment.BottomCenter)
                // 941x1672 reference: the cyan arc sits slightly below the
                // previous native capture (without touching the bottom art).
                .padding(bottom = if (landscape) 10.dp else if (compact) 24.dp else 74.dp)
                .size(if (landscape) 24.dp else if (compact) 30.dp else 36.dp)
                .testTag("splash-loading")
                .semantics { contentDescription = "Učitavanje igre" }
        ) {
            drawArc(
                color = Ink.blue.copy(alpha = .40f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
            // Soft bloom underneath the crisp reference cyan stroke.
            drawArc(
                color = Ink.cyan.copy(alpha = .23f),
                startAngle = -93f,
                sweepAngle = 145f,
                useCenter = false,
                style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round),
            )
            drawArc(
                color = Ink.cyan,
                startAngle = -93f,
                sweepAngle = 145f,
                useCenter = false,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}
