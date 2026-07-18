package com.example.personalisedtracker.feature.workout.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.personalisedtracker.feature.workout.data.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAll(): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ExerciseEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ExerciseEntity): Long

    @Update
    suspend fun update(item: ExerciseEntity)

    @Query("DELETE FROM exercises WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustom(id: Long)

    @Query("UPDATE exercises SET imageUrl = :url WHERE name = :name AND (imageUrl IS NULL OR imageUrl <> :url)")
    suspend fun updateImageUrlByName(name: String, url: String): Int

    @Query("UPDATE exercises SET imageUrlSecondary = :url WHERE name = :name AND (imageUrlSecondary IS NULL OR imageUrlSecondary <> :url)")
    suspend fun updateSecondaryImageUrlByName(name: String, url: String): Int
}


