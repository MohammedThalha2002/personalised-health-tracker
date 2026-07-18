package com.example.personalisedtracker.feature.body.data.csv

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InBodyCsvParserTest {

    private val sampleCsv = """
        Date,Measurement device.,Weight(lb),Skeletal Muscle Mass(lb),Soft Lean Mass(lb),Body Fat Mass(lb),BMI(kg/m²),Percent Body Fat(%),Basal Metabolic Rate(kJ),InBody Score
        20260507091906,InBody 970,165.3,74.5,140.0,28.7,24.1,17.4,6700,82
        20260601100000,InBody 970,163.5,74.8,140.5,27.0,23.9,16.8,6720,84
    """.trimIndent()

    @Test
    fun `parses two rows with lb-to-kg conversion`() {
        val result = InBodyCsvParser.parse(sampleCsv)
        assertTrue("Errors: ${result.errors}", result.errors.isEmpty())
        assertEquals(2, result.rows.size)
        // 165.3 lb * 0.453592 ≈ 74.97 kg
        val first = result.rows[0]
        assertEquals(20260507, first.date)
        assertEquals(74.97, first.weightKg, 0.05)
        assertEquals(17.4, first.bodyFatPercent ?: 0.0, 0.001)
    }

    @Test
    fun `bad date row is rejected with an error`() {
        val bad = """
            Date,Measurement device.,Weight(lb)
            notADate,InBody 970,165.3
        """.trimIndent()
        val result = InBodyCsvParser.parse(bad)
        assertEquals(0, result.rows.size)
        assertEquals(1, result.errors.size)
    }
}

