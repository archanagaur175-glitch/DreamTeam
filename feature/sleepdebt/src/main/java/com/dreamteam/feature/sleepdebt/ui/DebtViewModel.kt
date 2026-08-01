package com.dreamteam.feature.sleepdebt.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreamteam.core.common.SleepSession
import com.dreamteam.feature.sleepdebt.domain.DebtRepository
import com.dreamteam.feature.sleepdebt.domain.RollingDebt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DebtViewModel @Inject constructor(
    private val repository: DebtRepository,
) : ViewModel() {

    val debt: StateFlow<RollingDebt?> = repository.observeDebt()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sessions: StateFlow<List<SleepSession>> = repository.observeSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val baselineHours: StateFlow<Double> = repository.observeBaselineHours()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 8.0)

    fun deleteSession(id: Long) {
        viewModelScope.launch { repository.deleteSession(id) }
    }
}
