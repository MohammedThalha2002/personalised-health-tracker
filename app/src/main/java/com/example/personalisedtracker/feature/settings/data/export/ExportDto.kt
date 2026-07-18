package com.example.personalisedtracker.feature.settings.data.export

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Versioned JSON envelope written by [JsonExporter] / read by [JsonImporter].
 * Body / sleep / food / habits sections are reserved for later phases — they're
 * declared optional now so a Phase 1 export can already be parsed by a future
 * Phase 2+ build without crashing.
 */
@Serializable
data class TrackerExportDto(
    @SerialName("exported_at") val exportedAt: String,
    @SerialName("app_version") val appVersion: String,
    @SerialName("schema_version") val schemaVersion: Int = 3,
    val workouts: List<WorkoutDto> = emptyList(),
    val exercises: List<ExerciseDto> = emptyList(),
    val routines: List<RoutineDto> = emptyList(),
    @SerialName("body_weights") val bodyWeights: List<BodyWeightDto> = emptyList(),
    @SerialName("inbody_scans") val inbodyScans: List<InBodyScanDto> = emptyList(),
    @SerialName("waist_measurements") val waistMeasurements: List<WaistDto> = emptyList(),
    @SerialName("sleep_entries") val sleepEntries: List<SleepDto> = emptyList(),
    @SerialName("food_entries") val foodEntries: List<FoodDto> = emptyList(),
    @SerialName("meal_templates") val mealTemplates: List<MealTemplateDto> = emptyList(),
    @SerialName("habits") val habits: List<HabitDto> = emptyList(),
    @SerialName("habit_completions") val habitCompletions: List<HabitCompletionDto> = emptyList(),
)

@Serializable
class JsonStubDto

@Serializable
data class BodyWeightDto(
    val id: Long, val date: Int,
    @SerialName("weight_kg") val weightKg: Double,
    val note: String,
)

@Serializable
data class WaistDto(
    val id: Long, val date: Int,
    @SerialName("waist_cm") val waistCm: Double,
)

@Serializable
data class InBodyScanDto(
    val id: Long, val date: Int,
    @SerialName("raw_timestamp") val rawTimestamp: Long,
    @SerialName("weight_kg") val weightKg: Double,
    @SerialName("skeletal_muscle_mass_kg") val skeletalMuscleMassKg: Double?,
    @SerialName("body_fat_mass_kg") val bodyFatMassKg: Double?,
    val bmi: Double?,
    @SerialName("body_fat_percent") val bodyFatPercent: Double?,
    @SerialName("basal_metabolic_rate_kcal") val basalMetabolicRateKcal: Double?,
    @SerialName("inbody_score") val inbodyScore: Double?,
    @SerialName("visceral_fat_level") val visceralFatLevel: Double?,
    @SerialName("waist_hip_ratio") val waistHipRatio: Double?,
    @SerialName("waist_circumference_cm") val waistCircumferenceCm: Double?,
    @SerialName("trunk_fat_kg") val trunkFatKg: Double?,
    @SerialName("smi_kg_per_m2") val smiKgPerM2: Double?,
)

@Serializable
data class SleepDto(
    val id: Long, val date: Int, val hours: Double,
    @SerialName("quality_1_to_5") val quality: Int,
    val note: String,
)

@Serializable
data class FoodDto(
    val id: Long, val date: Int,
    @SerialName("meal_type") val meal: String,
    val description: String,
    @SerialName("approx_calories") val calories: Int,
    @SerialName("approx_protein_g") val protein: Int,
    @SerialName("fat_g") val fatG: Int = 0,
    @SerialName("carbs_g") val carbsG: Int = 0,
    @SerialName("template_id") val templateId: Long? = null,
)

@Serializable
data class MealTemplateDto(
    val id: Long,
    val name: String,
    @SerialName("meal_type") val meal: String,
    val items: List<String> = emptyList(),
    @SerialName("protein_g") val proteinG: Int,
    @SerialName("fat_g") val fatG: Int,
    @SerialName("carbs_g") val carbsG: Int,
    @SerialName("calories_kcal") val caloriesKcal: Int,
    @SerialName("use_count") val useCount: Int = 0,
    val archived: Boolean = false,
)

@Serializable
data class HabitDto(
    val id: Long, val name: String,
    @SerialName("target_frequency") val frequency: String,
    val active: Boolean,
    @SerialName("sort_order") val sortOrder: Int,
)

@Serializable
data class HabitCompletionDto(
    val id: Long,
    @SerialName("habit_id") val habitId: Long,
    val date: Int,
    val completed: Boolean,
)

@Serializable
data class ExerciseDto(
    val id: Long,
    val name: String,
    val category: String,
    @SerialName("muscle_groups") val muscleGroups: List<String>,
    val equipment: String,
    @SerialName("is_custom") val isCustom: Boolean,
)

@Serializable
data class RoutineDto(
    val id: Long,
    val name: String,
    @SerialName("day_of_week") val dayOfWeek: Int?,
    val notes: String,
    @SerialName("created_at") val createdAt: Long,
    val archived: Boolean,
    val exercises: List<RoutineExerciseDto>,
)

@Serializable
data class RoutineExerciseDto(
    @SerialName("exercise_id") val exerciseId: Long,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("target_sets") val targetSets: Int,
    @SerialName("target_rep_range") val targetRepRange: String,
    @SerialName("target_weight_hint_kg") val targetWeightHintKg: Double?,
    @SerialName("rest_seconds") val restSeconds: Int,
)

@Serializable
data class WorkoutDto(
    val id: Long,
    @SerialName("routine_id") val routineId: Long?,
    @SerialName("routine_name") val routineName: String,
    @SerialName("started_at") val startedAt: Long,
    @SerialName("ended_at") val endedAt: Long?,
    val notes: String,
    val sets: List<WorkoutSetDto>,
)

@Serializable
data class WorkoutSetDto(
    val id: Long,
    @SerialName("exercise_id") val exerciseId: Long,
    @SerialName("set_number") val setNumber: Int,
    val reps: Int,
    @SerialName("weight_kg") val weightKg: Double,
    val rpe: Int?,
    @SerialName("is_warmup") val isWarmup: Boolean,
    val completed: Boolean,
    @SerialName("order_index") val orderIndex: Int,
)

