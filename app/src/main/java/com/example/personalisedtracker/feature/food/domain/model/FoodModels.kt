package com.example.personalisedtracker.feature.food.domain.model

enum class FoodMeal { BREAKFAST, LUNCH, SNACK, DINNER }

data class FoodEntry(
    val id: Long,
    val date: Int,
    val meal: FoodMeal,
    val description: String,
    val approxCalories: Int,
    val approxProteinG: Int,
    val fatG: Int = 0,
    val carbsG: Int = 0,
    val templateId: Long? = null,
)

/** Aggregated daily macro totals derived from [FoodEntry]s. */
data class DailyMacros(
    val proteinG: Int = 0,
    val fatG: Int = 0,
    val carbsG: Int = 0,
    val caloriesKcal: Int = 0,
) {
    companion object {
        fun from(entries: List<FoodEntry>): DailyMacros = DailyMacros(
            proteinG = entries.sumOf { it.approxProteinG },
            fatG = entries.sumOf { it.fatG },
            carbsG = entries.sumOf { it.carbsG },
            caloriesKcal = entries.sumOf { it.approxCalories },
        )
    }
}

/**
 * Reusable meal preset. Defined once, then logged in 1 tap from the food tab.
 * [items] is a newline-separated text breakdown for human / AI context.
 */
data class MealTemplate(
    val id: Long,
    val name: String,
    val meal: FoodMeal,
    val items: List<String>,
    val proteinG: Int,
    val fatG: Int,
    val carbsG: Int,
    val caloriesKcal: Int,
    val useCount: Int = 0,
    val archived: Boolean = false,
)

/** 4·P + 9·F + 4·C — used by the quick-macro dialog when kcal is left blank. */
object MacroMath {
    fun kcalFrom(proteinG: Int, fatG: Int, carbsG: Int): Int =
        (proteinG * 4) + (fatG * 9) + (carbsG * 4)
}

/** Legacy "most used free-text descriptions" — kept for back-compat / migration. */
data class CommonFood(
    val description: String,
    val uses: Int,
    val avgCalories: Int,
    val avgProteinG: Int,
)
