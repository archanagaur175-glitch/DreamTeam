package com.dreamteam.feature.logger.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreamteam.feature.logger.domain.CorrelationCalculator
import com.dreamteam.feature.logger.domain.CorrelationResult
import com.dreamteam.feature.logger.domain.FactorLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CorrelationsViewModel @Inject constructor(
    repository: FactorLogRepository,
) : ViewModel() {

    val correlations: StateFlow<List<CorrelationResult>> =
        combine(repository.observeLogs(), repository.observeSessions()) { logs, sessions ->
            CorrelationCalculator.compute(logs, sessions, LocalDate.now())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
