package com.example.personalisedtracker.feature.workout.presentation.routines

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.example.personalisedtracker.core.ui.SlimTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.personalisedtracker.feature.workout.domain.model.Routine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RoutinesScreen(
    onStartWorkout: (workoutId: Long) -> Unit,
    onEditRoutine: (routineId: Long?) -> Unit,
    onOpenHistory: () -> Unit,
    onResumeActive: (workoutId: Long) -> Unit,
    viewModel: RoutinesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            SlimTopAppBar(
                title = "Routines",
                actions = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Default.MoreVert, contentDescription = "History")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEditRoutine(null) },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New routine") },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            state.activeWorkoutId?.let { wid ->
                Card(
                    Modifier.fillMaxWidth().padding(12.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp).combinedClickable(
                            onClick = { onResumeActive(wid) }
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Text(
                            "Resume active workout",
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.routines, key = { it.id }) { r ->
                    RoutineRow(
                        routine = r,
                        onStart = {
                            scope.launch {
                                val wid = viewModel.startFromRoutine(r.id)
                                onStartWorkout(wid)
                            }
                        },
                        onEdit = { onEditRoutine(r.id) },
                        onArchive = { viewModel.archive(r.id) },
                    )
                }
                if (state.routines.isEmpty()) {
                    item {
                        Text(
                            "No routines yet. Tap + to create one.",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoutineRow(
    routine: Routine,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Card(
        Modifier.fillMaxWidth().combinedClickable(
            onClick = onStart,
            onLongClick = { menu = true },
        ),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(routine.name, style = MaterialTheme.typography.titleMedium)
                routine.dayOfWeek?.let {
                    Text(dayName(it), style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onStart) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start")
            }
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("Edit") }, onClick = { menu = false; onEdit() })
                DropdownMenuItem(text = { Text("Archive") }, onClick = { menu = false; onArchive() })
            }
        }
    }
}

private fun dayName(d: Int): String = when (d) {
    1 -> "Monday"; 2 -> "Tuesday"; 3 -> "Wednesday"; 4 -> "Thursday"
    5 -> "Friday"; 6 -> "Saturday"; 7 -> "Sunday"; else -> ""
}

