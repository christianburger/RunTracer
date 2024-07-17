package com.runtracer.model

import java.util.UUID

data class ActivityInstant(
    val uid: UUID,
    val activityId: UUID,
    val instantId: UUID,
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val distance_km: Double = 0.0,
    val distance_miles: Double = 0.0,
    val calories_burned: Double = 0.0,
    val pace_min_per_km: Double = 0.0,
    val pace_min_per_mile: Double = 0.0,
    val speed_kph: Double = 0.0,
    val speed_mph: Double = 0.0,
    val steps: Int = 0,
    val heart_rate: Double = 0.0,
    val notes: String? = null
)
