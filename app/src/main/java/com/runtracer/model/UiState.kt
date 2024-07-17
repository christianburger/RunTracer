package com.runtracer.model

sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    data class Success(val activityData: ActivityData) : UiState()
    data class Error(val message: String) : UiState()
}
