package com.example.dronedetectorusingartificialvision.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dronedetectorusingartificialvision.model.EngineType
import com.example.dronedetectorusingartificialvision.model.ModelConfig
import com.example.dronedetectorusingartificialvision.model.ModelPrecision
import com.example.dronedetectorusingartificialvision.viewmodel.ModelSelectionViewModel

// ─── Paleta de colores por motor ─────────────────────────────────────────────
private val engineColors: Map<EngineType, Pair<Color, Color>> = mapOf(
    EngineType.PYTORCH to (Color(0xFFEF5350) to Color(0xFFB71C1C)),
    EngineType.TFLITE  to (Color(0xFF42A5F5) to Color(0xFF0D47A1)),
    EngineType.ONNX    to (Color(0xFF66BB6A) to Color(0xFF1B5E20))
)

private val precisionColors: Map<ModelPrecision, Color> = mapOf(
    ModelPrecision.FP32 to Color(0xFF90A4AE),
    ModelPrecision.FP16 to Color(0xFF4FC3F7),
    ModelPrecision.INT8 to Color(0xFF81C784)
)

@Composable
fun ModelSelectionScreen(
    onModelSelected: (modelId: String) -> Unit,
    viewModel: ModelSelectionViewModel = viewModel()
) {
    val models by viewModel.models.collectAsState()
    val selected by viewModel.selectedModel.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0A0F), Color(0xFF0D1117))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Header
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "AI Vision Detector",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Selecciona un modelo para comenzar",
                color = Color(0xFF8B8FA8),
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lista de modelos agrupados por motor
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Agrupa por tipo de motor
                EngineType.entries.forEach { engineType ->
                    val group = models.filter { it.engineType == engineType }
                    if (group.isNotEmpty()) {
                        item {
                            val (colorStart, _) = engineColors[engineType]!!
                            Text(
                                text = engineType.displayName,
                                color = colorStart,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, start = 4.dp, bottom = 4.dp)
                            )
                        }
                        items(group, key = { it.id }) { model ->
                            ModelCard(
                                model    = model,
                                isSelected = selected?.id == model.id,
                                onClick  = { viewModel.selectModel(model) }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Botón flotante Iniciar
        if (selected != null) {
            ExtendedFloatingActionButton(
                onClick = { onModelSelected(selected!!.id) },
                icon    = { Icon(Icons.Filled.PlayArrow, contentDescription = "Iniciar") },
                text    = { Text("Iniciar Detección") },
                containerColor = Color(0xFF6C63FF),
                contentColor   = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
private fun ModelCard(
    model     : ModelConfig,
    isSelected: Boolean,
    onClick   : () -> Unit
) {
    val (colorStart, colorEnd) = engineColors[model.engineType]!!
    val precisionColor = precisionColors[model.precision] ?: Color.Gray

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(200),
        label = "card_scale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colorStart else Color(0xFF2A2D3A),
        animationSpec = tween(200),
        label = "border_color"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF13161F))
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicador de color por motor
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(listOf(colorStart, colorEnd))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = model.baseModel.take(2),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.displayName,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PrecisionChip(label = model.precision.label, color = precisionColor)
                PrecisionChip(label = "${model.fileSizeMb} MB", color = Color(0xFF8B8FA8))
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Seleccionado",
                tint = colorStart,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun PrecisionChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
