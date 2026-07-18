package com.example.personalisedtracker.core.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * App-wide compact top bar. M3's default [TopAppBar] is 64 dp tall plus the
 * status-bar inset which felt chunky across the tab destinations of this app.
 *
 * This wrapper trims the bar height to **48 dp** (status-bar inset is still
 * respected automatically) and downsizes the title typography to
 * [MaterialTheme.typography.titleMedium] for a tighter visual rhythm.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlimTopAppBar(
    title: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
        },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        scrollBehavior = scrollBehavior,
        expandedHeight = 48.dp,
    )
}

