package com.example.personalisedtracker.feature.food.data.repository

import com.example.personalisedtracker.core.common.DispatcherProvider
import com.example.personalisedtracker.feature.food.data.dao.FoodDao
import com.example.personalisedtracker.feature.food.data.dao.MealTemplateDao
import com.example.personalisedtracker.feature.food.data.entity.FoodEntryEntity
import com.example.personalisedtracker.feature.food.data.entity.MealTemplateEntity
import com.example.personalisedtracker.feature.food.domain.model.CommonFood
import com.example.personalisedtracker.feature.food.domain.model.FoodEntry
import com.example.personalisedtracker.feature.food.domain.model.FoodMeal
import com.example.personalisedtracker.feature.food.domain.model.MealTemplate
import com.example.personalisedtracker.feature.food.domain.repository.FoodRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private fun FoodEntryEntity.toDomain() = FoodEntry(
    id = id,
    date = date,
    meal = runCatching { FoodMeal.valueOf(mealType) }.getOrDefault(FoodMeal.SNACK),
    description = description,
    approxCalories = approxCalories,
    approxProteinG = approxProteinG,
    fatG = fatG,
    carbsG = carbsG,
    templateId = templateId,
)

private fun MealTemplateEntity.toDomain() = MealTemplate(
    id = id,
    name = name,
    meal = runCatching { FoodMeal.valueOf(mealType) }.getOrDefault(FoodMeal.SNACK),
    items = items.split('\n').map { it.trim() }.filter { it.isNotEmpty() },
    proteinG = proteinG,
    fatG = fatG,
    carbsG = carbsG,
    caloriesKcal = caloriesKcal,
    useCount = useCount,
    archived = archived,
)

private fun MealTemplate.toEntity(existing: MealTemplateEntity? = null) = MealTemplateEntity(
    id = id,
    name = name.trim(),
    mealType = meal.name,
    items = items.joinToString("\n") { it.trim() }.trim(),
    proteinG = proteinG.coerceAtLeast(0),
    fatG = fatG.coerceAtLeast(0),
    carbsG = carbsG.coerceAtLeast(0),
    caloriesKcal = caloriesKcal.coerceAtLeast(0),
    sortOrder = existing?.sortOrder ?: 0,
    archived = archived,
    useCount = existing?.useCount ?: useCount.coerceAtLeast(0),
    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
)

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val dao: FoodDao,
    private val templateDao: MealTemplateDao,
    private val dispatchers: DispatcherProvider,
) : FoodRepository {

    override fun observeForDate(date: Int): Flow<List<FoodEntry>> =
        dao.observeForDate(date).map { it.map(FoodEntryEntity::toDomain) }.flowOn(dispatchers.io)

    override fun observeSince(since: Int): Flow<List<FoodEntry>> =
        dao.observeSince(since).map { it.map(FoodEntryEntity::toDomain) }.flowOn(dispatchers.io)

    override fun observeCommonFoods(limit: Int): Flow<List<CommonFood>> =
        dao.observeCommonFoods(limit)
            .map { rows ->
                rows.map {
                    CommonFood(it.description, it.uses, it.avgCal.toInt(), it.avgProtein.toInt())
                }
            }
            .flowOn(dispatchers.io)

    override fun observeTemplates(): Flow<List<MealTemplate>> =
        templateDao.observeAll().map { it.map(MealTemplateEntity::toDomain) }.flowOn(dispatchers.io)

    override fun observeTemplatesForMeal(meal: FoodMeal): Flow<List<MealTemplate>> =
        templateDao.observeForMeal(meal.name)
            .map { it.map(MealTemplateEntity::toDomain) }
            .flowOn(dispatchers.io)

    override suspend fun add(
        date: Int,
        meal: FoodMeal,
        description: String,
        kcal: Int,
        proteinG: Int,
        fatG: Int,
        carbsG: Int,
    ) = withContext(dispatchers.io) {
        dao.upsert(
            FoodEntryEntity(
                date = date,
                mealType = meal.name,
                description = description.trim(),
                approxCalories = kcal.coerceAtLeast(0),
                approxProteinG = proteinG.coerceAtLeast(0),
                fatG = fatG.coerceAtLeast(0),
                carbsG = carbsG.coerceAtLeast(0),
                templateId = null,
            )
        )
        Unit
    }

    override suspend fun logFromTemplate(date: Int, templateId: Long): Long = withContext(dispatchers.io) {
        val t = templateDao.getById(templateId)
            ?: return@withContext -1L
        val id = dao.upsert(
            FoodEntryEntity(
                date = date,
                mealType = t.mealType,
                description = t.name,
                approxCalories = t.caloriesKcal,
                approxProteinG = t.proteinG,
                fatG = t.fatG,
                carbsG = t.carbsG,
                templateId = t.id,
            )
        )
        templateDao.bumpUseCount(t.id)
        id
    }

    override suspend fun saveTemplate(template: MealTemplate): Long = withContext(dispatchers.io) {
        val existing = if (template.id != 0L) templateDao.getById(template.id) else null
        templateDao.upsert(template.toEntity(existing))
    }

    override suspend fun archiveTemplate(id: Long) = withContext(dispatchers.io) {
        templateDao.archive(id)
    }

    override suspend fun deleteTemplate(id: Long) = withContext(dispatchers.io) {
        templateDao.delete(id)
    }

    override suspend fun delete(id: Long) = withContext(dispatchers.io) { dao.delete(id) }
}
