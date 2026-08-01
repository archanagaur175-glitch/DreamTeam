package com.dreamteam.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dreamteam.core.database.entity.DailyFactorLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FactorLogDao {

    @Query("SELECT * FROM factor_logs ORDER BY dateEpochDay DESC")
    fun observeLogs(): Flow<List<DailyFactorLogEntity>>

    @Query("SELECT * FROM factor_logs WHERE dateEpochDay = :dateEpochDay")
    suspend fun logFor(dateEpochDay: Long): DailyFactorLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: DailyFactorLogEntity)
}
