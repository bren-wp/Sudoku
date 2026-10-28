package com.brendigo.sudoku.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedNotesRecoveryTest {
    @Test fun absentNotesKeepTheFullBoardSize() {
        for (size in listOf(4, 6, 9)) {
            val recovered = SavedNotesRecovery.recover(size, null)
            assertEquals(size * size, recovered.size)
            assertEquals(0, recovered.count { it != 0 })
        }
    }

    @Test fun aShortOrLegacyNotesArrayDoesNotDiscardThePuzzle() {
        val recovered = SavedNotesRecovery.recover(9, intArrayOf(1 shl 3, 1 shl 7))
        assertEquals(81, recovered.size)
        assertEquals(1 shl 3, recovered[0])
        assertEquals(1 shl 7, recovered[1])
        assertEquals(0, recovered[80])
    }

    @Test fun damagedBitsNeverAppearAsUnsupportedCandidates() {
        val size = 6
        val unexpected = intArrayOf(-1, 1, 1 shl 9, (1 shl 1) or (1 shl 6))
        val recovered = SavedNotesRecovery.recover(size, unexpected)
        assertEquals(((1 shl 7) - 2), recovered[0])
        assertEquals(0, recovered[1]) // bit zero is never a candidate
        assertEquals(0, recovered[2]) // 9 is outside the 6x6 board
        assertEquals((1 shl 1) or (1 shl 6), recovered[3])
    }

    @Test fun extraNotesAreDiscardedWithoutChangingValidMarks() {
        val source = IntArray(100) { 1 shl 2 }
        val recovered = SavedNotesRecovery.recover(4, source)
        assertEquals(16, recovered.size)
        assertArrayEquals(IntArray(16) { 1 shl 2 }, recovered)
    }

    @Test(expected = IllegalArgumentException::class)
    fun unsupportedBoardSizeIsNotSilentlyAccepted() {
        SavedNotesRecovery.recover(5, intArrayOf(1))
    }
}
