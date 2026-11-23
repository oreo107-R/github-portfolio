package com.app.dinolearnerfree

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Progress(
    val unlockedLevel: Int = 1,                       // 1 = only Level 1 unlocked
    val objectives: Map<String, Boolean> = emptyMap() // e.g. "quiz_az_passed" -> true
)

@Serializable
data class Profile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatarHue: Float = (0..359).random().toFloat(), // colored circle avatar
    val progress: Progress = Progress()
)
