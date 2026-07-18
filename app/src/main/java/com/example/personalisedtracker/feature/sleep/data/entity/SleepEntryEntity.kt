package com.example.personalisedtracker.feature.sleep.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** One sleep entry per night. */
@Entity(tableName = "sleep_entries", indices = [Index("date", unique = true)])
data class SleepEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Int, // yyyyMMdd — the morning you woke up
    val hours: Double,
    val quality1to5: Int,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

