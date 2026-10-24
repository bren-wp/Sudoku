package com.brendigo.sudoku.engine

import kotlin.math.roundToInt
import kotlin.random.Random

/** A deterministic, dependency-free Sudoku generator with uniqueness verification. */
object SudokuEngine {
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

    fun isValidCompleted(board: IntArray, size: Int, br: Int, bc: Int): Boolean {
        if (board.size != size * size || br * bc != size) return false
        val expected = (1..size).toSet()
        for (r in 0 until size) {
            if ((0 until size).map { board[r * size + it] }.toSet() != expected) return false
        }
        for (c in 0 until size) {
            if ((0 until size).map { board[it * size + c] }.toSet() != expected) return false
        }
        for (r in 0 until size step br) for (c in 0 until size step bc) {
            val values = (0 until br).flatMap { y ->
                (0 until bc).map { x -> board[(r + y) * size + c + x] }
            }
            if (values.toSet() != expected) return false
        }
        return true
    }

    /** Counts at most limit solutions, using a minimum-remaining-values search. */
    fun countSolutions(input: IntArray, size: Int, br: Int, bc: Int, limit: Int = 2): Int {
        if (input.size != size * size || br * bc != size || limit < 1) return 0
        val board = input.clone()
        var solutions = 0
        fun candidates(index: Int): Int {
            val row = index / size
            val col = index % size
            var mask = ((1 shl (size + 1)) - 2)
            for (i in 0 until size) {
                val a = board[row * size + i]
                val b = board[i * size + col]
                if (a > 0) mask = mask and (1 shl a).inv()
                if (b > 0) mask = mask and (1 shl b).inv()
            }
            val r0 = (row / br) * br
            val c0 = (col / bc) * bc
            for (r in r0 until r0 + br) for (c in c0 until c0 + bc) {
                val v = board[r * size + c]
                if (v > 0) mask = mask and (1 shl v).inv()
            }
            return mask
        }
        // Reject contradictory givens.
        for (i in board.indices) if (board[i] != 0) {
            val v = board[i]
            if (v !in 1..size) return 0
            board[i] = 0
            val valid = candidates(i) and (1 shl v) != 0
            board[i] = v
            if (!valid) return 0
        }
        fun search() {
            if (solutions >= limit) return
            var bestIndex = -1
            var bestMask = 0
            var bestCount = size + 1
            for (i in board.indices) if (board[i] == 0) {
                val mask = candidates(i)
                val count = Integer.bitCount(mask)
                if (count == 0) return
                if (count < bestCount) {
                    bestCount = count
                    bestIndex = i
                    bestMask = mask
                    if (count == 1) break
                }
            }
            if (bestIndex < 0) {
                solutions++
                return
            }
            for (value in 1..size) if (bestMask and (1 shl value) != 0) {
                board[bestIndex] = value
                search()
                board[bestIndex] = 0
                if (solutions >= limit) return
            }
        }
        search()
        return solutions
    }
}
