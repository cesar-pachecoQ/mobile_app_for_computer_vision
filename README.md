# 🛩️ Drone Detection Evaluation

Evaluación de modelos YOLO para detección de drones, aviones y helicópteros.

## 📁 Estructura del Proyecto

```
drones_detection_eval/
├── data/
│   ├── raw/                # Archivos originales del Drive (no se tocan)
│   │   ├── dataset/        # Contenido de Dataset.zip
│   │   └── videos/         # Videos de Avion, Dron y Helicoptero
│   └── processed/          # Resultados de la tarea 1
│       ├── images/         # Drones detectados, FN y FP
│       └── stats/          # CSVs generados
├── models/                 # Modelos .pt descargados del Drive
│   ├── yolo8n.pt           # De Yolo8n_entrenado_02_Marzo
│   └── yolo26n.pt          # De Yolo26n_entrenado_06_Marzo
├── notebooks/              # Para exploración rápida (opcional)
├── src/                    # Scripts de Python (el "motor")
│   ├── __init__.py
│   ├── utils.py            # Funciones compartidas (ej. categorizar área)
│   ├── eval_images.py      # Implementación del PDF (Tarea 1)
│   └── process_videos.py   # Implementación del correo (Tarea 2)
├── output_videos/          # Videos finales con bounding boxes
├── .gitignore              # Para no subir datos pesados ni modelos a Git
├── requirements.txt        # Librerías necesarias
└── README.md               # Este archivo
```

## 🚀 Instalación

1. **Clonar el repositorio:**
   ```bash
   git clone <url-del-repo>
   cd drone_detection_eval
   ```

2. **Crear entorno virtual:**
   ```bash
   python -m venv venv
   source venv/bin/activate  # Linux/Mac
   # venv\Scripts\activate   # Windows
   ```

3. **Instalar dependencias:**
   ```bash
   pip install -r requirements.txt
   ```

4. **Descargar datos y modelos desde el Drive:**
   - Colocar el contenido de `Dataset.zip` en `data/raw/dataset/`
   - Colocar los videos en `data/raw/videos/`
   - Colocar los modelos `.pt` en `models/`

## 📋 Tareas

### Tarea 1: Evaluación en Imágenes (`src/eval_images.py`)
Evalúa los modelos sobre el dataset de imágenes. Genera:
- Imágenes con detecciones (TP), falsos negativos (FN) y falsos positivos (FP) en `data/processed/images/`
- Estadísticas en CSV en `data/processed/stats/`

```bash
python -m src.eval_images
```

### Tarea 2: Procesamiento de Videos (`src/process_videos.py`)
Procesa los videos con los modelos y genera videos con bounding boxes en `output_videos/`.

```bash
python -m src.process_videos
```

## 🧰 Modelos

| Modelo       | Archivo       | Fecha de Entrenamiento |
|-------------|---------------|----------------------|
| YOLOv8-nano | `yolo8n.pt`   | 02 de Marzo          |
| YOLOv26-nano| `yolo26n.pt`  | 06 de Marzo          |

## 📦 Dependencias Principales

- **ultralytics** - Framework YOLO para detección de objetos
- **opencv-python** - Procesamiento de imágenes y video
- **pandas** - Manejo y análisis de datos (CSVs)
- **matplotlib / seaborn** - Visualización de resultados
