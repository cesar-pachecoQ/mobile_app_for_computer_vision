package com.example.dronedetectorusingartificialvision.inference

import android.content.Context
import android.graphics.Bitmap
import com.example.dronedetectorusingartificialvision.model.DetectionResult
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Motor de inferencia usando TensorFlow Lite (.tflite).
 *
 * IMPORTANTE: Ultralytics YOLO exporta TFLite con output shape
 * [1, num_anchors, num_attributes], es decir los anchors primero
 * (ej. [1, 8400, 12] para 8 clases con input 640x640).
 *
 * Este engine detecta automáticamente la orientación del tensor.
 *
 * GPU Delegate desactivado por defecto: causa crashes en muchos
 * dispositivos con modelos YOLO cuantizados. Se usa CPU multi-hilo.
 */
class TFLiteEngine : InferenceEngine {

    override val engineName = "TensorFlow Lite"

    private var interpreter: Interpreter? = null
    private var inputSize = 640
    private val classLabels = mutableListOf<String>()

    // true  → output es [1, num_anchors, num_attributes]   ← formato ultralytics
    // false → output es [1, num_attributes, num_anchors]   ← formato alternativo
    private var anchorsFirst = true

    override fun loadModel(context: Context, modelFileName: String, numThreads: Int) {
        // Libera intérprete anterior si existía
        interpreter?.close()
        interpreter = null

        val modelBuffer = loadModelBuffer(context, modelFileName)

        // Solo CPU para máxima compatibilidad
        // El GPU delegate causa crashes con ciertos modelos cuantizados
        val options = Interpreter.Options().apply {
            setNumThreads(numThreads)
            setUseNNAPI(false)          // NNAPI también puede crashear con YOLO
        }

        interpreter = Interpreter(modelBuffer, options)

        // Detectar input size: [1, H, W, 3]
        val inShape = interpreter!!.getInputTensor(0).shape()
        inputSize = inShape[1]  // H

        // Detectar orientación del output tensor
        val outShape = interpreter!!.getOutputTensor(0).shape()
        // outShape[1] > outShape[2] → anchors es la dim mayor → anchors primero
        anchorsFirst = outShape[1] > outShape[2]

        classLabels.clear()
        classLabels.addAll(getDefaultClassLabels())
    }

    override fun detect(
        bitmap: Bitmap,
        confidenceThreshold: Float,
        iouThreshold: Float
    ): List<DetectionResult> {
        val interp = interpreter ?: return emptyList()

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        // Input buffer: [1, H, W, 3] float normalizado [0, 1]
        val inputBuffer = ByteBuffer
            .allocateDirect(1 * inputSize * inputSize * 3 * 4)
            .apply { order(ByteOrder.nativeOrder()) }

        val pixels = IntArray(inputSize * inputSize)
        scaledBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        for (pixel in pixels) {
            inputBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
            inputBuffer.putFloat(((pixel shr 8)  and 0xFF) / 255.0f)
            inputBuffer.putFloat((pixel           and 0xFF) / 255.0f)
        }
        inputBuffer.rewind()

        // Output shape
        val outShape     = interp.getOutputTensor(0).shape()
        // Determinar dims: si anchorsFirst → [1, numAnchors, numAttrs]
        val numAnchors    = if (anchorsFirst) outShape[1] else outShape[2]
        val numAttributes = if (anchorsFirst) outShape[2] else outShape[1]

        // Allocamos siempre en [1, dim1, dim2] tal como lo devuelve el modelo
        val outputBuffer = Array(1) { Array(outShape[1]) { FloatArray(outShape[2]) } }
        interp.run(inputBuffer, outputBuffer)

        // Convertimos a [num_attributes][num_anchors] para YoloPostProcessor
        val rawOutput = Array(numAttributes) { attrIdx ->
            FloatArray(numAnchors) { anchorIdx ->
                if (anchorsFirst) {
                    // output[0][anchorIdx][attrIdx]
                    outputBuffer[0][anchorIdx][attrIdx]
                } else {
                    // output[0][attrIdx][anchorIdx]
                    outputBuffer[0][attrIdx][anchorIdx]
                }
            }
        }

        return YoloPostProcessor.decode(
            rawOutput, inputSize, classLabels, confidenceThreshold, iouThreshold
        )
    }

    override fun close() {
        interpreter?.close()
        interpreter = null
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun loadModelBuffer(context: Context, assetName: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(assetName)
        return FileInputStream(assetFileDescriptor.fileDescriptor).use { stream ->
            stream.channel.map(
                FileChannel.MapMode.READ_ONLY,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.declaredLength
            )
        }
    }

    private fun getDefaultClassLabels(): List<String> = listOf(
        "airplane",    // 0
        "bird",        // 1
        "drone",       // 2
        "horse",       // 3
        "motorcycle",  // 4
        "person",      // 5
        "truck",       // 6
        "helicopter"   // 7
    )
}
