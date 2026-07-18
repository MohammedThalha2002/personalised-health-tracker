package com.example.personalisedtracker.feature.habit.presentation

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.personalisedtracker.feature.habit.domain.model.HabitWithProgress

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitsScreen(viewModel: HabitsViewModel = hiltViewModel()) {
    val habits by viewModel.state.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { SlimTopAppBar(title = "Habits") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New habit") },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(habits, key = { it.habit.id }) { hp ->
                HabitRow(
                    item = hp,
                    onToggle = { v -> viewModel.toggle(hp.habit.id, v) },
                    onRename = { name -> viewModel.rename(hp.habit.id, name, hp.habit.sortOrder) },
                    onArchive = { viewModel.archive(hp.habit.id) },
                )
            }
            if (habits.isEmpty()) {
                item { Text("No habits yet.", modifier = Modifier.padding(24.dp)) }
            }
        }
    }

    if (showAdd) {
        TextDialog(
            title = "New habit",
            onDismiss = { showAdd = false },
            onConfirm = { viewModel.add(it); showAdd = false },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitRow(
    item: HabitWithProgress,
    onToggle: (Boolean) -> Unit,
    onRename: (String) -> Unit,
    onArchive: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    var rename by remember { mutableStateOf(false) }
    Card(
        Modifier.fillMaxWidth().combinedClickable(
            onClick = { onToggle(!item.doneToday) },
            onLongClick = { menu = true },
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.doneToday, onCheckedChange = onToggle)
                Column(Modifier.weight(1f)) {
                    Text(item.habit.name, style = MaterialTheme.typography.titleSmall)
                    Text("🔥 ${item.currentStreak}-day streak", style = MaterialTheme.typography.bodySmall)
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Rename") }, onClick = { menu = false; rename = true })
                    DropdownMenuItem(text = { Text("Archive") }, onClick = { menu = false; onArchive() })
                }
            }
            LinearProgressIndicator(
                progress = { item.completionRate30d },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
            Text(
                "${(item.completionRate30d * 100).toInt()}% (30d)",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
    if (rename) {
        TextDialog(
            title = "Rename habit",
            initial = item.habit.name,
            onDismiss = { rename = false },
            onConfirm = { onRename(it); rename = false },
        )
    }
}

@Composable
private fun TextDialog(
    title: String,
    initial: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true) },
        confirmButton = {
            TextButton(enabled = text.isNotBlank(), onClick = { onConfirm(text) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

