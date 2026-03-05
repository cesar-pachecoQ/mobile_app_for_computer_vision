package com.example.dronedetectorusingartificialvision.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Gestiona el ciclo de vida de CameraX: Preview + ImageAnalysis para inferencia.
 *
 * @param onFrameCaptured Callback llamado en [analyzerExecutor] con cada frame
 *                        como [Bitmap] ARGB_8888 y su timestamp.
 */
class CameraManager(
    private val context: Context,
    private val onFrameCaptured: (bitmap: Bitmap, timestampMs: Long) -> Unit
) {
    private val analyzerExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null

    /**
     * Inicia la cámara, vinculada al [lifecycleOwner].
     *
     * @param lifecycleOwner    Activity o Fragment que controla el ciclo de vida.
     * @param previewView       Vista donde se renderiza el preview.
     * @param lensFacing        Cámara a usar (trasera por defecto).
     * @param targetResolution  Resolución del análisis (menor → más rápido).
     */
    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        targetResolution: Size = Size(640, 640)
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(targetResolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(analyzerExecutor) { imageProxy ->
                        processFrame(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /** Detiene la cámara y libera recursos. Llamar desde onDestroy. */
    fun stop() {
        cameraProvider?.unbindAll()
        analyzerExecutor.shutdown()
    }

    // ─── Frame processing ─────────────────────────────────────────────────────

    private fun processFrame(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap()
        val timestamp = System.currentTimeMillis()
        try {
            onFrameCaptured(bitmap, timestamp)
        } finally {
            imageProxy.close()
        }
    }
}
