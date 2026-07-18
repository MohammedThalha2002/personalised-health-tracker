package com.example.personalisedtracker.feature.workout.data.seed

import com.example.personalisedtracker.feature.workout.data.entity.ExerciseEntity
import com.example.personalisedtracker.feature.workout.domain.model.Equipment
import com.example.personalisedtracker.feature.workout.domain.model.ExerciseCategory

/**
 * The pre-seeded exercise catalog. ~60 exercises across Push / Pull / Legs /
 * Core / Cardio. Names are stable strings — routine seeding looks up by name.
 */
internal data class Seed(
    val name: String,
    val category: ExerciseCategory,
    val equipment: Equipment,
    val muscles: List<String>,
) {
    fun toEntity() = ExerciseEntity(
        name = name,
        category = category.name,
        muscleGroups = muscles.joinToString(","),
        equipment = equipment.name,
        isCustom = false,
        imageUrl = ExerciseImageMap.urlFor(name),
        imageUrlSecondary = ExerciseImageMap.secondaryUrlFor(name),
    )
}

internal val SEED_EXERCISES: List<Seed> = buildList {
    // ---- Push ----
    add(Seed("Push-up", ExerciseCategory.PUSH, Equipment.BODYWEIGHT, listOf("Chest", "Triceps", "Shoulders")))
    add(Seed("Diamond push-up", ExerciseCategory.PUSH, Equipment.BODYWEIGHT, listOf("Triceps", "Chest")))
    add(Seed("Pike push-up", ExerciseCategory.PUSH, Equipment.BODYWEIGHT, listOf("Shoulders", "Triceps")))
    add(Seed("Pseudo-planche push-up", ExerciseCategory.PUSH, Equipment.BODYWEIGHT, listOf("Chest", "Shoulders")))
    add(Seed("Dumbbell floor press", ExerciseCategory.PUSH, Equipment.DUMBBELL, listOf("Chest", "Triceps")))
    add(Seed("Dumbbell bench press", ExerciseCategory.PUSH, Equipment.DUMBBELL, listOf("Chest", "Triceps")))
    add(Seed("Barbell bench press", ExerciseCategory.PUSH, Equipment.BARBELL, listOf("Chest", "Triceps")))
    add(Seed("Dumbbell overhead press", ExerciseCategory.PUSH, Equipment.DUMBBELL, listOf("Shoulders", "Triceps")))
    add(Seed("Barbell overhead press", ExerciseCategory.PUSH, Equipment.BARBELL, listOf("Shoulders", "Triceps")))
    add(Seed("Dumbbell lateral raise", ExerciseCategory.PUSH, Equipment.DUMBBELL, listOf("Side delts")))
    add(Seed("Dumbbell front raise", ExerciseCategory.PUSH, Equipment.DUMBBELL, listOf("Front delts")))
    add(Seed("Dips", ExerciseCategory.PUSH, Equipment.BODYWEIGHT, listOf("Chest", "Triceps")))
    add(Seed("Triceps extension (DB)", ExerciseCategory.PUSH, Equipment.DUMBBELL, listOf("Triceps")))
    add(Seed("Skullcrusher", ExerciseCategory.PUSH, Equipment.BARBELL, listOf("Triceps")))

    // ---- Pull ----
    add(Seed("Pull-up", ExerciseCategory.PULL, Equipment.BODYWEIGHT, listOf("Lats", "Biceps")))
    add(Seed("Chin-up", ExerciseCategory.PULL, Equipment.BODYWEIGHT, listOf("Biceps", "Lats")))
    add(Seed("Weighted pull-up", ExerciseCategory.PULL, Equipment.BODYWEIGHT, listOf("Lats", "Biceps")))
    add(Seed("Australian row", ExerciseCategory.PULL, Equipment.BODYWEIGHT, listOf("Upper back", "Biceps")))
    add(Seed("Dumbbell bent-over row", ExerciseCategory.PULL, Equipment.DUMBBELL, listOf("Upper back", "Lats")))
    add(Seed("Single-arm DB row", ExerciseCategory.PULL, Equipment.DUMBBELL, listOf("Lats", "Upper back")))
    add(Seed("Lat pulldown", ExerciseCategory.PULL, Equipment.CABLE, listOf("Lats", "Biceps")))
    add(Seed("Seated cable row", ExerciseCategory.PULL, Equipment.CABLE, listOf("Upper back", "Lats")))
    add(Seed("Face pull", ExerciseCategory.PULL, Equipment.CABLE, listOf("Rear delts", "Upper back")))
    add(Seed("Dumbbell bicep curl", ExerciseCategory.PULL, Equipment.DUMBBELL, listOf("Biceps")))
    add(Seed("Hammer curl", ExerciseCategory.PULL, Equipment.DUMBBELL, listOf("Biceps", "Forearms")))
    add(Seed("Barbell curl", ExerciseCategory.PULL, Equipment.BARBELL, listOf("Biceps")))

    // ---- Legs ----
    add(Seed("Barbell squat", ExerciseCategory.LEGS, Equipment.BARBELL, listOf("Quads", "Glutes")))
    add(Seed("Dumbbell goblet squat", ExerciseCategory.LEGS, Equipment.DUMBBELL, listOf("Quads", "Glutes")))
    add(Seed("Front squat", ExerciseCategory.LEGS, Equipment.BARBELL, listOf("Quads", "Core")))
    add(Seed("Romanian deadlift (DB)", ExerciseCategory.LEGS, Equipment.DUMBBELL, listOf("Hamstrings", "Glutes")))
    add(Seed("Romanian deadlift (BB)", ExerciseCategory.LEGS, Equipment.BARBELL, listOf("Hamstrings", "Glutes")))
    add(Seed("Conventional deadlift", ExerciseCategory.LEGS, Equipment.BARBELL, listOf("Posterior chain")))
    add(Seed("Bulgarian split squat", ExerciseCategory.LEGS, Equipment.DUMBBELL, listOf("Quads", "Glutes")))
    add(Seed("Walking lunge", ExerciseCategory.LEGS, Equipment.DUMBBELL, listOf("Quads", "Glutes")))
    add(Seed("Dumbbell step-up", ExerciseCategory.LEGS, Equipment.DUMBBELL, listOf("Quads", "Glutes")))
    add(Seed("Leg press", ExerciseCategory.LEGS, Equipment.MACHINE, listOf("Quads", "Glutes")))
    add(Seed("Leg extension", ExerciseCategory.LEGS, Equipment.MACHINE, listOf("Quads")))
    add(Seed("Hamstring curl", ExerciseCategory.LEGS, Equipment.MACHINE, listOf("Hamstrings")))
    add(Seed("Standing calf raise", ExerciseCategory.LEGS, Equipment.MACHINE, listOf("Calves")))
    add(Seed("Seated calf raise", ExerciseCategory.LEGS, Equipment.MACHINE, listOf("Calves")))
    add(Seed("Pistol squat", ExerciseCategory.LEGS, Equipment.BODYWEIGHT, listOf("Quads", "Glutes")))
    add(Seed("Hip thrust", ExerciseCategory.LEGS, Equipment.BARBELL, listOf("Glutes", "Hamstrings")))

    // ---- Core ----
    add(Seed("Plank", ExerciseCategory.CORE, Equipment.BODYWEIGHT, listOf("Core")))
    add(Seed("Side plank", ExerciseCategory.CORE, Equipment.BODYWEIGHT, listOf("Obliques", "Core")))
    add(Seed("Hollow body hold", ExerciseCategory.CORE, Equipment.BODYWEIGHT, listOf("Core")))
    add(Seed("L-sit", ExerciseCategory.CORE, Equipment.BODYWEIGHT, listOf("Core", "Hip flexors")))
    add(Seed("Hanging knee raise", ExerciseCategory.CORE, Equipment.BODYWEIGHT, listOf("Core", "Hip flexors")))
    add(Seed("Hanging leg raise", ExerciseCategory.CORE, Equipment.BODYWEIGHT, listOf("Core", "Hip flexors")))
    add(Seed("Dragon flag", ExerciseCategory.CORE, Equipment.BODYWEIGHT, listOf("Core")))
    add(Seed("Ab wheel", ExerciseCategory.CORE, Equipment.OTHER, listOf("Core")))
    add(Seed("Russian twist", ExerciseCategory.CORE, Equipment.BODYWEIGHT, listOf("Obliques")))
    add(Seed("Cable wood chop", ExerciseCategory.CORE, Equipment.CABLE, listOf("Obliques", "Core")))
    add(Seed("Pallof press", ExerciseCategory.CORE, Equipment.CABLE, listOf("Core")))
    add(Seed("Crunch", ExerciseCategory.CORE, Equipment.BODYWEIGHT, listOf("Core")))

    // ---- Cardio ----
    add(Seed("Treadmill walk", ExerciseCategory.CARDIO, Equipment.MACHINE, listOf("Cardio")))
    add(Seed("Treadmill run", ExerciseCategory.CARDIO, Equipment.MACHINE, listOf("Cardio")))
    add(Seed("Stationary bike", ExerciseCategory.CARDIO, Equipment.MACHINE, listOf("Cardio")))
    add(Seed("Rowing machine", ExerciseCategory.CARDIO, Equipment.MACHINE, listOf("Cardio", "Full body")))
    add(Seed("Jump rope", ExerciseCategory.CARDIO, Equipment.OTHER, listOf("Cardio")))
    add(Seed("Stair climber", ExerciseCategory.CARDIO, Equipment.MACHINE, listOf("Cardio", "Glutes")))
    add(Seed("Outdoor run", ExerciseCategory.CARDIO, Equipment.BODYWEIGHT, listOf("Cardio")))
    add(Seed("Cricket", ExerciseCategory.CARDIO, Equipment.OTHER, listOf("Cardio")))
}

