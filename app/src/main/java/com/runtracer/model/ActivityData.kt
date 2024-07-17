package com.runtracer.model

import android.location.Location
import java.util.UUID

sealed class ActivityData {
    data class Success(
        val activityType: String,
        val totalDurationSeconds: Long,
        val isPaused: Boolean,
        val heartRate: Int,
        val caloriesBurned: Int,
        val gpsCoordinates: List<Location>
    ) : ActivityData()

    object Loading : ActivityData()
    object Error : ActivityData()
}
