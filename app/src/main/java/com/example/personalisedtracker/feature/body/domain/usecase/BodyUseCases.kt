package com.example.personalisedtracker.feature.body.domain.usecase

import com.example.personalisedtracker.feature.body.domain.model.BodyWeight
import com.example.personalisedtracker.feature.body.domain.model.InBodyScan
import com.example.personalisedtracker.feature.body.domain.model.WaistMeasurement
import com.example.personalisedtracker.feature.body.domain.repository.BodyRepository
import com.example.personalisedtracker.feature.body.domain.repository.InBodyImportPreview
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveWeightsSinceUseCase @Inject constructor(private val repo: BodyRepository) {
    operator fun invoke(since: Int): Flow<List<BodyWeight>> = repo.observeWeightsSince(since)
}
class ObserveWaistUseCase @Inject constructor(private val repo: BodyRepository) {
    operator fun invoke(): Flow<List<WaistMeasurement>> = repo.observeWaist()
}
class ObserveLatestScanUseCase @Inject constructor(private val repo: BodyRepository) {
    operator fun invoke(): Flow<InBodyScan?> = repo.observeLatestScan()
}
class LogWeightUseCase @Inject constructor(private val repo: BodyRepository) {
    suspend operator fun invoke(date: Int, kg: Double, note: String = "") = repo.logWeight(date, kg, note)
}
class LogWaistUseCase @Inject constructor(private val repo: BodyRepository) {
    suspend operator fun invoke(date: Int, cm: Double) = repo.logWaist(date, cm)
}
class PreviewInBodyCsvUseCase @Inject constructor(private val repo: BodyRepository) {
    suspend operator fun invoke(text: String): InBodyImportPreview = repo.previewInBodyCsv(text)
}
class CommitInBodyImportUseCase @Inject constructor(private val repo: BodyRepository) {
    suspend operator fun invoke(rows: List<InBodyScan>): Int = repo.importInBodyScans(rows)
}

