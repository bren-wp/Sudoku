package com.brendigo.sudoku.data

import java.time.LocalDate

/**
 * Picks the safest available existing save without fabricating puzzle progress.
 *
 * Keep legacy keys for upgrade compatibility. Undo moves must be restored from
 * the same snapshot as the board; otherwise Undo can alter an unrelated game.
 */
internal object SavedGameRecovery {
    data class Restored(val session: Session?, val undoKey: String)

    fun choose(
        primary: Session?,
        regular: Session?,
        daily: Session?,
        today: LocalDate,
    ): Restored {
        // Keep the currently active puzzle and its own Undo stack when valid.
        // A completed/failed primary is not a reason to hide a different
        // unfinished regular or today's daily puzzle.
        if (primary != null && !primary.completed && !primary.failed &&
            (!primary.daily || primary.day == today.toString())
        ) return Restored(primary, "undo")
        if (regular != null && !regular.completed && !regular.failed && !regular.daily) {
            return Restored(regular, "regular_undo")
        }
        if (daily != null && daily.daily && daily.day == today.toString() &&
            !daily.completed && !daily.failed
        ) {
            return Restored(daily, "daily_undo")
        }
        // Preserve the last completed game for existing result screens and
        // upgrade compatibility when there is no resumable named snapshot.
        return Restored(primary, "undo")
    }
}
