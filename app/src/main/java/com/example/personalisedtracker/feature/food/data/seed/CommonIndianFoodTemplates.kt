package com.example.personalisedtracker.feature.food.data.seed

import com.example.personalisedtracker.feature.food.data.entity.MealTemplateEntity
import com.example.personalisedtracker.feature.food.domain.model.FoodMeal

/**
 * Additional common Indian food items sourced from a standard calorie chart.
 *
 * These complement [TamilFoodTemplates] with **larger / restaurant-style portions**
 * (150–300 g servings, 1-plate biryanis, etc.) and pan-Indian dishes (paneer
 * gravies, Punjabi sabzis, sweets). Names are explicitly portion-qualified so
 * they never collide with the per-katori / per-cup entries already seeded.
 *
 * Macros are estimated from the calorie target using realistic splits:
 *   - rice / grain mains  →  ~10 % P, 25 % F, 65 % C
 *   - paneer / cream gravies →  ~15 % P, 65 % F, 20 % C
 *   - chicken / non-veg mains → ~40 % P, 45 % F, 15 % C
 *   - sweets / fried snacks  → ~5 % P, 40 % F, 55 % C
 *   - light sides / salads   → ~15 % P, 25 % F, 60 % C
 *
 * Sort order: 200-range (veg mains), 250-range (sides/sweets), 300-range (non-veg).
 */
internal object CommonIndianFoodTemplates {

    fun all(): List<MealTemplateEntity> = vegMains + vegSides + sweetsAndSnacks + nonVeg

    // ----- Vegetarian mains (rice / grain / curry plates) -----
    private val vegMains: List<MealTemplateEntity> = listOf(
        item("Tamarind rice (150 g)", FoodMeal.LUNCH, p = 7, f = 12, c = 65, kcal = 415, order = 200),
        item("Plain rice (150 g)", FoodMeal.LUNCH, p = 6, f = 1, c = 68, kcal = 306, order = 201),
        item("Curd rice (300 g)", FoodMeal.LUNCH, p = 14, f = 12, c = 60, kcal = 433, order = 202),
        item("Bisi bele bath (300 g)", FoodMeal.LUNCH, p = 18, f = 15, c = 75, kcal = 535, order = 203),
        item("Jeera pulao (150 g)", FoodMeal.LUNCH, p = 8, f = 12, c = 60, kcal = 388, order = 204),
        item("Veg fried rice (150 g)", FoodMeal.LUNCH, p = 6, f = 8, c = 42, kcal = 258, order = 205),
        item("Veg noodles (150 g)", FoodMeal.DINNER, p = 8, f = 10, c = 50, kcal = 316, order = 206),
        item("Vegetable makhani (150 g)", FoodMeal.DINNER, p = 4, f = 8, c = 12, kcal = 130, order = 207),
        item("Paneer kalimirch (150 g)", FoodMeal.DINNER, p = 18, f = 25, c = 8, kcal = 346, order = 208),
        item("Aloo mooli bhaji (150 g)", FoodMeal.LUNCH, p = 4, f = 12, c = 18, kcal = 196, order = 209),
        item("Aloo okra chilly (150 g)", FoodMeal.LUNCH, p = 6, f = 18, c = 28, kcal = 326, order = 210),
        item("Banana poriyal (100 g)", FoodMeal.LUNCH, p = 4, f = 20, c = 38, kcal = 353, order = 211),
        item("Sorekai kootu (150 g)", FoodMeal.LUNCH, p = 8, f = 22, c = 45, kcal = 415, order = 212),
        item("Brinjal sambar (150 g)", FoodMeal.LUNCH, p = 9, f = 9, c = 22, kcal = 199, order = 213),
        item("Garlic rasam (150 g)", FoodMeal.LUNCH, p = 1, f = 1, c = 6, kcal = 36, order = 214),
        item("Dal lasooni (100 g)", FoodMeal.LUNCH, p = 11, f = 14, c = 20, kcal = 237, order = 215),
        item("Chapathi (3 pieces)", FoodMeal.LUNCH, p = 9, f = 6, c = 42, kcal = 240, order = 216),
        item("Paratha (1 small)", FoodMeal.BREAKFAST, p = 2, f = 3, c = 11, kcal = 80, order = 217),
        item("Stuffed paratha (1)", FoodMeal.BREAKFAST, p = 2, f = 2, c = 18, kcal = 110, order = 218),
    )

