package com.example.personalisedtracker.feature.workout.data.repository

import com.example.personalisedtracker.core.common.DispatcherProvider
import com.example.personalisedtracker.feature.workout.data.dao.ExerciseDao
import com.example.personalisedtracker.feature.workout.data.dao.RoutineDao
import com.example.personalisedtracker.feature.workout.data.dao.WorkoutDao
import com.example.personalisedtracker.feature.workout.data.entity.ExerciseEntity
import com.example.personalisedtracker.feature.workout.data.entity.RoutineExerciseEntity
import com.example.personalisedtracker.feature.workout.data.entity.WorkoutEntity
import com.example.personalisedtracker.feature.workout.data.entity.WorkoutSetEntity
import com.example.personalisedtracker.feature.workout.data.mapper.toDomain
import com.example.personalisedtracker.feature.workout.data.mapper.toEntity
import com.example.personalisedtracker.feature.workout.domain.model.ActiveWorkout
import com.example.personalisedtracker.feature.workout.domain.model.Equipment
import com.example.personalisedtracker.feature.workout.domain.model.Exercise
import com.example.personalisedtracker.feature.workout.domain.model.ExerciseCategory
import com.example.personalisedtracker.feature.workout.domain.model.Routine
import com.example.personalisedtracker.feature.workout.domain.model.RoutineExercise
import com.example.personalisedtracker.feature.workout.domain.model.RoutineWithExercises
import com.example.personalisedtracker.feature.workout.domain.model.Workout
import com.example.personalisedtracker.feature.workout.domain.model.WorkoutExerciseGroup
import com.example.personalisedtracker.feature.workout.domain.model.WorkoutSet
import com.example.personalisedtracker.feature.workout.domain.repository.WorkoutRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Room-backed [WorkoutRepository]. All work happens on [DispatcherProvider.io].
 * Flows are emitted by Room on a background dispatcher already, but we still
 * `flowOn(io)` to be explicit and so [DispatcherProvider] can be swapped in tests.
 */
