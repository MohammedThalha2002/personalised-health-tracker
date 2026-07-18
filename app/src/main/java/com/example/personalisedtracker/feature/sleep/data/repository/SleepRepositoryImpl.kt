package com.example.personalisedtracker.feature.sleep.data.repository

import com.example.personalisedtracker.core.common.DispatcherProvider
import com.example.personalisedtracker.feature.sleep.data.dao.SleepDao
import com.example.personalisedtracker.feature.sleep.data.entity.SleepEntryEntity
import com.example.personalisedtracker.feature.sleep.domain.model.SleepEntry
import com.example.personalisedtracker.feature.sleep.domain.repository.SleepRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private fun SleepEntryEntity.toDomain() = SleepEntry(id, date, hours, quality1to5, note)

@Singleton
class SleepRepositoryImpl @Inject constructor(
    private val dao: SleepDao,
    private val dispatchers: DispatcherProvider,
) : SleepRepository {

    override fun observeAll(): Flow<List<SleepEntry>> =
        dao.observeAll().map { it.map(SleepEntryEntity::toDomain) }.flowOn(dispatchers.io)

    override fun observeSince(since: Int): Flow<List<SleepEntry>> =
        dao.observeSince(since).map { it.map(SleepEntryEntity::toDomain) }.flowOn(dispatchers.io)

    override suspend fun upsert(date: Int, hours: Double, quality1to5: Int, note: String) =
        withContext(dispatchers.io) {
            val existing = dao.getForDate(date)
            dao.upsert(
                SleepEntryEntity(
                    id = existing?.id ?: 0,
                    date = date,
                    hours = hours,
                    quality1to5 = quality1to5.coerceIn(1, 5),
                    note = note,
                )
            )
            Unit
        }

    override suspend fun delete(id: Long) = withContext(dispatchers.io) { dao.delete(id) }
}

