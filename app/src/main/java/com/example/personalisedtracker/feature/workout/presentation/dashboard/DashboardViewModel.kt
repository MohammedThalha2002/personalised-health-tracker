package com.example.personalisedtracker.feature.workout.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalisedtracker.core.common.DateInt
import com.example.personalisedtracker.feature.body.domain.usecase.ObserveWeightsSinceUseCase
import com.example.personalisedtracker.feature.habit.domain.usecase.ObserveHabitsWithProgressUseCase
import com.example.personalisedtracker.feature.sleep.domain.usecase.ObserveSleepSinceUseCase
import com.example.personalisedtracker.feature.workout.domain.model.Workout
import com.example.personalisedtracker.feature.workout.domain.usecase.ObserveActiveWorkoutUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.ObserveWorkoutHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val lastWorkout: Workout? = null,
    val activeWorkoutId: Long? = null,
    val todayWeightKg: Double? = null,
    val lastSleepHours: Double? = null,
    val lastSleepDate: Int? = null,
    val habitsDoneToday: Int = 0,
    val habitsTotal: Int = 0,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    history: ObserveWorkoutHistoryUseCase,
    active: ObserveActiveWorkoutUseCase,
    weightsSince: ObserveWeightsSinceUseCase,
    sleepSince: ObserveSleepSinceUseCase,
    habits: ObserveHabitsWithProgressUseCase,
) : ViewModel() {
    private val today = DateInt.today()

    val state: StateFlow<DashboardUiState> = combine(
        history(),
        active(),
        weightsSince(DateInt.minusDays(today, 7)),
        sleepSince(DateInt.minusDays(today, 7)),
        habits(today),
    ) { h, a, weights, sleep, habitsList ->
        val todayWeight = weights.firstOrNull { it.date == today }?.weightKg
            ?: weights.maxByOrNull { it.date }?.weightKg
        val lastSleep = sleep.maxByOrNull { it.date }
        DashboardUiState(
            lastWorkout = h.firstOrNull(),
            activeWorkoutId = a?.id,
            todayWeightKg = todayWeight,
            lastSleepHours = lastSleep?.hours,
            lastSleepDate = lastSleep?.date,
            habitsDoneToday = habitsList.count { it.doneToday },
            habitsTotal = habitsList.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}

