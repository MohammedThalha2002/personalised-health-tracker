package com.example.personalisedtracker.feature.workout.presentation.active_workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalisedtracker.feature.workout.domain.model.ActiveWorkout
import com.example.personalisedtracker.feature.workout.domain.model.WorkoutSet
import com.example.personalisedtracker.feature.workout.domain.usecase.DeleteSetUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.DiscardWorkoutUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.FinishWorkoutUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.LastSetForExerciseUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.LogSetUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.ObserveActiveWorkoutDetailUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.UpdateSetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the most-used screen. State is sourced directly from Room so it
 * survives process death (acceptance criterion: app killed mid-workout → resumes).
 */
@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    savedState: SavedStateHandle,
    observeDetail: ObserveActiveWorkoutDetailUseCase,
    private val logSet: LogSetUseCase,
    private val updateSet: UpdateSetUseCase,
    private val deleteSet: DeleteSetUseCase,
    private val finish: FinishWorkoutUseCase,
    private val discardUc: DiscardWorkoutUseCase,
    private val lastSetFor: LastSetForExerciseUseCase,
) : ViewModel() {

    val workoutId: Long = checkNotNull(savedState["workoutId"]) {
        "workoutId is required"
    }

    val state: StateFlow<ActiveWorkout?> =
        observeDetail(workoutId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Logs a new completed set. We compute set number = (max existing + 1) for
     * the exercise within this workout, and reuse the existing group's
     * orderIndex if present, else append.
     */
    fun logSet(exerciseId: Long, reps: Int, weightKg: Double, isWarmup: Boolean = false) {
        viewModelScope.launch {
            val current = state.value ?: return@launch
            val group = current.groups.firstOrNull { it.exercise.id == exerciseId }
            val setNumber = (group?.sets?.maxOfOrNull { it.setNumber } ?: 0) + 1
            val orderIndex = group?.orderIndex
                ?: ((current.groups.maxOfOrNull { it.orderIndex } ?: -1) + 1)
            logSet.invoke(
                workoutId = workoutId,
                exerciseId = exerciseId,
                setNumber = setNumber,
                reps = reps,
                weightKg = weightKg,
                isWarmup = isWarmup,
                orderIndex = orderIndex,
            )
        }
    }

    fun editSet(set: WorkoutSet) = viewModelScope.launch { updateSet(set) }

    fun removeSet(setId: Long) = viewModelScope.launch { deleteSet(setId) }

    /** Returns a (weightKg, reps) hint for the user from the previous logged set. */
    suspend fun prefillFor(exerciseId: Long): Pair<Double, Int>? {
        val current = state.value
        val withinSession = current?.groups
            ?.firstOrNull { it.exercise.id == exerciseId }
            ?.sets?.lastOrNull()
        if (withinSession != null) return withinSession.weightKg to withinSession.reps
        return lastSetFor(exerciseId)?.let { it.weightKg to it.reps }
    }

    fun finishWorkout(onDone: () -> Unit) = viewModelScope.launch {
        finish(workoutId)
        onDone()
    }

    fun discard(onDone: () -> Unit) = viewModelScope.launch {
        discardUc(workoutId)
        onDone()
    }
}

