package com.example.personalisedtracker.feature.food.data.seed

import com.example.personalisedtracker.feature.food.data.entity.MealTemplateEntity
import com.example.personalisedtracker.feature.food.domain.model.FoodMeal

/**
 * Curated list of common Tamil Nadu / South Indian foods as atomic templates.
 * Each template represents a *single serving* — users compose a meal by tapping
 * multiple chips (e.g. "Idly ×2" + "Sambar" + "Coconut chutney").
 *
 * Macros are realistic estimates per standard home serving — every template is
 * editable from the "Manage templates" screen so the user can dial them in.
 *
 * Sort order is grouped: breakfast (10-29) → rice/curries (30-49) →
 * sides (50-69) → proteins (70-89) → snacks (90-99).
 */
internal object TamilFoodTemplates {

    fun all(): List<MealTemplateEntity> = breakfast + riceDishes + sides + proteins + snacks

    // ----- Breakfast tiffin items (TN staples) -----
    private val breakfast: List<MealTemplateEntity> = listOf(
        item("Idly (1 piece)", FoodMeal.BREAKFAST, p = 2, f = 0, c = 8, kcal = 35, order = 10),
        item("Idly (2 pieces)", FoodMeal.BREAKFAST, p = 4, f = 0, c = 16, kcal = 70, order = 11),
        item("Idly (3 pieces)", FoodMeal.BREAKFAST, p = 6, f = 0, c = 24, kcal = 105, order = 12),
        item("Plain dosa (1)", FoodMeal.BREAKFAST, p = 3, f = 4, c = 18, kcal = 120, order = 13),
        item("Masala dosa (1)", FoodMeal.BREAKFAST, p = 5, f = 7, c = 30, kcal = 200, order = 14),
        item("Rava dosa (1)", FoodMeal.BREAKFAST, p = 3, f = 6, c = 22, kcal = 150, order = 15),
        item("Uthappam (1)", FoodMeal.BREAKFAST, p = 4, f = 4, c = 23, kcal = 150, order = 16),
        item("Pongal (1 cup)", FoodMeal.BREAKFAST, p = 6, f = 8, c = 30, kcal = 220, order = 17),
        item("Upma (1 cup)", FoodMeal.BREAKFAST, p = 5, f = 7, c = 28, kcal = 200, order = 18),
        item("Idiyappam (2)", FoodMeal.BREAKFAST, p = 4, f = 0, c = 44, kcal = 200, order = 19),
        item("Appam (1)", FoodMeal.BREAKFAST, p = 2, f = 3, c = 22, kcal = 120, order = 20),
        item("Medu vada (1)", FoodMeal.BREAKFAST, p = 4, f = 7, c = 12, kcal = 130, order = 21),
        item("Poori (1)", FoodMeal.BREAKFAST, p = 2, f = 5, c = 12, kcal = 100, order = 22),
        item("Chapathi (1)", FoodMeal.LUNCH, p = 3, f = 2, c = 14, kcal = 80, order = 23),
        item("Chapathi (2)", FoodMeal.LUNCH, p = 6, f = 4, c = 28, kcal = 160, order = 24),
        item("Paratha plain (1)", FoodMeal.LUNCH, p = 3, f = 5, c = 17, kcal = 130, order = 25),
    )

    // ----- Rice dishes -----
    private val riceDishes: List<MealTemplateEntity> = listOf(
        item("White rice (1 cup)", FoodMeal.LUNCH, p = 4, f = 0, c = 45, kcal = 200, order = 30),
        item("White rice (1/2 cup)", FoodMeal.LUNCH, p = 2, f = 0, c = 22, kcal = 100, order = 31),
        item("Curd rice (1 cup)", FoodMeal.LUNCH, p = 6, f = 5, c = 25, kcal = 180, order = 32),
        item("Sambar rice (1 cup)", FoodMeal.LUNCH, p = 7, f = 5, c = 35, kcal = 220, order = 33),
        item("Lemon rice (1 cup)", FoodMeal.LUNCH, p = 5, f = 8, c = 38, kcal = 250, order = 34),
        item("Tomato rice (1 cup)", FoodMeal.LUNCH, p = 5, f = 8, c = 38, kcal = 250, order = 35),
        item("Tamarind rice / Puliyodharai (1 cup)", FoodMeal.LUNCH, p = 4, f = 9, c = 40, kcal = 270, order = 36),
        item("Bisi bele bath (1 cup)", FoodMeal.LUNCH, p = 9, f = 8, c = 40, kcal = 280, order = 37),
        item("Veg biryani (1 cup)", FoodMeal.LUNCH, p = 7, f = 10, c = 38, kcal = 280, order = 38),
        item("Chicken biryani (1 cup)", FoodMeal.LUNCH, p = 18, f = 12, c = 36, kcal = 320, order = 39),
    )

