package com.brendigo.sudoku

import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The lower scenery is extracted from the owner's reference illustration:
 * it must decode reliably and never replace the interactive Sudoku UI.
 */
@RunWith(AndroidJUnit4::class)
class SudokuSceneryAssetTest {
    @Test
    fun originalArtworkLoadsAndFadesIntoTheNativeBackdrop() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val bitmap = context.assets.open("scenery-reference.webp").use { stream ->
            BitmapFactory.decodeStream(stream)
        }
        requireNotNull(bitmap) { "Original nightscape WebP is missing or cannot decode" }
        try {
            assertEquals(236, bitmap.width)
            assertEquals(162, bitmap.height)
            assertTrue("Scenery should retain alpha for blending", bitmap.hasAlpha())
            assertTrue("Top row must fade into the real Compose background",
                Color.alpha(bitmap.getPixel(bitmap.width / 2, 0)) < 128)
            assertTrue("Nightscape should become fully visible near the bottom",
                Color.alpha(bitmap.getPixel(bitmap.width / 2, bitmap.height - 1)) > 245)
        } finally {
            bitmap.recycle()
        }
    }
}
