package com.example.dronedetectorusingartificialvision.inference

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import com.example.dronedetectorusingartificialvision.model.DetectionResult
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer

/**
 * Motor de inferencia usando ONNX Runtime para Android (.onnx).
 *
 * ONNX Runtime acepta el modelo desde disco (similar a PyTorch).
 * Output shape esperado: [1, num_attributes, num_anchors].
 */
class OnnxEngine : InferenceEngine {

    override val engineName = "ONNX Runtime"

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private var inputName = "images"
    private var inputSize = 640
    private val classLabels = mutableListOf<String>()

    override fun loadModel(context: Context, modelFileName: String, numThreads: Int) {
        ortEnv = OrtEnvironment.getEnvironment()

        val modelFile = copyAssetToCache(context, modelFileName)
        val sessionOptions = OrtSession.SessionOptions().apply {
            setIntraOpNumThreads(numThreads)
        }

        ortSession = ortEnv!!.createSession(modelFile.absolutePath, sessionOptions)

        // Obtiene el nombre e input size real del modelo
        ortSession!!.inputInfo.entries.firstOrNull()?.let { entry ->
            inputName = entry.key
            val shape = (entry.value.info as? ai.onnxruntime.TensorInfo)?.shape
            if (shape != null && shape.size >= 3) {
                inputSize = shape[2].toInt()  // [1, 3, H, W] → H
            }
        }

        classLabels.clear()
        classLabels.addAll(getDefaultClassLabels())
    }

    override fun detect(
        bitmap: Bitmap,
        confidenceThreshold: Float,
        iouThreshold: Float
    ): List<DetectionResult> {
        val env     = ortEnv     ?: return emptyList()
        val session = ortSession ?: return emptyList()

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        // ONNX Runtime espera NCHW: [1, 3, H, W] con valores [0, 1]
        val floatBuffer = FloatBuffer.allocate(1 * 3 * inputSize * inputSize)
        val pixels = IntArray(inputSize * inputSize)
        scaledBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        // Canales separados: primero R, luego G, luego B
        for (pixel in pixels) floatBuffer.put(((pixel shr 16) and 0xFF) / 255.0f)
        for (pixel in pixels) floatBuffer.put(((pixel shr 8)  and 0xFF) / 255.0f)
        for (pixel in pixels) floatBuffer.put((pixel           and 0xFF) / 255.0f)
        floatBuffer.rewind()

        val inputTensor = OnnxTensor.createTensor(
            env,
            floatBuffer,
            longArrayOf(1, 3, inputSize.toLong(), inputSize.toLong())
        )

        val results = session.run(mapOf(inputName to inputTensor))
        val outputTensor = results[0].value as Array<*>  // [1, num_attributes, num_anchors]

        val rawData = outputTensor[0] as Array<*>        // [num_attributes][num_anchors]
        val numAttributes = rawData.size
        val rawOutput = Array(numAttributes) { i -> rawData[i] as FloatArray }

        inputTensor.close()
        results.close()

        return YoloPostProcessor.decode(
            rawOutput, inputSize, classLabels, confidenceThreshold, iouThreshold
        )
    }

    override fun close() {
        ortSession?.close()
        ortEnv?.close()
        ortSession = null
        ortEnv = null
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun copyAssetToCache(context: Context, assetName: String): File {
        val cacheFile = File(context.cacheDir, assetName)
        if (!cacheFile.exists()) {
            context.assets.open(assetName).use { input ->
                FileOutputStream(cacheFile).use { output -> input.copyTo(output) }
            }
        }
        return cacheFile
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
