package com.example.personalisedtracker.feature.sleep.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalisedtracker.core.common.DateInt
import com.example.personalisedtracker.feature.sleep.domain.model.SleepEntry
import com.example.personalisedtracker.feature.sleep.domain.usecase.DeleteSleepUseCase
import com.example.personalisedtracker.feature.sleep.domain.usecase.LogSleepUseCase
import com.example.personalisedtracker.feature.sleep.domain.usecase.ObserveSleepSinceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SleepViewModel @Inject constructor(
    observe: ObserveSleepSinceUseCase,
    private val logSleep: LogSleepUseCase,
    private val deleteUc: DeleteSleepUseCase,
) : ViewModel() {

    private val since = DateInt.minusDays(DateInt.today(), 30)
    val entries: StateFlow<List<SleepEntry>> =
        observe(since).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun log(date: Int, hours: Double, quality: Int, note: String = "") = viewModelScope.launch {
        logSleep(date, hours, quality, note)
    }

    fun delete(id: Long) = viewModelScope.launch { deleteUc(id) }
}

