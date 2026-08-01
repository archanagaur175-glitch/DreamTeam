package com.dreamteam.feature.logger.domain

import com.dreamteam.core.common.FactorTag
import com.dreamteam.core.common.SleepSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Data boundary for the logger feature. Implemented in the app module. */
interface FactorLogRepository {

    fun observeLogs(): Flow<List<DailyLog>>

    fun observeSessions(): Flow<List<SleepSession>>

    suspend fun saveTags(date: LocalDate, tags: Set<FactorTag>, note: String?)

    suspend fun addSession(session: SleepSession)

    suspend fun deleteSession(id: Long)
}
