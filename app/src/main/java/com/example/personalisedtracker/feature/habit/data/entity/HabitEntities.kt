package com.example.personalisedtracker.feature.habit.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetFrequency: String, // HabitFrequency.name
    val active: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

/** One row per (habit, day). [date] is yyyyMMdd. */
@Entity(
    tableName = "habit_completions",
    indices = [Index(value = ["habitId", "date"], unique = true), Index("date")],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: Int,
    val completed: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
)

