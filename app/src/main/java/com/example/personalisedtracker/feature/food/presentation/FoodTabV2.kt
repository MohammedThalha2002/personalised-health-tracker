package com.example.personalisedtracker.feature.food.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.personalisedtracker.core.ui.SlimTopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.personalisedtracker.feature.food.domain.model.FoodEntry
import com.example.personalisedtracker.feature.food.domain.model.FoodMeal
import com.example.personalisedtracker.feature.food.domain.model.MacroMath
import com.example.personalisedtracker.feature.food.domain.model.MealTemplate

/**
 * Food tab — three logging paths, in order of priority:
 *   1. One-tap template chips per meal slot   (used 90% of the time)
 *   2. Quick-macro bottom sheet  (P/F/C → kcal auto)
 *   3. Manage templates (full-screen CRUD)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodTabV2(viewModel: FoodViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showQuick by remember { mutableStateOf(false) }
    var showManage by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            MacrosTodayCard(
                p = state.totals.proteinG,
                f = state.totals.fatG,
                c = state.totals.carbsG,
                kcal = state.totals.caloriesKcal,
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FoodMeal.entries.forEach { meal ->
                    val mealEntries = state.entries.filter { it.meal == meal }
                    val mealTemplates = state.templates.filter { it.meal == meal }
                    if (mealEntries.isEmpty() && mealTemplates.isEmpty()) return@forEach

                    item(key = "header_${meal.name}") {
                        Text(
                            meal.label(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    if (mealTemplates.isNotEmpty()) {
                        item(key = "templates_${meal.name}") {
                            TemplateChipRow(
                                templates = mealTemplates,
                                onTap = { viewModel.logTemplate(it.id) },
                            )
                        }
                    }
                    items(mealEntries, key = { it.id }) { e ->
                        FoodEntryRow(e, onDelete = { viewModel.delete(e.id) })
                    }
                }

                item(key = "manage_row") {
                    OutlinedButton(
                        onClick = { showManage = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 80.dp),
                    ) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Manage templates")
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showQuick = true },
            icon = { Icon(Icons.Default.Add, null) },
            text = { Text("Quick macros") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding(),
        )
    }

    if (showQuick) {
        QuickMacrosSheet(
            onDismiss = { showQuick = false },
            onAdd = { meal, desc, kcal, p, f, c ->
                viewModel.quickAdd(meal, desc, kcal, p, f, c)
                showQuick = false
            },
        )
    }
    if (showManage) {
        ManageTemplatesScreen(
            templates = state.templates,
            onDismiss = { showManage = false },
            onSave = viewModel::saveTemplate,
            onDelete = viewModel::deleteTemplate,
        )
    }
}

private fun FoodMeal.label(): String = name.lowercase().replaceFirstChar { it.uppercase() }

/* ----------------------------- Macros card ----------------------------- */

@Composable
private fun MacrosTodayCard(p: Int, f: Int, c: Int, kcal: Int) {
    Card(
        Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Today", style = MaterialTheme.typography.labelMedium)
            Text(
                "$kcal kcal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                MacroPill("P", p)
                MacroPill("F", f)
                MacroPill("C", c)
            }
        }
    }
}

