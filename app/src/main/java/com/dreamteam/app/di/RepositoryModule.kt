package com.dreamteam.app.di

import com.dreamteam.core.common.FactorTag
import com.dreamteam.core.common.SleepSession
import com.dreamteam.core.common.todayFlow
import com.dreamteam.core.database.dao.AlarmConfigDao
import com.dreamteam.core.database.dao.FactorLogDao
import com.dreamteam.core.database.dao.SleepBaselineDao
import com.dreamteam.core.database.dao.SleepSessionDao
import com.dreamteam.core.database.entity.SleepBaselineEntity
import com.dreamteam.feature.circadian.domain.CircadianEngine
import com.dreamteam.feature.circadian.domain.CircadianRepository
import com.dreamteam.feature.circadian.domain.EnergyCurve
import com.dreamteam.feature.logger.domain.DailyLog
import com.dreamteam.feature.logger.domain.FactorLogRepository
import com.dreamteam.feature.sleepdebt.domain.DebtRepository
import com.dreamteam.feature.sleepdebt.domain.RollingDebt
import com.dreamteam.feature.sleepdebt.domain.SleepDebtEngine
import com.dreamteam.feature.smartalarm.alarm.AlarmScheduler
import com.dreamteam.feature.smartalarm.domain.AlarmConfig
import com.dreamteam.feature.smartalarm.domain.AlarmConfigRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebtRepositoryImpl @Inject constructor(
    private val sessionDao: SleepSessionDao,
    private val baselineDao: SleepBaselineDao,
) : DebtRepository {

    override fun observeDebt(): Flow<RollingDebt> = combine(
        sessionDao.observeSessions(),
        baselineDao.observeBaseline(SleepBaselineEntity.DEFAULT_USER_ID),
        todayFlow(),
    ) { sessions, baseline, today ->
        SleepDebtEngine.computeRollingDebt(
            sessions = sessions.map { it.toDomain() },
            baselineHours = baseline?.targetSleepHours ?: DEFAULT_BASELINE_HOURS,
            today = today,
        )
    }.distinctUntilChanged()

    override fun observeBaselineHours(): Flow<Double> =
        baselineDao.observeBaseline(SleepBaselineEntity.DEFAULT_USER_ID)
            .map { it?.targetSleepHours ?: DEFAULT_BASELINE_HOURS }

    override suspend fun setBaselineHours(hours: Double) {
        baselineDao.upsert(
            SleepBaselineEntity(
                userId = SleepBaselineEntity.DEFAULT_USER_ID,
                targetSleepHours = hours,
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    override fun observeSessions(): Flow<List<SleepSession>> =
        sessionDao.observeSessions().map { list -> list.map { it.toDomain() } }

    override suspend fun addSession(session: SleepSession) {
        sessionDao.insert(session.toEntity())
    }

    override suspend fun deleteSession(id: Long) {
        sessionDao.deleteById(id)
    }
}

@Singleton
class CircadianRepositoryImpl @Inject constructor(
    private val sessionDao: SleepSessionDao,
    debtRepository: DebtRepository,
) : CircadianRepository {

    private val debtFlow = debtRepository.observeDebt()

    override fun observeCurve(): Flow<EnergyCurve> = combine(
        sessionDao.observeSessions(),
        debtFlow,
    ) { sessions, debt ->
        val wakeTime = sessions.firstOrNull()?.sleepEnd?.toLocalTime() ?: LocalTime.of(7, 0)
        CircadianEngine.computeEnergyCurve(wakeTime, debt.totalDeficitHours)
    }.distinctUntilChanged()
}

@Singleton
class FactorLogRepositoryImpl @Inject constructor(
    private val factorLogDao: FactorLogDao,
    private val sessionDao: SleepSessionDao,
) : FactorLogRepository {

    override fun observeLogs(): Flow<List<DailyLog>> =
        factorLogDao.observeLogs().map { list -> list.map { it.toDomain() } }

    override fun observeSessions(): Flow<List<SleepSession>> =
        sessionDao.observeSessions().map { list -> list.map { it.toDomain() } }

    override suspend fun saveTags(date: LocalDate, tags: Set<FactorTag>, note: String?) {
        factorLogDao.upsert(DailyLog(date, tags, note).toEntity())
    }

    override suspend fun addSession(session: SleepSession) {
        sessionDao.insert(session.toEntity())
    }

    override suspend fun deleteSession(id: Long) {
        sessionDao.deleteById(id)
    }
}

@Singleton
class AlarmConfigRepositoryImpl @Inject constructor(
    private val dao: AlarmConfigDao,
    private val scheduler: AlarmScheduler,
) : AlarmConfigRepository {

    override fun observeConfig(): Flow<AlarmConfig?> =
        dao.observeConfig().map { it?.toDomain() }

    override suspend fun save(config: AlarmConfig) {
        dao.upsert(config.toEntity())
        scheduler.schedule(config)
    }

    override suspend fun clear() {
        dao.clear()
        scheduler.cancel()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDebtRepository(impl: DebtRepositoryImpl): DebtRepository

    @Binds
    @Singleton
    abstract fun bindCircadianRepository(impl: CircadianRepositoryImpl): CircadianRepository

    @Binds
    @Singleton
    abstract fun bindFactorLogRepository(impl: FactorLogRepositoryImpl): FactorLogRepository

    @Binds
    @Singleton
    abstract fun bindAlarmConfigRepository(impl: AlarmConfigRepositoryImpl): AlarmConfigRepository
}
