#!/usr/bin/env python3
"""
scripts/train_improved_model.py
=============================================================================
ParkWatch DevOps Project - YOLOv8n Improved Model Training
Student: Sajiya Shaikh | Roll: 24102A2005

Trains an improved YOLOv8n detector on PKLot parking dataset with CPU optimization:
- Uses representative stratified 300-image training subset
- Uses 50-image validation subset for fast per-epoch evaluation
- Preserves full held-out test split (1,242 images) untouched
- Saves model to models/parkwatch_yolov8n_improved/
=============================================================================
"""

import os
import sys
import json
import time
from pathlib import Path

os.environ["YOLO_AUTOINSTALL"] = "False"

from ultralytics import YOLO

DATA_YAML = Path("datasets/pklot_yolo/data_fast.yaml")
BASE_WEIGHTS = Path("yolov8n.pt")
OUTPUT_PROJECT = Path("models")
RUN_NAME = "parkwatch_yolov8n_improved"
EPOCHS = 6
BATCH = 16
IMGSZ = 416


def main():
    print("=" * 70)
    print("PARKWATCH - IMPROVED YOLOV8N TRAINING PIPELINE")
    print("=" * 70)
    print(f"Data Config:      {DATA_YAML.resolve()}")
    print(f"Base Weights:     {BASE_WEIGHTS.resolve()}")
    print(f"Epochs:           {EPOCHS}")
    print(f"Batch Size:       {BATCH}")
    print(f"Image Resolution: {IMGSZ}x{IMGSZ}")
    print(f"Target Output:    {OUTPUT_PROJECT.resolve() / RUN_NAME}")
    print("=" * 70)

    if not DATA_YAML.exists():
        print(f"[ERROR] Data YAML not found: {DATA_YAML}")
        sys.exit(1)

    if not BASE_WEIGHTS.exists():
        print(f"[ERROR] Base weights not found: {BASE_WEIGHTS}")
        sys.exit(1)

    # Load base YOLO model
    model = YOLO(str(BASE_WEIGHTS.resolve()))

    start_time = time.time()
    results = model.train(
        data=str(DATA_YAML.resolve()),
        epochs=EPOCHS,
        batch=BATCH,
        imgsz=IMGSZ,
        device="cpu",
        workers=2,
        project=str(OUTPUT_PROJECT.resolve()),
        name=RUN_NAME,
        exist_ok=True,
        seed=42,
        deterministic=True,
        plots=True,
        warmup_epochs=1.0,
        lr0=0.01,
        lrf=0.01,
        verbose=True
    )
    elapsed = time.time() - start_time
    print(f"\n[SUCCESS] Training finished in {elapsed:.1f} seconds ({elapsed / 60:.2f} minutes).")

    save_dir = Path(results.save_dir) if hasattr(results, "save_dir") else OUTPUT_PROJECT / RUN_NAME
    best_weights = save_dir / "weights" / "best.pt"
    print(f"[SUCCESS] Best checkpoint saved to: {best_weights.resolve()}")

    # Write training metadata
    meta = {
        "timestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "model_architecture": "YOLOv8n",
        "pretrained_weights": "yolov8n.pt",
        "dataset_yaml": str(DATA_YAML.resolve()),
        "device": "CPU (12-core)",
        "epochs": EPOCHS,
        "batch_size": BATCH,
        "imgsz": IMGSZ,
        "train_samples": 300,
        "val_samples": 50,
        "elapsed_seconds": round(elapsed, 2),
        "weights_path": str(best_weights.resolve()),
        "status": "COMPLETED"
    }
    with open(save_dir / "training_metadata.json", "w", encoding="utf-8") as f:
        json.dump(meta, f, indent=2)


if __name__ == "__main__":
    main()
