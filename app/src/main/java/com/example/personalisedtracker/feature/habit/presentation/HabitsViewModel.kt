package com.example.personalisedtracker.feature.habit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalisedtracker.core.common.DateInt
import com.example.personalisedtracker.feature.habit.domain.model.HabitWithProgress
import com.example.personalisedtracker.feature.habit.domain.usecase.ArchiveHabitUseCase
import com.example.personalisedtracker.feature.habit.domain.usecase.ObserveHabitsWithProgressUseCase
import com.example.personalisedtracker.feature.habit.domain.usecase.SetHabitCompletionUseCase
import com.example.personalisedtracker.feature.habit.domain.usecase.UpsertHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HabitsViewModel @Inject constructor(
    observe: ObserveHabitsWithProgressUseCase,
    private val setCompletion: SetHabitCompletionUseCase,
    private val upsertUc: UpsertHabitUseCase,
    private val archiveUc: ArchiveHabitUseCase,
) : ViewModel() {

    private val today = DateInt.today()

    val state: StateFlow<List<HabitWithProgress>> =
        observe(today).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggle(habitId: Long, completed: Boolean) = viewModelScope.launch {
        setCompletion(habitId, today, completed)
    }

    fun add(name: String) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        upsertUc(id = 0, name = name, sortOrder = state.value.size)
    }

    fun rename(id: Long, name: String, sortOrder: Int) = viewModelScope.launch {
        upsertUc(id, name, sortOrder)
    }

    fun archive(id: Long) = viewModelScope.launch { archiveUc(id) }
}

