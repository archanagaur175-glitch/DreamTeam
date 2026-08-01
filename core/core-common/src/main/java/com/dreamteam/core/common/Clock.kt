package com.dreamteam.core.common

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalTime

/**
 * Emits the current time immediately, then every [intervalMillis].
 * Drives the "now" marker on the energy curve and the live alarm-window state.
 */
fun nowFlow(intervalMillis: Long = 60_000L): Flow<LocalTime> = flow {
    while (true) {
        emit(LocalTime.now())
        delay(intervalMillis)
    }
}

/**
 * Emits today's date immediately, then every [intervalMillis].
 * Drives day-rollover recomputation of the rolling debt window.
 */
fun todayFlow(intervalMillis: Long = 60_000L): Flow<LocalDate> = flow {
    while (true) {
        emit(LocalDate.now())
        delay(intervalMillis)
    }
}
