package com.example.personalisedtracker.feature.body.data.mapper

import com.example.personalisedtracker.feature.body.data.entity.BodyWeightEntity
import com.example.personalisedtracker.feature.body.data.entity.InBodyScanEntity
import com.example.personalisedtracker.feature.body.data.entity.WaistMeasurementEntity
import com.example.personalisedtracker.feature.body.domain.model.BodyWeight
import com.example.personalisedtracker.feature.body.domain.model.InBodyScan
import com.example.personalisedtracker.feature.body.domain.model.WaistMeasurement

internal fun BodyWeightEntity.toDomain() = BodyWeight(id, date, weightKg, note)
internal fun WaistMeasurementEntity.toDomain() = WaistMeasurement(id, date, waistCm)

internal fun InBodyScanEntity.toDomain() = InBodyScan(
    id = id,
    date = date,
    rawTimestamp = rawTimestamp,
    weightKg = weightKg,
    skeletalMuscleMassKg = skeletalMuscleMassKg,
    bodyFatMassKg = bodyFatMassKg,
    bmi = bmi,
    bodyFatPercent = bodyFatPercent,
    basalMetabolicRateKcal = basalMetabolicRateKcal,
    inbodyScore = inbodyScore,
    visceralFatLevel = visceralFatLevel,
    waistHipRatio = waistHipRatio,
    waistCircumferenceCm = waistCircumferenceCm,
    trunkFatKg = trunkFatKg,
    smiKgPerM2 = smiKgPerM2,
)

