package com.dreamteam.feature.logger.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreamteam.core.common.FactorTag
import com.dreamteam.core.common.SleepSession
import com.dreamteam.core.common.SleepSource
import com.dreamteam.feature.logger.domain.DailyLog
import com.dreamteam.feature.logger.domain.FactorLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class LoggerViewModel @Inject constructor(
    private val repository: FactorLogRepository,
) : ViewModel() {

    val logs: StateFlow<List<DailyLog>> = repository.observeLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessions: StateFlow<List<SleepSession>> = repository.observeSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedDate = MutableStateFlow(LocalDate.now().minusDays(1))
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val selectedLog: StateFlow<DailyLog?> = combine(logs, _selectedDate) { all, date ->
        all.firstOrNull { it.date == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun toggleTag(tag: FactorTag) {
        val current = selectedLog.value?.tags ?: emptySet()
        val next = if (tag in current) current - tag else current + tag
        viewModelScope.launch {
            repository.saveTags(_selectedDate.value, next, selectedLog.value?.freeNote)
        }
    }

    fun saveNote(note: String) {
        viewModelScope.launch {
            repository.saveTags(
                _selectedDate.value,
                selectedLog.value?.tags ?: emptySet(),
                note.ifBlank { null },
            )
        }
    }

    fun addSession(start: LocalDateTime, end: LocalDateTime, quality: Int?) {
        if (!end.isAfter(start)) return
        viewModelScope.launch {
            repository.addSession(
                SleepSession(
                    sleepStart = start,
                    sleepEnd = end,
                    source = SleepSource.MANUAL,
                    qualityScore = quality,
                ),
            )
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch { repository.deleteSession(id) }
    }
}
