package com.example.personalisedtracker.feature.workout.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.personalisedtracker.feature.workout.data.entity.ExerciseEntity
import com.example.personalisedtracker.feature.workout.data.entity.RoutineEntity
import com.example.personalisedtracker.feature.workout.data.entity.RoutineExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines WHERE archived = 0 ORDER BY COALESCE(dayOfWeek, 99), createdAt")
    fun observeActive(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines ORDER BY createdAt ASC")
    suspend fun getAllForExport(): List<RoutineEntity>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getById(id: Long): RoutineEntity?

    @Query("SELECT COUNT(*) FROM routines")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RoutineEntity): Long

    @Update
    suspend fun update(item: RoutineEntity)

    @Query("UPDATE routines SET archived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    // -- routine-exercise link table --

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    fun observeExercisesFor(routineId: Long): Flow<List<RoutineExerciseEntity>>

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    suspend fun getExercisesFor(routineId: Long): List<RoutineExerciseEntity>

    @Query(
        """
        SELECT e.* FROM exercises e
        INNER JOIN routine_exercises re ON re.exerciseId = e.id
        WHERE re.routineId = :routineId
        ORDER BY re.orderIndex ASC
        """
    )
    suspend fun getExerciseEntitiesFor(routineId: Long): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(items: List<RoutineExerciseEntity>): List<Long>

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun clearExercisesFor(routineId: Long)

    @Transaction
    suspend fun replaceRoutineExercises(routineId: Long, items: List<RoutineExerciseEntity>) {
        clearExercisesFor(routineId)
        insertRoutineExercises(items)
    }
}


