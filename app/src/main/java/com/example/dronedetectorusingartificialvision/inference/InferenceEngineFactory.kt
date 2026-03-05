package com.example.dronedetectorusingartificialvision.inference

import com.example.dronedetectorusingartificialvision.model.EngineType
import com.example.dronedetectorusingartificialvision.model.ModelConfig

/**
 * Fábrica que instancia el motor de inferencia correcto según el [ModelConfig].
 */
object InferenceEngineFactory {

    fun create(modelConfig: ModelConfig): InferenceEngine = when (modelConfig.engineType) {
        EngineType.PYTORCH -> PyTorchEngine()
        EngineType.TFLITE  -> TFLiteEngine()
        EngineType.ONNX    -> OnnxEngine()
    }
}
