package com.runtracer.model

import java.util.UUID

data class HealthMetrics(
    val uid: UUID,
    val vo2max: Double = 0.0,
    val cff: Double = 0.0,
    val bmr: Double = 0.0,
    val rmr: Double = 0.0,
    val bmi: Double = 0.0,
    val bai: Double = 0.0,
    val rhr_state: Int = 0,
    val hr_reading: Int = 0,
    val current_hr: Double = 0.0,
    val last_hr: Double = 0.0,
    val resting_hr: Double = 0.0,
    val hr_reserve: Double = 0.0,
    val maximum_hr: Double = 0.0,
    val recovery_hr: Double = 0.0,
    val target_hr_light: Double = 0.0,
    val target_hr_moderate: Double = 0.0,
    val target_hr_heavy: Double = 0.0,
    val target_hr_very_heavy: Double = 0.0
)
