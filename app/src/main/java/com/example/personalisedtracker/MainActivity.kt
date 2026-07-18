package com.example.personalisedtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.personalisedtracker.core.navigation.AppNavHost
import com.example.personalisedtracker.feature.workout.data.seed.DatabaseSeeder
import com.example.personalisedtracker.ui.theme.PersonalisedTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * The single Activity. Hosts the entire Compose tree via [AppNavHost] and
 * triggers idempotent first-launch seeding through [DatabaseSeeder].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var seeder: DatabaseSeeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch { seeder.seedIfNeeded() }
        setContent {
            PersonalisedTrackerTheme {
                AppNavHost()
            }
        }
    }
}
