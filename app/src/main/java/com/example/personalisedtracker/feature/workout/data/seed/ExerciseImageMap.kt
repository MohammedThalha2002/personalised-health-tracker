package com.example.personalisedtracker.feature.workout.data.seed

/**
 * Maps our seed exercise names → free-exercise-db slug IDs.
 *
 * Source: https://github.com/yuhonas/free-exercise-db (MIT licensed,
 * exercise images & metadata). Each entry's image is served at:
 *
 *   https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/{slug}/images/0.jpg
 *
 * Coverage is best-effort — names with no good match are simply omitted and
 * the UI falls back to a placeholder icon. New exercises added by the user
 * (custom) stay imageless.
 */
internal object ExerciseImageMap {

    private const val BASE =
        "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"

    private val nameToSlug: Map<String, String> = mapOf(
        // ---- Push
        "Push-up" to "Pushups",
        "Diamond push-up" to "Diamond_Push-Up",
        "Pike push-up" to "Pike_Pushup",
        "Pseudo-planche push-up" to "Pushups",
        "Dumbbell floor press" to "Dumbbell_Floor_Press",
        "Dumbbell bench press" to "Dumbbell_Bench_Press",
        "Barbell bench press" to "Barbell_Bench_Press_-_Medium_Grip",
        "Dumbbell overhead press" to "Dumbbell_Shoulder_Press",
        "Barbell overhead press" to "Standing_Military_Press",
        "Dumbbell lateral raise" to "Side_Lateral_Raise",
        "Dumbbell front raise" to "Front_Dumbbell_Raise",
        "Dips" to "Dips_-_Triceps_Version",
        "Triceps extension (DB)" to "Seated_Triceps_Press",
        "Skullcrusher" to "EZ-Bar_Skullcrusher",

        // ---- Pull
        "Pull-up" to "Pullups",
        "Chin-up" to "Chin-Up",
        "Weighted pull-up" to "Weighted_Pull-Ups",
        "Australian row" to "Bodyweight_Mid_Row",
        "Dumbbell bent-over row" to "Bent_Over_Two-Dumbbell_Row",
        "Single-arm DB row" to "One-Arm_Dumbbell_Row",
        "Lat pulldown" to "Wide-Grip_Lat_Pulldown",
        "Seated cable row" to "Seated_Cable_Rows",
        "Face pull" to "Face_Pull",
        "Dumbbell bicep curl" to "Dumbbell_Bicep_Curl",
        "Hammer curl" to "Hammer_Curls",
        "Barbell curl" to "Barbell_Curl",

        // ---- Legs
        "Barbell squat" to "Barbell_Squat",
        "Dumbbell goblet squat" to "Goblet_Squat",
        "Front squat" to "Front_Barbell_Squat",
        "Romanian deadlift (DB)" to "Dumbbell_Romanian_Deadlift",
        "Romanian deadlift (BB)" to "Romanian_Deadlift",
        "Conventional deadlift" to "Barbell_Deadlift",
        "Bulgarian split squat" to "Bulgarian_Split_Squat",
        "Walking lunge" to "Dumbbell_Lunges",
        "Dumbbell step-up" to "Dumbbell_Step-Ups",
        "Leg press" to "Leg_Press",
        "Leg extension" to "Leg_Extensions",
        "Hamstring curl" to "Lying_Leg_Curls",
        "Standing calf raise" to "Standing_Barbell_Calf_Raise",
        "Seated calf raise" to "Seated_Calf_Raise",
        "Pistol squat" to "Single-Leg_Squat",
        "Hip thrust" to "Barbell_Hip_Thrust",

        // ---- Core
        "Plank" to "Plank",
        "Side plank" to "Side_Bridge",
        "Hollow body hold" to "Hollow_Body_Hold",
        "L-sit" to "L-Sit",
        "Hanging knee raise" to "Hanging_Leg_Raise",
        "Hanging leg raise" to "Hanging_Leg_Raise",
        "Dragon flag" to "Dragon_Flag",
        "Ab wheel" to "Ab_Roller",
        "Russian twist" to "Russian_Twist",
        "Cable wood chop" to "Cable_Russian_Twists",
        "Pallof press" to "Cable_Pallof_Press",
        "Crunch" to "Crunches",

        // ---- Cardio (mostly bodyweight / no images in DB — leave null)
        "Jump rope" to "Rope_Jumping",
        "Rowing machine" to "Rowing_Stationary",
        "Stationary bike" to "Stationary_Bike_Run_v._2",
    )

    /** @return image URL for [exerciseName] or `null` if no mapping exists. */
    /** @return start-position image URL (`0.jpg`) for [exerciseName], or null. */
    fun urlFor(exerciseName: String): String? =
        nameToSlug[exerciseName]?.let { "$BASE$it/0.jpg" }

    /** @return end-position image URL (`1.jpg`) for [exerciseName], or null. */
    fun secondaryUrlFor(exerciseName: String): String? =
        nameToSlug[exerciseName]?.let { "$BASE$it/1.jpg" }
}


