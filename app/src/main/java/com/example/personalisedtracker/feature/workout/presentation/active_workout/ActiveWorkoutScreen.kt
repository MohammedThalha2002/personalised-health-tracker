package com.example.personalisedtracker.feature.workout.presentation.active_workout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.personalisedtracker.core.ui.SlimTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.personalisedtracker.feature.workout.domain.model.WorkoutExerciseGroup
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    onFinished: () -> Unit,
    onPickExercise: (workoutId: Long) -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel(),
) {
    val workout by viewModel.state.collectAsStateWithLifecycle()
    var confirmFinish by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    var restRemaining by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(restRemaining) {
        val r = restRemaining ?: return@LaunchedEffect
        if (r <= 0) { restRemaining = null; return@LaunchedEffect }
        delay(1000)
        restRemaining = r - 1
    }

    Scaffold(
        topBar = {
            SlimTopAppBar(
                title = workout?.workout?.routineName ?: "Workout",
                navigationIcon = {
                    IconButton(onClick = { confirmDiscard = true }) {
                        Icon(Icons.Default.Close, "Discard")
                    }
                },
                actions = {
                    TextButton(onClick = { confirmFinish = true }) { Text("Finish") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        val data = workout
        if (data == null) {
            Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(Modifier.fillMaxSize().padding(padding)) {
            restRemaining?.let { secs ->
                RestTimerBar(seconds = secs, onCancel = { restRemaining = null })
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(data.groups, key = { it.exercise.id }) { group ->
                    ExerciseGroupCard(
                        group = group,
                        onLogSet = { reps, weight, warmup ->
                            viewModel.logSet(group.exercise.id, reps, weight, warmup)
                            restRemaining = group.restSeconds
                        },
                        onPrefill = { viewModel.prefillFor(group.exercise.id) },
                        onDeleteSet = viewModel::removeSet,
                    )
                }
                item {
                    OutlinedButton(
                        onClick = { onPickExercise(viewModel.workoutId) },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add exercise")
                    }
                }
            }
        }
    }

    if (confirmFinish) {
        AlertDialog(
            onDismissRequest = { confirmFinish = false },
            title = { Text("Finish workout?") },
            text = { Text("This will end the current session.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmFinish = false
                    viewModel.finishWorkout(onFinished)
                }) { Text("Finish") }
            },
            dismissButton = { TextButton(onClick = { confirmFinish = false }) { Text("Cancel") } },
        )
    }
    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Discard workout?") },
            text = { Text("All logged sets in this session will be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    viewModel.discard(onFinished)
                }) { Text("Discard") }
            },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun RestTimerBar(seconds: Int, onCancel: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Rest ${seconds}s", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onCancel) { Text("Skip") }
            }
            LinearProgressIndicator(
                progress = { (seconds.coerceAtLeast(0) / 180f).coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ExerciseGroupCard(
    group: WorkoutExerciseGroup,
    onLogSet: (reps: Int, weight: Double, warmup: Boolean) -> Unit,
    onPrefill: suspend () -> Pair<Double, Int>?,
    onDeleteSet: (Long) -> Unit,
) {
    var weightText by remember(group.exercise.id) { mutableStateOf("") }
    var repsText by remember(group.exercise.id) { mutableStateOf("") }

    LaunchedEffect(group.exercise.id) {
        if (weightText.isEmpty() && repsText.isEmpty()) {
            onPrefill()?.let { (w, r) ->
                weightText = if (w == w.toInt().toDouble()) w.toInt().toString() else w.toString()
                repsText = r.toString()
            }
        }
    }

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            val hasMedia = !group.exercise.imageUrl.isNullOrBlank()
            // Collapsed by default — scrolling 5 animated banners would be busy
            // and waste data/battery. Tap the expand chevron to reveal.
            var heroExpanded by remember(group.exercise.id) { mutableStateOf(false) }

            // Title row: name + target on the left, expand toggle on the right.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        group.exercise.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "${group.targetSets} × ${group.targetRepRange}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (hasMedia) {
                    IconButton(
                        onClick = { heroExpanded = !heroExpanded },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = if (heroExpanded) Icons.Default.ExpandLess
                                          else Icons.Default.ExpandMore,
                            contentDescription = if (heroExpanded) "Hide demo" else "Show demo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Animated hero — only composed (and only running its animation
            // loop / network fetch) while expanded.
            AnimatedVisibility(visible = hasMedia && heroExpanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    ExerciseHero(
                        primaryUrl = group.exercise.imageUrl!!,
                        secondaryUrl = group.exercise.imageUrlSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                    )
                }
            }

            if (group.sets.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                group.sets.forEach { s ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "#${s.setNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.width(28.dp),
                        )
                        Text(
                            "${formatWeight(s.weightKg)} kg",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(72.dp),
                        )
                        Text("× ${s.reps}", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { onDeleteSet(s.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.Close,
                                "Remove set",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Input row — bigger fields, prominent confirm
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it.filter { ch -> ch.isDigit() || ch == '.' }.take(6) },
                    placeholder = { Text("kg") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                    textStyle = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = repsText,
                    onValueChange = { repsText = it.filter(Char::isDigit).take(3) },
                    placeholder = { Text("reps") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                    textStyle = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        val reps = repsText.toIntOrNull() ?: return@FilledIconButton
                        val w = weightText.toDoubleOrNull() ?: 0.0
                        onLogSet(reps, w, false)
                    },
                    modifier = Modifier.size(52.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(Icons.Default.Check, "Log set", modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

/**
 * Small thumbnail. Falls back to a tinted icon when no URL is mapped
 * (custom exercises, cardio, etc.).
 */
@Composable
internal fun ExerciseThumb(url: String?, size: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isNullOrBlank()) {
            ThumbFallback(size)
        } else {
            val context = LocalContext.current
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
                loading = { ThumbFallback(size) },
                error = { ThumbFallback(size) },
            )
        }
    }
}

@Composable
private fun ThumbFallback(size: androidx.compose.ui.unit.Dp) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(size),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size((size.value * 0.55f).dp),
            )
        }
    }
}

/**
 * Big hero banner shown above each exercise in the active workout. When a
 * [secondaryUrl] (end-position image) is available, automatically alternates
 * between the two frames every ~1.1 s with a fade so the user sees the motion.
 * Falls back gracefully when one or both URLs fail to load.
 */
@Composable
private fun ExerciseHero(
    primaryUrl: String,
    secondaryUrl: String?,
    modifier: Modifier = Modifier,
) {
    val hasTwoFrames = !secondaryUrl.isNullOrBlank()
    var showSecond by remember(primaryUrl, secondaryUrl) { mutableStateOf(false) }

    LaunchedEffect(primaryUrl, secondaryUrl) {
        if (!hasTwoFrames) return@LaunchedEffect
        while (true) {
            delay(1100)
            showSecond = !showSecond
        }
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        val context = LocalContext.current
        val currentUrl = if (showSecond && hasTwoFrames) secondaryUrl!! else primaryUrl

        AnimatedContent(
            targetState = currentUrl,
            transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(350)) },
            label = "exercise-hero",
        ) { url ->
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(false) // crossfade handled by AnimatedContent
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                error = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                },
            )
        }
    }
}

/** Pretty-prints kg without trailing `.0`. */
private fun formatWeight(kg: Double): String =
    if (kg == kg.toLong().toDouble()) kg.toLong().toString() else "%.2f".format(kg).trimEnd('0').trimEnd('.')



