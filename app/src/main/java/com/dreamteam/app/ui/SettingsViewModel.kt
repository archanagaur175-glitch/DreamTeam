package com.dreamteam.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreamteam.feature.sleepdebt.domain.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val debtRepository: DebtRepository,
) : ViewModel() {

    val baselineHours: StateFlow<Double> = debtRepository.observeBaselineHours()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 8.0)

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun setBaseline(hours: Double) {
        viewModelScope.launch {
            debtRepository.setBaselineHours(hours)
            _saved.value = true
        }
    }
}