    // ----- Generic veg sides / accompaniments -----
    private val vegSides: List<MealTemplateEntity> = listOf(
        item("Veg side dish, no paneer (60 g)", FoodMeal.LUNCH, p = 3, f = 6, c = 14, kcal = 130, order = 250),
        item("Veg side dish with butter / paneer (60 g)", FoodMeal.LUNCH, p = 5, f = 11, c = 8, kcal = 155, order = 251),
        item("Vegetable curry (1 katori)", FoodMeal.LUNCH, p = 3, f = 5, c = 10, kcal = 90, order = 252),
        item("Dal (1 katori)", FoodMeal.LUNCH, p = 4, f = 2, c = 8, kcal = 60, order = 253),
        item("Raita (1 katori, 80 ml)", FoodMeal.LUNCH, p = 5, f = 6, c = 8, kcal = 112, order = 254),
        item("Salad (5 pieces)", FoodMeal.LUNCH, p = 2, f = 1, c = 12, kcal = 65, order = 255),
        item("Veg soup, no cream (1 katori)", FoodMeal.DINNER, p = 2, f = 1, c = 7, kcal = 42, order = 256),
        item("Papad fried (1)", FoodMeal.LUNCH, p = 4, f = 8, c = 18, kcal = 160, order = 257),
        item("Fryums (5 pieces)", FoodMeal.SNACK, p = 1, f = 5, c = 14, kcal = 100, order = 258),
        item("Pickle (1 tsp)", FoodMeal.LUNCH, p = 0, f = 1, c = 0, kcal = 8, order = 259),
    )

    // ----- Sweets / Indian dessert portions -----
    private val sweetsAndSnacks: List<MealTemplateEntity> = listOf(
        item("Gulab jamun (40 g)", FoodMeal.SNACK, p = 2, f = 6, c = 20, kcal = 143, order = 270),
        item("Jalebi (100 g)", FoodMeal.SNACK, p = 4, f = 16, c = 55, kcal = 380, order = 271),
        item("Dry sweet (1 piece, 60-80 g)", FoodMeal.SNACK, p = 2, f = 5, c = 16, kcal = 115, order = 272),
        item("Kheer / payasam (1 katori)", FoodMeal.SNACK, p = 4, f = 4, c = 14, kcal = 107, order = 273),
    )

    // ----- Non-vegetarian plates -----
    private val nonVeg: List<MealTemplateEntity> = listOf(
        item("Chicken biryani (1 plate, 200 g)", FoodMeal.LUNCH, p = 26, f = 18, c = 50, kcal = 470, order = 300),
        item("Mutton biryani (1 plate, 200 g)", FoodMeal.LUNCH, p = 24, f = 18, c = 48, kcal = 450, order = 301),
        item("Egg fried rice (1 plate, 200 g)", FoodMeal.LUNCH, p = 14, f = 12, c = 42, kcal = 340, order = 302),
        item("Kadai chicken (200 g)", FoodMeal.DINNER, p = 32, f = 18, c = 12, kcal = 350, order = 303),
        item("Chicken hyderabadi (200 g)", FoodMeal.DINNER, p = 55, f = 38, c = 25, kcal = 700, order = 304),
        item("Chicken chilly fry (200 g)", FoodMeal.DINNER, p = 28, f = 15, c = 12, kcal = 300, order = 305),
        item("Chicken lollypop (1 piece, 60 g)", FoodMeal.SNACK, p = 9, f = 5, c = 4, kcal = 100, order = 306),
        item("Garlic chicken (150 g)", FoodMeal.DINNER, p = 30, f = 16, c = 8, kcal = 314, order = 307),
        item("Chicken kalimirch (150 g)", FoodMeal.DINNER, p = 32, f = 18, c = 8, kcal = 341, order = 308),
        item("Ginger egg (150 g)", FoodMeal.DINNER, p = 18, f = 22, c = 12, kcal = 343, order = 309),
        item("Egg burji masala (150 g)", FoodMeal.BREAKFAST, p = 14, f = 18, c = 6, kcal = 258, order = 310),
        item("Egg omelette (1 egg)", FoodMeal.BREAKFAST, p = 7, f = 10, c = 1, kcal = 130, order = 311),
        item("Egg & dal curry (200 g)", FoodMeal.DINNER, p = 18, f = 16, c = 18, kcal = 300, order = 312),
        item("Fish fry (100 g)", FoodMeal.DINNER, p = 22, f = 14, c = 4, kcal = 240, order = 313),
        item("Fish curry (200 g)", FoodMeal.DINNER, p = 30, f = 28, c = 12, kcal = 460, order = 314),
    )

    private fun item(
        name: String,
        meal: FoodMeal,
        p: Int, f: Int, c: Int, kcal: Int,
        order: Int,
    ) = MealTemplateEntity(
        name = name,
        mealType = meal.name,
        items = "",
        proteinG = p, fatG = f, carbsG = c,
        caloriesKcal = kcal,
        sortOrder = order,
    )
}
