package com.example.dronedetectorusingartificialvision.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dronedetectorusingartificialvision.inference.InferenceEngine
import com.example.dronedetectorusingartificialvision.inference.InferenceEngineFactory
import com.example.dronedetectorusingartificialvision.model.DetectionResult
import com.example.dronedetectorusingartificialvision.model.ModelConfig
import com.example.dronedetectorusingartificialvision.model.ResourceSnapshot
import com.example.dronedetectorusingartificialvision.monitor.ResourceMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.graphics.Bitmap

/**
 * ViewModel para la pantalla de detección.
 * Coordina el motor de inferencia, el monitor de recursos y el estado de la UI.
 */
class DetectionViewModel(application: Application) : AndroidViewModel(application) {

    // ─── Estado de detección ──────────────────────────────────────────────────
    private val _detections = MutableStateFlow<List<DetectionResult>>(emptyList())
    val detections: StateFlow<List<DetectionResult>> = _detections.asStateFlow()

    // ─── Estado de recursos ───────────────────────────────────────────────────
    private val _resources = MutableStateFlow(ResourceSnapshot())
    val resources: StateFlow<ResourceSnapshot> = _resources.asStateFlow()

    // ─── Estado de carga ──────────────────────────────────────────────────────
    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()

    private val _loadingError = MutableStateFlow<String?>(null)
    val loadingError: StateFlow<String?> = _loadingError.asStateFlow()

    private val _currentModelConfig = MutableStateFlow<ModelConfig?>(null)
    val currentModelConfig: StateFlow<ModelConfig?> = _currentModelConfig.asStateFlow()

    // ─── Internos ─────────────────────────────────────────────────────────────
    private var engine: InferenceEngine? = null
    private lateinit var resourceMonitor: ResourceMonitor

    // Para calcular FPS
    private var frameCount = 0
    private var lastFpsCheckTime = System.currentTimeMillis()
    private var currentFps = 0f

    /**
     * Carga el modelo seleccionado en un hilo de fondo.
     * Inicia el monitor de recursos una vez cargado.
     */
    fun loadModel(modelConfig: ModelConfig) {
        _currentModelConfig.value = modelConfig
        _isModelLoaded.value = false
        _loadingError.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                engine?.close()
                val newEngine = InferenceEngineFactory.create(modelConfig)
                newEngine.loadModel(getApplication(), modelConfig.fileName)
                engine = newEngine
                _isModelLoaded.value = true

                // Inicia el monitor de recursos
                resourceMonitor = ResourceMonitor(getApplication())
                startResourceMonitoring()
            } catch (e: Exception) {
                _loadingError.value = "Error cargando modelo: ${e.message}"
            }
        }
    }

    /**
     * Procesa un frame de la cámara: ejecuta inferencia y actualiza detecciones.
     * Debe llamarse desde el hilo del ImageAnalysis analyzer.
     */
    fun processFrame(bitmap: Bitmap) {
        val eng = engine ?: return
        if (!_isModelLoaded.value) return

        val startMs = System.currentTimeMillis()
        val results = eng.detect(bitmap)
        val inferenceMs = System.currentTimeMillis() - startMs

        // Calcular FPS
        frameCount++
        val now = System.currentTimeMillis()
        val elapsed = now - lastFpsCheckTime
        if (elapsed >= 1000L) {
            currentFps = frameCount * 1000f / elapsed
            frameCount = 0
            lastFpsCheckTime = now
        }

        // Actualizar estados en el hilo de inferencia (StateFlow es thread-safe)
        _detections.value = results
        if (::resourceMonitor.isInitialized) {
            resourceMonitor.updateInferenceStats(currentFps, inferenceMs)
        }
    }

    private fun startResourceMonitoring() {
        viewModelScope.launch(Dispatchers.IO) {
            resourceMonitor.resourceFlow.collect { snapshot ->
                _resources.value = snapshot
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine?.close()
    }
}
