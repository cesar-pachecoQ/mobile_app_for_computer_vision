package com.example.dronedetectorusingartificialvision.inference

import android.content.Context
import android.graphics.Bitmap
import com.example.dronedetectorusingartificialvision.model.DetectionResult
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream

/**
 * Motor de inferencia usando PyTorch Mobile Lite (.ptl).
 *
 * El modelo YOLOv8/v26 exportado como TorchScript produce un tensor de salida
 * con forma [1, (4 + num_classes), num_anchors].
 */
class PyTorchEngine : InferenceEngine {

    override val engineName = "PyTorch Mobile"

    private var module: Module? = null
    private var inputSize = 640
    private val classLabels = mutableListOf<String>()

    override fun loadModel(context: Context, modelFileName: String, numThreads: Int) {
        // PyTorch necesita el archivo en el filesystem, no directo desde assets
        val modelFile = copyAssetToCache(context, modelFileName)
        module = LiteModuleLoader.load(modelFile.absolutePath)

        // Etiquetas de clase hardcodeadas para el modelo personalizado
        // Ajusta esta lista con tus clases reales de entrenamiento
        classLabels.clear()
        classLabels.addAll(getDefaultClassLabels())
    }

    override fun detect(
        bitmap: Bitmap,
        confidenceThreshold: Float,
        iouThreshold: Float
    ): List<DetectionResult> {
        val mod = module ?: return emptyList()

        // Escala el bitmap al input del modelo
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        // Normalización estándar de ImageNet que YOLO usa internamente
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            scaledBitmap,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
            TensorImageUtils.TORCHVISION_NORM_STD_RGB
        )

        val outputTensor = mod.forward(IValue.from(inputTensor)).toTensor()
        val shape = outputTensor.shape()  // [1, num_attributes, num_anchors]

        val numAttributes = shape[1].toInt()
        val numAnchors    = shape[2].toInt()
        val rawData       = outputTensor.dataAsFloatArray

        // Reorganiza en [num_attributes][num_anchors]
        val rawOutput = Array(numAttributes) { attrIdx ->
            FloatArray(numAnchors) { anchorIdx ->
                rawData[attrIdx * numAnchors + anchorIdx]
            }
        }

        return YoloPostProcessor.decode(
            rawOutput, inputSize, classLabels, confidenceThreshold, iouThreshold
        )
    }

    override fun close() {
        module?.destroy()
        module = null
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /** Copia el model file desde assets al cache dir (requerido por PyTorch). */
    private fun copyAssetToCache(context: Context, assetName: String): File {
        val cacheFile = File(context.cacheDir, assetName)
        if (!cacheFile.exists()) {
            context.assets.open(assetName).use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return cacheFile
    }

    /** Etiquetas de clase del modelo personalizado. Actualiza con tus clases reales. */
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
