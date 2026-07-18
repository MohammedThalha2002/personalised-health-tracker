package com.example.personalisedtracker.feature.food.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalisedtracker.core.common.DateInt
import com.example.personalisedtracker.feature.food.domain.model.DailyMacros
import com.example.personalisedtracker.feature.food.domain.model.FoodEntry
import com.example.personalisedtracker.feature.food.domain.model.FoodMeal
import com.example.personalisedtracker.feature.food.domain.model.MealTemplate
import com.example.personalisedtracker.feature.food.domain.usecase.AddFoodUseCase
import com.example.personalisedtracker.feature.food.domain.usecase.ArchiveTemplateUseCase
import com.example.personalisedtracker.feature.food.domain.usecase.DeleteFoodUseCase
import com.example.personalisedtracker.feature.food.domain.usecase.DeleteTemplateUseCase
import com.example.personalisedtracker.feature.food.domain.usecase.LogFromTemplateUseCase
import com.example.personalisedtracker.feature.food.domain.usecase.ObserveFoodForDateUseCase
import com.example.personalisedtracker.feature.food.domain.usecase.ObserveMealTemplatesUseCase
import com.example.personalisedtracker.feature.food.domain.usecase.SaveTemplateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state for the food tab. Templates are grouped by meal slot in the UI;
 * the VM exposes the flat list and lets the screen partition.
 */
data class FoodUiState(
    val entries: List<FoodEntry> = emptyList(),
    val templates: List<MealTemplate> = emptyList(),
    val totals: DailyMacros = DailyMacros(),
)

@HiltViewModel
class FoodViewModel @Inject constructor(
    observeForDate: ObserveFoodForDateUseCase,
    observeTemplates: ObserveMealTemplatesUseCase,
    private val addUc: AddFoodUseCase,
    private val logFromTemplateUc: LogFromTemplateUseCase,
    private val saveTemplateUc: SaveTemplateUseCase,
    private val archiveTemplateUc: ArchiveTemplateUseCase,
    private val deleteTemplateUc: DeleteTemplateUseCase,
    private val deleteUc: DeleteFoodUseCase,
) : ViewModel() {

    private val today = DateInt.today()

    val state: StateFlow<FoodUiState> = combine(
        observeForDate(today),
        observeTemplates(),
    ) { entries, templates ->
        FoodUiState(
            entries = entries,
            templates = templates,
            totals = DailyMacros.from(entries),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FoodUiState())

    /** One-tap log from a saved template (the primary fast path). */
    fun logTemplate(templateId: Long) = viewModelScope.launch {
        logFromTemplateUc(today, templateId)
    }

    /** Quick-macro entry (the fallback path for ad-hoc days). */
    fun quickAdd(meal: FoodMeal, description: String, kcal: Int, p: Int, f: Int, c: Int) =
        viewModelScope.launch {
            val effectiveDesc = description.ifBlank {
                meal.name.lowercase().replaceFirstChar { it.uppercase() } + " (quick log)"
            }
            addUc(today, meal, effectiveDesc, kcal, p, f, c)
        }

    fun saveTemplate(t: MealTemplate) = viewModelScope.launch { saveTemplateUc(t) }
    fun archiveTemplate(id: Long) = viewModelScope.launch { archiveTemplateUc(id) }
    fun deleteTemplate(id: Long) = viewModelScope.launch { deleteTemplateUc(id) }
    fun delete(id: Long) = viewModelScope.launch { deleteUc(id) }
}
