package com.example.personalisedtracker.core.ui

/** Standardised three-state wrapper for screen-level UI. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

