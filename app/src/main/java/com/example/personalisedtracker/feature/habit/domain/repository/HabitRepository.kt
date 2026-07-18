package com.example.personalisedtracker.feature.habit.domain.repository

import com.example.personalisedtracker.feature.habit.domain.model.Habit
import com.example.personalisedtracker.feature.habit.domain.model.HabitCompletion
import com.example.personalisedtracker.feature.habit.domain.model.HabitFrequency
import com.example.personalisedtracker.feature.habit.domain.model.HabitWithProgress
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun observeActiveWithProgress(today: Int): Flow<List<HabitWithProgress>>
    fun observeAll(): Flow<List<Habit>>
    suspend fun upsert(
        id: Long = 0,
        name: String,
        frequency: HabitFrequency = HabitFrequency.DAILY,
        sortOrder: Int = 0,
    ): Long
    suspend fun archive(id: Long)
    suspend fun setCompletion(habitId: Long, date: Int, completed: Boolean)
    suspend fun seedDefaults(defaults: List<String>)
    fun observeCompletionsSince(since: Int): Flow<List<HabitCompletion>>
}

