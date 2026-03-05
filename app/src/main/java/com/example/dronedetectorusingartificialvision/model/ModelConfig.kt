package com.example.dronedetectorusingartificialvision.model

/**
 * Tipo de motor de inferencia disponible.
 */
enum class EngineType(val displayName: String) {
    PYTORCH("PyTorch Mobile"),
    TFLITE("TensorFlow Lite"),
    ONNX("ONNX Runtime")
}

/**
 * Precisión numérica del modelo.
 */
enum class ModelPrecision(val label: String) {
    FP32("Float32"),
    FP16("Float16"),
    INT8("Int8 (Quantizado)")
}

/**
 * Configuración de un modelo de visión artificial.
 *
 * @param id         Identificador único del modelo.
 * @param displayName Nombre legible para la UI.
 * @param fileName   Nombre del archivo en assets/.
 * @param engineType Motor de inferencia requerido.
 * @param precision  Precisión numérica del modelo.
 * @param baseModel  Arquitectura base (ej. "YOLOv8n", "YOLOv26n").
 * @param fileSizeMb Tamaño aproximado en MB.
 */
data class ModelConfig(
    val id: String,
    val displayName: String,
    val fileName: String,
    val engineType: EngineType,
    val precision: ModelPrecision,
    val baseModel: String,
    val fileSizeMb: Float
) {
    companion object {
        /** Catálogo completo de modelos disponibles en assets/. */
        val ALL: List<ModelConfig> = listOf(
            // ─── TorchScript (.ptl) ───────────────────────────────────────────
            ModelConfig(
                id = "yolov26n_ptl_fp32",
                displayName = "YOLOv26n — PTL FP32",
                fileName = "yolov26n_custom_torchscript.ptl",
                engineType = EngineType.PYTORCH,
                precision = ModelPrecision.FP32,
                baseModel = "YOLOv26n",
                fileSizeMb = 9.3f
            ),
            ModelConfig(
                id = "yolov26n_ptl_fp16",
                displayName = "YOLOv26n — PTL FP16",
                fileName = "yolov26n_custom_torchscript_fp16.ptl",
                engineType = EngineType.PYTORCH,
                precision = ModelPrecision.FP16,
                baseModel = "YOLOv26n",
                fileSizeMb = 9.3f
            ),
            ModelConfig(
                id = "yolov8n_ptl_fp32",
                displayName = "YOLOv8n — PTL FP32",
                fileName = "yolov8n_custom_torchscript.ptl",
                engineType = EngineType.PYTORCH,
                precision = ModelPrecision.FP32,
                baseModel = "YOLOv8n",
                fileSizeMb = 11.6f
            ),
            ModelConfig(
                id = "yolov8n_ptl_fp16",
                displayName = "YOLOv8n — PTL FP16",
                fileName = "yolov8n_custom_torchscript_fp16.ptl",
                engineType = EngineType.PYTORCH,
                precision = ModelPrecision.FP16,
                baseModel = "YOLOv8n",
                fileSizeMb = 11.6f
            ),
            // ─── TFLite (.tflite) ────────────────────────────────────────────
            ModelConfig(
                id = "yolov26n_tflite_fp32",
                displayName = "YOLOv26n — TFLite FP32",
                fileName = "yolov26n_custom_fp32.tflite",
                engineType = EngineType.TFLITE,
                precision = ModelPrecision.FP32,
                baseModel = "YOLOv26n",
                fileSizeMb = 9.5f
            ),
            ModelConfig(
                id = "yolov26n_tflite_fp16",
                displayName = "YOLOv26n — TFLite FP16",
                fileName = "yolov26n_custom_fp16.tflite",
                engineType = EngineType.TFLITE,
                precision = ModelPrecision.FP16,
                baseModel = "YOLOv26n",
                fileSizeMb = 4.9f
            ),
            ModelConfig(
                id = "yolov26n_tflite_int8",
                displayName = "YOLOv26n — TFLite INT8",
                fileName = "yolov26n_custom_int8.tflite",
                engineType = EngineType.TFLITE,
                precision = ModelPrecision.INT8,
                baseModel = "YOLOv26n",
                fileSizeMb = 2.8f
            ),
            ModelConfig(
                id = "yolov8n_tflite_fp32",
                displayName = "YOLOv8n — TFLite FP32",
                fileName = "yolov8n_custom_fp32.tflite",
                engineType = EngineType.TFLITE,
                precision = ModelPrecision.FP32,
                baseModel = "YOLOv8n",
                fileSizeMb = 11.7f
            ),
            ModelConfig(
                id = "yolov8n_tflite_fp16",
                displayName = "YOLOv8n — TFLite FP16",
                fileName = "yolov8n_custom_fp16.tflite",
                engineType = EngineType.TFLITE,
                precision = ModelPrecision.FP16,
                baseModel = "YOLOv8n",
                fileSizeMb = 5.9f
            ),
            ModelConfig(
                id = "yolov8n_tflite_int8",
                displayName = "YOLOv8n — TFLite INT8",
                fileName = "yolov8n_custom_int8.tflite",
                engineType = EngineType.TFLITE,
                precision = ModelPrecision.INT8,
                baseModel = "YOLOv8n",
                fileSizeMb = 3.2f
            ),
            // ─── ONNX (.onnx) ────────────────────────────────────────────────
            ModelConfig(
                id = "yolov26n_onnx",
                displayName = "YOLOv26n — ONNX",
                fileName = "yolov26n_custom.onnx",
                engineType = EngineType.ONNX,
                precision = ModelPrecision.FP32,
                baseModel = "YOLOv26n",
                fileSizeMb = 9.4f
            ),
            ModelConfig(
                id = "yolov8n_onnx",
                displayName = "YOLOv8n — ONNX",
                fileName = "yolov8n_custom.onnx",
                engineType = EngineType.ONNX,
                precision = ModelPrecision.FP32,
                baseModel = "YOLOv8n",
                fileSizeMb = 11.7f
            )
        )
    }
}
