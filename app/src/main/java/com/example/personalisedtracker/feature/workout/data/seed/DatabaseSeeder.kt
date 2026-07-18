package com.example.personalisedtracker.feature.workout.data.seed

import com.example.personalisedtracker.feature.food.data.seed.MealTemplateSeeder
import com.example.personalisedtracker.feature.habit.domain.repository.HabitRepository
import com.example.personalisedtracker.feature.workout.data.dao.ExerciseDao
import com.example.personalisedtracker.feature.workout.data.dao.RoutineDao
import com.example.personalisedtracker.feature.workout.data.entity.RoutineEntity
import com.example.personalisedtracker.feature.workout.data.entity.RoutineExerciseEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds the catalog (~60+ exercises), starter routines, and default habits on
 * first launch. Idempotent — guarded by count() checks so safe on every boot.
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val routineDao: RoutineDao,
    private val habitRepository: HabitRepository,
    private val mealTemplateSeeder: MealTemplateSeeder,
) {

    suspend fun seedIfNeeded() {
        if (exerciseDao.count() == 0) {
            exerciseDao.insertAll(SEED_EXERCISES.map { it.toEntity() })
        } else {
            // Backfill / repair imageUrl for existing installs whenever the
            // ExerciseImageMap mapping changes (e.g. URL pattern fix).
            backfillImageUrls()
        }
        if (routineDao.count() == 0) {
            seedRoutines()
        }
        habitRepository.seedDefaults(DEFAULT_HABITS)
        mealTemplateSeeder.seedIfNeeded()
    }

    private suspend fun backfillImageUrls() {
        SEED_EXERCISES.forEach { seed ->
            ExerciseImageMap.urlFor(seed.name)?.let {
                exerciseDao.updateImageUrlByName(seed.name, it)
            }
            ExerciseImageMap.secondaryUrlFor(seed.name)?.let {
                exerciseDao.updateSecondaryImageUrlByName(seed.name, it)
            }
        }
    }

    private suspend fun seedRoutines() {
        val byName = exerciseDao.getAll().associateBy { it.name }
        fun id(name: String): Long? = byName[name]?.id

        suspend fun create(name: String, day: Int, items: List<Triple<String, Int, String>>) {
            val routineId = routineDao.insert(RoutineEntity(name = name, dayOfWeek = day))
            val rows = items.mapIndexedNotNull { idx, (exName, sets, reps) ->
                val exId = id(exName) ?: return@mapIndexedNotNull null
                RoutineExerciseEntity(
                    routineId = routineId,
                    exerciseId = exId,
                    orderIndex = idx,
                    targetSets = sets,
                    targetRepRange = reps,
                )
            }
            routineDao.insertRoutineExercises(rows)
        }

        create("Monday — Home Push", 1, listOf(
            Triple("Push-up", 3, "12-20"),
            Triple("Dumbbell floor press", 4, "8-12"),
            Triple("Dumbbell overhead press", 3, "8-12"),
            Triple("Dumbbell lateral raise", 3, "12-15"),
            Triple("Triceps extension (DB)", 3, "10-15"),
        ))
        create("Tuesday — Gym Legs", 2, listOf(
            Triple("Barbell squat", 4, "6-10"),
            Triple("Romanian deadlift (BB)", 3, "8-12"),
            Triple("Bulgarian split squat", 3, "8-10"),
            Triple("Leg extension", 3, "12-15"),
            Triple("Standing calf raise", 4, "12-15"),
        ))
        create("Wednesday — Home Pull", 3, listOf(
            Triple("Pull-up", 4, "5-10"),
            Triple("Single-arm DB row", 3, "8-12"),
            Triple("Australian row", 3, "10-15"),
            Triple("Dumbbell bicep curl", 3, "10-12"),
            Triple("Face pull", 3, "12-15"),
        ))
        create("Thursday — Gym Upper", 4, listOf(
            Triple("Barbell bench press", 4, "6-10"),
            Triple("Lat pulldown", 3, "8-12"),
            Triple("Barbell overhead press", 3, "6-10"),
            Triple("Seated cable row", 3, "8-12"),
            Triple("Hammer curl", 3, "10-12"),
            Triple("Skullcrusher", 3, "10-12"),
        ))
        create("Friday — Calisthenics", 5, listOf(
            Triple("Pull-up", 4, "AMRAP"),
            Triple("Dips", 4, "AMRAP"),
            Triple("Pike push-up", 3, "8-12"),
            Triple("Hanging leg raise", 3, "8-12"),
            Triple("L-sit", 3, "15-30s"),
        ))
    }
}

/** Default habits seeded on first launch — user can edit / archive. */
internal val DEFAULT_HABITS: List<String> = listOf(
    "Finasteride + Minoxidil (AM)",
    "Finasteride + Minoxidil (PM)",
    "Saline nasal rinse",
    "3L water",
    "Sleep before 10:30 PM",
    "Prayers (5×)",
    "Quran (Mulk before sleep)",
)

