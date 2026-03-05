package com.example.dronedetectorusingartificialvision.viewmodel

import androidx.lifecycle.ViewModel
import com.example.dronedetectorusingartificialvision.model.ModelConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel para la pantalla de selección de modelos.
 * Gestiona qué modelos están disponibles y cuál está seleccionado.
 */
class ModelSelectionViewModel : ViewModel() {

    private val _models = MutableStateFlow(ModelConfig.ALL)
    val models: StateFlow<List<ModelConfig>> = _models.asStateFlow()

    private val _selectedModel = MutableStateFlow<ModelConfig?>(null)
    val selectedModel: StateFlow<ModelConfig?> = _selectedModel.asStateFlow()

    fun selectModel(model: ModelConfig) {
        _selectedModel.value = model
    }

    fun clearSelection() {
        _selectedModel.value = null
    }
}