@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val routineDao: RoutineDao,
    private val workoutDao: WorkoutDao,
    private val dispatchers: DispatcherProvider,
) : WorkoutRepository {

    // ---------- exercises ----------

    override fun observeExercises(): Flow<List<Exercise>> =
        exerciseDao.observeAll()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override suspend fun getExercises(): List<Exercise> = withContext(dispatchers.io) {
        exerciseDao.getAll().map { it.toDomain() }
    }

    override suspend fun addCustomExercise(
        name: String,
        category: ExerciseCategory,
        muscleGroups: List<String>,
        equipment: Equipment,
    ): Long = withContext(dispatchers.io) {
        exerciseDao.insert(
            ExerciseEntity(
                name = name.trim(),
                category = category.name,
                muscleGroups = muscleGroups.joinToString(","),
                equipment = equipment.name,
                isCustom = true,
            )
        )
    }

    // ---------- routines ----------

    override fun observeRoutines(): Flow<List<Routine>> =
        routineDao.observeActive()
            .map { it.map { e -> e.toDomain() } }
            .flowOn(dispatchers.io)

    override suspend fun getRoutineWithExercises(routineId: Long): RoutineWithExercises? =
        withContext(dispatchers.io) {
            val routine = routineDao.getById(routineId)?.toDomain() ?: return@withContext null
            val links = routineDao.getExercisesFor(routineId)
            val exerciseMap = links.mapNotNull { link ->
                exerciseDao.getById(link.exerciseId)?.let { link to it.toDomain() }
            }
            val items = exerciseMap.map { (link, ex) -> link.toDomainRoutineExercise(ex) }
            RoutineWithExercises(routine, items)
        }

    override suspend fun upsertRoutine(
        routine: Routine,
        exercises: List<RoutineExercise>,
    ): Long = withContext(dispatchers.io) {
        val id = routineDao.insert(routine.toEntity())
        val effectiveId = if (routine.id == 0L) id else routine.id
        val entities = exercises.mapIndexed { idx, item ->
            RoutineExerciseEntity(
                id = 0,
                routineId = effectiveId,
                exerciseId = item.exercise.id,
                orderIndex = idx,
                targetSets = item.targetSets,
                targetRepRange = item.targetRepRange,
                targetWeightHintKg = item.targetWeightHintKg,
                restSeconds = item.restSeconds,
            )
        }
        routineDao.replaceRoutineExercises(effectiveId, entities)
        effectiveId
    }

    override suspend fun archiveRoutine(routineId: Long) = withContext(dispatchers.io) {
        routineDao.archive(routineId)
    }

    // ---------- workouts ----------

    override fun observeActiveWorkout(): Flow<Workout?> =
        workoutDao.observeActiveWorkout()
            .map { it?.toDomain() }
            .flowOn(dispatchers.io)

    override fun observeWorkoutHistory(): Flow<List<Workout>> =
        workoutDao.observeHistory()
            .map { it.map { e -> e.toDomain() } }
            .flowOn(dispatchers.io)

    override suspend fun getActiveWorkout(): ActiveWorkout? = withContext(dispatchers.io) {
        val active = workoutDao.getActiveWorkout() ?: return@withContext null
        buildActiveWorkout(active)
    }

    override fun observeActiveWorkoutDetail(workoutId: Long): Flow<ActiveWorkout?> {
        val workoutFlow = workoutDao.observeWorkout(workoutId)
        val setsFlow = workoutDao.observeSetsFor(workoutId)
        return combine(workoutFlow, setsFlow) { w, sets ->
            if (w == null) null else buildActiveWorkoutBlocking(w, sets)
        }.flowOn(dispatchers.io)
    }

    private suspend fun buildActiveWorkout(active: WorkoutEntity): ActiveWorkout {
        val sets = workoutDao.getSetsFor(active.id)
        return buildActiveWorkoutBlocking(active, sets)
    }

    /** Pulls exercises referenced by the current sets + the originating routine. */
    private suspend fun buildActiveWorkoutBlocking(
        w: WorkoutEntity,
        sets: List<WorkoutSetEntity>,
    ): ActiveWorkout {
        val routineLinks = w.routineId?.let { routineDao.getExercisesFor(it) }.orEmpty()
        val orderMap = LinkedHashMap<Long, Int>()
        routineLinks.forEach { orderMap.putIfAbsent(it.exerciseId, it.orderIndex) }
        // Include exercises added mid-workout (not in routine)
        sets.forEach { s ->
            orderMap.putIfAbsent(s.exerciseId, (orderMap.values.maxOrNull() ?: -1) + 1)
        }
        // Resolve every exercise in one go
        val exercises: Map<Long, Exercise> = orderMap.keys
            .mapNotNull { id -> exerciseDao.getById(id)?.toDomain() }
            .associateBy { it.id }

        val groups = orderMap.entries
            .sortedBy { it.value }
            .mapNotNull { (exId, orderIdx) ->
                val ex = exercises[exId] ?: return@mapNotNull null
                val link = routineLinks.firstOrNull { it.exerciseId == exId }
                WorkoutExerciseGroup(
                    exercise = ex,
                    targetSets = link?.targetSets ?: 3,
                    targetRepRange = link?.targetRepRange ?: "8-12",
                    restSeconds = link?.restSeconds ?: 90,
                    sets = sets.filter { it.exerciseId == exId }
                        .sortedBy { it.setNumber }
                        .map { it.toDomain() },
                    orderIndex = orderIdx,
                )
            }
        return ActiveWorkout(workout = w.toDomain(), groups = groups)
    }

    override suspend fun startWorkoutFromRoutine(routineId: Long): Long = withContext(dispatchers.io) {
        val routine = routineDao.getById(routineId)
            ?: error("Routine $routineId not found")
        val now = System.currentTimeMillis()
        workoutDao.insertWorkout(
            WorkoutEntity(
                routineId = routineId,
                routineNameSnapshot = routine.name,
                startedAt = now,
            )
        )
    }

    override suspend fun startAdHocWorkout(name: String): Long = withContext(dispatchers.io) {
        workoutDao.insertWorkout(
            WorkoutEntity(
                routineId = null,
                routineNameSnapshot = name.ifBlank { "Quick workout" },
                startedAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun addSet(
        workoutId: Long,
        exerciseId: Long,
        setNumber: Int,
        reps: Int,
        weightKg: Double,
        isWarmup: Boolean,
        orderIndex: Int,
    ): Long = withContext(dispatchers.io) {
        workoutDao.insertSet(
            WorkoutSetEntity(
                workoutId = workoutId,
                exerciseId = exerciseId,
                setNumber = setNumber,
                reps = reps,
                weightKg = weightKg,
                isWarmup = isWarmup,
                completed = true,
                orderIndex = orderIndex,
            )
        )
    }

    override suspend fun updateSet(set: WorkoutSet) = withContext(dispatchers.io) {
        workoutDao.updateSet(set.toEntity())
    }

    override suspend fun deleteSet(setId: Long) = withContext(dispatchers.io) {
        workoutDao.deleteSet(setId)
    }

    override suspend fun finishWorkout(workoutId: Long, notes: String) = withContext(dispatchers.io) {
        val w = workoutDao.getWorkout(workoutId) ?: return@withContext
        workoutDao.updateWorkout(
            w.copy(endedAt = System.currentTimeMillis(), notes = notes)
        )
    }

    override suspend fun discardWorkout(workoutId: Long) = withContext(dispatchers.io) {
        workoutDao.deleteWorkout(workoutId)
    }

    override suspend fun addExerciseToActiveWorkout(
        workoutId: Long,
        exerciseId: Long,
    ): Int = withContext(dispatchers.io) {
        // The exercise has no sets yet — we just insert a placeholder warmup-flag-false set?
        // Instead, we record nothing here; the UI prompts the user to add a set, which
        // will assign a fresh orderIndex via [nextOrderIndex].
        nextOrderIndex(workoutId)
    }

    /** Returns the next free orderIndex for an exercise group inside [workoutId]. */
    private suspend fun nextOrderIndex(workoutId: Long): Int {
        val sets = workoutDao.getSetsFor(workoutId)
        return (sets.maxOfOrNull { it.orderIndex } ?: -1) + 1
    }

    override suspend fun lastSetFor(exerciseId: Long): WorkoutSet? = withContext(dispatchers.io) {
        workoutDao.getRecentSetsForExercise(exerciseId, limit = 1)
            .firstOrNull()
            ?.toDomain()
    }
}

/** Local helper because mapper needs a resolved [Exercise]. */
private fun RoutineExerciseEntity.toDomainRoutineExercise(exercise: Exercise): RoutineExercise =
    RoutineExercise(
        id = id,
        routineId = routineId,
        exercise = exercise,
        orderIndex = orderIndex,
        targetSets = targetSets,
        targetRepRange = targetRepRange,
        targetWeightHintKg = targetWeightHintKg,
        restSeconds = restSeconds,
    )




