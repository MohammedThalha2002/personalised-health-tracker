package com.example.personalisedtracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt-enabled [Application] entry point. Owns the process-wide DI graph
 * (Room, repositories, use cases). Single-activity architecture.
 */
@HiltAndroidApp
class TrackerApplication : Application()

