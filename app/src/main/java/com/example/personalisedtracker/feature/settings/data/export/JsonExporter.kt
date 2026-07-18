package com.example.personalisedtracker.feature.settings.data.export

import android.content.Context
import androidx.core.content.FileProvider
import com.example.personalisedtracker.core.common.DispatcherProvider
import com.example.personalisedtracker.feature.body.data.dao.BodyWeightDao
import com.example.personalisedtracker.feature.body.data.dao.InBodyScanDao
import com.example.personalisedtracker.feature.body.data.dao.WaistMeasurementDao
import com.example.personalisedtracker.feature.food.data.dao.FoodDao
import com.example.personalisedtracker.feature.food.data.dao.MealTemplateDao
import com.example.personalisedtracker.feature.habit.data.dao.HabitDao
import com.example.personalisedtracker.feature.sleep.data.dao.SleepDao
import com.example.personalisedtracker.feature.workout.data.dao.ExerciseDao
import com.example.personalisedtracker.feature.workout.data.dao.RoutineDao
import com.example.personalisedtracker.feature.workout.data.dao.WorkoutDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Writes a JSON snapshot of the full Phase 1 schema (exercises + routines +
 * workouts + sets) to app cache, returning a content:// URI safe to share via
 * the system share-sheet.
 */
@Singleton
class JsonExporter @Inject constructor(
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
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun export(appVersion: String = "1.0.0"): android.net.Uri = withContext(dispatchers.io) {
        val exercises = exerciseDao.getAll().map {
            ExerciseDto(
                id = it.id,
                name = it.name,
                category = it.category,
                muscleGroups = it.muscleGroups.split(',').map(String::trim).filter(String::isNotEmpty),
                equipment = it.equipment,
                isCustom = it.isCustom,
            )
        }
        val routineRows: List<RoutineDto> = buildRoutineDtos()

        val workouts = workoutDao.getAllForExport()
        val allSets = workoutDao.getAllSetsForExport().groupBy { it.workoutId }
        val workoutDtos = workouts.map { w ->
            WorkoutDto(
                id = w.id,
                routineId = w.routineId,
                routineName = w.routineNameSnapshot,
                startedAt = w.startedAt,
                endedAt = w.endedAt,
                notes = w.notes,
                sets = (allSets[w.id].orEmpty()).map { s ->
                    WorkoutSetDto(
                        id = s.id,
                        exerciseId = s.exerciseId,
                        setNumber = s.setNumber,
                        reps = s.reps,
                        weightKg = s.weightKg,
                        rpe = s.rpe,
                        isWarmup = s.isWarmup,
                        completed = s.completed,
                        orderIndex = s.orderIndex,
                    )
                },
            )
        }

        val dto = TrackerExportDto(
            exportedAt = iso8601Now(),
            appVersion = appVersion,
            workouts = workoutDtos,
            exercises = exercises,
            routines = routineRows,
            bodyWeights = bodyWeightDao.getAll().map {
                BodyWeightDto(it.id, it.date, it.weightKg, it.note)
            },
            waistMeasurements = waistDao.getAll().map {
                WaistDto(it.id, it.date, it.waistCm)
            },
            inbodyScans = inBodyScanDao.getAll().map { s ->
                InBodyScanDto(
                    id = s.id, date = s.date, rawTimestamp = s.rawTimestamp,
                    weightKg = s.weightKg,
                    skeletalMuscleMassKg = s.skeletalMuscleMassKg,
                    bodyFatMassKg = s.bodyFatMassKg,
                    bmi = s.bmi, bodyFatPercent = s.bodyFatPercent,
                    basalMetabolicRateKcal = s.basalMetabolicRateKcal,
                    inbodyScore = s.inbodyScore,
                    visceralFatLevel = s.visceralFatLevel,
                    waistHipRatio = s.waistHipRatio,
                    waistCircumferenceCm = s.waistCircumferenceCm,
                    trunkFatKg = s.trunkFatKg,
                    smiKgPerM2 = s.smiKgPerM2,
                )
            },
            sleepEntries = sleepDao.getAll().map {
                SleepDto(it.id, it.date, it.hours, it.quality1to5, it.note)
            },
            foodEntries = foodDao.getAll().map {
                FoodDto(
                    it.id, it.date, it.mealType, it.description,
                    it.approxCalories, it.approxProteinG,
                    fatG = it.fatG, carbsG = it.carbsG, templateId = it.templateId,
                )
            },
            mealTemplates = mealTemplateDao.getAll().map { t ->
                MealTemplateDto(
                    id = t.id, name = t.name, meal = t.mealType,
                    items = t.items.split('\n').map(String::trim).filter(String::isNotEmpty),
                    proteinG = t.proteinG, fatG = t.fatG, carbsG = t.carbsG,
                    caloriesKcal = t.caloriesKcal,
                    useCount = t.useCount, archived = t.archived,
                )
            },
            habits = habitDao.getAll().map {
                HabitDto(it.id, it.name, it.targetFrequency, it.active, it.sortOrder)
            },
            habitCompletions = habitDao.getAllCompletions().map {
                HabitCompletionDto(it.id, it.habitId, it.date, it.completed)
            },
        )

        val text = json.encodeToString(dto)
        val file = File(context.cacheDir, "tracker-export-${nowFileStamp()}.json")
        file.writeText(text)

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    private suspend fun buildRoutineDtos(): List<RoutineDto> {
        // No paginated DAO; we read every routine via a one-shot query helper.
        val list = mutableListOf<RoutineDto>()
        // Pull active + archived: use a raw query through routineDao via getById iteration is wasteful;
        // we add a dedicated read in RoutineDao if needed. For now we expose `getAllForExport`.
        return routineDao.getAllForExport().map { r ->
            val items = routineDao.getExercisesFor(r.id).map { link ->
                RoutineExerciseDto(
                    exerciseId = link.exerciseId,
                    orderIndex = link.orderIndex,
                    targetSets = link.targetSets,
                    targetRepRange = link.targetRepRange,
                    targetWeightHintKg = link.targetWeightHintKg,
                    restSeconds = link.restSeconds,
                )
            }
            RoutineDto(
                id = r.id,
                name = r.name,
                dayOfWeek = r.dayOfWeek,
                notes = r.notes,
                createdAt = r.createdAt,
                archived = r.archived,
                exercises = items,
            )
        }.also { list.addAll(it) }
    }

    private fun iso8601Now(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
    private fun nowFileStamp(): String =
        SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
}



