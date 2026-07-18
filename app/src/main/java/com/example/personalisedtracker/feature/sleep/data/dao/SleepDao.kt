package com.example.personalisedtracker.feature.sleep.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.personalisedtracker.feature.sleep.data.entity.SleepEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_entries ORDER BY date DESC")
    fun observeAll(): Flow<List<SleepEntryEntity>>

    @Query("SELECT * FROM sleep_entries WHERE date >= :since ORDER BY date ASC")
    fun observeSince(since: Int): Flow<List<SleepEntryEntity>>

    @Query("SELECT * FROM sleep_entries WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: Int): SleepEntryEntity?

    @Query("SELECT * FROM sleep_entries ORDER BY date DESC")
    suspend fun getAll(): List<SleepEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SleepEntryEntity): Long

    @Query("DELETE FROM sleep_entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SleepEntryEntity>)
}

