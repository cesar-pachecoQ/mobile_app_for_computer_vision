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
