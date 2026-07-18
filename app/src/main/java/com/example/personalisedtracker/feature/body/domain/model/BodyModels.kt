package com.example.personalisedtracker.feature.body.domain.model

data class BodyWeight(
    val id: Long,
    val date: Int,
    val weightKg: Double,
    val note: String,
)

data class WaistMeasurement(
    val id: Long,
    val date: Int,
    val waistCm: Double,
)

/** A single InBody scan in domain form (all kg). */
data class InBodyScan(
    val id: Long,
    val date: Int,
    val rawTimestamp: Long,
    val weightKg: Double,
    val skeletalMuscleMassKg: Double?,
    val bodyFatMassKg: Double?,
    val bmi: Double?,
    val bodyFatPercent: Double?,
    val basalMetabolicRateKcal: Double?,
    val inbodyScore: Double?,
    val visceralFatLevel: Double?,
    val waistHipRatio: Double?,
    val waistCircumferenceCm: Double?,
    val trunkFatKg: Double?,
    val smiKgPerM2: Double?,
)

