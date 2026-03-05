package com.example.dronedetectorusingartificialvision.model

/**
 * Resultado de una inferencia: un objeto detectado con su bounding box,
 * etiqueta y score de confianza.
 *
 * Las coordenadas están normalizadas [0.0, 1.0] relativas al frame de la cámara.
 */
data class DetectionResult(
    val label: String,
    val confidence: Float,
    /** Coordenadas normalizadas (0..1) */
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)
