package com.example.personalisedtracker.feature.settings.data.export

import android.content.Context
import android.net.Uri
import com.example.personalisedtracker.core.common.AppError
import com.example.personalisedtracker.core.common.DataResult
import com.example.personalisedtracker.core.common.DispatcherProvider
import com.example.personalisedtracker.feature.body.data.dao.BodyWeightDao
import com.example.personalisedtracker.feature.body.data.dao.InBodyScanDao
import com.example.personalisedtracker.feature.body.data.dao.WaistMeasurementDao
import com.example.personalisedtracker.feature.body.data.entity.BodyWeightEntity
import com.example.personalisedtracker.feature.body.data.entity.InBodyScanEntity
import com.example.personalisedtracker.feature.body.data.entity.WaistMeasurementEntity
import com.example.personalisedtracker.feature.food.data.dao.FoodDao
import com.example.personalisedtracker.feature.food.data.dao.MealTemplateDao
import com.example.personalisedtracker.feature.food.data.entity.FoodEntryEntity
import com.example.personalisedtracker.feature.food.data.entity.MealTemplateEntity
import com.example.personalisedtracker.feature.habit.data.dao.HabitDao
import com.example.personalisedtracker.feature.habit.data.entity.HabitCompletionEntity
import com.example.personalisedtracker.feature.habit.data.entity.HabitEntity
import com.example.personalisedtracker.feature.sleep.data.dao.SleepDao
import com.example.personalisedtracker.feature.sleep.data.entity.SleepEntryEntity
import com.example.personalisedtracker.feature.workout.data.dao.ExerciseDao
import com.example.personalisedtracker.feature.workout.data.dao.RoutineDao
import com.example.personalisedtracker.feature.workout.data.dao.WorkoutDao
import com.example.personalisedtracker.feature.workout.data.entity.ExerciseEntity
import com.example.personalisedtracker.feature.workout.data.entity.RoutineEntity
import com.example.personalisedtracker.feature.workout.data.entity.RoutineExerciseEntity
import com.example.personalisedtracker.feature.workout.data.entity.WorkoutEntity
import com.example.personalisedtracker.feature.workout.data.entity.WorkoutSetEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Restores a JSON snapshot produced by [JsonExporter]. Conservative: maps every
 * exercise/routine id to its (possibly newly assigned) row id so we never
 * dangling-reference. Idempotent enough — re-importing the same file is safe
 * but will produce duplicate routines/workouts (we treat import as additive,
 * not destructive, per acceptance criteria).
 */
