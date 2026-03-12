"""
process_videos.py - Procesamiento de videos con modelos YOLO (Tarea 2).

Implementa el procesamiento descrito en el correo:
- Carga de videos de Avión, Dron y Helicóptero.
- Inferencia frame-a-frame con los modelos YOLO.
- Dibujo de bounding boxes sobre cada frame.
- Generación de videos de salida con las detecciones.

Uso:
    python -m src.process_videos
"""

from utils import (
    DATA_RAW_VIDEOS_DIR,
    OUTPUT_VIDEOS_DIR,
    ensure_dirs,
    get_model_path,
)


def main():
    """Punto de entrada principal para el procesamiento de videos."""
    ensure_dirs()
    # TODO: Implementar procesamiento de videos
    print("🎥 Procesamiento de videos - Por implementar")
    print(f"   Videos entrada: {DATA_RAW_VIDEOS_DIR}")
    print(f"   Videos salida: {OUTPUT_VIDEOS_DIR}")


if __name__ == "__main__":
    main()
