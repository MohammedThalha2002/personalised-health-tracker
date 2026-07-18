package com.example.personalisedtracker.feature.workout.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for the exercise catalog. Mix of pre-seeded items and
 * user-defined ones ([isCustom] = true).
 */
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,          // ExerciseCategory.name
    val muscleGroups: String,      // comma-separated
    val equipment: String,         // Equipment.name
    val isCustom: Boolean = false,
    /**
     * Optional remote image / GIF URL from
     * [free-exercise-db](https://github.com/yuhonas/free-exercise-db).
     * Null for custom exercises with no media match. This is the
     * **start position** (`0.jpg`).
     */
    val imageUrl: String? = null,
    /**
     * **End position** image (`1.jpg`) for the same exercise. Combined with
     * [imageUrl] on the workout screen to create a simple two-frame animation
     * that conveys the motion.
     */
    val imageUrlSecondary: String? = null,
)