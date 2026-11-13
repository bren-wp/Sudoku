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
    @Test fun rejectsAlternativeSubgridsThatAreNotSupportedByTheApp() {
        // A 1x4 box turns the standard 4x4 game into a different variant.
        // Its dimensions still multiply to 4, which the old guard accepted.
        val four = SudokuEngine.create(4, 1, 4001)
        assertTrue(SudokuEngine.isValidCompleted(
            four.solution, 4, four.boxRows, four.boxColumns
        ))
        assertFalse(SudokuEngine.isValidCompleted(four.solution, 4, 1, 4))
        assertEquals(0, SudokuEngine.countSolutions(four.givens, 4, 1, 4))

        val six = SudokuEngine.create(6, 2, 6002)
        assertTrue(SudokuEngine.isValidCompleted(
            six.solution, 6, six.boxRows, six.boxColumns
        ))
        assertFalse(SudokuEngine.isValidCompleted(six.solution, 6, 3, 2))
        assertEquals(0, SudokuEngine.countSolutions(six.givens, 6, 3, 2))

        val nine = SudokuEngine.create(9, 2, 9002)
        assertFalse(SudokuEngine.isValidCompleted(nine.solution, 9, 1, 9))
        assertEquals(0, SudokuEngine.countSolutions(nine.givens, 9, 1, 9))
    }

}
