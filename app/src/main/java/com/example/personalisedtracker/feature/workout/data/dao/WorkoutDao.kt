package com.example.personalisedtracker.feature.workout.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.personalisedtracker.feature.workout.data.entity.WorkoutEntity
import com.example.personalisedtracker.feature.workout.data.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // ---- workouts ----

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkout(id: Long): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE id = :id")
    fun observeWorkout(id: Long): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    fun observeActiveWorkout(): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveWorkout(): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE endedAt IS NOT NULL ORDER BY startedAt DESC")
    fun observeHistory(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts ORDER BY startedAt DESC")
    suspend fun getAllForExport(): List<WorkoutEntity>

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkout(id: Long)

    // ---- sets ----

    @Insert
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE id = :id")
    suspend fun deleteSet(id: Long)

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId ORDER BY orderIndex ASC, setNumber ASC")
    fun observeSetsFor(workoutId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId ORDER BY orderIndex ASC, setNumber ASC")
    suspend fun getSetsFor(workoutId: Long): List<WorkoutSetEntity>

    @Query(
        """
        SELECT * FROM workout_sets
        WHERE exerciseId = :exerciseId AND completed = 1
        ORDER BY id DESC LIMIT :limit
        """
    )
    suspend fun getRecentSetsForExercise(exerciseId: Long, limit: Int = 10): List<WorkoutSetEntity>

    @Query("SELECT * FROM workout_sets ORDER BY workoutId, orderIndex, setNumber")
    suspend fun getAllSetsForExport(): List<WorkoutSetEntity>

    // ---- bulk insert (used by JSON import) ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(items: List<WorkoutEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(items: List<WorkoutSetEntity>)
}

