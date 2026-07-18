package com.example.personalisedtracker.feature.body.data.csv

import com.example.personalisedtracker.core.common.DateInt
import com.example.personalisedtracker.feature.body.data.entity.InBodyScanEntity

private const val LB_TO_KG = 0.453592

/**
 * Parses one InBody machine CSV. The header column count matches the spec
 * provided in the project brief — we map by **column name**, not by index,
 * so reorders or additional columns from a newer firmware don't break the
 * importer.
 */
internal object InBodyCsvParser {

    data class ParseResult(
        val rows: List<InBodyScanEntity>,
        val errors: List<String>,
    )

    fun parse(text: String): ParseResult {
        val lines = text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()
        if (lines.size < 2) return ParseResult(emptyList(), listOf("CSV has no data rows"))

        val header = splitCsvLine(lines.first()).map { it.normalize() }
        val rows = mutableListOf<InBodyScanEntity>()
        val errors = mutableListOf<String>()
        for ((i, raw) in lines.drop(1).withIndex()) {
            try {
                val cells = splitCsvLine(raw)
                if (cells.size != header.size) {
                    errors.add("Row ${i + 2}: ${cells.size} cells vs ${header.size} headers")
                    continue
                }
                val map = header.zip(cells).toMap()
                rows.add(rowFromMap(map))
            } catch (t: Throwable) {
                errors.add("Row ${i + 2}: ${t.message}")
            }
        }
        return ParseResult(rows, errors)
    }

    private fun rowFromMap(m: Map<String, String>): InBodyScanEntity {
        val rawDate = m["date"]?.trim().orEmpty()
        val (dateInt, ts) = DateInt.parseInbody(rawDate)
            ?: error("Invalid Date '$rawDate'")
        fun lb(name: String) = m[name]?.toDoubleOrNull()?.let { it * LB_TO_KG }
        fun n(name: String) = m[name]?.toDoubleOrNull()
        fun inchToCm(name: String) = m[name]?.toDoubleOrNull()?.let { it * 2.54 }
        return InBodyScanEntity(
            date = dateInt,
            rawTimestamp = ts,
            measurementDevice = m["measurement device."] ?: m["measurement device"] ?: "",
            weightKg = lb("weight(lb)") ?: error("Missing weight"),
            skeletalMuscleMassKg = lb("skeletal muscle mass(lb)"),
            softLeanMassKg = lb("soft lean mass(lb)"),
            bodyFatMassKg = lb("body fat mass(lb)"),
            bmi = n("bmi(kg/m²)"),
            bodyFatPercent = n("percent body fat(%)"),
            basalMetabolicRateKcal = n("basal metabolic rate(kj)")?.let { it / 4.184 },
            inbodyScore = n("inbody score"),
            rightArmLeanKg = lb("right arm lean mass(lb)"),
            leftArmLeanKg = lb("left arm lean mass(lb)"),
            trunkLeanKg = lb("trunk lean mass(lb)"),
            rightLegLeanKg = lb("right leg lean mass(lb)"),
            leftLegLeanKg = lb("left leg lean mass(lb)"),
            rightArmFatKg = lb("right arm fat mass(lb)"),
            leftArmFatKg = lb("left arm fat mass(lb)"),
            trunkFatKg = lb("trunk fat mass(lb)"),
            rightLegFatKg = lb("right leg fat mass(lb)"),
            leftLegFatKg = lb("left leg fat mass(lb)"),
            rightArmEcwRatio = n("right arm ecw ratio"),
            leftArmEcwRatio = n("left arm ecw ratio"),
            trunkEcwRatio = n("trunk ecw ratio"),
            rightLegEcwRatio = n("right leg ecw ratio"),
            leftLegEcwRatio = n("left leg ecw ratio"),
            waistHipRatio = n("waist hip ratio"),
            waistCircumferenceCm = inchToCm("waist circumference(inch)"),
            visceralFatAreaCm2 = n("visceral fat area(cm²)"),
            visceralFatLevel = n("visceral fat level(level)"),
            totalBodyWaterKg = lb("total body water(lb)"),
            intracellularWaterKg = lb("intracellular water(lb)"),
            extracellularWaterKg = lb("extracellular water(lb)"),
            ecwRatio = n("ecw ratio"),
            upperLower = n("upper-lower"),
            upper = n("upper"),
            lower = n("lower"),
            legMuscleLevel = n("leg muscle level(level)"),
            legLeanKg = lb("leg lean mass(lb)"),
            proteinKg = lb("protein(lb)"),
            mineralKg = lb("mineral(lb)"),
            boneMineralContentKg = lb("bone mineral content(lb)"),
            bodyCellMassKg = lb("body cell mass(lb)"),
            smiKgPerM2 = n("smi(kg/m²)"),
            phaseAngleDeg = n("whole body phase angle(°)") ?: n("whole body phase angle"),
        )
    }

    // Minimal RFC-4180-ish splitter: handles quoted commas; ignores escaping
    // edge cases that the InBody machine doesn't emit.
    private fun splitCsvLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuote = false
        for (c in line) {
            when {
                c == '"' -> inQuote = !inQuote
                c == ',' && !inQuote -> { out.add(sb.toString()); sb.clear() }
                else -> sb.append(c)
            }
        }
        out.add(sb.toString())
        return out
    }

    private fun String.normalize(): String = trim().lowercase()
}

