package com.dreamteam.feature.smartalarm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreamteam.feature.smartalarm.domain.AlarmConfig
import com.dreamteam.feature.smartalarm.domain.AlarmConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmSetupViewModel @Inject constructor(
    private val repository: AlarmConfigRepository,
) : ViewModel() {

    val config: StateFlow<AlarmConfig?> = repository.observeConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun saveAndSchedule(config: AlarmConfig) {
        viewModelScope.launch {
            repository.save(config)
            _saved.value = true
        }
    }

    fun cancelAlarm() {
        viewModelScope.launch {
            repository.clear()
            _saved.value = false
        }
    }
}
