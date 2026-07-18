package com.example.personalisedtracker.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.personalisedtracker.feature.body.presentation.BodyScreen
import com.example.personalisedtracker.feature.habit.presentation.HabitsScreen
import com.example.personalisedtracker.feature.logs.presentation.LogsScreen
import com.example.personalisedtracker.feature.settings.presentation.SettingsScreen
import com.example.personalisedtracker.feature.workout.presentation.active_workout.ActiveWorkoutScreen
import com.example.personalisedtracker.feature.workout.presentation.dashboard.DashboardScreen
import com.example.personalisedtracker.feature.workout.presentation.exercise_catalog.ExerciseCatalogScreen
import com.example.personalisedtracker.feature.workout.presentation.history.WorkoutHistoryScreen
import com.example.personalisedtracker.feature.workout.presentation.routine_editor.RoutineEditorScreen
import com.example.personalisedtracker.feature.workout.presentation.routines.RoutinesScreen

private data class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val tabs = listOf(
    BottomTab(Routes.DASHBOARD, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomTab(Routes.WORKOUTS, "Workouts", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    BottomTab(Routes.BODY, "Body", Icons.Filled.MonitorWeight, Icons.Outlined.MonitorWeight),
    BottomTab(Routes.LOGS, "Logs", Icons.Filled.Notes, Icons.Outlined.Notes),
    BottomTab(Routes.HABITS, "Habits", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    BottomTab(Routes.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun AppNavHost(nav: NavHostController = rememberNavController()) {
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                ) {
                    tabs.forEach { tab ->
                        val selected = currentRoute?.startsWith(tab.route) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label,
                                )
                            },
                            label = {
                                Text(
                                    tab.label,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onStartQuickWorkout = { nav.navigate(Routes.WORKOUTS) },
                    onResumeActive = { nav.navigate(Routes.activeWorkout(it)) },
                    onOpenWorkouts = { nav.navigate(Routes.WORKOUT_HISTORY) },
                    onOpenBody = { nav.navigate(Routes.BODY) },
                    onOpenLogs = { nav.navigate(Routes.LOGS) },
                    onOpenHabits = { nav.navigate(Routes.HABITS) },
                )
            }
            composable(Routes.WORKOUTS) {
                RoutinesScreen(
                    onStartWorkout = { wid -> nav.navigate(Routes.activeWorkout(wid)) },
                    onEditRoutine = { id -> nav.navigate(Routes.routineEditor(id)) },
                    onOpenHistory = { nav.navigate(Routes.WORKOUT_HISTORY) },
                    onResumeActive = { nav.navigate(Routes.activeWorkout(it)) },
                )
            }
            composable(Routes.BODY) { BodyScreen() }
            composable(Routes.LOGS) { LogsScreen() }
            composable(Routes.HABITS) { HabitsScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }

            composable(Routes.WORKOUT_HISTORY) {
                WorkoutHistoryScreen(
                    onBack = { nav.popBackStack() },
                    onOpen = { /* Phase 1: read-only detail piggybacks on active screen for now */
                        nav.navigate(Routes.activeWorkout(it))
                    },
                )
            }
            composable(
                route = "${Routes.ACTIVE_WORKOUT}/{workoutId}",
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
            ) {
                ActiveWorkoutScreen(
                    onFinished = { nav.popBackStack() },
                    onPickExercise = { wid ->
                        nav.navigate(Routes.exerciseCatalog(workoutId = wid))
                    },
                )
            }
            composable(
                route = "${Routes.ROUTINE_EDITOR}?routineId={routineId}",
                arguments = listOf(navArgument("routineId") { type = NavType.StringType; nullable = true; defaultValue = "" }),
            ) { entry ->
                RoutineEditorScreen(
                    backStackEntry = entry,
                    onNavigateBack = { nav.popBackStack() },
                    onPickExercises = {
                        // pass routineId so picker enters picker mode
                        val rid = entry.arguments?.getString("routineId") ?: ""
                        nav.navigate(Routes.exerciseCatalog(routineId = rid.toLongOrNull() ?: -1L))
                    },
                )
            }
            composable(
                route = "${Routes.EXERCISE_CATALOG}?routineId={routineId}&workoutId={workoutId}",
                arguments = listOf(
                    navArgument("routineId") { type = NavType.StringType; nullable = true; defaultValue = "" },
                    navArgument("workoutId") { type = NavType.StringType; nullable = true; defaultValue = "" },
                ),
            ) {
                ExerciseCatalogScreen(
                    onBack = { nav.popBackStack() },
                    onConfirmPick = { ids ->
                        nav.previousBackStackEntry?.savedStateHandle?.set("picked_exercise_ids", ids)
                        nav.popBackStack()
                    },
                )
            }
        }
    }
}

private fun shouldShowBottomBar(route: String?): Boolean {
    if (route == null) return true
    return tabs.any { route.startsWith(it.route) && !route.contains("/") }
}


