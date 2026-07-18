package com.example.personalisedtracker.feature.habit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.personalisedtracker.feature.habit.data.entity.HabitCompletionEntity
import com.example.personalisedtracker.feature.habit.data.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE active = 1 ORDER BY sortOrder ASC, id ASC")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY sortOrder ASC, id ASC")
    suspend fun getAll(): List<HabitEntity>

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HabitEntity>): List<Long>

    @Update
    suspend fun update(item: HabitEntity)

    @Query("UPDATE habits SET active = 0 WHERE id = :id")
    suspend fun archive(id: Long)

    // ---- completions ----

    @Query("SELECT * FROM habit_completions WHERE date = :date")
    fun observeForDate(date: Int): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date >= :since ORDER BY date ASC")
    fun observeForHabitSince(habitId: Long, since: Int): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun completionsForHabit(habitId: Long): List<HabitCompletionEntity>

    @Query("SELECT * FROM habit_completions WHERE date >= :since ORDER BY date ASC")
    suspend fun completionsSince(since: Int): List<HabitCompletionEntity>

    @Query("SELECT * FROM habit_completions ORDER BY date ASC")
    suspend fun getAllCompletions(): List<HabitCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompletion(item: HabitCompletionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCompletions(items: List<HabitCompletionEntity>)
}

