package com.example.personalisedtracker.feature.workout.presentation.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    onBack: () -> Unit,
    onOpen: (Long) -> Unit,
    viewModel: WorkoutHistoryViewModel = hiltViewModel(),
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            SlimTopAppBar(
                title = "History",
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (workouts.isEmpty()) {
                item { Text("No workouts yet.", modifier = Modifier.padding(24.dp)) }
            }
            items(workouts, key = { it.id }) { w ->
                Card(Modifier.fillMaxWidth().clickable { onOpen(w.id) }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(w.routineName, style = MaterialTheme.typography.titleMedium)
                        Text(formatDate(w.startedAt), style = MaterialTheme.typography.bodySmall)
                        w.endedAt?.let {
                            val mins = ((it - w.startedAt) / 60_000).coerceAtLeast(0)
                            Text("${mins} min", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(ts: Long): String =
    SimpleDateFormat("EEE, dd MMM yyyy · HH:mm", Locale.getDefault()).format(Date(ts))

