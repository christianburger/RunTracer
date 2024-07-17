package com.runtracer.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtracer.model.ActivityData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ActivityData>(ActivityData.Loading)
    val uiState: StateFlow<ActivityData> = _uiState.asStateFlow()

    fun startActivityTracking(activityType: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                val countDownTimer = object : CountDownTimer(60000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val currentState = _uiState.value as? ActivityData.Success

                        _uiState.value = currentState?.copy(
                            totalDurationSeconds = (currentState.totalDurationSeconds + 1)
                        ) ?: ActivityData.Success(
                            activityType = activityType,
                            totalDurationSeconds = 1,
                            isPaused = false,
                            heartRate = 0,
                            caloriesBurned = 0,
                            gpsCoordinates = emptyList()
                        )
                    }

                    override fun onFinish() {
                        _uiState.value = (uiState.value as? ActivityData.Success)?.copy(
                            isPaused = true
                        ) ?: ActivityData.Success(
                            activityType = activityType,
                            totalDurationSeconds = 0,
                            isPaused = true,
                            heartRate = 0,
                            caloriesBurned = 0,
                            gpsCoordinates = emptyList()
                        )
                    }
                }
                countDownTimer.start()

                _uiState.value = ActivityData.Success(
                    activityType = activityType,
                    totalDurationSeconds = 0,
                    isPaused = false,
                    heartRate = 0,
                    caloriesBurned = 0,
                    gpsCoordinates = emptyList()
                )
            }
        }
    }

    fun togglePauseResume() {
        val currentState = _uiState.value as? ActivityData.Success
        _uiState.value = currentState?.copy(
            isPaused = !currentState.isPaused
        ) ?: ActivityData.Success(
            activityType = "Unknown",
            totalDurationSeconds = 0,
            isPaused = false,
            heartRate = 0,
            caloriesBurned = 0,
            gpsCoordinates = emptyList()
        )
    }

    fun stopActivityTracking() {
        _uiState.value = ActivityData.Success(
            activityType = "Stopped",
            totalDurationSeconds = 0,
            isPaused = true,
            heartRate = 0,
            caloriesBurned = 0,
            gpsCoordinates = emptyList()
        )
    }
}