@Composable
private fun MacroPill(label: String, value: Int) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text("${value}g", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

/* ----------------------------- Template chip row ----------------------------- */

@Composable
private fun TemplateChipRow(templates: List<MealTemplate>, onTap: (MealTemplate) -> Unit) {
    val chunks = templates.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        chunks.forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pair.forEach { t ->
                    AssistChip(
                        onClick = { onTap(t) },
                        label = {
                            Column {
                                Text(
                                    t.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    "${t.caloriesKcal} · ${t.proteinG}P / ${t.fatG}F / ${t.carbsG}C",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/* ----------------------------- Entry rows ----------------------------- */

@Composable
private fun FoodEntryRow(e: FoodEntry, onDelete: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(e.description.ifBlank { "Quick macros" }, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${e.approxCalories} kcal · ${e.approxProteinG}P / ${e.fatG}F / ${e.carbsG}C",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/* ----------------------------- Quick macros — bottom sheet ----------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickMacrosSheet(
    onDismiss: () -> Unit,
    onAdd: (FoodMeal, String, Int, Int, Int, Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var meal by remember { mutableStateOf(FoodMeal.SNACK) }
    var desc by remember { mutableStateOf("") }
    var p by remember { mutableStateOf("") }
    var f by remember { mutableStateOf("") }
    var c by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var kcalUserEdited by remember { mutableStateOf(false) }

    LaunchedEffect(p, f, c, kcalUserEdited) {
        if (!kcalUserEdited) {
            val auto = MacroMath.kcalFrom(p.toIntOrNull() ?: 0, f.toIntOrNull() ?: 0, c.toIntOrNull() ?: 0)
            kcal = if (auto == 0) "" else auto.toString()
        }
    }

    val canSubmit = (kcal.toIntOrNull() ?: 0) > 0 ||
        (p.toIntOrNull() ?: 0) > 0 || (f.toIntOrNull() ?: 0) > 0 || (c.toIntOrNull() ?: 0) > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Quick macros", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                FoodMeal.entries.forEach { m ->
                    FilterChip(
                        selected = meal == m,
                        onClick = { meal = m },
                        label = { Text(m.label()) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Note (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MacroField("Protein g", p, Modifier.weight(1f)) { p = it }
                MacroField("Fat g", f, Modifier.weight(1f)) { f = it }
                MacroField("Carbs g", c, Modifier.weight(1f)) { c = it }
            }
            OutlinedTextField(
                value = kcal,
                onValueChange = { kcal = it.filter(Char::isDigit).take(4); kcalUserEdited = true },
                label = { Text("kcal (auto from P/F/C)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(
                    onClick = {
                        onAdd(
                            meal, desc,
                            kcal.toIntOrNull() ?: 0,
                            p.toIntOrNull() ?: 0,
                            f.toIntOrNull() ?: 0,
                            c.toIntOrNull() ?: 0,
                        )
                    },
                    enabled = canSubmit,
                    modifier = Modifier.weight(1f),
                ) { Text("Add") }
            }
        }
    }
}

@Composable
private fun MacroField(label: String, value: String, modifier: Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter(Char::isDigit).take(3)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

/* ----------------------------- Manage templates — full-screen dialog ----------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageTemplatesScreen(
    templates: List<MealTemplate>,
    onDismiss: () -> Unit,
    onSave: (MealTemplate) -> Unit,
    onDelete: (Long) -> Unit,
) {
    var editing by remember { mutableStateOf<MealTemplate?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Scaffold(
                topBar = {
                    SlimTopAppBar(
                        title = "Meal templates",
                        navigationIcon = {
                            IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, "Close") }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = {
                            editing = MealTemplate(
                                id = 0, name = "", meal = FoodMeal.BREAKFAST,
                                items = emptyList(), proteinG = 0, fatG = 0,
                                carbsG = 0, caloriesKcal = 0,
                            )
                        },
                        icon = { Icon(Icons.Default.Add, null) },
                        text = { Text("New template") },
                    )
                },
                containerColor = MaterialTheme.colorScheme.background,
            ) { pad ->
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 12.dp, end = 12.dp,
                        top = pad.calculateTopPadding() + 4.dp,
                        bottom = pad.calculateBottomPadding() + 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(templates, key = { it.id }) { t ->
                        Card(
                            Modifier.fillMaxWidth().clickable { editing = t },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(t.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${t.meal.label()} · ${t.caloriesKcal} kcal · ${t.proteinG}P / ${t.fatG}F / ${t.carbsG}C",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (t.items.isNotEmpty()) {
                                        Text(
                                            t.items.joinToString(" · "),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                IconButton(onClick = { onDelete(t.id) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { current ->
        TemplateEditorSheet(
            initial = current,
            onDismiss = { editing = null },
            onSave = {
                onSave(it)
                editing = null
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateEditorSheet(
    initial: MealTemplate,
    onDismiss: () -> Unit,
    onSave: (MealTemplate) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(initial.name) }
    var meal by remember { mutableStateOf(initial.meal) }
    var itemsText by remember { mutableStateOf(initial.items.joinToString("\n")) }
    var p by remember { mutableStateOf(initial.proteinG.takeIf { it > 0 }?.toString() ?: "") }
    var f by remember { mutableStateOf(initial.fatG.takeIf { it > 0 }?.toString() ?: "") }
    var c by remember { mutableStateOf(initial.carbsG.takeIf { it > 0 }?.toString() ?: "") }
    var kcal by remember { mutableStateOf(initial.caloriesKcal.takeIf { it > 0 }?.toString() ?: "") }
    var kcalEdited by remember { mutableStateOf(initial.caloriesKcal > 0) }

    LaunchedEffect(p, f, c, kcalEdited) {
        if (!kcalEdited) {
            val auto = MacroMath.kcalFrom(p.toIntOrNull() ?: 0, f.toIntOrNull() ?: 0, c.toIntOrNull() ?: 0)
            kcal = if (auto == 0) "" else auto.toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (initial.id == 0L) "New template" else "Edit template",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Name (e.g. Standard breakfast)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                FoodMeal.entries.forEach { m ->
                    FilterChip(
                        selected = meal == m,
                        onClick = { meal = m },
                        label = { Text(m.label()) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            OutlinedTextField(
                value = itemsText,
                onValueChange = { itemsText = it },
                label = { Text("Items (one per line)") },
                minLines = 3, maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MacroField("Protein g", p, Modifier.weight(1f)) { p = it }
                MacroField("Fat g", f, Modifier.weight(1f)) { f = it }
                MacroField("Carbs g", c, Modifier.weight(1f)) { c = it }
            }
            OutlinedTextField(
                value = kcal,
                onValueChange = { kcal = it.filter(Char::isDigit).take(4); kcalEdited = true },
                label = { Text("kcal") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(
                    onClick = {
                        onSave(
                            initial.copy(
                                name = name.trim(),
                                meal = meal,
                                items = itemsText.lines().map { it.trim() }.filter { it.isNotEmpty() },
                                proteinG = p.toIntOrNull() ?: 0,
                                fatG = f.toIntOrNull() ?: 0,
                                carbsG = c.toIntOrNull() ?: 0,
                                caloriesKcal = kcal.toIntOrNull() ?: 0,
                            )
                        )
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f),
                ) { Text("Save") }
            }
        }
    }
}
