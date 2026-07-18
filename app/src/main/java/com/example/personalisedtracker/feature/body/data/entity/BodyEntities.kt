package com.example.personalisedtracker.feature.body.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per day (or per measurement). [date] is `yyyyMMdd` so we can sort
 * cheaply and avoid TZ ambiguity for the personal use case.
 */
@Entity(tableName = "body_weights", indices = [Index("date", unique = true)])
data class BodyWeightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Int, // yyyyMMdd
    val weightKg: Double,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

/** Optional waist tracking (not strictly part of an InBody scan). */
@Entity(tableName = "waist_measurements", indices = [Index("date", unique = true)])
data class WaistMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Int, // yyyyMMdd
    val waistCm: Double,
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * Full InBody scan row. We persist the **full schema** (in kg) so a future
 * report can mine any of these without re-importing. Lb → kg conversion
 * happens during CSV import (multiply by 0.453592).
 */
@Entity(tableName = "inbody_scans", indices = [Index("date", unique = true)])
data class InBodyScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Int, // yyyyMMdd
    val measurementDevice: String = "",
    val weightKg: Double,
    val skeletalMuscleMassKg: Double?,
    val softLeanMassKg: Double?,
    val bodyFatMassKg: Double?,
    val bmi: Double?,
    val bodyFatPercent: Double?,
    val basalMetabolicRateKcal: Double?,
    val inbodyScore: Double?,
    val rightArmLeanKg: Double?,
    val leftArmLeanKg: Double?,
    val trunkLeanKg: Double?,
    val rightLegLeanKg: Double?,
    val leftLegLeanKg: Double?,
    val rightArmFatKg: Double?,
    val leftArmFatKg: Double?,
    val trunkFatKg: Double?,
    val rightLegFatKg: Double?,
    val leftLegFatKg: Double?,
    val rightArmEcwRatio: Double?,
    val leftArmEcwRatio: Double?,
    val trunkEcwRatio: Double?,
    val rightLegEcwRatio: Double?,
    val leftLegEcwRatio: Double?,
    val waistHipRatio: Double?,
    val waistCircumferenceCm: Double?,
    val visceralFatAreaCm2: Double?,
    val visceralFatLevel: Double?,
    val totalBodyWaterKg: Double?,
    val intracellularWaterKg: Double?,
    val extracellularWaterKg: Double?,
    val ecwRatio: Double?,
    val upperLower: Double?,
    val upper: Double?,
    val lower: Double?,
    val legMuscleLevel: Double?,
    val legLeanKg: Double?,
    val proteinKg: Double?,
    val mineralKg: Double?,
    val boneMineralContentKg: Double?,
    val bodyCellMassKg: Double?,
    val smiKgPerM2: Double?,
    val phaseAngleDeg: Double?,
    val rawTimestamp: Long, // yyyyMMddhhmmss parsed to epoch ms
)

