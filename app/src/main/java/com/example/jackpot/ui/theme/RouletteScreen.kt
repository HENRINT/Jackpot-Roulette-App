package com.example.jackpot.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RouletteRoute(
    viewModel: RouletteViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RouletteScreen(
        uiState = uiState,
        onNewOptionTextChanged = viewModel::onNewOptionTextChanged,
        onAddOption = viewModel::addOption,
        onRemoveOption = viewModel::removeOption,
        onSpinClicked = viewModel::spinWheel,
        onResetClicked = viewModel::resetGame,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouletteScreen(
    uiState: RouletteUiState,
    onNewOptionTextChanged: (String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (OptionItem) -> Unit,
    onSpinClicked: () -> Unit,
    onResetClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedRotation by animateFloatAsState(
        targetValue = uiState.targetRotation,
        animationSpec = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
        label = "RuletaRotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "JACKPOT",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF5C3D2E),
                        letterSpacing = 2.sp
                    )
                },
                actions = {
                    OutlinedButton(
                        onClick = onResetClicked,
                        enabled = !uiState.isSpinning,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC65D57)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC65D57)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Reiniciar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF0E5D8))
            )
        },
        containerColor = Color(0xFFFAF6F0)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (uiState.selectedOption != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF8E6C8F)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFFD4B2D8), RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = "★ ¡GANADOR: ${uiState.selectedOption.text.uppercase()}! ★",
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(290.dp)
                    .background(Color(0xFFEFE3D3), shape = RoundedCornerShape(145.dp))
                    .border(5.dp, Color(0xFFC89B7B), shape = RoundedCornerShape(145.dp))
                    .padding(12.dp)
            ) {
                WheelCanvas(
                    options = uiState.options,
                    rotationAngle = animatedRotation
                )
            }

            Button(
                onClick = onSpinClicked,
                enabled = !uiState.isSpinning && uiState.options.size >= 2,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7E527F),
                    disabledContainerColor = Color(0xFFC7B8C8)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (uiState.isSpinning) "GIRANDO..." else "¡GIRAR!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Text(
                text = "OPCIONES (${uiState.options.size}/10)",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5C3D2E)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.newOptionText,
                    onValueChange = onNewOptionTextChanged,
                    label = { Text("Nueva opción...", color = Color(0xFF8C7A6B)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF4A3427),
                        unfocusedTextColor = Color(0xFF4A3427),
                        focusedBorderColor = Color(0xFF8E6C8F),
                        unfocusedBorderColor = Color(0xFFC8B9A6)
                    ),
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isSpinning && uiState.options.size < 10
                )
                Button(
                    onClick = onAddOption,
                    enabled = !uiState.isSpinning && uiState.newOptionText.isNotBlank() && uiState.options.size < 10,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF729B79))
                ) {
                    Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(uiState.options, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = item.color),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.text,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF332219)
                            )
                            IconButton(
                                onClick = { onRemoveOption(item) },
                                enabled = !uiState.isSpinning && uiState.options.size > 2
                            ) {
                                Text(
                                    "✕",
                                    color = if (uiState.options.size > 2) Color(0xFF332219) else Color(0xFFB5A7A7),
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WheelCanvas(
    options: List<OptionItem>,
    rotationAngle: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width.coerceAtMost(size.height) / 2f

        if (options.isEmpty()) {
            drawCircle(color = Color(0xFFE2D6C5), radius = radius, center = center)
            return@Canvas
        }

        val sweepAngle = 360f / options.size

        options.forEachIndexed { index, option ->
            val startAngle = rotationAngle + (index * sweepAngle)

            drawArc(
                color = option.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            val angleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
            val textDistance = radius * 0.65f
            val textX = (center.x + textDistance * cos(angleRad)).toFloat()
            val textY = (center.y + textDistance * sin(angleRad)).toFloat()

            drawContext.canvas.nativeCanvas.apply {
                save()
                rotate((startAngle + sweepAngle / 2f) + 90f, textX, textY)
                val paint = Paint().apply {
                    color = android.graphics.Color.parseColor("#332219")
                    textSize = 32f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                drawText(option.text, textX, textY, paint)
                restore()
            }
        }

        val pointerPath = Path().apply {
            moveTo(center.x, center.y - radius + 12f)
            lineTo(center.x - 16f, center.y - radius - 22f)
            lineTo(center.x + 16f, center.y - radius - 22f)
            close()
        }
        drawPath(pointerPath, color = Color(0xFFC89B7B))
    }
}