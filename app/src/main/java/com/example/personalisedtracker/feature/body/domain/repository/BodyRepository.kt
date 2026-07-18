package com.example.personalisedtracker.feature.body.domain.repository

import com.example.personalisedtracker.feature.body.domain.model.BodyWeight
import com.example.personalisedtracker.feature.body.domain.model.InBodyScan
import com.example.personalisedtracker.feature.body.domain.model.WaistMeasurement
import kotlinx.coroutines.flow.Flow

interface BodyRepository {
    // weight
    fun observeWeights(): Flow<List<BodyWeight>>
    fun observeWeightsSince(since: Int): Flow<List<BodyWeight>>
    suspend fun logWeight(date: Int, kg: Double, note: String = "")
    suspend fun deleteWeight(id: Long)

    // waist
    fun observeWaist(): Flow<List<WaistMeasurement>>
    suspend fun logWaist(date: Int, cm: Double)
    suspend fun deleteWaist(id: Long)

    // InBody
    fun observeScans(): Flow<List<InBodyScan>>
    fun observeLatestScan(): Flow<InBodyScan?>

    /** Parses InBody CSV bytes (utf-8). Returns the rows that *would* be
     *  imported (excluding duplicates). The UI shows this for confirmation. */
    suspend fun previewInBodyCsv(text: String): InBodyImportPreview

    /** Commits a previously previewed import. */
    suspend fun importInBodyScans(rows: List<InBodyScan>): Int
}

data class InBodyImportPreview(
    val newRows: List<InBodyScan>,
    val duplicateDates: List<Int>,
    val parseErrors: List<String>,
)

