package com.brendigo.sudoku.engine

import org.junit.Assert.*
import org.junit.Test

class SudokuEngineTest {
    @Test fun eachSizeProducesUniqueSolvableBoard() {
        for (size in listOf(4, 6, 9)) for (difficulty in 0..5) {
            val puzzle = SudokuEngine.create(size, difficulty, size * 100 + difficulty)
            assertEquals(size * size, puzzle.givens.size)
            assertTrue(SudokuEngine.isValidCompleted(
                puzzle.solution, size, puzzle.boxRows, puzzle.boxColumns
            ))
            assertEquals(1, SudokuEngine.countSolutions(
                puzzle.givens, size, puzzle.boxRows, puzzle.boxColumns
            ))
            for (i in puzzle.givens.indices) if (puzzle.givens[i] != 0) {
                assertEquals(puzzle.solution[i], puzzle.givens[i])
            }
        }
    }

    @Test fun seedIsStable() {
        val a = SudokuEngine.create(9, 2, 123456)
        val b = SudokuEngine.create(9, 2, 123456)
        assertArrayEquals(a.givens, b.givens)
        assertArrayEquals(a.solution, b.solution)
    }

    @Test fun rejectsUnsupportedDimensionsWithoutCrashing() {
        assertFalse(SudokuEngine.isValidCompleted(IntArray(0), 0, 0, 0))
        assertEquals(0, SudokuEngine.countSolutions(IntArray(0), 0, 0, 0))
        assertFalse(SudokuEngine.isValidCompleted(IntArray(16), 4, -2, -2))
        assertEquals(0, SudokuEngine.countSolutions(IntArray(16), 4, -2, -2))
        assertEquals(0, SudokuEngine.countSolutions(IntArray(81), 9, 0, 0))
    }

    @Test fun rejectsContradictoryGivens() {
        val board = IntArray(81)
        board[0] = 5
        board[1] = 5
        assertEquals(0, SudokuEngine.countSolutions(board, 9, 3, 3))
    }
}
