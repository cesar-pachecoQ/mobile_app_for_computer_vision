package com.example.dronedetectorusingartificialvision.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dronedetectorusingartificialvision.camera.CameraManager
import com.example.dronedetectorusingartificialvision.model.DetectionResult
import com.example.dronedetectorusingartificialvision.model.ModelConfig
import com.example.dronedetectorusingartificialvision.model.ResourceSnapshot
import com.example.dronedetectorusingartificialvision.viewmodel.DetectionViewModel

private val boxColors = listOf(
    Color(0xFFEF5350), Color(0xFF42A5F5), Color(0xFF66BB6A),
    Color(0xFFFFCA28), Color(0xFFAB47BC), Color(0xFF26C6DA)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionScreen(
    modelId       : String,
    onNavigateBack: () -> Unit,
    viewModel     : DetectionViewModel = viewModel()
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val modelConfig = remember(modelId) { ModelConfig.ALL.find { it.id == modelId } }
    val detections  by viewModel.detections.collectAsState()
    val resources   by viewModel.resources.collectAsState()
    val isLoaded    by viewModel.isModelLoaded.collectAsState()
    val loadingError by viewModel.loadingError.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    // Cargar el modelo al entrar a la pantalla
    LaunchedEffect(modelConfig) {
        if (modelConfig != null) {
            viewModel.loadModel(modelConfig)
        }
    }

    // Camera manager
    val cameraManager = remember {
        CameraManager(context) { bitmap, _ ->
            viewModel.processFrame(bitmap)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F))
    ) {
        if (!hasCameraPermission) {
            // Pantalla de permiso
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Se necesita acceso a la cámara", color = Color.White, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Permitir cámara")
                }
            }
        } else {
            // Preview de cámara
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).also { previewView ->
                        cameraManager.startCamera(lifecycleOwner, previewView)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay de bounding boxes
            Canvas(modifier = Modifier.fillMaxSize()) {
                detections.forEach { det ->
                    drawBoundingBox(det)
                }
            }

            // Loading indicator mientras carga el modelo
            if (!isLoaded && loadingError == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF6C63FF))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Cargando ${modelConfig?.engineType?.displayName ?: "modelo"}...",
                            color = Color.White, fontSize = 14.sp
                        )
                    }
                }
            }

            // Error de carga
            loadingError?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = error, color = Color(0xFFEF5350), fontSize = 14.sp)
                }
            }
        }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                colors  = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = modelConfig?.displayName ?: "Detección",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        // Panel de recursos en la parte inferior
        ResourceDashboard(
            resources = resources,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
        )
    }

    // Liberar cámara al salir
    DisposableEffect(lifecycleOwner) {
        onDispose { cameraManager.stop() }
    }
}

// ─── Bounding Box Overlay ────────────────────────────────────────────────────

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBoundingBox(
    det: DetectionResult
) {
    val canvasW = size.width
    val canvasH = size.height

    val left   = det.left   * canvasW
    val top    = det.top    * canvasH
    val right  = det.right  * canvasW
    val bottom = det.bottom * canvasH

    val color = boxColors[(det.label.hashCode() and 0x7FFFFFFF) % boxColors.size]

    // Rectángulo del bounding box
    drawRect(
        color  = color,
        topLeft = Offset(left, top),
        size   = Size(right - left, bottom - top),
        style  = Stroke(width = 3f)
    )

    // Etiqueta de clase + confianza
    val label = "${det.label} ${"%.0f".format(det.confidence * 100)}%"
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            textSize = 36f
            setColor(android.graphics.Color.WHITE)
            setShadowLayer(6f, 0f, 0f, android.graphics.Color.BLACK)
        }
        val bgPaint = android.graphics.Paint().apply {
            setColor(color.copy(alpha = 0.8f).hashCode())
        }
        val textBounds = android.graphics.Rect()
        paint.getTextBounds(label, 0, label.length, textBounds)
        drawRect(
            left, top - textBounds.height() - 8f,
            left + textBounds.width() + 12f, top,
            bgPaint
        )
        drawText(label, left + 6f, top - 6f, paint)
    }
}

// ─── Resource Dashboard ──────────────────────────────────────────────────────

@Composable
fun ResourceDashboard(
    resources: ResourceSnapshot,
    modifier : Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                Color.Black.copy(alpha = 0.65f),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        MetricItem(
            label = "CPU",
            value = "${"%.1f".format(resources.cpuPercent)}%",
            color = if (resources.cpuPercent > 80) Color(0xFFEF5350) else Color(0xFF66BB6A)
        )
        MetricDivider()
        MetricItem(
            label = "RAM",
            value = "${resources.ramUsedMb}MB",
            color = Color(0xFF42A5F5)
        )
        MetricDivider()
        MetricItem(
            label = "Bat",
            value = "${resources.batteryPercent}%",
            color = if (resources.batteryPercent < 20) Color(0xFFEF5350) else Color(0xFFFFCA28)
        )
        MetricDivider()
        MetricItem(
            label = "FPS",
            value = "${"%.1f".format(resources.fps)}",
            color = Color(0xFFAB47BC)
        )
        MetricDivider()
        MetricItem(
            label = "ms",
            value = "${resources.inferenceMs}",
            color = Color(0xFF26C6DA)
        )
    }
}

@Composable
private fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text  = value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text  = label, color = Color(0xFF8B8FA8), fontSize = 10.sp)
    }
}

@Composable
private fun MetricDivider() {
    Box(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp)
            .background(Color(0xFF2A2D3A))
    )
}
