package com.runtracer.model

import java.util.Date
import java.util.UUID

data class UserInfo(
    val uid: UUID,
    val first_name: String? = null,
    val last_name: String? = null,
    val email: String? = null,
    val gender: String? = null,
    val birthday_date: Date? = null,
    val password: String? = null,
    val status: String? = null,
    val created_at: String? = null,
)
