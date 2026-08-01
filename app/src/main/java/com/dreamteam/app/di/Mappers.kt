package com.dreamteam.app.di

import com.dreamteam.core.common.FactorTag
import com.dreamteam.core.common.SleepSession
import com.dreamteam.core.common.SleepSource
import com.dreamteam.core.common.toEpochDayLong
import com.dreamteam.core.common.toEpochMillis
import com.dreamteam.core.common.toLocalDate
import com.dreamteam.core.common.toLocalDateTime
import com.dreamteam.core.common.toLocalTime
import com.dreamteam.core.common.toMinutesSinceMidnight
import com.dreamteam.core.database.entity.AlarmConfigEntity
import com.dreamteam.core.database.entity.DailyFactorLogEntity
import com.dreamteam.core.database.entity.SleepBaselineEntity
import com.dreamteam.core.database.entity.SleepSessionEntity
import com.dreamteam.feature.logger.domain.DailyLog
import com.dreamteam.feature.smartalarm.domain.AlarmConfig
import com.dreamteam.feature.smartalarm.domain.SensorMode

fun SleepSessionEntity.toDomain(): SleepSession = SleepSession(
    id = id,
    sleepStart = sleepStartEpochMillis.toLocalDateTime(),
    sleepEnd = sleepEndEpochMillis.toLocalDateTime(),
    source = runCatching { SleepSource.valueOf(source) }.getOrDefault(SleepSource.MANUAL),
    qualityScore = qualityScore,
    notes = notes,
)

fun SleepSession.toEntity(): SleepSessionEntity = SleepSessionEntity(
    id = id,
    sleepStartEpochMillis = sleepStart.toEpochMillis(),
    sleepEndEpochMillis = sleepEnd.toEpochMillis(),
    source = source.name,
    qualityScore = qualityScore,
    notes = notes,
)

fun DailyFactorLogEntity.toDomain(): DailyLog = DailyLog(
    date = dateEpochDay.toLocalDate(),
    tags = tags
        .split(',')
        .filter { it.isNotBlank() }
        .mapNotNull { runCatching { FactorTag.valueOf(it) }.getOrNull() }
        .toSet(),
    freeNote = freeNote,
)

fun DailyLog.toEntity(): DailyFactorLogEntity = DailyFactorLogEntity(
    dateEpochDay = date.toEpochDayLong(),
    tags = tags.joinToString(",") { it.name },
    freeNote = freeNote,
)

fun AlarmConfigEntity.toDomain(): AlarmConfig = AlarmConfig(
    targetWakeTime = targetWakeTimeMinutes.toLocalTime(),
    windowMinutes = windowMinutes,
    soundUri = soundUri,
    vibrationEnabled = vibrationEnabled,
    sensorMode = runCatching { SensorMode.valueOf(sensorMode) }.getOrDefault(SensorMode.ACCELEROMETER),
)

fun AlarmConfig.toEntity(): AlarmConfigEntity = AlarmConfigEntity(
    id = 1,
    targetWakeTimeMinutes = targetWakeTime.toMinutesSinceMidnight(),
    windowMinutes = windowMinutes,
    soundUri = soundUri,
    vibrationEnabled = vibrationEnabled,
    sensorMode = sensorMode.name,
)

const val DEFAULT_BASELINE_HOURS = 8.0
