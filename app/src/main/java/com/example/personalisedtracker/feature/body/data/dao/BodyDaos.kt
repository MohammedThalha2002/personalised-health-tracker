package com.example.personalisedtracker.feature.body.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.personalisedtracker.feature.body.data.entity.BodyWeightEntity
import com.example.personalisedtracker.feature.body.data.entity.InBodyScanEntity
import com.example.personalisedtracker.feature.body.data.entity.WaistMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyWeightDao {
    @Query("SELECT * FROM body_weights ORDER BY date DESC")
    fun observeAll(): Flow<List<BodyWeightEntity>>

    @Query("SELECT * FROM body_weights WHERE date >= :since ORDER BY date ASC")
    fun observeSince(since: Int): Flow<List<BodyWeightEntity>>

    @Query("SELECT * FROM body_weights WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: Int): BodyWeightEntity?

    @Query("SELECT * FROM body_weights ORDER BY date DESC")
    suspend fun getAll(): List<BodyWeightEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: BodyWeightEntity): Long

    @Query("DELETE FROM body_weights WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<BodyWeightEntity>)
}

@Dao
interface WaistMeasurementDao {
    @Query("SELECT * FROM waist_measurements ORDER BY date DESC")
    fun observeAll(): Flow<List<WaistMeasurementEntity>>

    @Query("SELECT * FROM waist_measurements ORDER BY date DESC")
    suspend fun getAll(): List<WaistMeasurementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: WaistMeasurementEntity): Long

    @Query("DELETE FROM waist_measurements WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WaistMeasurementEntity>)
}

@Dao
interface InBodyScanDao {
    @Query("SELECT * FROM inbody_scans ORDER BY date DESC")
    fun observeAll(): Flow<List<InBodyScanEntity>>

    @Query("SELECT * FROM inbody_scans ORDER BY date DESC LIMIT 1")
    fun observeLatest(): Flow<InBodyScanEntity?>

    @Query("SELECT * FROM inbody_scans ORDER BY date DESC")
    suspend fun getAll(): List<InBodyScanEntity>

    @Query("SELECT date FROM inbody_scans")
    suspend fun existingDates(): List<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<InBodyScanEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InBodyScanEntity): Long
}

