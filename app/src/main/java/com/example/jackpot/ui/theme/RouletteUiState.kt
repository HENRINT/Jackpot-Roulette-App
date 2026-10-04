package com.example.jackpot.ui

import androidx.compose.ui.graphics.Color

data class OptionItem(
    val id: Int,
    val text: String,
    val color: Color
)

data class RouletteUiState(
    val options: List<OptionItem> = emptyList(),
    val targetRotation: Float = 0f,
    val selectedOption: OptionItem? = null,
    val isSpinning: Boolean = false,
    val newOptionText: String = ""
)