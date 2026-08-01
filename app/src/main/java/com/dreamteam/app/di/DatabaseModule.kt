package com.dreamteam.app.di

import android.content.Context
import androidx.room.Room
import com.dreamteam.core.database.DreamTeamDatabase
import com.dreamteam.core.database.dao.AlarmConfigDao
import com.dreamteam.core.database.dao.FactorLogDao
import com.dreamteam.core.database.dao.SleepBaselineDao
import com.dreamteam.core.database.dao.SleepSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DreamTeamDatabase =
        Room.databaseBuilder(context, DreamTeamDatabase::class.java, "dreamteam.db").build()

    @Provides
    fun provideSleepSessionDao(db: DreamTeamDatabase): SleepSessionDao = db.sleepSessionDao()

    @Provides
    fun provideSleepBaselineDao(db: DreamTeamDatabase): SleepBaselineDao = db.sleepBaselineDao()

    @Provides
    fun provideFactorLogDao(db: DreamTeamDatabase): FactorLogDao = db.factorLogDao()

    @Provides
    fun provideAlarmConfigDao(db: DreamTeamDatabase): AlarmConfigDao = db.alarmConfigDao()
}
