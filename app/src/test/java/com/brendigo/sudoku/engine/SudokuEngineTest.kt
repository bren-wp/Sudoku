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


    @Test fun seededStressCasesStayValidUniqueAndDoNotMutateInputs() {
        // Test many deterministic seeds without introducing a timing-sensitive gate.
        val seeds = (-50..49).toList() + listOf(2026, Int.MIN_VALUE, Int.MAX_VALUE)
        var puzzles = 0
        val creationNanos = ArrayList<Long>(3 * 6 * seeds.size)
        val startNanos = System.nanoTime()
        for (size in listOf(4, 6, 9)) for (difficulty in 0..5) for (seed in seeds) {
            val generationStarted = System.nanoTime()
            val puzzle = SudokuEngine.create(size, difficulty, seed)
            creationNanos.add(System.nanoTime() - generationStarted)
            assertTrue(
                "Invalid solution for " + size + "/" + difficulty + "/" + seed,
                SudokuEngine.isValidCompleted(
                    puzzle.solution, size, puzzle.boxRows, puzzle.boxColumns
                )
            )
            val givensBefore = puzzle.givens.clone()
            assertEquals(
                "Non-unique puzzle for " + size + "/" + difficulty + "/" + seed, 1,
                SudokuEngine.countSolutions(
                    puzzle.givens, size, puzzle.boxRows, puzzle.boxColumns
                )
            )
            assertArrayEquals("Solver changed caller data", givensBefore, puzzle.givens)
            assertArrayEquals(
                "Seed changed output across identical invocations",
                puzzle.givens,
                SudokuEngine.create(size, difficulty, seed).givens
            )
            puzzles++
        }
        val sorted = creationNanos.sorted()
        fun percentileMs(pct: Int): Long = sorted[(sorted.lastIndex * pct) / 100] / 1_000_000
        println(
            "Sudoku engine regression: " + puzzles + " seeded puzzles in " +
                ((System.nanoTime() - startNanos) / 1_000_000) + " ms" +
                "; create p50=" + percentileMs(50) + "ms" +
                ", p95=" + percentileMs(95) + "ms" +
                ", p99=" + percentileMs(99) + "ms"
        )
    }

    @Test fun emptyBoardHasMultipleSolutionsAndObeysSearchLimit() {
        val blank = IntArray(16)
        assertEquals(1, SudokuEngine.countSolutions(blank, 4, 2, 2, limit = 1))
        assertEquals(2, SudokuEngine.countSolutions(blank, 4, 2, 2, limit = 2))
        assertEquals(3, SudokuEngine.countSolutions(blank, 4, 2, 2, limit = 3))
        assertEquals(0, SudokuEngine.countSolutions(blank, 4, 2, 2, limit = 0))
        assertArrayEquals(IntArray(16), blank)
    }

    @Test fun rejectCorruptDigitsAndDuplicateHousesForEverySize() {
        for (size in listOf(4, 6, 9)) {
            val br = if (size == 9) 3 else 2
            val bc = size / br
            val solved = SudokuEngine.create(size, 1, seed = size * 93).solution
            val wrong = solved.clone()
            wrong[0] = size + 1
            assertFalse(SudokuEngine.isValidCompleted(wrong, size, br, bc))
            assertEquals(0, SudokuEngine.countSolutions(wrong, size, br, bc))
            wrong[0] = -1
            assertFalse(SudokuEngine.isValidCompleted(wrong, size, br, bc))
            assertEquals(0, SudokuEngine.countSolutions(wrong, size, br, bc))
            wrong[0] = solved[1]
            assertFalse(SudokuEngine.isValidCompleted(wrong, size, br, bc))
            assertEquals(0, SudokuEngine.countSolutions(wrong, size, br, bc))
            wrong[0] = 0
            assertFalse(SudokuEngine.isValidCompleted(wrong, size, br, bc))
            assertEquals(1, SudokuEngine.countSolutions(wrong, size, br, bc))
            assertEquals(0, SudokuEngine.countSolutions(IntArray(size * size - 1), size, br, bc))
        }
    }

    @Test fun solvedBoardsAreImmutableForSolutionCounter() {
        val puzzle = SudokuEngine.create(9, 5, seed = 73422)
        val solved = puzzle.solution.clone()
        assertEquals(1, SudokuEngine.countSolutions(solved, 9, 3, 3))
        assertArrayEquals(puzzle.solution, solved)
    }


    @Test fun historicalSeedFixturesStayByteForByteCompatible() {
        // Captured from the v1.0.77 generator/solver before the bit-mask
        // optimization. Keep them stable for existing seeded daily games.
        val expected4 = intArrayOf(
            0, 1, 0, 3,
            0, 2, 1, 4,
            2, 0, 0, 1,
            0, 3, 4, 2,
        )
        val expected6 = intArrayOf(
            0, 0, 0, 5, 2, 6,
            5, 6, 0, 0, 0, 0,
            0, 1, 0, 4, 3, 5,
            0, 0, 3, 0, 0, 0,
            0, 0, 0, 0, 0, 2,
            3, 2, 5, 0, 0, 4,
        )
        val expected9 = intArrayOf(
            5, 1, 2, 0, 8, 0, 0, 0, 0,
            0, 0, 0, 1, 5, 0, 3, 0, 6,
            0, 0, 0, 0, 0, 9, 0, 0, 0,
            9, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 4, 7, 0, 0, 1, 0, 9, 3,
            0, 0, 1, 8, 0, 0, 0, 2, 0,
            1, 2, 4, 0, 0, 0, 0, 0, 0,
            0, 0, 5, 0, 7, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 6, 0, 5,
        )
        assertArrayEquals(expected4, SudokuEngine.create(4, 0, 4000).givens)
        assertArrayEquals(expected6, SudokuEngine.create(6, 3, 6030).givens)
        assertArrayEquals(expected9, SudokuEngine.create(9, 5, 9005).givens)
    }

}
