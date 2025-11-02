package com.oliver.utils

import java.time.OffsetDateTime
import java.util.Date

import java.time.ZoneId

fun parsedDate(date: String): Date {
    return OffsetDateTime
        .parse(date)
        .toInstant()
        .let { instant -> Date.from(instant) }
}

fun localizationTime(date: Date): String {
    val localDateTime = date.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    val hour = localDateTime.hour
    val minute = localDateTime.minute
    val second = localDateTime.second

    return "%02d시 %02d분 %02d초".format(hour, minute, second)
}