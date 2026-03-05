package com.example.dronedetectorusingartificialvision.inference

import android.content.Context
import android.graphics.Bitmap
import com.example.dronedetectorusingartificialvision.model.DetectionResult

/**
 * Interfaz común para todos los motores de inferencia de visión artificial.
 * Permite intercambiar PyTorch, TFLite y ONNX Runtime sin cambiar el
 * código de la cámara o la UI.
 */
interface InferenceEngine {

    /**
     * Carga el modelo desde assets. Debe llamarse una sola vez antes de
     * comenzar la inferencia. Es una operación bloqueante: ejecutar en
     * un hilo de fondo (ej. Dispatchers.IO).
     *
     * @param context Contexto de Android para acceder a assets.
     * @param modelFileName Nombre del archivo en assets/.
     * @param numThreads Número de hilos para el executor.
     */
    fun loadModel(context: Context, modelFileName: String, numThreads: Int = 4)

    /**
     * Ejecuta inferencia sobre un frame de la cámara.
     *
     * @param bitmap Frame en formato ARGB_8888.
     * @param confidenceThreshold Umbral mínimo de confianza [0.0, 1.0].
     * @param iouThreshold Umbral IoU para NMS [0.0, 1.0].
     * @return Lista de detecciones con coordenadas normalizadas.
     */
    fun detect(
        bitmap: Bitmap,
        confidenceThreshold: Float = 0.45f,
        iouThreshold: Float = 0.45f
    ): List<DetectionResult>

    /**
     * Libera los recursos nativos del motor. Llamar cuando el motor ya no
     * se necesita (ej. onDestroy del ViewModel).
     */
    fun close()

    /** Nombre del motor para logging y métricas. */
    val engineName: String
}
