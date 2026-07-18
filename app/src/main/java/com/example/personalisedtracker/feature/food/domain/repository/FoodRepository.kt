package com.example.personalisedtracker.feature.food.domain.repository

import com.example.personalisedtracker.feature.food.domain.model.CommonFood
import com.example.personalisedtracker.feature.food.domain.model.FoodEntry
import com.example.personalisedtracker.feature.food.domain.model.FoodMeal
import com.example.personalisedtracker.feature.food.domain.model.MealTemplate
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun observeForDate(date: Int): Flow<List<FoodEntry>>
    fun observeSince(since: Int): Flow<List<FoodEntry>>
    fun observeCommonFoods(limit: Int = 12): Flow<List<CommonFood>>

    /** All non-archived templates, ordered most-used first. */
    fun observeTemplates(): Flow<List<MealTemplate>>

    /** Templates filtered to a specific meal slot. */
    fun observeTemplatesForMeal(meal: FoodMeal): Flow<List<MealTemplate>>

    /** Free-text / quick-macro entry. */
    suspend fun add(
        date: Int,
        meal: FoodMeal,
        description: String,
        kcal: Int,
        proteinG: Int,
        fatG: Int = 0,
        carbsG: Int = 0,
    )

    /** One-tap entry from a template: copies its macros + name + bumps useCount. */
    suspend fun logFromTemplate(date: Int, templateId: Long): Long

    suspend fun saveTemplate(template: MealTemplate): Long
    suspend fun archiveTemplate(id: Long)
    suspend fun deleteTemplate(id: Long)

    suspend fun delete(id: Long)
}
