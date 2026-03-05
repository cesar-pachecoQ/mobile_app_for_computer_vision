package com.example.dronedetectorusingartificialvision.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dronedetectorusingartificialvision.ui.screens.DetectionScreen
import com.example.dronedetectorusingartificialvision.ui.screens.ModelSelectionScreen

/** Rutas de navegación de la app. */
object Routes {
    const val MODEL_SELECTION = "model_selection"
    const val DETECTION       = "detection/{modelId}"

    fun detection(modelId: String) = "detection/$modelId"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController   = navController,
        startDestination = Routes.MODEL_SELECTION
    ) {
        composable(Routes.MODEL_SELECTION) {
            ModelSelectionScreen(
                onModelSelected = { modelId ->
                    navController.navigate(Routes.detection(modelId))
                }
            )
        }

        composable(
            route     = Routes.DETECTION,
            arguments = listOf(navArgument("modelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val modelId = backStackEntry.arguments?.getString("modelId") ?: ""
            DetectionScreen(
                modelId    = modelId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
