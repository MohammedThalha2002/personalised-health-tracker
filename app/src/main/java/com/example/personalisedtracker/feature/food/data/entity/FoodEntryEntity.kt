package com.example.personalisedtracker.feature.food.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A meal logged on a given day. Can be either:
 *  - sourced from a [MealTemplateEntity] ([templateId] non-null) — common case, 1-tap log
 *  - a free-text ad-hoc entry ([templateId] null) — quick-macro fallback
 *
 * Stores full P / F / C macros plus total kcal. kcal is denormalised on insert
 * so historical entries are stable even if the user edits a template later.
 */
@Entity(tableName = "food_entries", indices = [Index("date"), Index("templateId")])
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Int, // yyyyMMdd
    val mealType: String, // FoodMeal.name
    val description: String,
    val approxCalories: Int,
    val approxProteinG: Int,
    val fatG: Int = 0,
    val carbsG: Int = 0,
    val templateId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
