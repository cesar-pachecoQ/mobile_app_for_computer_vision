"""
eval_images.py - Evaluación de modelos YOLO sobre dataset de imágenes (Tarea 1).

Implementa la evaluación descrita en el PDF:
- Carga del dataset de imágenes con anotaciones.
- Inferencia con los modelos YOLO.
- Clasificación de detecciones en TP, FP y FN.
- Guardado de imágenes con bounding boxes.
- Generación de estadísticas en CSV.

Uso:
    python -m src.eval_images
"""

from utils import (
    DATA_RAW_DATASET_DIR,
    DATA_PROCESSED_IMAGES_DIR,
    DATA_PROCESSED_STATS_DIR,
    MODELS_DIR,
    ensure_dirs,
    categorize_area,
    get_model_path,
)


def main():
    """Punto de entrada principal para la evaluación en imágenes."""
    ensure_dirs()
    # TODO: Implementar evaluación
    print("🔍 Evaluación de imágenes - Por implementar")
    print(f"   Dataset: {DATA_RAW_DATASET_DIR}")
    print(f"   Salida imágenes: {DATA_PROCESSED_IMAGES_DIR}")
    print(f"   Salida stats: {DATA_PROCESSED_STATS_DIR}")


if __name__ == "__main__":
    main()
