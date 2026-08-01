package com.dreamteam.feature.circadian.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreamteam.feature.circadian.domain.CircadianRepository
import com.dreamteam.feature.circadian.domain.EnergyCurve
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CircadianViewModel @Inject constructor(
    repository: CircadianRepository,
) : ViewModel() {

    val curve: StateFlow<EnergyCurve?> = repository.observeCurve()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
