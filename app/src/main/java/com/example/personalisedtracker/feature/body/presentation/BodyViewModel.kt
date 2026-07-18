package com.example.personalisedtracker.feature.body.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalisedtracker.core.common.DateInt
import com.example.personalisedtracker.core.common.DispatcherProvider
import com.example.personalisedtracker.feature.body.domain.model.BodyWeight
import com.example.personalisedtracker.feature.body.domain.model.InBodyScan
import com.example.personalisedtracker.feature.body.domain.model.WaistMeasurement
import com.example.personalisedtracker.feature.body.domain.repository.InBodyImportPreview
import com.example.personalisedtracker.feature.body.domain.usecase.CommitInBodyImportUseCase
import com.example.personalisedtracker.feature.body.domain.usecase.LogWaistUseCase
import com.example.personalisedtracker.feature.body.domain.usecase.LogWeightUseCase
import com.example.personalisedtracker.feature.body.domain.usecase.ObserveLatestScanUseCase
import com.example.personalisedtracker.feature.body.domain.usecase.ObserveWaistUseCase
import com.example.personalisedtracker.feature.body.domain.usecase.ObserveWeightsSinceUseCase
import com.example.personalisedtracker.feature.body.domain.usecase.PreviewInBodyCsvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** UI selectable range for charts. */
enum class BodyRange(val days: Long, val label: String) {
    D30(30, "30d"), D90(90, "90d"), D365(365, "1y")
}

data class BodyUiState(
    val range: BodyRange = BodyRange.D30,
    val weights: List<BodyWeight> = emptyList(),
    val waist: List<WaistMeasurement> = emptyList(),
    val latestScan: InBodyScan? = null,
    val previousScan: InBodyScan? = null,
)

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class BodyViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
    private val observeWeights: ObserveWeightsSinceUseCase,
    private val observeWaist: ObserveWaistUseCase,
    private val observeLatestScan: ObserveLatestScanUseCase,
    private val logWeightUc: LogWeightUseCase,
    private val logWaistUc: LogWaistUseCase,
    private val previewCsv: PreviewInBodyCsvUseCase,
    private val commitImport: CommitInBodyImportUseCase,
) : ViewModel() {

    private val range = MutableStateFlow(BodyRange.D30)
    private val preview = MutableStateFlow<InBodyImportPreview?>(null)
    val pendingPreview: StateFlow<InBodyImportPreview?> = preview

    val state: StateFlow<BodyUiState> = combine(
        range.flatMapLatest { r ->
            observeWeights(DateInt.minusDays(DateInt.today(), r.days))
        },
        observeWaist(),
        observeLatestScan(),
        range,
    ) { weights, waist, latest, r ->
        BodyUiState(
            range = r,
            weights = weights,
            waist = waist,
            latestScan = latest,
            previousScan = null, // filled below if available
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BodyUiState())

    fun setRange(r: BodyRange) { range.value = r }

    fun logWeight(kg: Double, note: String = "") = viewModelScope.launch {
        logWeightUc(DateInt.today(), kg, note)
    }
    fun logWaist(cm: Double) = viewModelScope.launch { logWaistUc(DateInt.today(), cm) }

    fun previewCsvImport(uri: Uri) = viewModelScope.launch {
        val text = withContext(dispatchers.io) {
            context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
        } ?: return@launch
        preview.value = previewCsv(text)
    }

    fun confirmImport() = viewModelScope.launch {
        val p = preview.value ?: return@launch
        commitImport(p.newRows)
        preview.value = null
    }

    fun dismissPreview() { preview.value = null }
}



