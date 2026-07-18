package com.example.personalisedtracker.feature.workout.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.example.personalisedtracker.core.ui.SlimTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.personalisedtracker.feature.workout.domain.model.Workout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Home screen — Phase 1 only shows the workouts card meaningfully; other
 * cards display "Phase 2" placeholders to make the layout intent obvious.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onStartQuickWorkout: () -> Unit,
    onResumeActive: (Long) -> Unit,
    onOpenWorkouts: () -> Unit,
    onOpenBody: () -> Unit,
    onOpenLogs: () -> Unit,
    onOpenHabits: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val today = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date()) }

    Scaffold(
        topBar = { SlimTopAppBar(title = today) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val id = state.activeWorkoutId
                    if (id != null) onResumeActive(id) else onStartQuickWorkout()
                },
                icon = { Icon(Icons.Default.PlayArrow, null) },
                text = { Text(if (state.activeWorkoutId != null) "Resume" else "Start workout") },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LastWorkoutCard(state.lastWorkout, onClick = onOpenWorkouts, modifier = Modifier.weight(1f))
                StatCard(
                    title = "Today's weight",
                    value = state.todayWeightKg?.let { "%.1f kg".format(it) } ?: "Log weight",
                    onClick = onOpenBody,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Sleep last night",
                    value = state.lastSleepHours?.let { "%.1f hrs".format(it) } ?: "Log sleep",
                    onClick = onOpenLogs,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "Habits today",
                    value = if (state.habitsTotal > 0)
                        "${state.habitsDoneToday}/${state.habitsTotal} done" else "—",
                    onClick = onOpenHabits,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun LastWorkoutCard(workout: Workout?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.aspectRatio(1f).clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text("Last workout", style = MaterialTheme.typography.labelLarge)
            if (workout?.endedAt != null) {
                Column {
                    Text(workout.routineName, style = MaterialTheme.typography.titleMedium)
                    Text(daysAgo(workout.startedAt), style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Text("No workouts yet", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.aspectRatio(1f).clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun daysAgo(ts: Long): String {
    val d = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - ts)
    return when (d) {
        0L -> "Today"
        1L -> "Yesterday"
        else -> "$d days ago"
    }
}



