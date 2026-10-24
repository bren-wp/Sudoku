package com.brendigo.sudoku.engine

import org.junit.Assert.*
import org.junit.Test

class SudokuCandidatesTest {
    @Test fun candidatesRespectRowsColumnsAndBoxes() {
        val board = IntArray(81)
        board[0] = 1
        board[1] = 2
        board[9] = 3
        board[10] = 4
        val candidates = SudokuCandidates.possibleMask(board, 9, 3, 3, 2)
        for (digit in 1..4) assertEquals(0, candidates and (1 shl digit))
        assertTrue(candidates and (1 shl 5) != 0)
        assertEquals(0, SudokuCandidates.possibleMask(board, 9, 3, 3, 0))
    }

    @Test fun worksForNonSquareSixBySixBoxes() {
        val board = IntArray(36)
        board[0] = 1
        board[4] = 2
        board[7] = 3
        board[8] = 4
        val mask = SudokuCandidates.possibleMask(board, 6, 2, 3, 2)
        assertEquals(0, mask and (1 shl 1))
        assertEquals(0, mask and (1 shl 3))
        assertEquals(0, mask and (1 shl 4))
        assertTrue(mask and (1 shl 5) != 0)
    }

    @Test fun fillsEmptyCellsOnly() {
        for (size in listOf(4, 6, 9)) {
            val boxRows = if (size == 9) 3 else 2
            val boxColumns = size / boxRows
            val puzzle = SudokuEngine.create(size, 2, 945 * size)
            val notes = SudokuCandidates.automaticNotes(
                puzzle.givens, size, boxRows, boxColumns
            )
            assertEquals(size * size, notes.size)
            for (i in notes.indices) {
                if (puzzle.givens[i] != 0) assertEquals(0, notes[i])
                else assertTrue(notes[i] and (1 shl puzzle.solution[i]) != 0)
            }
        }
    }

    @Test fun sameHouseDetectsPeers() {
        assertTrue(SudokuCandidates.sameHouse(0, 1, 9, 3, 3))
        assertTrue(SudokuCandidates.sameHouse(0, 9, 9, 3, 3))
        assertTrue(SudokuCandidates.sameHouse(0, 10, 9, 3, 3))
        assertFalse(SudokuCandidates.sameHouse(0, 40, 9, 3, 3))
    }
}
