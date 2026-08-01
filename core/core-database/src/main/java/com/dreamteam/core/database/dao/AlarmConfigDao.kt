package com.dreamteam.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dreamteam.core.database.entity.AlarmConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmConfigDao {

    @Query("SELECT * FROM alarm_config WHERE id = 1")
    fun observeConfig(): Flow<AlarmConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: AlarmConfigEntity)

    @Query("DELETE FROM alarm_config WHERE id = 1")
    suspend fun clear()
}
