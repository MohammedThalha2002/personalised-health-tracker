package com.example.personalisedtracker.feature.food.domain.usecase

import com.example.personalisedtracker.feature.food.domain.model.CommonFood
import com.example.personalisedtracker.feature.food.domain.model.FoodEntry
import com.example.personalisedtracker.feature.food.domain.model.FoodMeal
import com.example.personalisedtracker.feature.food.domain.model.MealTemplate
import com.example.personalisedtracker.feature.food.domain.repository.FoodRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveFoodForDateUseCase @Inject constructor(private val repo: FoodRepository) {
    operator fun invoke(date: Int): Flow<List<FoodEntry>> = repo.observeForDate(date)
}
class ObserveCommonFoodsUseCase @Inject constructor(private val repo: FoodRepository) {
    operator fun invoke(): Flow<List<CommonFood>> = repo.observeCommonFoods()
}
class ObserveMealTemplatesUseCase @Inject constructor(private val repo: FoodRepository) {
    operator fun invoke(): Flow<List<MealTemplate>> = repo.observeTemplates()
}
class AddFoodUseCase @Inject constructor(private val repo: FoodRepository) {
    suspend operator fun invoke(
        date: Int, meal: FoodMeal, description: String,
        kcal: Int, proteinG: Int, fatG: Int = 0, carbsG: Int = 0,
    ) = repo.add(date, meal, description, kcal, proteinG, fatG, carbsG)
}
class LogFromTemplateUseCase @Inject constructor(private val repo: FoodRepository) {
    suspend operator fun invoke(date: Int, templateId: Long): Long =
        repo.logFromTemplate(date, templateId)
}
class SaveTemplateUseCase @Inject constructor(private val repo: FoodRepository) {
    suspend operator fun invoke(t: MealTemplate): Long = repo.saveTemplate(t)
}
class ArchiveTemplateUseCase @Inject constructor(private val repo: FoodRepository) {
    suspend operator fun invoke(id: Long) = repo.archiveTemplate(id)
}
class DeleteTemplateUseCase @Inject constructor(private val repo: FoodRepository) {
    suspend operator fun invoke(id: Long) = repo.deleteTemplate(id)
}
class DeleteFoodUseCase @Inject constructor(private val repo: FoodRepository) {
    suspend operator fun invoke(id: Long) = repo.delete(id)
}
