package com.example.dronedetectorusingartificialvision.model

/**
 * Snapshot de recursos del dispositivo en un instante dado.
 *
 * @param cpuPercent     Uso de CPU del proceso (0..100).
 * @param ramUsedMb      RAM usada por la app en MB.
 * @param ramTotalMb     RAM total del dispositivo en MB.
 * @param batteryPercent Nivel de batería (0..100).
 * @param fps            Frames por segundo procesados por el motor de inferencia.
 * @param inferenceMs    Última latencia de inferencia en milisegundos.
 */
data class ResourceSnapshot(
    val cpuPercent: Float = 0f,
    val ramUsedMb: Long = 0L,
    val ramTotalMb: Long = 0L,
    val batteryPercent: Int = 0,
    val fps: Float = 0f,
    val inferenceMs: Long = 0L
)
