package com.example.personalisedtracker.feature.workout.presentation.exercise_catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalisedtracker.feature.workout.domain.model.Equipment
import com.example.personalisedtracker.feature.workout.domain.model.Exercise
import com.example.personalisedtracker.feature.workout.domain.model.ExerciseCategory
import com.example.personalisedtracker.feature.workout.domain.repository.WorkoutRepository
import com.example.personalisedtracker.feature.workout.domain.usecase.ObserveExercisesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogFilters(
    val query: String = "",
    val category: ExerciseCategory? = null,
    val equipment: Equipment? = null,
)

data class CatalogUiState(
    val exercises: List<Exercise> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val filters: CatalogFilters = CatalogFilters(),
)

@HiltViewModel
class ExerciseCatalogViewModel @Inject constructor(
    savedState: SavedStateHandle,
    observeExercises: ObserveExercisesUseCase,
    private val repo: WorkoutRepository,
) : ViewModel() {

    val isPickerMode: Boolean = savedState.get<String>("routineId")?.isNotBlank() == true ||
        savedState.get<String>("workoutId")?.isNotBlank() == true

    private val filters = MutableStateFlow(CatalogFilters())
    private val selected = MutableStateFlow<Set<Long>>(emptySet())

    val state: StateFlow<CatalogUiState> = combine(
        observeExercises(), filters, selected,
    ) { list, f, sel ->
        val filtered = list.asSequence()
            .filter { f.category == null || it.category == f.category }
            .filter { f.equipment == null || it.equipment == f.equipment }
            .filter { f.query.isBlank() || it.name.contains(f.query, ignoreCase = true) }
            .toList()
        CatalogUiState(exercises = filtered, selectedIds = sel, filters = f)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogUiState())

    fun setQuery(q: String) = filters.update { it.copy(query = q) }
    fun setCategory(c: ExerciseCategory?) = filters.update { it.copy(category = c) }
    fun setEquipment(e: Equipment?) = filters.update { it.copy(equipment = e) }

    fun toggle(id: Long) = selected.update { if (id in it) it - id else it + id }

    fun addCustom(name: String, category: ExerciseCategory, equipment: Equipment) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repo.addCustomExercise(name.trim(), category, emptyList(), equipment)
        }
    }
}

