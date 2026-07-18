package com.example.personalisedtracker.feature.food.data.seed

import com.example.personalisedtracker.feature.food.data.dao.MealTemplateDao
import com.example.personalisedtracker.feature.food.data.entity.MealTemplateEntity
import com.example.personalisedtracker.feature.food.domain.model.FoodMeal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds starter meal templates. **Additive**: on every launch we insert any
 * default template whose [MealTemplateEntity.name] doesn't already exist in
 * the DB. This means:
 *   - first launch: full catalog gets seeded
 *   - subsequent launches after an app update: new defaults appear, user edits
 *     to existing templates are preserved (we match by name, never overwrite)
 *   - user-archived templates stay archived (we still check by name)
 *
 * Catalog: meal-level presets (see [DEFAULTS] below) + atomic Tamil Nadu /
 * South Indian foods (see [TamilFoodTemplates]).
 */
@Singleton
class MealTemplateSeeder @Inject constructor(
    private val dao: MealTemplateDao,
) {
    suspend fun seedIfNeeded() {
        val catalog: List<MealTemplateEntity> =
            DEFAULTS + TamilFoodTemplates.all() + CommonIndianFoodTemplates.all()
        val existingNames = dao.getAll().map { it.name.lowercase() }.toHashSet()
        val toInsert = catalog.filter { it.name.lowercase() !in existingNames }
        if (toInsert.isNotEmpty()) dao.insertAll(toInsert)
    }

    private companion object {
        // ---- User's stated breakfast pattern, encoded as a template. ----
        // P/F/C estimates: 2 idly (4g P, 1g F, 24g C) + sambar (3/1/8) +
        // 2 egg whites (7/0/0) + buttermilk (3/2/4) + 1 cup fruit (1/0/12) ≈
        // 18g protein, 4g fat, 48g carbs → 300 kcal.
        val DEFAULTS: List<MealTemplateEntity> = listOf(
            MealTemplateEntity(
                name = "Standard idly breakfast",
                mealType = FoodMeal.BREAKFAST.name,
                items = listOf(
                    "2 idly with sambar / chutney",
                    "2 egg whites",
                    "1 cup buttermilk",
                    "1 cup fruit (muskmelon / watermelon / guava)",
                ).joinToString("\n"),
                proteinG = 18, fatG = 4, carbsG = 48, caloriesKcal = 300,
                sortOrder = 1,
            ),
            MealTemplateEntity(
                name = "Sprouts + eggs breakfast",
                mealType = FoodMeal.BREAKFAST.name,
                items = listOf(
                    "1 cup green moong sprouts",
                    "2 egg whites",
                    "1 cup buttermilk",
                ).joinToString("\n"),
                proteinG = 22, fatG = 3, carbsG = 30, caloriesKcal = 240,
                sortOrder = 2,
            ),
            MealTemplateEntity(
                name = "Standard lunch (rice + dal + sabzi)",
                mealType = FoodMeal.LUNCH.name,
                items = listOf(
                    "1 cup rice / 2 chapati",
                    "1 bowl dal",
                    "1 bowl sabzi",
                    "Curd / salad",
                ).joinToString("\n"),
                proteinG = 25, fatG = 12, carbsG = 75, caloriesKcal = 560,
                sortOrder = 3,
            ),
            MealTemplateEntity(
                name = "Chicken / paneer lunch",
                mealType = FoodMeal.LUNCH.name,
                items = listOf(
                    "150 g chicken curry OR 120 g paneer",
                    "2 chapati / 1 cup rice",
                    "Salad",
                ).joinToString("\n"),
                proteinG = 40, fatG = 18, carbsG = 60, caloriesKcal = 620,
                sortOrder = 4,
            ),
            MealTemplateEntity(
                name = "Light dinner (chapati + sabzi)",
                mealType = FoodMeal.DINNER.name,
                items = listOf(
                    "2 chapati",
                    "1 bowl sabzi",
                    "Curd",
                ).joinToString("\n"),
                proteinG = 18, fatG = 10, carbsG = 50, caloriesKcal = 380,
                sortOrder = 5,
            ),
            MealTemplateEntity(
                name = "Protein shake snack",
                mealType = FoodMeal.SNACK.name,
                items = listOf(
                    "1 scoop whey",
                    "1 cup milk / water",
                ).joinToString("\n"),
                proteinG = 25, fatG = 2, carbsG = 5, caloriesKcal = 140,
                sortOrder = 6,
            ),
            MealTemplateEntity(
                name = "Fruit snack",
                mealType = FoodMeal.SNACK.name,
                items = listOf(
                    "1 banana OR 1 apple OR 1 cup papaya",
                ).joinToString("\n"),
                proteinG = 1, fatG = 0, carbsG = 25, caloriesKcal = 100,
                sortOrder = 7,
            ),
        )
    }
}
