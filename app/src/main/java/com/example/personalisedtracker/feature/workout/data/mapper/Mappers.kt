package com.example.personalisedtracker.feature.workout.data.mapper

import com.example.personalisedtracker.feature.workout.data.entity.ExerciseEntity
import com.example.personalisedtracker.feature.workout.data.entity.RoutineEntity
import com.example.personalisedtracker.feature.workout.data.entity.RoutineExerciseEntity
import com.example.personalisedtracker.feature.workout.data.entity.WorkoutEntity
import com.example.personalisedtracker.feature.workout.data.entity.WorkoutSetEntity
import com.example.personalisedtracker.feature.workout.domain.model.Equipment
import com.example.personalisedtracker.feature.workout.domain.model.Exercise
import com.example.personalisedtracker.feature.workout.domain.model.ExerciseCategory
import com.example.personalisedtracker.feature.workout.domain.model.Routine
import com.example.personalisedtracker.feature.workout.domain.model.RoutineExercise
import com.example.personalisedtracker.feature.workout.domain.model.Workout
import com.example.personalisedtracker.feature.workout.domain.model.WorkoutSet

/** Entity ↔ domain mapping. Kept dumb so domain stays Room-free. */
internal fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    category = runCatching { ExerciseCategory.valueOf(category) }.getOrDefault(ExerciseCategory.OTHER),
    muscleGroups = muscleGroups.split(',').map { it.trim() }.filter { it.isNotEmpty() },
    equipment = runCatching { Equipment.valueOf(equipment) }.getOrDefault(Equipment.OTHER),
    isCustom = isCustom,
    imageUrl = imageUrl,
    imageUrlSecondary = imageUrlSecondary,
)

internal fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name,
    category = category.name,
    muscleGroups = muscleGroups.joinToString(","),
    equipment = equipment.name,
    isCustom = isCustom,
    imageUrl = imageUrl,
    imageUrlSecondary = imageUrlSecondary,
)

internal fun RoutineEntity.toDomain(): Routine = Routine(
    id = id,
    name = name,
    dayOfWeek = dayOfWeek,
    notes = notes,
    createdAt = createdAt,
    archived = archived,
)

internal fun Routine.toEntity(): RoutineEntity = RoutineEntity(
    id = id,
    name = name,
    dayOfWeek = dayOfWeek,
    notes = notes,
    createdAt = createdAt,
    archived = archived,
)

internal fun RoutineExerciseEntity.toDomain(exercise: Exercise): RoutineExercise = RoutineExercise(
    id = id,
    routineId = routineId,
    exercise = exercise,
    orderIndex = orderIndex,
    targetSets = targetSets,
    targetRepRange = targetRepRange,
    targetWeightHintKg = targetWeightHintKg,
    restSeconds = restSeconds,
)

internal fun WorkoutEntity.toDomain(): Workout = Workout(
    id = id,
    routineId = routineId,
    routineName = routineNameSnapshot,
    startedAt = startedAt,
    endedAt = endedAt,
    notes = notes,
)

internal fun WorkoutSetEntity.toDomain(): WorkoutSet = WorkoutSet(
    id = id,
    workoutId = workoutId,
    exerciseId = exerciseId,
    setNumber = setNumber,
    reps = reps,
    weightKg = weightKg,
    rpe = rpe,
    isWarmup = isWarmup,
    completed = completed,
    orderIndex = orderIndex,
)

internal fun WorkoutSet.toEntity(): WorkoutSetEntity = WorkoutSetEntity(
    id = id,
    workoutId = workoutId,
    exerciseId = exerciseId,
    setNumber = setNumber,
    reps = reps,
    weightKg = weightKg,
    rpe = rpe,
    isWarmup = isWarmup,
    completed = completed,
    orderIndex = orderIndex,
)

