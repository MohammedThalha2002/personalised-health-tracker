package com.example.personalisedtracker.feature.habit.data.repository

import com.example.personalisedtracker.core.common.DateInt
import com.example.personalisedtracker.core.common.DispatcherProvider
import com.example.personalisedtracker.feature.habit.data.dao.HabitDao
import com.example.personalisedtracker.feature.habit.data.entity.HabitCompletionEntity
import com.example.personalisedtracker.feature.habit.data.entity.HabitEntity
import com.example.personalisedtracker.feature.habit.domain.model.Habit
import com.example.personalisedtracker.feature.habit.domain.model.HabitCompletion
import com.example.personalisedtracker.feature.habit.domain.model.HabitFrequency
import com.example.personalisedtracker.feature.habit.domain.model.HabitWithProgress
import com.example.personalisedtracker.feature.habit.domain.repository.HabitRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private fun HabitEntity.toDomain() = Habit(
    id = id,
    name = name,
    frequency = runCatching { HabitFrequency.valueOf(targetFrequency) }
        .getOrDefault(HabitFrequency.DAILY),
    active = active,
    sortOrder = sortOrder,
)

private fun HabitCompletionEntity.toDomain() = HabitCompletion(id, habitId, date, completed)

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao,
    private val dispatchers: DispatcherProvider,
) : HabitRepository {

    override fun observeActiveWithProgress(today: Int): Flow<List<HabitWithProgress>> {
        val since = DateInt.minusDays(today, 30)
        return combine(
            dao.observeActive(),
            dao.observeForDate(today),
        ) { habits, todayCompletions ->
            val completionMap = todayCompletions.associateBy { it.habitId }
            // Pull recent completions for streaks/rate in one batched suspending call
            val recentByHabit = dao.completionsSince(since).groupBy { it.habitId }
            habits.map { h ->
                val recent = recentByHabit[h.id].orEmpty().filter { it.completed }
                    .map { it.date }.toSet()
                HabitWithProgress(
                    habit = h.toDomain(),
                    doneToday = completionMap[h.id]?.completed == true,
                    currentStreak = streak(today, recent),
                    completionRate30d = (recent.size / 30f).coerceIn(0f, 1f),
                )
            }
        }.flowOn(dispatchers.io)
    }

    private fun streak(today: Int, doneDates: Set<Int>): Int {
        var s = 0
        var d = today
        while (d in doneDates) {
            s++
            d = DateInt.minusDays(d, 1)
        }
        return s
    }

    override fun observeAll(): Flow<List<Habit>> =
        dao.observeAll().map { it.map(HabitEntity::toDomain) }.flowOn(dispatchers.io)

    override suspend fun upsert(
        id: Long,
        name: String,
        frequency: HabitFrequency,
        sortOrder: Int,
    ): Long = withContext(dispatchers.io) {
        dao.upsert(
            HabitEntity(
                id = id,
                name = name.trim(),
                targetFrequency = frequency.name,
                active = true,
                sortOrder = sortOrder,
            )
        )
    }

    override suspend fun archive(id: Long) = withContext(dispatchers.io) { dao.archive(id) }

    override suspend fun setCompletion(habitId: Long, date: Int, completed: Boolean) =
        withContext(dispatchers.io) {
            dao.upsertCompletion(
                HabitCompletionEntity(habitId = habitId, date = date, completed = completed)
            )
            Unit
        }

    override suspend fun seedDefaults(defaults: List<String>) = withContext(dispatchers.io) {
        if (dao.count() > 0) return@withContext
        dao.insertAll(
            defaults.mapIndexed { idx, name ->
                HabitEntity(
                    name = name,
                    targetFrequency = HabitFrequency.DAILY.name,
                    sortOrder = idx,
                )
            }
        )
        Unit
    }

    override fun observeCompletionsSince(since: Int): Flow<List<HabitCompletion>> =
        // We re-use observeForDate for today's UI; for "since" we'd want a Flow query —
        // synthesise via observeAll + filter to keep DAO surface small.
        dao.observeActive() // trigger emissions on habit changes too
            .map { _ -> dao.completionsSince(since).map(HabitCompletionEntity::toDomain) }
            .flowOn(dispatchers.io)
}

