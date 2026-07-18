package com.example.personalisedtracker.feature.sleep.domain.repository

import com.example.personalisedtracker.feature.sleep.domain.model.SleepEntry
import kotlinx.coroutines.flow.Flow

interface SleepRepository {
    fun observeAll(): Flow<List<SleepEntry>>
    fun observeSince(since: Int): Flow<List<SleepEntry>>
    suspend fun upsert(date: Int, hours: Double, quality1to5: Int, note: String = "")
    suspend fun delete(id: Long)
}

