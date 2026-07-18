package com.example.personalisedtracker.feature.workout.domain.usecase

import com.example.personalisedtracker.feature.workout.domain.model.ActiveWorkout
import com.example.personalisedtracker.feature.workout.domain.model.Exercise
import com.example.personalisedtracker.feature.workout.domain.model.Routine
import com.example.personalisedtracker.feature.workout.domain.model.RoutineExercise
import com.example.personalisedtracker.feature.workout.domain.model.RoutineWithExercises
import com.example.personalisedtracker.feature.workout.domain.model.Workout
import com.example.personalisedtracker.feature.workout.domain.model.WorkoutSet
import com.example.personalisedtracker.feature.workout.domain.repository.WorkoutRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * One UseCase = one verb. All side-effects funnel through these so
 * ViewModels stay thin and easy to unit-test.
 */

class ObserveRoutinesUseCase @Inject constructor(private val repo: WorkoutRepository) {
    operator fun invoke(): Flow<List<Routine>> = repo.observeRoutines()
}

class ObserveExercisesUseCase @Inject constructor(private val repo: WorkoutRepository) {
    operator fun invoke(): Flow<List<Exercise>> = repo.observeExercises()
}

class GetRoutineWithExercisesUseCase @Inject constructor(private val repo: WorkoutRepository) {
    suspend operator fun invoke(routineId: Long): RoutineWithExercises? =
        repo.getRoutineWithExercises(routineId)
}

class UpsertRoutineUseCase @Inject constructor(private val repo: WorkoutRepository) {
    suspend operator fun invoke(routine: Routine, exercises: List<RoutineExercise>): Long =
        repo.upsertRoutine(routine, exercises)
}

class ArchiveRoutineUseCase @Inject constructor(private val repo: WorkoutRepository) {
    suspend operator fun invoke(routineId: Long) = repo.archiveRoutine(routineId)
}

class StartWorkoutFromRoutineUseCase @Inject constructor(private val repo: WorkoutRepository) {
    suspend operator fun invoke(routineId: Long): Long = repo.startWorkoutFromRoutine(routineId)
}

class StartAdHocWorkoutUseCase @Inject constructor(private val repo: WorkoutRepository) {
    suspend operator fun invoke(name: String = "Quick workout"): Long = repo.startAdHocWorkout(name)
}

class ObserveActiveWorkoutUseCase @Inject constructor(private val repo: WorkoutRepository) {
    operator fun invoke(): Flow<Workout?> = repo.observeActiveWorkout()
}

class ObserveActiveWorkoutDetailUseCase @Inject constructor(private val repo: WorkoutRepository) {
    operator fun invoke(id: Long): Flow<ActiveWorkout?> = repo.observeActiveWorkoutDetail(id)
}

class ObserveWorkoutHistoryUseCase @Inject constructor(private val repo: WorkoutRepository) {
    operator fun invoke(): Flow<List<Workout>> = repo.observeWorkoutHistory()
}

class LogSetUseCase @Inject constructor(private val repo: WorkoutRepository) {
    suspend operator fun invoke(
        workoutId: Long,
        exerciseId: Long,
        setNumber: Int,
        reps: Int,
        weightKg: Double,
        isWarmup: Boolean,
        orderIndex: Int,
    ): Long = repo.addSet(workoutId, exerciseId, setNumber, reps, weightKg, isWarmup, orderIndex)
}

class UpdateSetUseCase @Inject constructor(private val repo: WorkoutRepository) {
    suspend operator fun invoke(set: WorkoutSet) = repo.updateSet(set)
}

class DeleteSetUseCase @Inject constructor(private val repo: WorkoutRepository) {
    suspend operator fun invoke(setId: Long) = repo.deleteSet(setId)
}

class FinishWorkoutUseCase @Inject constructor(private val repo: WorkoutRepository) {
    suspend operator fun invoke(workoutId: Long, notes: String = "") =
        repo.finishWorkout(workoutId, notes)
}

class DiscardWorkoutUseCase @Inject constructor(private val repo: WorkoutRepository) {
    suspend operator fun invoke(workoutId: Long) = repo.discardWorkout(workoutId)
}

class LastSetForExerciseUseCase @Inject constructor(private val repo: WorkoutRepository) {
    suspend operator fun invoke(exerciseId: Long): WorkoutSet? = repo.lastSetFor(exerciseId)
}

