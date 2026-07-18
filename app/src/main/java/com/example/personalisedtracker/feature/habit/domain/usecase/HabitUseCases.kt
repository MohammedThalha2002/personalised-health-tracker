package com.example.personalisedtracker.feature.habit.domain.usecase

import com.example.personalisedtracker.feature.habit.domain.model.HabitWithProgress
import com.example.personalisedtracker.feature.habit.domain.repository.HabitRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveHabitsWithProgressUseCase @Inject constructor(private val repo: HabitRepository) {
    operator fun invoke(today: Int): Flow<List<HabitWithProgress>> =
        repo.observeActiveWithProgress(today)
}
class SetHabitCompletionUseCase @Inject constructor(private val repo: HabitRepository) {
    suspend operator fun invoke(habitId: Long, date: Int, completed: Boolean) =
        repo.setCompletion(habitId, date, completed)
}
class UpsertHabitUseCase @Inject constructor(private val repo: HabitRepository) {
    suspend operator fun invoke(id: Long, name: String, sortOrder: Int): Long =
        repo.upsert(id = id, name = name, sortOrder = sortOrder)
}
class ArchiveHabitUseCase @Inject constructor(private val repo: HabitRepository) {
    suspend operator fun invoke(id: Long) = repo.archive(id)
}

