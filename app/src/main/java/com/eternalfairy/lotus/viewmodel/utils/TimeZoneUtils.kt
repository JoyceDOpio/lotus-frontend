package com.eternalfairy.lotus.viewmodel.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object TimeZoneUtils {
    // Output format: 2026-04-21 08:15:34.082732+00
    fun createTimestampTz (): String {
        return DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())
    }

    // Output format: 2026-04-22
    fun createDateString (date: LocalDate): String {
        return date.toString()
    }
}