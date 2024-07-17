package com.runtracer.model

import java.util.UUID

data class PhysicalAttributes(
    val uid: UUID,
    val height: Double = 0.0,
    val hip_circumference: Double = 0.0,
    val current_weight: Double = 0.0,
    val current_fat: Double = 0.0,
    val target_weight: Double = 0.0,
    val target_fat: Double = 0.0
)
