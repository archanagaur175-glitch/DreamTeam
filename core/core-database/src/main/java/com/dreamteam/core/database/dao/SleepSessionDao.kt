package com.dreamteam.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dreamteam.core.database.entity.SleepSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepSessionDao {

    @Query("SELECT * FROM sleep_sessions ORDER BY sleepEndEpochMillis DESC")
    fun observeSessions(): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE sleepEndEpochMillis >= :fromEpochMillis ORDER BY sleepEndEpochMillis DESC")
    fun observeSessionsSince(fromEpochMillis: Long): Flow<List<SleepSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SleepSessionEntity): Long

    @Query("DELETE FROM sleep_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
