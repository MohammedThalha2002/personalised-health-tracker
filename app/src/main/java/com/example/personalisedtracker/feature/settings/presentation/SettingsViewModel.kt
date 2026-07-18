package com.example.personalisedtracker.feature.settings.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalisedtracker.core.common.DataResult
import com.example.personalisedtracker.feature.reports.data.MarkdownReportWriter
import com.example.personalisedtracker.feature.settings.data.export.ImportSummary
import com.example.personalisedtracker.feature.settings.data.export.JsonExporter
import com.example.personalisedtracker.feature.settings.data.export.JsonImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SettingsEvent {
    data class ShareJson(val uri: Uri) : SettingsEvent
    data class ShareMarkdown(val uri: Uri) : SettingsEvent
    data class ImportFinished(val summary: ImportSummary) : SettingsEvent
    data class Error(val message: String) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val exporter: JsonExporter,
    private val importer: JsonImporter,
    private val markdownWriter: MarkdownReportWriter,
) : ViewModel() {

    private val _events = MutableStateFlow<SettingsEvent?>(null)
    val events: StateFlow<SettingsEvent?> = _events.asStateFlow()
    fun consumed() { _events.value = null }

    fun exportJson() = viewModelScope.launch {
        runCatching { exporter.export() }
            .onSuccess { _events.value = SettingsEvent.ShareJson(it) }
            .onFailure { _events.value = SettingsEvent.Error(it.message ?: "Export failed") }
    }

    fun exportMarkdown(days: Long) = viewModelScope.launch {
        runCatching { markdownWriter.write(days) }
            .onSuccess { _events.value = SettingsEvent.ShareMarkdown(it) }
            .onFailure { _events.value = SettingsEvent.Error(it.message ?: "Report failed") }
    }

    fun importJson(uri: Uri) = viewModelScope.launch {
        when (val r = importer.import(uri)) {
            is DataResult.Success -> _events.value = SettingsEvent.ImportFinished(r.data)
            is DataResult.Failure -> _events.value = SettingsEvent.Error(r.error.message)
        }
    }
}



