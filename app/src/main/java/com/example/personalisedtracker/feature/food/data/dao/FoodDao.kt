package com.example.personalisedtracker.feature.food.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.personalisedtracker.feature.food.data.entity.FoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY createdAt ASC")
    fun observeForDate(date: Int): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE date >= :since ORDER BY date ASC, createdAt ASC")
    fun observeSince(since: Int): Flow<List<FoodEntryEntity>>

    @Query("""
        SELECT description, COUNT(*) AS uses, AVG(approxCalories) AS avgCal, AVG(approxProteinG) AS avgProtein
        FROM food_entries
        GROUP BY LOWER(TRIM(description))
        ORDER BY uses DESC
        LIMIT :limit
    """)
    fun observeCommonFoods(limit: Int = 12): Flow<List<CommonFoodRow>>

    @Query("SELECT * FROM food_entries ORDER BY date ASC, createdAt ASC")
    suspend fun getAll(): List<FoodEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: FoodEntryEntity): Long

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FoodEntryEntity>)
}

/** Projection row for the "most-used foods" list. */
data class CommonFoodRow(
    val description: String,
    val uses: Int,
    val avgCal: Double,
    val avgProtein: Double,
)

