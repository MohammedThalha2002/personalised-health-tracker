package com.example.personalisedtracker.feature.body.data.repository

import com.example.personalisedtracker.core.common.DispatcherProvider
import com.example.personalisedtracker.feature.body.data.csv.InBodyCsvParser
import com.example.personalisedtracker.feature.body.data.dao.BodyWeightDao
import com.example.personalisedtracker.feature.body.data.dao.InBodyScanDao
import com.example.personalisedtracker.feature.body.data.dao.WaistMeasurementDao
import com.example.personalisedtracker.feature.body.data.entity.BodyWeightEntity
import com.example.personalisedtracker.feature.body.data.entity.WaistMeasurementEntity
import com.example.personalisedtracker.feature.body.data.mapper.toDomain
import com.example.personalisedtracker.feature.body.domain.model.BodyWeight
import com.example.personalisedtracker.feature.body.domain.model.InBodyScan
import com.example.personalisedtracker.feature.body.domain.model.WaistMeasurement
import com.example.personalisedtracker.feature.body.domain.repository.BodyRepository
import com.example.personalisedtracker.feature.body.domain.repository.InBodyImportPreview
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class BodyRepositoryImpl @Inject constructor(
    private val weightDao: BodyWeightDao,
    private val waistDao: WaistMeasurementDao,
    private val scanDao: InBodyScanDao,
    private val dispatchers: DispatcherProvider,
) : BodyRepository {

    /** Stash of parsed (but not yet committed) entities, keyed by yyyyMMdd. */
    private val parseCache =
        mutableMapOf<Int, com.example.personalisedtracker.feature.body.data.entity.InBodyScanEntity>()

    override fun observeWeights(): Flow<List<BodyWeight>> =
        weightDao.observeAll().map { it.map { e -> e.toDomain() } }.flowOn(dispatchers.io)

    override fun observeWeightsSince(since: Int): Flow<List<BodyWeight>> =
        weightDao.observeSince(since).map { it.map { e -> e.toDomain() } }.flowOn(dispatchers.io)

    override suspend fun logWeight(date: Int, kg: Double, note: String) = withContext(dispatchers.io) {
        val existing = weightDao.getForDate(date)
        weightDao.upsert(
            BodyWeightEntity(id = existing?.id ?: 0, date = date, weightKg = kg, note = note)
        )
        Unit
    }

    override suspend fun deleteWeight(id: Long) = withContext(dispatchers.io) { weightDao.delete(id) }

    override fun observeWaist(): Flow<List<WaistMeasurement>> =
        waistDao.observeAll().map { it.map { e -> e.toDomain() } }.flowOn(dispatchers.io)

    override suspend fun logWaist(date: Int, cm: Double) = withContext(dispatchers.io) {
        waistDao.upsert(WaistMeasurementEntity(date = date, waistCm = cm))
        Unit
    }

    override suspend fun deleteWaist(id: Long) = withContext(dispatchers.io) { waistDao.delete(id) }

    override fun observeScans(): Flow<List<InBodyScan>> =
        scanDao.observeAll().map { it.map { e -> e.toDomain() } }.flowOn(dispatchers.io)

    override fun observeLatestScan(): Flow<InBodyScan?> =
        scanDao.observeLatest().map { it?.toDomain() }.flowOn(dispatchers.io)

    override suspend fun previewInBodyCsv(text: String): InBodyImportPreview =
        withContext(dispatchers.io) {
            val parsed = InBodyCsvParser.parse(text)
            val existing = scanDao.existingDates().toSet()
            val (dup, fresh) = parsed.rows.partition { it.date in existing }
            parseCache.clear()
            fresh.forEach { parseCache[it.date] = it }
            InBodyImportPreview(
                newRows = fresh.map { it.toDomain() },
                duplicateDates = dup.map { it.date },
                parseErrors = parsed.errors,
            )
        }

    override suspend fun importInBodyScans(rows: List<InBodyScan>): Int = withContext(dispatchers.io) {
        val entities = rows.mapNotNull { parseCache[it.date] }
        val inserted = scanDao.insertAll(entities).count { it >= 0 }
        parseCache.clear()
        inserted
    }
}



