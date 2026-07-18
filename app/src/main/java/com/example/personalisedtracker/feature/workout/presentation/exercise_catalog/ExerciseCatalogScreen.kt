package com.example.personalisedtracker.feature.workout.presentation.exercise_catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import com.example.personalisedtracker.core.ui.SlimTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.personalisedtracker.feature.workout.domain.model.Equipment
import com.example.personalisedtracker.feature.workout.domain.model.Exercise
import com.example.personalisedtracker.feature.workout.domain.model.ExerciseCategory

/**
 * Shared catalog screen. In picker mode (driven by VM.isPickerMode) it returns
 * the selected ids to the previous screen via SavedStateHandle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCatalogScreen(
    onBack: () -> Unit,
    onConfirmPick: (ids: LongArray) -> Unit,
    viewModel: ExerciseCatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SlimTopAppBar(
                title = if (viewModel.isPickerMode) "Pick exercises" else "Exercise catalog",
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    if (viewModel.isPickerMode && state.selectedIds.isNotEmpty()) {
                        TextButton(onClick = { onConfirmPick(state.selectedIds.toLongArray()) }) {
                            Text("Add (${state.selectedIds.size})")
                        }
                    }
                    IconButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, "Add custom") }
                },
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.filters.query,
                onValueChange = viewModel::setQuery,
                label = { Text("Search") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(12.dp),
            )
            // Category chips
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(
                    selected = state.filters.category == null,
                    onClick = { viewModel.setCategory(null) },
                    label = { Text("All") },
                )
                ExerciseCategory.entries.forEach { c ->
                    FilterChip(
                        selected = state.filters.category == c,
                        onClick = { viewModel.setCategory(if (state.filters.category == c) null else c) },
                        label = { Text(c.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(
                    selected = state.filters.equipment == null,
                    onClick = { viewModel.setEquipment(null) },
                    label = { Text("Any equipment") },
                )
                Equipment.entries.forEach { e ->
                    FilterChip(
                        selected = state.filters.equipment == e,
                        onClick = { viewModel.setEquipment(if (state.filters.equipment == e) null else e) },
                        label = { Text(e.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(state.exercises, key = { it.id }) { ex ->
                    ExerciseRow(
                        exercise = ex,
                        selected = ex.id in state.selectedIds,
                        pickerMode = viewModel.isPickerMode,
                        onClick = { viewModel.toggle(ex.id) },
                    )
                }
            }
        }
    }

    if (showAdd) {
        AddCustomDialog(
            onDismiss = { showAdd = false },
            onAdd = { name, cat, eq ->
                viewModel.addCustom(name, cat, eq)
                showAdd = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseRow(
    exercise: Exercise,
    selected: Boolean,
    pickerMode: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            com.example.personalisedtracker.feature.workout.presentation.active_workout.ExerciseThumb(
                url = exercise.imageUrl,
                size = 56.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${exercise.category.name.lowercase().replaceFirstChar { it.uppercase() }} · " +
                        exercise.equipment.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (pickerMode) {
                IconButton(onClick = onClick) {
                    Icon(
                        if (selected) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, ExerciseCategory, Equipment) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf(ExerciseCategory.OTHER) }
    var eq by remember { mutableStateOf(Equipment.OTHER) }
    var catOpen by remember { mutableStateOf(false) }
    var eqOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add custom exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                ExposedDropdownMenuBox(expanded = catOpen, onExpandedChange = { catOpen = it }) {
                    OutlinedTextField(
                        value = cat.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catOpen) },
                        modifier = Modifier.menuAnchor(),
                    )
                    DropdownMenu(expanded = catOpen, onDismissRequest = { catOpen = false }) {
                        ExerciseCategory.entries.forEach {
                            DropdownMenuItem(text = { Text(it.name) }, onClick = { cat = it; catOpen = false })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = eqOpen, onExpandedChange = { eqOpen = it }) {
                    OutlinedTextField(
                        value = eq.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Equipment") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eqOpen) },
                        modifier = Modifier.menuAnchor(),
                    )
                    DropdownMenu(expanded = eqOpen, onDismissRequest = { eqOpen = false }) {
                        Equipment.entries.forEach {
                            DropdownMenuItem(text = { Text(it.name) }, onClick = { eq = it; eqOpen = false })
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(name, cat, eq) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

