package com.example.personalisedtracker.feature.food.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A reusable "meal preset" the user defines once and then taps to log.
 * Stores both the *items* (free-text breakdown for context — fed to the AI on
 * export) and the *macros* (the numeric truth used for daily totals).
 *
 * [items] is a newline-separated list of components, e.g.:
 *   "2 idly + sambar"
 *   "2 egg whites"
 *   "1 cup buttermilk"
 *   "1 cup fruit (muskmelon / watermelon / guava)"
 */
@Entity(
    tableName = "meal_templates",
    indices = [Index("mealType"), Index("archived")],
)
data class MealTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mealType: String,                 // FoodMeal.name
    val items: String = "",               // newline-separated free-text
    val proteinG: Int,
    val fatG: Int,
    val carbsG: Int,
    val caloriesKcal: Int,                // either user-typed or computed 4P+9F+4C
    val sortOrder: Int = 0,
    val archived: Boolean = false,
    val useCount: Int = 0,                // bumped each time logged → drives "most-used" ordering
    val createdAt: Long = System.currentTimeMillis(),
)

