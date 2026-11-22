package com.brendigo.sudoku.engine

import kotlin.math.roundToInt
import kotlin.random.Random

/** A deterministic, dependency-free Sudoku generator with uniqueness verification. */
object SudokuEngine {
    /**
     * The shipped game uses exactly 2x2, 2x3 and 3x3 subgrids.
     * A product-only check would accept 1x4 or 3x2 boxes and validate
     * a different puzzle variant as though it were a saved Sudoku game.
     */
    private fun hasCanonicalBoxes(size: Int, br: Int, bc: Int): Boolean = when (size) {
        4 -> br == 2 && bc == 2
        6 -> br == 2 && bc == 3
        9 -> br == 3 && bc == 3
        else -> false
    }

    data class Puzzle(
        val size: Int,
        val boxRows: Int,
        val boxColumns: Int,
        val solution: IntArray,
        val givens: IntArray,
    )

    fun create(size: Int, difficulty: Int, seed: Int = Random.nextInt()): Puzzle {
        require(size in setOf(4, 6, 9)) { "Supported sizes: 4, 6, 9" }
        require(difficulty in 0..5) { "Difficulty must be 0..5" }
        val br = if (size == 9) 3 else 2
        val bc = size / br
        val random = Random(seed)
        val digits = (1..size).shuffled(random)
        val rowGroups = (0 until size / br).shuffled(random)
        val columnGroups = (0 until size / bc).shuffled(random)
        val rows = rowGroups.flatMap { group ->
            (group * br until (group + 1) * br).shuffled(random)
        }
        val columns = columnGroups.flatMap { group ->
            (group * bc until (group + 1) * bc).shuffled(random)
        }
        val solution = IntArray(size * size) { index ->
            val r = rows[index / size]
            val c = columns[index % size]
            digits[(r * bc + r / br + c) % size]
        }
        val board = solution.clone()
        // These are clue-density tiers, not a claim of solver-rated difficulty.
        val removalFractions = doubleArrayOf(.35, .43, .50, .57, .64, .70)
        val target = (size * size * removalFractions[difficulty]).roundToInt()
        var removed = 0
        for (cell in board.indices.shuffled(random)) {
            if (removed >= target) break
            val old = board[cell]
            board[cell] = 0
            if (countSolutions(board, size, br, bc, 2) == 1) {
                removed++
            } else {
                board[cell] = old
            }
        }
        return Puzzle(size, br, bc, solution, board)
    }

    /**
     * Allocation-light, linear validation: every row, column and canonical box
     * must contain each 1..size digit exactly once. Duplicate detection in a
     * full house also proves no digits are missing.
     */
    fun isValidCompleted(board: IntArray, size: Int, br: Int, bc: Int): Boolean {
        if (!hasCanonicalBoxes(size, br, bc) || board.size != size * size) return false
        val rowUsed = IntArray(size)
        val columnUsed = IntArray(size)
        val boxUsed = IntArray(size)
        val boxesPerRow = size / bc
        for (index in board.indices) {
            val value = board[index]
            if (value !in 1..size) return false
            val bit = 1 shl value
            val row = index / size
            val column = index % size
            val box = (row / br) * boxesPerRow + column / bc
            if ((rowUsed[row] or columnUsed[column] or boxUsed[box]) and bit != 0) {
                return false
            }
            rowUsed[row] = rowUsed[row] or bit
            columnUsed[column] = columnUsed[column] or bit
            boxUsed[box] = boxUsed[box] or bit
        }
        return true
    }

    /**
     * Counts at most [limit] solutions using minimum-remaining-values
     * backtracking and incremental row/column/box bit masks. Unlike rescanning
     * every house for every candidate in the search tree, an availability mask
     * now requires only three integer reads.
     *
     * Both successful and invalid searches leave [input] untouched. The
     * generator's seeded order and the puzzle/save format are unchanged.
     */
    fun countSolutions(input: IntArray, size: Int, br: Int, bc: Int, limit: Int = 2): Int {
        if (!hasCanonicalBoxes(size, br, bc) || input.size != size * size || limit < 1) {
            return 0
        }
        val board = input.clone()
        val fullMask = (1 shl (size + 1)) - 2
        val rowUsed = IntArray(size)
        val columnUsed = IntArray(size)
        val boxUsed = IntArray(size)
        val boxesPerRow = size / bc
        val cellBox = IntArray(board.size) { index ->
            (index / size / br) * boxesPerRow + (index % size) / bc
        }

        // Reject contradictory or corrupt givens before entering the search.
        for (index in board.indices) {
            val value = board[index]
            if (value == 0) continue
            if (value !in 1..size) return 0
            val bit = 1 shl value
            val row = index / size
            val column = index % size
            val box = cellBox[index]
            if ((rowUsed[row] or columnUsed[column] or boxUsed[box]) and bit != 0) {
                return 0
            }
            rowUsed[row] = rowUsed[row] or bit
            columnUsed[column] = columnUsed[column] or bit
            boxUsed[box] = boxUsed[box] or bit
        }

        var solutions = 0
        fun search() {
            if (solutions >= limit) return
            var bestIndex = -1
            var bestMask = 0
            var bestCount = size + 1
            for (index in board.indices) {
                if (board[index] != 0) continue
                val row = index / size
                val column = index % size
                val box = cellBox[index]
                val available = fullMask and
                    (rowUsed[row] or columnUsed[column] or boxUsed[box]).inv()
                val candidateCount = Integer.bitCount(available)
                if (candidateCount == 0) return
                if (candidateCount < bestCount) {
                    bestIndex = index
                    bestCount = candidateCount
                    bestMask = available
                    if (candidateCount == 1) break
                }
            }
            if (bestIndex == -1) {
                solutions++
                return
            }

            val row = bestIndex / size
            val column = bestIndex % size
            val box = cellBox[bestIndex]
            var choices = bestMask
            while (choices != 0 && solutions < limit) {
                val bit = choices and -choices
                val digit = Integer.numberOfTrailingZeros(bit)
                board[bestIndex] = digit
                rowUsed[row] = rowUsed[row] or bit
                columnUsed[column] = columnUsed[column] or bit
                boxUsed[box] = boxUsed[box] or bit
                search()
                board[bestIndex] = 0
                rowUsed[row] = rowUsed[row] and bit.inv()
                columnUsed[column] = columnUsed[column] and bit.inv()
                boxUsed[box] = boxUsed[box] and bit.inv()
                choices = choices and (choices - 1)
            }
        }
        search()
        return solutions
    }
}
