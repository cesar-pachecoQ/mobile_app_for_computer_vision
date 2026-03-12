"""
utils.py - Funciones compartidas para la evaluación de detección de drones.

Contiene utilidades como categorización de área de bounding boxes,
cálculo de métricas, y funciones auxiliares de I/O.
"""

import os
from pathlib import Path


# ── Rutas del proyecto ──────────────────────────────────────────────────────
PROJECT_ROOT = Path(__file__).resolve().parent.parent

DATA_RAW_DIR = PROJECT_ROOT / "data" / "raw"
DATA_RAW_DATASET_DIR = DATA_RAW_DIR / "dataset"
DATA_RAW_VIDEOS_DIR = DATA_RAW_DIR / "videos"

DATA_PROCESSED_DIR = PROJECT_ROOT / "data" / "processed"
DATA_PROCESSED_IMAGES_DIR = DATA_PROCESSED_DIR / "images"
DATA_PROCESSED_STATS_DIR = DATA_PROCESSED_DIR / "stats"

MODELS_DIR = PROJECT_ROOT / "models"
OUTPUT_VIDEOS_DIR = PROJECT_ROOT / "output_videos"


# ── Funciones de utilidad ───────────────────────────────────────────────────

def categorize_area(area: float) -> str:
    """
    Categoriza el área del bounding box en tamaño descriptivo.

    Args:
        area: Área del bounding box en píxeles cuadrados.

    Returns:
        Categoría del tamaño: 'Pequeño', 'Mediano' o 'Grande'.
    """
    if area < 1000:
        return "Pequeño"
    elif area < 10000:
        return "Mediano"
    else:
        return "Grande"


def ensure_dirs():
    """Crea los directorios de salida si no existen."""
    for dir_path in [
        DATA_PROCESSED_IMAGES_DIR,
        DATA_PROCESSED_STATS_DIR,
        OUTPUT_VIDEOS_DIR,
    ]:
        dir_path.mkdir(parents=True, exist_ok=True)


def get_model_path(model_name: str) -> Path:
    """
    Retorna la ruta completa al modelo .pt dado su nombre.

    Args:
        model_name: Nombre del archivo del modelo (ej. 'yolo8n.pt').

    Returns:
        Ruta completa al modelo.

    Raises:
        FileNotFoundError: Si el modelo no existe.
    """
    model_path = MODELS_DIR / model_name
    if not model_path.exists():
        raise FileNotFoundError(
            f"Modelo no encontrado: {model_path}\n"
            f"Descárgalo del Drive y colócalo en {MODELS_DIR}"
        )
    return model_path
