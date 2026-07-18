package com.example.personalisedtracker.feature.workout.presentation.routine_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalisedtracker.feature.workout.domain.model.Exercise
import com.example.personalisedtracker.feature.workout.domain.model.Routine
import com.example.personalisedtracker.feature.workout.domain.model.RoutineExercise
import com.example.personalisedtracker.feature.workout.domain.repository.WorkoutRepository
import com.example.personalisedtracker.feature.workout.domain.usecase.GetRoutineWithExercisesUseCase
import com.example.personalisedtracker.feature.workout.domain.usecase.UpsertRoutineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Editable copy of the routine being authored. */
data class RoutineEditorState(
    val name: String = "",
    val dayOfWeek: Int? = null,
    val notes: String = "",
    val exercises: List<RoutineExercise> = emptyList(),
    val routineId: Long = 0L,
)

@HiltViewModel
class RoutineEditorViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val getRoutine: GetRoutineWithExercisesUseCase,
    private val upsert: UpsertRoutineUseCase,
    private val repo: WorkoutRepository,
) : ViewModel() {

    private val initialRoutineId: Long? = savedState.get<String>("routineId")
        ?.takeIf { it.isNotBlank() }
        ?.toLongOrNull()

    private val _state = MutableStateFlow(RoutineEditorState())
    val state: StateFlow<RoutineEditorState> = _state.asStateFlow()

    init {
        initialRoutineId?.let { id ->
            viewModelScope.launch {
                getRoutine(id)?.let { rwx ->
                    _state.value = RoutineEditorState(
                        name = rwx.routine.name,
                        dayOfWeek = rwx.routine.dayOfWeek,
                        notes = rwx.routine.notes,
                        exercises = rwx.exercises,
                        routineId = rwx.routine.id,
                    )
                }
            }
        }
    }

    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setDay(v: Int?) = _state.update { it.copy(dayOfWeek = v) }
    fun setNotes(v: String) = _state.update { it.copy(notes = v) }

    fun addExercises(exercises: List<Exercise>) = _state.update { st ->
        val base = st.exercises.size
        val added = exercises.mapIndexed { idx, ex ->
            RoutineExercise(
                id = 0,
                routineId = st.routineId,
                exercise = ex,
                orderIndex = base + idx,
                targetSets = 3,
                targetRepRange = "8-12",
                targetWeightHintKg = null,
                restSeconds = 90,
            )
        }
        st.copy(exercises = st.exercises + added)
    }

    fun removeExercise(at: Int) = _state.update { st ->
        st.copy(exercises = st.exercises.toMutableList().apply { removeAt(at) })
    }

    fun move(from: Int, to: Int) = _state.update { st ->
        val list = st.exercises.toMutableList()
        if (from !in list.indices || to !in list.indices) return@update st
        val item = list.removeAt(from)
        list.add(to, item)
        st.copy(exercises = list)
    }

    fun updateItem(at: Int, transform: (RoutineExercise) -> RoutineExercise) = _state.update { st ->
        val list = st.exercises.toMutableList()
        if (at !in list.indices) return@update st
        list[at] = transform(list[at])
        st.copy(exercises = list)
    }

    suspend fun save(): Boolean {
        val s = _state.value
        if (s.name.isBlank() || s.exercises.isEmpty()) return false
        upsert(
            routine = Routine(
                id = s.routineId,
                name = s.name.trim(),
                dayOfWeek = s.dayOfWeek,
                notes = s.notes,
                createdAt = System.currentTimeMillis(),
                archived = false,
            ),
            exercises = s.exercises,
        )
        return true
    }

    /** One-shot id resolution used after the picker returns ids via SavedStateHandle. */
    suspend fun resolveExercises(ids: List<Long>): List<Exercise> {
        val byId = repo.getExercises().associateBy { it.id }
        return ids.mapNotNull { byId[it] }
    }
}


