package com.example.personalisedtracker.feature.sleep.domain.usecase

import com.example.personalisedtracker.feature.sleep.domain.model.SleepEntry
import com.example.personalisedtracker.feature.sleep.domain.repository.SleepRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveSleepSinceUseCase @Inject constructor(private val repo: SleepRepository) {
    operator fun invoke(since: Int): Flow<List<SleepEntry>> = repo.observeSince(since)
}
class LogSleepUseCase @Inject constructor(private val repo: SleepRepository) {
    suspend operator fun invoke(date: Int, hours: Double, quality1to5: Int, note: String = "") =
        repo.upsert(date, hours, quality1to5, note)
}
class DeleteSleepUseCase @Inject constructor(private val repo: SleepRepository) {
    suspend operator fun invoke(id: Long) = repo.delete(id)
}

