package com.dreamteam.feature.sleepdebt.domain

import com.dreamteam.core.common.SleepSession
import kotlinx.coroutines.flow.Flow

/** Data boundary for the sleep debt feature. Implemented in the app module. */
interface DebtRepository {

    /** Rolling debt, recomputed live whenever sessions or baseline change. */
    fun observeDebt(): Flow<RollingDebt>

    fun observeBaselineHours(): Flow<Double>

    suspend fun setBaselineHours(hours: Double)

    fun observeSessions(): Flow<List<SleepSession>>

    suspend fun addSession(session: SleepSession)

    suspend fun deleteSession(id: Long)
}
