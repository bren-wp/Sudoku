package com.brendigo.sudoku.ui

import java.time.Duration
import java.time.ZonedDateTime

/**
 * Refresh at the next local midnight, with a one-minute maximum polling interval
 * so a changed device date or time zone cannot leave daily UI permanently stale.
 * Android may pause UI coroutines in the background; date is re-read on resume.
 */
internal object DailyDateTicker {
    fun delayUntilDayRefresh(now: ZonedDateTime): Long {
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
        val millisToMidnight = Duration.between(now, nextMidnight).toMillis()
        return millisToMidnight.coerceAtLeast(1_000L).coerceAtMost(60_000L)
    }
}
