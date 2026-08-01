package com.dreamteam.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

fun LocalTime.toMinutesSinceMidnight(): Int = hour * 60 + minute

fun Int.toLocalTime(): LocalTime = LocalTime.of(this / 60, this % 60)

fun LocalDateTime.toEpochMillis(): Long = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

fun LocalDate.toEpochDayLong(): Long = toEpochDay()

fun Long.toLocalDate(): LocalDate = LocalDate.ofEpochDay(this)

fun LocalDateTime.minutesSinceMidnight(): Int = toLocalTime().toMinutesSinceMidnight()
