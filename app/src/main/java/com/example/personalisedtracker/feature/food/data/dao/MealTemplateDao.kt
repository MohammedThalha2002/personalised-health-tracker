package com.example.personalisedtracker.feature.food.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.personalisedtracker.feature.food.data.entity.MealTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealTemplateDao {

    @Query("SELECT * FROM meal_templates WHERE archived = 0 ORDER BY useCount DESC, sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<MealTemplateEntity>>

    @Query("""
        SELECT * FROM meal_templates
        WHERE archived = 0 AND mealType = :mealType
        ORDER BY useCount DESC, sortOrder ASC, name ASC
    """)
    fun observeForMeal(mealType: String): Flow<List<MealTemplateEntity>>

    @Query("SELECT * FROM meal_templates WHERE id = :id")
    suspend fun getById(id: Long): MealTemplateEntity?

    @Query("SELECT * FROM meal_templates ORDER BY id ASC")
    suspend fun getAll(): List<MealTemplateEntity>

    @Query("SELECT COUNT(*) FROM meal_templates")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(t: MealTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MealTemplateEntity>)

    @Update
    suspend fun update(t: MealTemplateEntity)

    @Query("UPDATE meal_templates SET useCount = useCount + 1 WHERE id = :id")
    suspend fun bumpUseCount(id: Long)

    @Query("UPDATE meal_templates SET archived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("DELETE FROM meal_templates WHERE id = :id")
    suspend fun delete(id: Long)
}

