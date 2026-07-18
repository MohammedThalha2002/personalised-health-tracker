package com.example.personalisedtracker.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.personalisedtracker.feature.body.data.dao.BodyWeightDao
import com.example.personalisedtracker.feature.body.data.dao.InBodyScanDao
import com.example.personalisedtracker.feature.body.data.dao.WaistMeasurementDao
import com.example.personalisedtracker.feature.body.data.entity.BodyWeightEntity
import com.example.personalisedtracker.feature.body.data.entity.InBodyScanEntity
import com.example.personalisedtracker.feature.body.data.entity.WaistMeasurementEntity
import com.example.personalisedtracker.feature.food.data.dao.FoodDao
import com.example.personalisedtracker.feature.food.data.dao.MealTemplateDao
import com.example.personalisedtracker.feature.food.data.entity.FoodEntryEntity
import com.example.personalisedtracker.feature.food.data.entity.MealTemplateEntity
import com.example.personalisedtracker.feature.habit.data.dao.HabitDao
import com.example.personalisedtracker.feature.habit.data.entity.HabitCompletionEntity
import com.example.personalisedtracker.feature.habit.data.entity.HabitEntity
import com.example.personalisedtracker.feature.sleep.data.dao.SleepDao
import com.example.personalisedtracker.feature.sleep.data.entity.SleepEntryEntity
import com.example.personalisedtracker.feature.workout.data.dao.ExerciseDao
import com.example.personalisedtracker.feature.workout.data.dao.RoutineDao
import com.example.personalisedtracker.feature.workout.data.dao.WorkoutDao
import com.example.personalisedtracker.feature.workout.data.entity.ExerciseEntity
import com.example.personalisedtracker.feature.workout.data.entity.RoutineEntity
import com.example.personalisedtracker.feature.workout.data.entity.RoutineExerciseEntity
import com.example.personalisedtracker.feature.workout.data.entity.WorkoutEntity
import com.example.personalisedtracker.feature.workout.data.entity.WorkoutSetEntity

/**
 * Single Room database for the whole app. Phase 2 adds body / sleep entities,
 * Phase 3 adds food / habit entities — both bumped via destructive migration
 * (this is a personal app; the user is expected to JSON-export before upgrading).
 */
@Database(
    entities = [
        // workout
        ExerciseEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutSetEntity::class,
        // body
        BodyWeightEntity::class,
        WaistMeasurementEntity::class,
        InBodyScanEntity::class,
        // sleep
        SleepEntryEntity::class,
        // food
        FoodEntryEntity::class,
        MealTemplateEntity::class,
        // habits
        HabitEntity::class,
        HabitCompletionEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class TrackerDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun bodyWeightDao(): BodyWeightDao
    abstract fun waistMeasurementDao(): WaistMeasurementDao
    abstract fun inBodyScanDao(): InBodyScanDao
    abstract fun sleepDao(): SleepDao
    abstract fun foodDao(): FoodDao
    abstract fun mealTemplateDao(): MealTemplateDao
    abstract fun habitDao(): HabitDao

    companion object {
        const val NAME = "tracker.db"
    }
}
