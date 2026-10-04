package com.example.jackpot.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class RouletteViewModel : ViewModel() {

    private val colorPalette = listOf(
        Color(0xFFF4C2C2), // Rosa Melocotón Cálido
        Color(0xFFE6C280), // Crema Dorado
        Color(0xFFD8A47F), // Terracota Suave
        Color(0xFFA8C3A8), // Verde Salvia Cálido
        Color(0xFFD6C7B2), // Beige / Arena
        Color(0xFFC4A484), // Café Dulce Suave
        Color(0xFFE8AEB7), // Coral Pastel
        Color(0xFFB5A7A7)  // Mauve Cálido
    )

    private fun generateDefaultOptions(): List<OptionItem> {
        return listOf("Opción 1", "Opción 2").mapIndexed { index, name ->
            OptionItem(id = index + 1, text = name, color = colorPalette[index % colorPalette.size])
        }
    }

    private val _uiState = MutableStateFlow(RouletteUiState(options = generateDefaultOptions()))
    val uiState: StateFlow<RouletteUiState> = _uiState.asStateFlow()

    fun onNewOptionTextChanged(text: String) {
        _uiState.update { it.copy(newOptionText = text) }
    }

    fun addOption() {
        val currentState = _uiState.value
        if (currentState.newOptionText.isBlank()) return
        if (currentState.options.size >= 10) return

        val newId = (currentState.options.maxOfOrNull { it.id } ?: 0) + 1
        val newOption = OptionItem(
            id = newId,
            text = currentState.newOptionText.trim(),
            color = Color.Unspecified
        )

        val updatedList = currentState.options + newOption
        _uiState.update {
            it.copy(
                options = recalculateColors(updatedList),
                newOptionText = ""
            )
        }
    }

    fun removeOption(option: OptionItem) {
        val currentState = _uiState.value
        if (currentState.options.size <= 2) return // No permite menos de 2 opciones

        val updatedList = currentState.options.filter { item -> item.id != option.id }
        _uiState.update {
            it.copy(options = recalculateColors(updatedList))
        }
    }

    fun resetGame() {
        if (_uiState.value.isSpinning) return
        _uiState.update {
            it.copy(
                options = generateDefaultOptions(), // Restablece a Opción 1 y Opción 2
                selectedOption = null,
                newOptionText = "",
                targetRotation = 0f
            )
        }
    }

    private fun recalculateColors(list: List<OptionItem>): List<OptionItem> {
        return list.mapIndexed { index, item ->
            item.copy(color = colorPalette[index % colorPalette.size])
        }
    }

    fun spinWheel() {
        val currentState = _uiState.value
        if (currentState.isSpinning || currentState.options.size < 2) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSpinning = true, selectedOption = null) }

            val totalOptions = currentState.options.size
            val sweepAngle = 360f / totalOptions
            val winningIndex = Random.nextInt(0, totalOptions)
            val selectedItem = currentState.options[winningIndex]

            val targetAngleInWheel = winningIndex * sweepAngle + (sweepAngle / 2f)
            val extraTurns = Random.nextInt(5, 8) * 360f
            val newTargetRotation = currentState.targetRotation + extraTurns + (360f - (currentState.targetRotation % 360f)) + (270f - targetAngleInWheel)

            _uiState.update {
                it.copy(targetRotation = newTargetRotation)
            }

            delay(3500)

            _uiState.update {
                it.copy(
                    isSpinning = false,
                    selectedOption = selectedItem
                )
            }
        }
    }
}