package com.example.personalisedtracker.feature.workout.presentation.routine_editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.personalisedtracker.core.ui.SlimTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavBackStackEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorScreen(
    backStackEntry: NavBackStackEntry,
    onNavigateBack: () -> Unit,
    onPickExercises: () -> Unit,
    viewModel: RoutineEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Receive selected exercise ids from the picker via SavedStateHandle.
    LaunchedEffect(backStackEntry) {
        backStackEntry.savedStateHandle
            .getStateFlow<LongArray?>("picked_exercise_ids", null)
            .collect { ids ->
                if (ids != null && ids.isNotEmpty()) {
                    val resolved = viewModel.resolveExercises(ids.toList())
                    viewModel.addExercises(resolved)
                    backStackEntry.savedStateHandle["picked_exercise_ids"] = null
                }
            }
    }

    Scaffold(
        topBar = {
            SlimTopAppBar(
                title = if (state.routineId == 0L) "New routine" else "Edit routine",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.Close, "Close") }
                },
                actions = {
                    TextButton(onClick = {
                        scope.launch { if (viewModel.save()) onNavigateBack() }
                    }) { Text("Save") }
                },
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::setName,
                label = { Text("Routine name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun").forEach { (n, label) ->
                    FilterChip(
                        selected = state.dayOfWeek == n,
                        onClick = { viewModel.setDay(if (state.dayOfWeek == n) null else n) },
                        label = { Text(label) },
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 6.dp),
            ) {
                itemsIndexed(state.exercises, key = { _, it -> it.exercise.id }) { idx, item ->
                    Card {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.exercise.name, style = MaterialTheme.typography.titleSmall)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SetCountField(
                                        value = item.targetSets,
                                        onChange = { v -> viewModel.updateItem(idx) { it.copy(targetSets = v) } },
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = item.targetRepRange,
                                        onValueChange = { v -> viewModel.updateItem(idx) { it.copy(targetRepRange = v) } },
                                        label = { Text("reps") },
                                        singleLine = true,
                                        modifier = Modifier.width(120.dp),
                                    )
                                }
                            }
                            Column {
                                IconButton(onClick = { viewModel.move(idx, idx - 1) }, enabled = idx > 0) {
                                    Icon(Icons.Default.ArrowUpward, "Move up")
                                }
                                IconButton(
                                    onClick = { viewModel.move(idx, idx + 1) },
                                    enabled = idx < state.exercises.lastIndex,
                                ) { Icon(Icons.Default.ArrowDownward, "Move down") }
                            }
                            IconButton(onClick = { viewModel.removeExercise(idx) }) {
                                Icon(Icons.Default.Close, "Remove")
                            }
                        }
                    }
                }
                item {
                    OutlinedButton(
                        onClick = onPickExercises,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp))
                        Text("Add exercise")
                    }
                }
            }
        }
    }
}

@Composable
private fun SetCountField(value: Int, onChange: (Int) -> Unit) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it.filter(Char::isDigit).take(2)
            text.toIntOrNull()?.let(onChange)
        },
        label = { Text("sets") },
        singleLine = true,
        modifier = Modifier.width(80.dp),
    )
}

