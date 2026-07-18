package com.example.personalisedtracker.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.personalisedtracker.core.common.DefaultDispatcherProvider
import com.example.personalisedtracker.core.common.DispatcherProvider
import com.example.personalisedtracker.core.data.db.TrackerDatabase
import com.example.personalisedtracker.feature.workout.data.dao.ExerciseDao
import com.example.personalisedtracker.feature.workout.data.dao.RoutineDao
import com.example.personalisedtracker.feature.workout.data.dao.WorkoutDao
import com.example.personalisedtracker.feature.workout.data.repository.WorkoutRepositoryImpl
import com.example.personalisedtracker.feature.workout.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): TrackerDatabase =
        Room.databaseBuilder(ctx, TrackerDatabase::class.java, TrackerDatabase.NAME)
            // Preserve workout history across the v4→v5 schema bump
            // (added ExerciseEntity.imageUrlSecondary).
            .addMigrations(MIGRATION_4_5)
            // Any other (older) schema gap → wipe. User is expected to
            // JSON-export before such upgrades.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun provideExerciseDao(db: TrackerDatabase): ExerciseDao = db.exerciseDao()
    @Provides fun provideRoutineDao(db: TrackerDatabase): RoutineDao = db.routineDao()
    @Provides fun provideWorkoutDao(db: TrackerDatabase): WorkoutDao = db.workoutDao()
    @Provides fun provideBodyWeightDao(db: TrackerDatabase) = db.bodyWeightDao()
    @Provides fun provideWaistDao(db: TrackerDatabase) = db.waistMeasurementDao()
    @Provides fun provideInBodyScanDao(db: TrackerDatabase) = db.inBodyScanDao()
    @Provides fun provideSleepDao(db: TrackerDatabase) = db.sleepDao()
    @Provides fun provideFoodDao(db: TrackerDatabase) = db.foodDao()
    @Provides fun provideMealTemplateDao(db: TrackerDatabase) = db.mealTemplateDao()
    @Provides fun provideHabitDao(db: TrackerDatabase) = db.habitDao()
}

/** v4 → v5: add nullable [ExerciseEntity.imageUrlSecondary] column. */
private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE exercises ADD COLUMN imageUrlSecondary TEXT")
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreBindingsModule {

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindBodyRepository(
        impl: com.example.personalisedtracker.feature.body.data.repository.BodyRepositoryImpl,
    ): com.example.personalisedtracker.feature.body.domain.repository.BodyRepository

    @Binds
    @Singleton
    abstract fun bindSleepRepository(
        impl: com.example.personalisedtracker.feature.sleep.data.repository.SleepRepositoryImpl,
    ): com.example.personalisedtracker.feature.sleep.domain.repository.SleepRepository

    @Binds
    @Singleton
    abstract fun bindFoodRepository(
        impl: com.example.personalisedtracker.feature.food.data.repository.FoodRepositoryImpl,
    ): com.example.personalisedtracker.feature.food.domain.repository.FoodRepository

    @Binds
    @Singleton
    abstract fun bindHabitRepository(
        impl: com.example.personalisedtracker.feature.habit.data.repository.HabitRepositoryImpl,
    ): com.example.personalisedtracker.feature.habit.domain.repository.HabitRepository
}
