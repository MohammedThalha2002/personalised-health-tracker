package com.example.personalisedtracker.feature.workout.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalisedtracker.feature.workout.domain.model.Workout
import com.example.personalisedtracker.feature.workout.domain.usecase.ObserveWorkoutHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class WorkoutHistoryViewModel @Inject constructor(
    observe: ObserveWorkoutHistoryUseCase,
) : ViewModel() {
    val workouts: StateFlow<List<Workout>> =
        observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

