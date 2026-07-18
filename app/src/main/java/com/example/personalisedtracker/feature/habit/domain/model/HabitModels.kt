package com.example.personalisedtracker.feature.habit.domain.model

enum class HabitFrequency { DAILY, WEEKLY }

data class Habit(
    val id: Long,
    val name: String,
    val frequency: HabitFrequency,
    val active: Boolean,
    val sortOrder: Int,
)

data class HabitCompletion(
    val id: Long,
    val habitId: Long,
    val date: Int,
    val completed: Boolean,
)

/** Habit + today's completed flag + current streak. UI consumes this. */
data class HabitWithProgress(
    val habit: Habit,
    val doneToday: Boolean,
    val currentStreak: Int,
    val completionRate30d: Float, // 0f..1f
)

