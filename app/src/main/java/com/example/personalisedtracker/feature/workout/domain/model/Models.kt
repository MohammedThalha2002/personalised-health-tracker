package com.example.personalisedtracker.feature.workout.domain.model

/** Top-level categorisation used for filtering the catalog. */
enum class ExerciseCategory { PUSH, PULL, LEGS, CORE, CARDIO, OTHER }

/** Equipment used by an exercise — also filterable. */
enum class Equipment { BARBELL, DUMBBELL, BODYWEIGHT, MACHINE, CABLE, BAND, OTHER }

/** Domain model for an exercise (catalog item). */
data class Exercise(
    val id: Long,
    val name: String,
    val category: ExerciseCategory,
    val muscleGroups: List<String>,
    val equipment: Equipment,
    val isCustom: Boolean,
    /** Start-position image (`0.jpg`). Used for catalog thumb + workout hero. */
    val imageUrl: String? = null,
    /** End-position image (`1.jpg`). Combined with [imageUrl] to animate motion. */
    val imageUrlSecondary: String? = null,
)

/** Domain model for a routine (template). */
data class Routine(
    val id: Long,
    val name: String,
    val dayOfWeek: Int?,
    val notes: String,
    val createdAt: Long,
    val archived: Boolean,
)

/** A line item inside a routine — exercise + its target sets/reps. */
data class RoutineExercise(
    val id: Long,
    val routineId: Long,
    val exercise: Exercise,
    val orderIndex: Int,
    val targetSets: Int,
    val targetRepRange: String,
    val targetWeightHintKg: Double?,
    val restSeconds: Int,
)

/** Full routine + its exercises (joined). */
data class RoutineWithExercises(
    val routine: Routine,
    val exercises: List<RoutineExercise>,
)

/** A workout session. */
data class Workout(
    val id: Long,
    val routineId: Long?,
    val routineName: String,
    val startedAt: Long,
    val endedAt: Long?,
    val notes: String,
)

/** A logged set inside a workout. */
data class WorkoutSet(
    val id: Long,
    val workoutId: Long,
    val exerciseId: Long,
    val setNumber: Int,
    val reps: Int,
    val weightKg: Double,
    val rpe: Int?,
    val isWarmup: Boolean,
    val completed: Boolean,
    val orderIndex: Int,
)

/** Sets for one exercise inside a workout, grouped for the active-workout UI. */
data class WorkoutExerciseGroup(
    val exercise: Exercise,
    val targetSets: Int,
    val targetRepRange: String,
    val restSeconds: Int,
    val sets: List<WorkoutSet>,
    val orderIndex: Int,
)

/** Full session, ready for the active-workout screen. */
data class ActiveWorkout(
    val workout: Workout,
    val groups: List<WorkoutExerciseGroup>,
)