    // ----- Curries / sides -----
    private val sides: List<MealTemplateEntity> = listOf(
        item("Sambar (1 bowl)", FoodMeal.LUNCH, p = 6, f = 3, c = 12, kcal = 100, order = 50),
        item("Rasam (1 bowl)", FoodMeal.LUNCH, p = 2, f = 1, c = 8, kcal = 50, order = 51),
        item("Toor dal (1 bowl)", FoodMeal.LUNCH, p = 9, f = 3, c = 18, kcal = 130, order = 52),
        item("Coconut chutney (2 tbsp)", FoodMeal.BREAKFAST, p = 1, f = 7, c = 3, kcal = 80, order = 53),
        item("Tomato chutney (2 tbsp)", FoodMeal.BREAKFAST, p = 1, f = 1, c = 4, kcal = 30, order = 54),
        item("Mixed veg poriyal (1 bowl)", FoodMeal.LUNCH, p = 3, f = 5, c = 10, kcal = 100, order = 55),
        item("Beans poriyal (1 bowl)", FoodMeal.LUNCH, p = 3, f = 4, c = 8, kcal = 80, order = 56),
        item("Cabbage poriyal (1 bowl)", FoodMeal.LUNCH, p = 2, f = 4, c = 8, kcal = 75, order = 57),
        item("Avial (1 bowl)", FoodMeal.LUNCH, p = 4, f = 10, c = 12, kcal = 150, order = 58),
        item("Kootu (1 bowl)", FoodMeal.LUNCH, p = 6, f = 5, c = 12, kcal = 120, order = 59),
        item("Paneer butter masala (100g)", FoodMeal.DINNER, p = 14, f = 22, c = 8, kcal = 280, order = 60),
    )

    // ----- Proteins -----
    private val proteins: List<MealTemplateEntity> = listOf(
        item("Boiled egg whole (1)", FoodMeal.BREAKFAST, p = 6, f = 5, c = 1, kcal = 70, order = 70),
        item("Egg whites (2)", FoodMeal.BREAKFAST, p = 7, f = 0, c = 1, kcal = 35, order = 71),
        item("Egg whites (3)", FoodMeal.BREAKFAST, p = 11, f = 0, c = 1, kcal = 50, order = 72),
        item("Omelette (2 egg)", FoodMeal.BREAKFAST, p = 13, f = 12, c = 1, kcal = 160, order = 73),
        item("Chicken curry (150 g)", FoodMeal.DINNER, p = 25, f = 12, c = 4, kcal = 220, order = 74),
        item("Grilled chicken (150 g)", FoodMeal.DINNER, p = 35, f = 6, c = 0, kcal = 200, order = 75),
        item("Fish curry (150 g)", FoodMeal.DINNER, p = 22, f = 8, c = 4, kcal = 180, order = 76),
        item("Egg curry (2 eggs)", FoodMeal.DINNER, p = 14, f = 14, c = 5, kcal = 200, order = 77),
        item("Paneer (50 g)", FoodMeal.SNACK, p = 9, f = 10, c = 2, kcal = 130, order = 78),
        item("Curd (1 cup)", FoodMeal.LUNCH, p = 6, f = 5, c = 8, kcal = 100, order = 79),
        item("Buttermilk (1 glass)", FoodMeal.LUNCH, p = 3, f = 2, c = 4, kcal = 40, order = 80),
        item("Milk (1 cup)", FoodMeal.BREAKFAST, p = 6, f = 6, c = 10, kcal = 120, order = 81),
        item("Whey scoop (30 g)", FoodMeal.SNACK, p = 24, f = 1, c = 3, kcal = 120, order = 82),
        item("Green moong sprouts (1 cup)", FoodMeal.BREAKFAST, p = 8, f = 1, c = 18, kcal = 100, order = 83),
    )

    // ----- Snacks / fruit / drinks -----
    private val snacks: List<MealTemplateEntity> = listOf(
        item("Banana (medium)", FoodMeal.SNACK, p = 1, f = 0, c = 25, kcal = 100, order = 90),
        item("Apple (medium)", FoodMeal.SNACK, p = 0, f = 0, c = 20, kcal = 80, order = 91),
        item("Guava (1)", FoodMeal.SNACK, p = 2, f = 0, c = 15, kcal = 70, order = 92),
        item("Muskmelon (1 cup)", FoodMeal.SNACK, p = 1, f = 0, c = 13, kcal = 55, order = 93),
        item("Watermelon (1 cup)", FoodMeal.SNACK, p = 1, f = 0, c = 11, kcal = 45, order = 94),
        item("Papaya (1 cup)", FoodMeal.SNACK, p = 1, f = 0, c = 14, kcal = 60, order = 95),
        item("Tender coconut water", FoodMeal.SNACK, p = 1, f = 0, c = 11, kcal = 50, order = 96),
        item("Tea with milk + sugar", FoodMeal.SNACK, p = 2, f = 2, c = 8, kcal = 60, order = 97),
        item("Filter coffee with sugar", FoodMeal.SNACK, p = 2, f = 2, c = 10, kcal = 70, order = 98),
        item("Black tea / coffee (no sugar)", FoodMeal.SNACK, p = 0, f = 0, c = 0, kcal = 5, order = 99),
        item("Murukku (2 pieces)", FoodMeal.SNACK, p = 2, f = 6, c = 10, kcal = 100, order = 100),
        item("Bonda (1)", FoodMeal.SNACK, p = 3, f = 8, c = 16, kcal = 150, order = 101),
        item("Mysore pak (1 piece)", FoodMeal.SNACK, p = 2, f = 10, c = 22, kcal = 180, order = 102),
        item("Payasam (1 cup)", FoodMeal.SNACK, p = 5, f = 10, c = 35, kcal = 250, order = 103),
    )

    private fun item(
        name: String,
        meal: FoodMeal,
        p: Int, f: Int, c: Int, kcal: Int,
        order: Int,
    ) = MealTemplateEntity(
        name = name,
        mealType = meal.name,
        items = "", // atomic single-food template — no breakdown needed
        proteinG = p, fatG = f, carbsG = c,
        caloriesKcal = kcal,
        sortOrder = order,
    )
}

