package com.example.personalisedtracker.core.navigation

/** Centralised typed route ids. Keep simple — Compose Navigation 2.8 type-safe routes
 *  require Serializable classes which we can adopt later. */
object Routes {
    const val DASHBOARD = "dashboard"
    const val WORKOUTS = "workouts"
    const val BODY = "body"
    const val LOGS = "logs"
    const val HABITS = "habits"
    const val SETTINGS = "settings"

    const val ROUTINE_EDITOR = "routine_editor"           // ?routineId=...
    const val ACTIVE_WORKOUT = "active_workout"           // /{workoutId}
    const val WORKOUT_HISTORY = "workout_history"
    const val EXERCISE_CATALOG = "exercise_catalog"       // ?pickFor=routine|workout&workoutId=
    const val WORKOUT_DETAIL = "workout_detail"           // /{workoutId}

    fun routineEditor(routineId: Long?): String =
        if (routineId == null) "$ROUTINE_EDITOR?routineId=" else "$ROUTINE_EDITOR?routineId=$routineId"

    fun activeWorkout(workoutId: Long) = "$ACTIVE_WORKOUT/$workoutId"
    fun workoutDetail(workoutId: Long) = "$WORKOUT_DETAIL/$workoutId"
    fun exerciseCatalog(routineId: Long? = null, workoutId: Long? = null): String =
        "$EXERCISE_CATALOG?routineId=${routineId ?: ""}&workoutId=${workoutId ?: ""}"
}

