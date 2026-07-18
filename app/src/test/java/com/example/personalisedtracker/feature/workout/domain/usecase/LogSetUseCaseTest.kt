package com.example.personalisedtracker.feature.workout.domain.usecase

import com.example.personalisedtracker.feature.workout.domain.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LogSetUseCaseTest {

    private val repo = mockk<WorkoutRepository>(relaxed = true)
    private val useCase = LogSetUseCase(repo)

    @Test
    fun `invoke delegates to repository with correct args`() = runTest {
        coEvery {
            repo.addSet(1L, 2L, 3, 10, 50.0, false, 0)
        } returns 42L

        val id = useCase(
            workoutId = 1L,
            exerciseId = 2L,
            setNumber = 3,
            reps = 10,
            weightKg = 50.0,
            isWarmup = false,
            orderIndex = 0,
        )

        assert(id == 42L)
        coVerify { repo.addSet(1L, 2L, 3, 10, 50.0, false, 0) }
    }
}

