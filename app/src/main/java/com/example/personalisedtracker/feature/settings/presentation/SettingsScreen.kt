package com.example.personalisedtracker.feature.settings.presentation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.example.personalisedtracker.core.ui.SlimTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val event by viewModel.events.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    val pickJson = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.importJson(it)
        }
    }

    LaunchedEffect(event) {
        when (val e = event) {
            is SettingsEvent.ShareJson -> {
                shareUri(context, e.uri, "application/json", "Share tracker JSON export")
                viewModel.consumed()
            }
            is SettingsEvent.ShareMarkdown -> {
                shareUri(context, e.uri, "text/markdown", "Share tracker Markdown report")
                viewModel.consumed()
            }
            is SettingsEvent.ImportFinished -> {
                snackbar.showSnackbar(
                    "Imported: ${e.summary.workouts} workouts · ${e.summary.bodyWeights} weights · " +
                        "${e.summary.inbodyScans} scans · ${e.summary.sleep} sleep · ${e.summary.food} food."
                )
                viewModel.consumed()
            }
            is SettingsEvent.Error -> {
                snackbar.showSnackbar("Error: ${e.message}")
                viewModel.consumed()
            }
            null -> Unit
        }
    }

    Scaffold(
        topBar = { SlimTopAppBar(title = "Settings") },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Backup & restore", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Local-only data. Use Export regularly and keep the file in your cloud drive.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Button(
                        onClick = viewModel::exportJson,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    ) { Text("Export all data (JSON)") }
                    OutlinedButton(
                        onClick = { pickJson.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    ) { Text("Import from JSON") }
                }
            }
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Markdown report", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Generate a human + AI-readable summary for the selected window.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(7L to "7d", 30L to "30d", 90L to "90d").forEach { (d, label) ->
                            OutlinedButton(onClick = { viewModel.exportMarkdown(d) }) { Text("Share $label") }
                        }
                    }
                }
            }
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("About", style = MaterialTheme.typography.titleMedium)
                    Text("Personalised Tracker · v1.0.0")
                    Text(
                        "Phase 1 (Workouts), Phase 2 (Body+Sleep), Phase 3 (Food+Habits), " +
                            "Phase 4 (Markdown report) — all enabled.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

private fun shareUri(
    context: android.content.Context,
    uri: android.net.Uri,
    mime: String,
    chooserTitle: String,
) {
    val share = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    ContextCompat.startActivity(context, Intent.createChooser(share, chooserTitle), null)
}

