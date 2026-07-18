package com.example.personalisedtracker.feature.habit.domain.usecase

import com.example.personalisedtracker.feature.habit.domain.repository.HabitRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SetHabitCompletionUseCaseTest {

    @Test
    fun `invoke delegates to repository`() = runTest {
        val repo = mockk<HabitRepository>(relaxed = true)
        val useCase = SetHabitCompletionUseCase(repo)

        useCase.invoke(habitId = 5L, date = 20260514, completed = true)

        coVerify(exactly = 1) { repo.setCompletion(5L, 20260514, true) }
    }
}

