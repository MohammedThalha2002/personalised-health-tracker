package com.example.personalisedtracker.feature.workout.presentation.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalisedtracker.feature.workout.domain.model.Routine
import com.example.personalisedtracker.feature.workout.domain.usecase.ArchiveRoutineUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.ObserveActiveWorkoutUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.ObserveRoutinesUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.StartAdHocWorkoutUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.StartWorkoutFromRoutineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lightweight DTO so the screen never sees domain types directly. */
data class RoutinesUiState(
    val routines: List<Routine> = emptyList(),
    val activeWorkoutId: Long? = null,
)

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    observeRoutines: ObserveRoutinesUseCase,
    observeActive: ObserveActiveWorkoutUseCase,
    private val startFromRoutineUc: StartWorkoutFromRoutineUseCase,
    private val startAdHoc: StartAdHocWorkoutUseCase,
    private val archiveUc: ArchiveRoutineUseCase,
) : ViewModel() {

    val state: StateFlow<RoutinesUiState> = combine(
        observeRoutines(), observeActive()
    ) { routines, active ->
        RoutinesUiState(routines = routines, activeWorkoutId = active?.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoutinesUiState())

    /** Returns the workout id to navigate to. */
    suspend fun startFromRoutine(routineId: Long): Long = startFromRoutineUc(routineId)

    suspend fun startQuickWorkout(): Long = startAdHoc()

    fun archive(routineId: Long) = viewModelScope.launch { archiveUc(routineId) }
}


