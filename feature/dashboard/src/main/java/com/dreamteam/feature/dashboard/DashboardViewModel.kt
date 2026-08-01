package com.dreamteam.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreamteam.core.common.FactorTag
import com.dreamteam.core.common.nowFlow
import com.dreamteam.feature.circadian.domain.CircadianRepository
import com.dreamteam.feature.circadian.domain.EnergyCurve
import com.dreamteam.feature.logger.domain.FactorLogRepository
import com.dreamteam.feature.sleepdebt.domain.DebtRepository
import com.dreamteam.feature.sleepdebt.domain.RollingDebt
import com.dreamteam.feature.smartalarm.domain.AlarmConfig
import com.dreamteam.feature.smartalarm.domain.AlarmConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/** The home experience: debt, energy curve, tonight's alarm, today's quick-log. */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    debtRepository: DebtRepository,
    circadianRepository: CircadianRepository,
    alarmRepository: AlarmConfigRepository,
    factorRepository: FactorLogRepository,
) : ViewModel() {

    val debt: StateFlow<RollingDebt?> = debtRepository.observeDebt()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val curve: StateFlow<EnergyCurve?> = circadianRepository.observeCurve()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val alarm: StateFlow<AlarmConfig?> = alarmRepository.observeConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val todayTags: StateFlow<Set<FactorTag>> = combine(
        factorRepository.observeLogs(),
        factorRepository.observeSessions(),
    ) { logs, _ ->
        logs.firstOrNull { it.date == LocalDate.now() }?.tags ?: emptySet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /** Live clock for the "now" marker and alarm-window status. */
    val now: StateFlow<LocalTime> = nowFlow(30_000)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LocalTime.now())
}
