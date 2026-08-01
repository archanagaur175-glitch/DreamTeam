package com.dreamteam.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dreamteam.core.database.entity.SleepBaselineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepBaselineDao {

    @Query("SELECT * FROM sleep_baseline WHERE userId = :userId")
    fun observeBaseline(userId: String): Flow<SleepBaselineEntity?>

    @Query("SELECT * FROM sleep_baseline WHERE userId = :userId")
    suspend fun getBaseline(userId: String): SleepBaselineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(baseline: SleepBaselineEntity)
}
