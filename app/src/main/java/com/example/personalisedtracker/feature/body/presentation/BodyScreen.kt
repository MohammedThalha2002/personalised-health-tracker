package com.example.personalisedtracker.feature.body.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.personalisedtracker.core.common.DateInt
import com.example.personalisedtracker.feature.body.domain.model.BodyWeight
import com.example.personalisedtracker.feature.body.domain.model.InBodyScan
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyScreen(viewModel: BodyViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val preview by viewModel.pendingPreview.collectAsStateWithLifecycle()
    var showWeight by remember { mutableStateOf(false) }
    var showWaist by remember { mutableStateOf(false) }

    val pickCsv = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.previewCsvImport(it) }
    }

    Scaffold(
        topBar = { SlimTopAppBar(title = "Body") },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { RangeChips(state.range, viewModel::setRange) }
            item { WeightChartCard(state.weights, state.range) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showWeight = true }, modifier = Modifier.weight(1f)) { Text("Log weight") }
                    OutlinedButton(
                        onClick = { pickCsv.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*")) },
                        modifier = Modifier.weight(1f),
                    ) { Text("Import InBody CSV") }
                }
            }
            state.latestScan?.let { item { LatestScanCard(it) } }
            item { WaistSection(state.waist.firstOrNull()?.waistCm, onLogWaist = { showWaist = true }) }
        }
    }

    if (showWeight) {
        NumberDialog(
            title = "Log weight (kg)",
            unitSuffix = "kg",
            initialValue = state.weights.firstOrNull()?.weightKg?.toString() ?: "",
            onDismiss = { showWeight = false },
            onConfirm = { v -> viewModel.logWeight(v); showWeight = false },
        )
    }
    if (showWaist) {
        NumberDialog(
            title = "Log waist (cm)",
            unitSuffix = "cm",
            initialValue = state.waist.firstOrNull()?.waistCm?.toString() ?: "",
            onDismiss = { showWaist = false },
            onConfirm = { v -> viewModel.logWaist(v); showWaist = false },
        )
    }

    preview?.let { p ->
        AlertDialog(
            onDismissRequest = viewModel::dismissPreview,
            title = { Text("Import preview") },
            text = {
                Column {
                    Text("New scans: ${p.newRows.size}")
                    Text("Duplicates skipped: ${p.duplicateDates.size}")
                    if (p.parseErrors.isNotEmpty()) {
                        Text("Errors:", style = MaterialTheme.typography.labelMedium)
                        p.parseErrors.take(5).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmImport, enabled = p.newRows.isNotEmpty()) {
                    Text("Import ${p.newRows.size}")
                }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissPreview) { Text("Cancel") } },
        )
    }
}

@Composable
private fun RangeChips(current: BodyRange, onSelect: (BodyRange) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        BodyRange.entries.forEach { r ->
            FilterChip(
                selected = current == r,
                onClick = { onSelect(r) },
                label = { Text(r.label) },
            )
        }
    }
}

@Composable
private fun WeightChartCard(weights: List<BodyWeight>, range: BodyRange) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Weight (kg)", style = MaterialTheme.typography.titleMedium)
            if (weights.size < 2) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Not enough data yet for last ${range.label}. Log a few days to see the chart.",
                    style = MaterialTheme.typography.bodySmall,
                )
                return@Column
            }
            val producer = remember(weights) { CartesianChartModelProducer() }
            val scope = rememberCoroutineScope()
            androidx.compose.runtime.LaunchedEffect(weights) {
                scope.launch {
                    producer.runTransaction {
                        lineSeries { series(weights.map { it.weightKg }) }
                    }
                }
            }
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(),
                ),
                modelProducer = producer,
                scrollState = rememberVicoScrollState(),
                modifier = Modifier.fillMaxWidth().height(180.dp),
            )
            val first = weights.first().weightKg
            val last = weights.last().weightKg
            val delta = last - first
            Text(
                "Δ ${"%+.1f".format(delta)} kg over ${range.label}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun LatestScanCard(scan: InBodyScan) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("InBody — ${DateInt.format(scan.date)}", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Cell("Weight", "${scan.weightKg.format1()} kg", Modifier.weight(1f))
                Cell("Body fat %", scan.bodyFatPercent?.let { "${it.format1()}%" } ?: "—", Modifier.weight(1f))
                Cell("SMM", scan.skeletalMuscleMassKg?.let { "${it.format1()} kg" } ?: "—", Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Cell("BMI", scan.bmi?.format1() ?: "—", Modifier.weight(1f))
                Cell("Visceral", scan.visceralFatLevel?.toInt()?.toString() ?: "—", Modifier.weight(1f))
                Cell("Score", scan.inbodyScore?.toInt()?.toString() ?: "—", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WaistSection(latestCm: Double?, onLogWaist: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Waist", style = MaterialTheme.typography.titleMedium)
            Text(latestCm?.let { "${it.format1()} cm" } ?: "No measurements yet")
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onLogWaist) { Text("Log waist") }
        }
    }
}

@Composable
private fun Cell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun NumberDialog(
    title: String,
    unitSuffix: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                label = { Text(unitSuffix) },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { text.toDoubleOrNull()?.let(onConfirm) ?: onDismiss() },
                enabled = text.toDoubleOrNull() != null,
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun Double.format1(): String = "%.1f".format(this)



