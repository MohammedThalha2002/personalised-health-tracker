package com.example.personalisedtracker.feature.sleep.domain.model

data class SleepEntry(
    val id: Long,
    val date: Int,
    val hours: Double,
    val quality1to5: Int,
    val note: String,
)