@Singleton
class JsonImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exerciseDao: ExerciseDao,
    private val routineDao: RoutineDao,
    private val workoutDao: WorkoutDao,
    private val bodyWeightDao: BodyWeightDao,
    private val waistDao: WaistMeasurementDao,
    private val inBodyScanDao: InBodyScanDao,
    private val sleepDao: SleepDao,
    private val foodDao: FoodDao,
    private val mealTemplateDao: MealTemplateDao,
    private val habitDao: HabitDao,
    private val dispatchers: DispatcherProvider,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun import(uri: Uri): DataResult<ImportSummary> = withContext(dispatchers.io) {
        try {
            val text = context.contentResolver.openInputStream(uri)?.use {
                it.bufferedReader().readText()
            } ?: return@withContext DataResult.Failure(AppError.NotFound("Could not open file"))

            val dto = json.decodeFromString(TrackerExportDto.serializer(), text)

            // Build exercise id remap: prefer existing by name, else insert.
            val existingByName = exerciseDao.getAll().associateBy { it.name }
            val exerciseIdMap = HashMap<Long, Long>()
            for (e in dto.exercises) {
                val existing = existingByName[e.name]
                val newId = existing?.id ?: exerciseDao.insert(
                    ExerciseEntity(
                        name = e.name,
                        category = e.category,
                        muscleGroups = e.muscleGroups.joinToString(","),
                        equipment = e.equipment,
                        isCustom = e.isCustom,
                    )
                )
                exerciseIdMap[e.id] = newId
            }

            // Routines
            val routineIdMap = HashMap<Long, Long>()
            for (r in dto.routines) {
                val newRoutineId = routineDao.insert(
                    RoutineEntity(
                        name = r.name,
                        dayOfWeek = r.dayOfWeek,
                        notes = r.notes,
                        createdAt = r.createdAt,
                        archived = r.archived,
                    )
                )
                routineIdMap[r.id] = newRoutineId
                val links = r.exercises.mapNotNull { link ->
                    val exId = exerciseIdMap[link.exerciseId] ?: return@mapNotNull null
                    RoutineExerciseEntity(
                        routineId = newRoutineId,
                        exerciseId = exId,
                        orderIndex = link.orderIndex,
                        targetSets = link.targetSets,
                        targetRepRange = link.targetRepRange,
                        targetWeightHintKg = link.targetWeightHintKg,
                        restSeconds = link.restSeconds,
                    )
                }
                routineDao.insertRoutineExercises(links)
            }

            // Workouts + sets
            var workoutCount = 0
            var setCount = 0
            for (w in dto.workouts) {
                val newWid = workoutDao.insertWorkout(
                    WorkoutEntity(
                        routineId = w.routineId?.let { routineIdMap[it] },
                        routineNameSnapshot = w.routineName,
                        startedAt = w.startedAt,
                        endedAt = w.endedAt,
                        notes = w.notes,
                    )
                )
                workoutCount++
                val setRows = w.sets.mapNotNull { s ->
                    val exId = exerciseIdMap[s.exerciseId] ?: return@mapNotNull null
                    WorkoutSetEntity(
                        workoutId = newWid,
                        exerciseId = exId,
                        setNumber = s.setNumber,
                        reps = s.reps,
                        weightKg = s.weightKg,
                        rpe = s.rpe,
                        isWarmup = s.isWarmup,
                        completed = s.completed,
                        orderIndex = s.orderIndex,
                    )
                }
                workoutDao.insertSets(setRows)
                setCount += setRows.size
            }

            DataResult.Success(
                ImportSummary(
                    exercises = dto.exercises.size,
                    routines = dto.routines.size,
                    workouts = workoutCount,
                    sets = setCount,
                    bodyWeights = ingestBodyWeights(dto),
                    waist = ingestWaist(dto),
                    inbodyScans = ingestInBody(dto),
                    sleep = ingestSleep(dto),
                    food = ingestFood(dto),
                    mealTemplates = ingestMealTemplates(dto),
                    habits = ingestHabits(dto),
                    habitCompletions = ingestHabitCompletions(dto),
                )
            )
        } catch (t: Throwable) {
            DataResult.Failure(AppError.Unknown(t.message ?: "Import failed", t))
        }
    }

    private suspend fun ingestBodyWeights(dto: TrackerExportDto): Int {
        if (dto.bodyWeights.isEmpty()) return 0
        bodyWeightDao.insertAll(dto.bodyWeights.map {
            BodyWeightEntity(date = it.date, weightKg = it.weightKg, note = it.note)
        })
        return dto.bodyWeights.size
    }

    private suspend fun ingestWaist(dto: TrackerExportDto): Int {
        if (dto.waistMeasurements.isEmpty()) return 0
        waistDao.insertAll(dto.waistMeasurements.map {
            WaistMeasurementEntity(date = it.date, waistCm = it.waistCm)
        })
        return dto.waistMeasurements.size
    }

    private suspend fun ingestInBody(dto: TrackerExportDto): Int {
        if (dto.inbodyScans.isEmpty()) return 0
        inBodyScanDao.insertAll(dto.inbodyScans.map { s ->
            InBodyScanEntity(
                date = s.date,
                measurementDevice = "",
                weightKg = s.weightKg,
                skeletalMuscleMassKg = s.skeletalMuscleMassKg,
                softLeanMassKg = null,
                bodyFatMassKg = s.bodyFatMassKg,
                bmi = s.bmi,
                bodyFatPercent = s.bodyFatPercent,
                basalMetabolicRateKcal = s.basalMetabolicRateKcal,
                inbodyScore = s.inbodyScore,
                rightArmLeanKg = null, leftArmLeanKg = null, trunkLeanKg = null,
                rightLegLeanKg = null, leftLegLeanKg = null,
                rightArmFatKg = null, leftArmFatKg = null, trunkFatKg = s.trunkFatKg,
                rightLegFatKg = null, leftLegFatKg = null,
                rightArmEcwRatio = null, leftArmEcwRatio = null,
                trunkEcwRatio = null, rightLegEcwRatio = null, leftLegEcwRatio = null,
                waistHipRatio = s.waistHipRatio,
                waistCircumferenceCm = s.waistCircumferenceCm,
                visceralFatAreaCm2 = null,
                visceralFatLevel = s.visceralFatLevel,
                totalBodyWaterKg = null, intracellularWaterKg = null, extracellularWaterKg = null,
                ecwRatio = null, upperLower = null, upper = null, lower = null,
                legMuscleLevel = null, legLeanKg = null,
                proteinKg = null, mineralKg = null, boneMineralContentKg = null,
                bodyCellMassKg = null, smiKgPerM2 = s.smiKgPerM2, phaseAngleDeg = null,
                rawTimestamp = s.rawTimestamp,
            )
        })
        return dto.inbodyScans.size
    }

    private suspend fun ingestSleep(dto: TrackerExportDto): Int {
        if (dto.sleepEntries.isEmpty()) return 0
        sleepDao.insertAll(dto.sleepEntries.map {
            SleepEntryEntity(date = it.date, hours = it.hours, quality1to5 = it.quality, note = it.note)
        })
        return dto.sleepEntries.size
    }

    private suspend fun ingestFood(dto: TrackerExportDto): Int {
        if (dto.foodEntries.isEmpty()) return 0
        foodDao.insertAll(dto.foodEntries.map {
            FoodEntryEntity(
                date = it.date, mealType = it.meal, description = it.description,
                approxCalories = it.calories, approxProteinG = it.protein,
                fatG = it.fatG, carbsG = it.carbsG,
                templateId = null, // template ids are remapped — drop on import
            )
        })
        return dto.foodEntries.size
    }

    private suspend fun ingestMealTemplates(dto: TrackerExportDto): Int {
        if (dto.mealTemplates.isEmpty()) return 0
        val existingByName = mealTemplateDao.getAll().associateBy { it.name.lowercase() }
        val toInsert = dto.mealTemplates
            .filter { it.name.lowercase() !in existingByName }
            .map { t ->
                MealTemplateEntity(
                    name = t.name, mealType = t.meal,
                    items = t.items.joinToString("\n"),
                    proteinG = t.proteinG, fatG = t.fatG, carbsG = t.carbsG,
                    caloriesKcal = t.caloriesKcal,
                    useCount = t.useCount, archived = t.archived,
                )
            }
        if (toInsert.isNotEmpty()) mealTemplateDao.insertAll(toInsert)
        return toInsert.size
    }

    private suspend fun ingestHabits(dto: TrackerExportDto): Map<Long, Long> {
        if (dto.habits.isEmpty()) return emptyMap()
        val existingByName = habitDao.getAll().associateBy { it.name }
        val map = HashMap<Long, Long>()
        for (h in dto.habits) {
            val existing = existingByName[h.name]
            val newId = existing?.id ?: habitDao.upsert(
                HabitEntity(
                    name = h.name, targetFrequency = h.frequency,
                    active = h.active, sortOrder = h.sortOrder,
                )
            )
            map[h.id] = newId
        }
        return map
    }

    @Suppress("UNUSED_PARAMETER")
    private suspend fun ingestHabitCompletions(dto: TrackerExportDto): Int {
        if (dto.habitCompletions.isEmpty()) return 0
        // Need habit-id remap. The export keeps ids stable so we lookup current
        // habits by name → id from the habits we just imported.
        val nameById = habitDao.getAll().associate { it.id to it.name }
        val byName = habitDao.getAll().associateBy { it.name }
        val rows = dto.habitCompletions.mapNotNull { c ->
            val originalName = dto.habits.firstOrNull { it.id == c.habitId }?.name ?: nameById[c.habitId]
            val habit = originalName?.let { byName[it] } ?: return@mapNotNull null
            HabitCompletionEntity(
                habitId = habit.id, date = c.date, completed = c.completed,
            )
        }
        habitDao.insertAllCompletions(rows)
        return rows.size
    }
}

data class ImportSummary(
    val exercises: Int,
    val routines: Int,
    val workouts: Int,
    val sets: Int,
    val bodyWeights: Int = 0,
    val waist: Int = 0,
    val inbodyScans: Int = 0,
    val sleep: Int = 0,
    val food: Int = 0,
    val mealTemplates: Int = 0,
    val habits: Map<Long, Long> = emptyMap(),
    val habitCompletions: Int = 0,
)

