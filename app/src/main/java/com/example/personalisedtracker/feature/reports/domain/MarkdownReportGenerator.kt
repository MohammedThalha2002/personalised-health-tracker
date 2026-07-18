package com.example.personalisedtracker.feature.reports.domain

import com.example.personalisedtracker.core.common.DateInt
import com.example.personalisedtracker.feature.body.data.dao.BodyWeightDao
import com.example.personalisedtracker.feature.body.data.dao.InBodyScanDao
import com.example.personalisedtracker.feature.body.data.dao.WaistMeasurementDao
import com.example.personalisedtracker.feature.food.data.dao.FoodDao
import com.example.personalisedtracker.feature.habit.data.dao.HabitDao
import com.example.personalisedtracker.feature.sleep.data.dao.SleepDao
import com.example.personalisedtracker.feature.workout.data.dao.ExerciseDao
import com.example.personalisedtracker.feature.workout.data.dao.WorkoutDao
import com.example.personalisedtracker.feature.workout.data.entity.WorkoutEntity
import com.example.personalisedtracker.feature.workout.data.entity.WorkoutSetEntity
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Generates the human + AI-readable Markdown report. Pure computation — every
 * input is a one-shot suspending DAO query, no Flows. Run on IO dispatcher.
 */
@Singleton
class MarkdownReportGenerator @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val bodyWeightDao: BodyWeightDao,
    private val inBodyScanDao: InBodyScanDao,
    private val waistDao: WaistMeasurementDao,
    private val sleepDao: SleepDao,
    private val foodDao: FoodDao,
    private val habitDao: HabitDao,
) {

    suspend fun generate(today: Int = DateInt.today(), days: Long): String {
        val since = DateInt.minusDays(today, days)
        val sinceMs = DateInt.toLocalDate(since).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val sb = StringBuilder()

        sb.appendLine("# Health & Workout Report")
        sb.appendLine("**Period**: ${DateInt.format(since, "dd MMM yyyy")} – ${DateInt.format(today, "dd MMM yyyy")}")
        sb.appendLine("**Generated**: ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}")
        sb.appendLine()

        // ---------- Body ----------
        val weights = bodyWeightDao.getAll().filter { it.date in since..today }
        val scans = inBodyScanDao.getAll().filter { it.date in since..today }.sortedBy { it.date }
        val waists = waistDao.getAll().filter { it.date in since..today }
        if (weights.isNotEmpty() || scans.isNotEmpty()) {
            sb.appendLine("## Body Composition")
            val firstW = weights.minByOrNull { it.date }?.weightKg
            val lastW = weights.maxByOrNull { it.date }?.weightKg
            if (firstW != null && lastW != null) {
                sb.appendLine("- Weight: ${firstW.f1()} kg → ${lastW.f1()} kg (${(lastW - firstW).fSign()} kg)")
            }
            scans.lastOrNull()?.let { s ->
                s.bodyFatPercent?.let { sb.appendLine("- Body fat %: ${it.f1()}% (last InBody: ${DateInt.format(s.date)})") }
                s.skeletalMuscleMassKg?.let { sb.appendLine("- Muscle mass: ${it.f1()} kg") }
                s.inbodyScore?.let { sb.appendLine("- InBody score: ${it.toInt()}") }
            }
            val firstWaist = waists.minByOrNull { it.date }?.waistCm
            val lastWaist = waists.maxByOrNull { it.date }?.waistCm
            if (firstWaist != null && lastWaist != null) {
                sb.appendLine("- Waist: ${firstWaist.f1()} cm → ${lastWaist.f1()} cm (${(lastWaist - firstWaist).fSign()} cm)")
            }
            sb.appendLine()
        }

        // ---------- Workouts ----------
        val workouts = workoutDao.getAllForExport()
            .filter { it.endedAt != null && it.startedAt >= sinceMs }
        val sets = workoutDao.getAllSetsForExport()
        val exercisesById = exerciseDao.getAll().associateBy { it.id }
        if (workouts.isNotEmpty()) {
            sb.appendLine("## Workouts (${workouts.size} session${if (workouts.size == 1) "" else "s"})")
            val byRoutine = workouts.groupBy { it.routineNameSnapshot }
            for ((routineName, routineSessions) in byRoutine.toSortedMap()) {
                sb.appendLine("### $routineName (${routineSessions.size} session${if (routineSessions.size == 1) "" else "s"})")
                val setsForRoutine = sets.filter { s -> routineSessions.any { it.id == s.workoutId } }
                val progressed = computeProgression(setsForRoutine, exercisesById)
                progressed.take(10).forEach { sb.appendLine("- $it") }
                sb.appendLine()
            }
        }

        // ---------- Sleep ----------
        val sleep = sleepDao.getAll().filter { it.date in since..today }
        if (sleep.isNotEmpty()) {
            sb.appendLine("## Sleep")
            val avg = sleep.map { it.hours }.average()
            val best = sleep.maxByOrNull { it.hours }
            val worst = sleep.minByOrNull { it.hours }
            val q = sleep.map { it.quality1to5 }.average()
            sb.appendLine("- Average: ${avg.f1()} hrs/night (over ${sleep.size} nights)")
            best?.let { sb.appendLine("- Best: ${it.hours.f1()} hrs (${DateInt.format(it.date)})") }
            worst?.let { sb.appendLine("- Worst: ${it.hours.f1()} hrs (${DateInt.format(it.date)})") }
            sb.appendLine("- Quality avg: ${q.f1()}/5")
            sb.appendLine()
        }

        // ---------- Habits ----------
        val habits = habitDao.getAll().filter { it.active }
        val totalDays = (days.coerceAtLeast(1)).toInt() + 1
        if (habits.isNotEmpty()) {
            sb.appendLine("## Habits (% completed)")
            val completions = habitDao.completionsSince(since).filter { it.completed }
            val byHabit = completions.groupBy { it.habitId }
            for (h in habits) {
                val done = byHabit[h.id]?.size ?: 0
                val pct = ((done.toFloat() / totalDays) * 100).toInt().coerceIn(0, 100)
                sb.appendLine("- ${h.name}: $pct%")
            }
            sb.appendLine()
        }

        // ---------- Food ----------
        val food = foodDao.getAll().filter { it.date in since..today }
        if (food.isNotEmpty()) {
            sb.appendLine("## Food")
            val daysWithLog = food.map { it.date }.distinct().size
            val byDay = food.groupBy { it.date }
            val avgKcal = byDay.values.map { day -> day.sumOf { it.approxCalories } }.average()
            val avgProtein = byDay.values.map { day -> day.sumOf { it.approxProteinG } }.average()
            val avgFat = byDay.values.map { day -> day.sumOf { it.fatG } }.average()
            val avgCarbs = byDay.values.map { day -> day.sumOf { it.carbsG } }.average()
            sb.appendLine("- Logged: $daysWithLog of $totalDays days")
            sb.appendLine("- Average calories: ${avgKcal.toInt()} kcal")
            sb.appendLine("- Average protein: ${avgProtein.toInt()} g")
            sb.appendLine("- Average fat: ${avgFat.toInt()} g")
            sb.appendLine("- Average carbs: ${avgCarbs.toInt()} g")

            // Most-used templates (gives the AI meal-pattern context)
            val byTemplate = food.filter { it.templateId != null }.groupBy { it.description }
            if (byTemplate.isNotEmpty()) {
                sb.appendLine("- Top meals:")
                byTemplate.entries
                    .sortedByDescending { it.value.size }
                    .take(5)
                    .forEach { (name, list) ->
                        sb.appendLine("  - $name × ${list.size}")
                    }
            }
            sb.appendLine()
        }

        // ---------- Observations ----------
        sb.appendLine("## Observations")
        sb.appendLine("- Data window: $totalDays days, ${workouts.size} workouts logged, ${sleep.size} sleep entries, ${food.size} food entries, ${weights.size} weight entries.")
        return sb.toString()
    }

    /**
     * Per-exercise first→last (max-weight × reps) summary. Cheap heuristic
     * — picks the heaviest working set per session for each exercise and
     * compares the first session to the last.
     */
    private fun computeProgression(
        sets: List<WorkoutSetEntity>,
        exercises: Map<Long, com.example.personalisedtracker.feature.workout.data.entity.ExerciseEntity>,
    ): List<String> {
        val byExercise = sets.filter { !it.isWarmup && it.completed }.groupBy { it.exerciseId }
        return byExercise.mapNotNull { (exId, exSets) ->
            val ex = exercises[exId] ?: return@mapNotNull null
            // Group sets by workout, pick top set per workout, then sort by workout.
            val perWorkout = exSets.groupBy { it.workoutId }
                .map { (_, list) -> list.maxBy { it.weightKg * 1000 + it.reps } }
                .sortedBy { it.workoutId }
            if (perWorkout.size < 2) return@mapNotNull "${ex.name}: ${perWorkout.size} session"
            val first = perWorkout.first()
            val last = perWorkout.last()
            val arrow = if (last.weightKg > first.weightKg || (last.weightKg == first.weightKg && last.reps > first.reps)) "↑" else "→"
            "${ex.name}: ${first.reps}×${first.weightKg.f1()}kg → ${last.reps}×${last.weightKg.f1()}kg $arrow"
        }.sorted()
    }
}

private fun Double.f1(): String = "%.1f".format(this)
private fun Double.fSign(): String = if (this >= 0) "+${f1()}" else f1()

