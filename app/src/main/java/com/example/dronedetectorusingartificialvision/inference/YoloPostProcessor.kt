package com.example.dronedetectorusingartificialvision.inference

import com.example.dronedetectorusingartificialvision.model.DetectionResult
import kotlin.math.max
import kotlin.math.min

/**
 * Post-procesamiento YOLO: decodificación de outputs crudos + NMS.
 *
 * Los modelos YOLO exportados (para PTL, TFLite, ONNX) producen un tensor de
 * forma [1, num_attributes, num_anchors].
 *
 * - num_attributes = 4 (bbox: cx, cy, w, h) + num_classes
 * - num_anchors = varía por modelo/input size
 *
 * Las coordenadas están normalizadas [0, inputSize].
 */
object YoloPostProcessor {

    /**
     * Decodifica el output crudo de un modelo YOLO a una lista de detecciones.
     *
     * @param rawOutput   Tensor crudo float[num_attributes][num_anchors].
     * @param inputSize   Tamaño del input del modelo (ej. 640 para YOLO estándar).
     * @param classLabels Lista de etiquetas de clase.
     * @param confThresh  Umbral de confianza.
     * @param iouThresh   Umbral IoU para NMS.
     */
    fun decode(
        rawOutput: Array<FloatArray>,
        inputSize: Int,
        classLabels: List<String>,
        confThresh: Float,
        iouThresh: Float
    ): List<DetectionResult> {
        val numAttributes = rawOutput.size          // 4 + numClasses
        val numAnchors = rawOutput[0].size
        val numClasses = numAttributes - 4

        val candidates = mutableListOf<DetectionResult>()

        for (anchor in 0 until numAnchors) {
            // Extrae bbox (formato cx, cy, w, h normalizado a inputSize)
            val cx = rawOutput[0][anchor]
            val cy = rawOutput[1][anchor]
            val w  = rawOutput[2][anchor]
            val h  = rawOutput[3][anchor]

            // Encuentra la clase con mayor score
            var maxScore = 0f
            var bestClass = 0
            for (c in 0 until numClasses) {
                val score = rawOutput[4 + c][anchor]
                if (score > maxScore) {
                    maxScore = score
                    bestClass = c
                }
            }

            if (maxScore < confThresh) continue

            // Convierte a coordenadas normalizadas [0, 1]
            val left   = (cx - w / 2f) / inputSize
            val top    = (cy - h / 2f) / inputSize
            val right  = (cx + w / 2f) / inputSize
            val bottom = (cy + h / 2f) / inputSize

            val label = if (bestClass < classLabels.size) classLabels[bestClass] else "class_$bestClass"
            candidates.add(DetectionResult(label, maxScore, left, top, right, bottom))
        }

        return applyNms(candidates, iouThresh)
    }

    /**
     * Non-Maximum Suppression — elimina detecciones solapadas.
     * Procesa todas las clases juntas (class-agnostic NMS).
     */
    fun applyNms(detections: List<DetectionResult>, iouThresh: Float): List<DetectionResult> {
        val sorted = detections.sortedByDescending { it.confidence }.toMutableList()
        val result = mutableListOf<DetectionResult>()

        while (sorted.isNotEmpty()) {
            val best = sorted.removeAt(0)
            result.add(best)
            sorted.removeAll { iou(best, it) > iouThresh }
        }
        return result
    }

    /** Calcula Intersection over Union entre dos bounding boxes. */
    private fun iou(a: DetectionResult, b: DetectionResult): Float {
        val interLeft   = max(a.left,   b.left)
        val interTop    = max(a.top,    b.top)
        val interRight  = min(a.right,  b.right)
        val interBottom = min(a.bottom, b.bottom)

        val interArea = max(0f, interRight - interLeft) * max(0f, interBottom - interTop)
        if (interArea == 0f) return 0f

        val aArea = (a.right - a.left) * (a.bottom - a.top)
        val bArea = (b.right - b.left) * (b.bottom - b.top)

        return interArea / (aArea + bArea - interArea)
    }
}
