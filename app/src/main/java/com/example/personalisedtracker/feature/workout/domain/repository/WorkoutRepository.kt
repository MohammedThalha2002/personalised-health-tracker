package com.example.personalisedtracker.feature.workout.domain.repository

import com.example.personalisedtracker.feature.workout.domain.model.ActiveWorkout
import com.example.personalisedtracker.feature.workout.domain.model.Exercise
import com.example.personalisedtracker.feature.workout.domain.model.ExerciseCategory
import com.example.personalisedtracker.feature.workout.domain.model.Equipment
import com.example.personalisedtracker.feature.workout.domain.model.Routine
import com.example.personalisedtracker.feature.workout.domain.model.RoutineExercise
import com.example.personalisedtracker.feature.workout.domain.model.RoutineWithExercises
import com.example.personalisedtracker.feature.workout.domain.model.Workout
import com.example.personalisedtracker.feature.workout.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

/**
 * The single source of truth for workout data. ViewModels only ever touch
 * this through use cases — keeps the domain layer pure.
 */
interface WorkoutRepository {
    // exercises
    fun observeExercises(): Flow<List<Exercise>>
    suspend fun getExercises(): List<Exercise>
    suspend fun addCustomExercise(
        name: String,
        category: ExerciseCategory,
        muscleGroups: List<String>,
        equipment: Equipment,
    ): Long

    // routines
    fun observeRoutines(): Flow<List<Routine>>
    suspend fun getRoutineWithExercises(routineId: Long): RoutineWithExercises?
    suspend fun upsertRoutine(routine: Routine, exercises: List<RoutineExercise>): Long
    suspend fun archiveRoutine(routineId: Long)

    // workouts
    fun observeActiveWorkout(): Flow<Workout?>
    fun observeWorkoutHistory(): Flow<List<Workout>>
    suspend fun getActiveWorkout(): ActiveWorkout?
    fun observeActiveWorkoutDetail(workoutId: Long): Flow<ActiveWorkout?>
    suspend fun startWorkoutFromRoutine(routineId: Long): Long
    suspend fun startAdHocWorkout(name: String): Long
    suspend fun addSet(
        workoutId: Long,
        exerciseId: Long,
        setNumber: Int,
        reps: Int,
        weightKg: Double,
        isWarmup: Boolean = false,
        orderIndex: Int,
    ): Long
    suspend fun updateSet(set: WorkoutSet)
    suspend fun deleteSet(setId: Long)
    suspend fun finishWorkout(workoutId: Long, notes: String = "")
    suspend fun discardWorkout(workoutId: Long)
    suspend fun addExerciseToActiveWorkout(workoutId: Long, exerciseId: Long): Int
    suspend fun lastSetFor(exerciseId: Long): WorkoutSet?
}


