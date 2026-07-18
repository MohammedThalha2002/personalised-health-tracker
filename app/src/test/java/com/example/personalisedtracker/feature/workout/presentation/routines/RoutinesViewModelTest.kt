package com.example.personalisedtracker.feature.workout.presentation.routines

import app.cash.turbine.test
import com.example.personalisedtracker.feature.workout.domain.model.Routine
import com.example.personalisedtracker.feature.workout.domain.model.Workout
import com.example.personalisedtracker.feature.workout.domain.usecase.ArchiveRoutineUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.ObserveActiveWorkoutUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.ObserveRoutinesUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.StartAdHocWorkoutUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.StartWorkoutFromRoutineUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoutinesViewModelTest {

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `state exposes routines and active id`() = runTest {
        val routine = Routine(1L, "Push", 1, "", 0L, false)
        val observeRoutines = mockk<ObserveRoutinesUseCase>()
        val observeActive = mockk<ObserveActiveWorkoutUseCase>()
        every { observeRoutines.invoke() } returns flowOf(listOf(routine))
        every { observeActive.invoke() } returns flowOf(
            Workout(99L, null, "Quick", 0L, null, "")
        )
        val vm = RoutinesViewModel(
            observeRoutines = observeRoutines,
            observeActive = observeActive,
            startFromRoutineUc = mockk(),
            startAdHoc = mockk(),
            archiveUc = mockk(),
        )

        vm.state.test {
            val s = awaitItem()
            // Skip initial empty
            val final = if (s.routines.isEmpty()) awaitItem() else s
            assertEquals(listOf(routine), final.routines)
            assertEquals(99L, final.activeWorkoutId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `startFromRoutine returns id`() = runTest {
        val start = mockk<StartWorkoutFromRoutineUseCase>()
        coEvery { start.invoke(5L) } returns 77L
        val observeRoutines = mockk<ObserveRoutinesUseCase>()
        val observeActive = mockk<ObserveActiveWorkoutUseCase>()
        every { observeRoutines.invoke() } returns flowOf(emptyList())
        every { observeActive.invoke() } returns flowOf(null)
        val vm = RoutinesViewModel(
            observeRoutines = observeRoutines,
            observeActive = observeActive,
            startFromRoutineUc = start,
            startAdHoc = mockk(),
            archiveUc = mockk(),
        )

        val wid = vm.startFromRoutine(5L)
        assertEquals(77L, wid)
    }
}

