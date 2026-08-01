package com.dreamteam.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dreamteam.core.database.dao.AlarmConfigDao
import com.dreamteam.core.database.dao.FactorLogDao
import com.dreamteam.core.database.dao.SleepBaselineDao
import com.dreamteam.core.database.dao.SleepSessionDao
import com.dreamteam.core.database.entity.AlarmConfigEntity
import com.dreamteam.core.database.entity.DailyFactorLogEntity
import com.dreamteam.core.database.entity.SleepBaselineEntity
import com.dreamteam.core.database.entity.SleepSessionEntity

@Database(
    entities = [
        SleepSessionEntity::class,
        SleepBaselineEntity::class,
        DailyFactorLogEntity::class,
        AlarmConfigEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class DreamTeamDatabase : RoomDatabase() {
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun sleepBaselineDao(): SleepBaselineDao
    abstract fun factorLogDao(): FactorLogDao
    abstract fun alarmConfigDao(): AlarmConfigDao
}
