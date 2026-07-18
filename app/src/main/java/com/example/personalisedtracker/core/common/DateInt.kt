package com.example.personalisedtracker.core.common

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * `yyyyMMdd` int format used everywhere — keeps Room sort-friendly and
 * timezone-stable for personal use.
 */
object DateInt {

    private val FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun today(zone: ZoneId = ZoneId.systemDefault()): Int = fromLocalDate(LocalDate.now(zone))

    fun fromLocalDate(date: LocalDate): Int =
        date.year * 10_000 + date.monthValue * 100 + date.dayOfMonth

    fun toLocalDate(d: Int): LocalDate {
        val year = d / 10_000
        val month = (d / 100) % 100
        val day = d % 100
        return LocalDate.of(year, month, day)
    }

    fun minusDays(today: Int, days: Long): Int =
        fromLocalDate(toLocalDate(today).minusDays(days))

    fun format(d: Int, pattern: String = "EEE, dd MMM yyyy"): String =
        toLocalDate(d).format(DateTimeFormatter.ofPattern(pattern))

    fun parseInbody(raw: String): Pair<Int, Long>? {
        // "20260507091906" → yyyyMMdd + epoch-ms
        if (raw.length < 8) return null
        val datePart = raw.take(8).toIntOrNull() ?: return null
        // Best-effort timestamp parse for the time portion
        return runCatching {
            val ldt = java.time.LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            datePart to ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.getOrElse {
            // Fallback: midnight of that date
            val ld = toLocalDate(datePart)
            datePart to ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    }
}

