package com.example.personalisedtracker.feature.logs.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import com.example.personalisedtracker.core.ui.SlimTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.personalisedtracker.core.common.DateInt
import com.example.personalisedtracker.feature.food.domain.model.FoodEntry
import com.example.personalisedtracker.feature.food.domain.model.FoodMeal
import com.example.personalisedtracker.feature.food.presentation.FoodViewModel
import com.example.personalisedtracker.feature.sleep.domain.model.SleepEntry
import com.example.personalisedtracker.feature.sleep.presentation.SleepViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen() {
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            Column {
                SlimTopAppBar(title = "Logs")
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Sleep") })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Food") })
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> SleepTab()
                else -> com.example.personalisedtracker.feature.food.presentation.FoodTabV2()
            }
        }
    }
}

/* ----------------------------- Sleep tab ----------------------------- */

@Composable
private fun SleepTab(viewModel: SleepViewModel = hiltViewModel()) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { SleepHeatmap(entries) }
        item {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Log sleep") },
            )
        }
        items(entries, key = { it.id }) { e -> SleepRow(e, onDelete = { viewModel.delete(e.id) }) }
    }

    if (showDialog) {
        LogSleepDialog(
            onDismiss = { showDialog = false },
            onConfirm = { h, q, n ->
                viewModel.log(DateInt.today(), h, q, n); showDialog = false
            }
        )
    }
}

@Composable
private fun SleepHeatmap(entries: List<SleepEntry>) {
    val today = DateInt.today()
    val days = (0 until 30).map { DateInt.minusDays(today, it.toLong()) }.reversed()
    val byDate = entries.associateBy { it.date }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Last 30 days", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                days.forEach { d ->
                    val e = byDate[d]
                    val color = colorForHours(e?.hours)
                    Box(
                        Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color),
                    )
                }
            }
        }
    }
}

private fun colorForHours(hours: Double?): Color = when {
    hours == null -> Color(0xFF2B2B2B)
    hours >= 7.5 -> Color(0xFF1B5E20)
    hours >= 6.5 -> Color(0xFF388E3C)
    hours >= 5.5 -> Color(0xFFFBC02D)
    else -> Color(0xFFD32F2F)
}

@Composable
private fun SleepRow(e: SleepEntry, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(DateInt.format(e.date), style = MaterialTheme.typography.titleSmall)
                Text("${"%.1f".format(e.hours)} hrs · quality ${e.quality1to5}/5")
                if (e.note.isNotBlank()) Text(e.note, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Close, "Delete") }
        }
    }
}

@Composable
private fun LogSleepDialog(
    onDismiss: () -> Unit,
    onConfirm: (hours: Double, quality: Int, note: String) -> Unit,
) {
    var hours by remember { mutableStateOf("") }
    var quality by remember { mutableIntStateOf(3) }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log sleep") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Hours") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                Text("Quality: $quality / 5")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { i ->
                        FilterChip(selected = quality == i, onClick = { quality = i }, label = { Text("$i") })
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { hours.toDoubleOrNull()?.let { onConfirm(it, quality, note) } },
                enabled = hours.toDoubleOrNull() != null,
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/* ----------------------------- Food tab ----------------------------- */
// The food tab implementation lives in FoodTabV2.kt — it offers a templates-
// first flow with quick-macro fallback. We just delegate from LogsScreen.

