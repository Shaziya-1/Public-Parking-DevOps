#!/usr/bin/env python3
"""
scripts/evaluate_ai_model.py
=============================================================================
ParkWatch DevOps Project - YOLOv8n Held-out Test Split Evaluator
Student: Sajiya Shaikh | Roll: 24102A2005

Evaluates a trained checkpoint strictly against the held-out test split (1,242 images).
- Computes mAP50, mAP50-95, precision, recall, and class-level metrics
- Distinguishes smoke tests from full benchmark evaluations
- Saves results and confusion matrix plots to report directory
=============================================================================
"""

import os
import sys
import json
import time
import shutil
import argparse
from pathlib import Path

# Prevent Ultralytics from attempting unauthorized pip installs
os.environ["YOLO_AUTOINSTALL"] = "False"

try:
    from ultralytics import YOLO
except ImportError:
    print("[FATAL] ultralytics package is not installed.")
    sys.exit(1)


DEFAULT_CHECKPOINT = Path("models/parkwatch_yolov8n/weights/best.pt")
DEFAULT_DATA_YAML = Path("datasets/pklot_yolo/data.yaml")
DEFAULT_OUTPUT_DIR = Path("REPORTS/ai_evaluation")


def evaluate_model(args):
    print("=" * 70)
    print("PARKWATCH - HELD-OUT TEST EVALUATION")
    print("=" * 70)

    model_path = Path(args.model)
    data_yaml = Path(args.data)
    output_dir = Path(args.output_dir)

    # Validate model checkpoint
    if not model_path.exists():
        print(f"[ERROR] Trained model checkpoint not found: {model_path}")
        print("Please train a model first using 'python scripts/train_ai_model.py --execute'.")
        print("Alternatively, pass an existing checkpoint with '--model <path_to_weights.pt>'.")
        sys.exit(1)

    if not data_yaml.exists():
        print(f"[ERROR] Dataset configuration not found: {data_yaml}")
        print("Please generate it with 'python scripts/coco_to_yolo.py'.")
        sys.exit(1)

    output_dir.mkdir(parents=True, exist_ok=True)

    eval_type = "SMOKE_TEST (Fast Verification)" if args.smoke_test else "FULL_EVALUATION (1,242 Test Images)"
    print(f"Evaluation Mode:    {eval_type}")
    print(f"Model Checkpoint:   {model_path.resolve()}")
    print(f"Dataset Config:     {data_yaml.resolve()}")
    print(f"Split Under Test:   {args.split} (Held-out)")
    print("=" * 70)

    print("\n[INFO] Loading model into Ultralytics validator...")
    model = YOLO(str(model_path.resolve()))

    val_kwargs = {
        "data": str(data_yaml.resolve()),
        "split": args.split,
        "batch": args.batch,
        "imgsz": args.imgsz,
        "device": "cpu",
        "plots": True,
        "project": str(output_dir.resolve()),
        "name": "test_run",
        "exist_ok": True
    }

    if args.smoke_test:
        # Evaluate on only 2% of the test split (~25 images)
        val_kwargs["fraction"] = 0.02

    start_time = time.time()
    metrics = model.val(**val_kwargs)
    elapsed = time.time() - start_time

    # Extract metrics
    map50 = float(metrics.box.map50)
    map50_95 = float(metrics.box.map)
    mp = float(metrics.box.mp)
    mr = float(metrics.box.mr)

    # Class-specific results (0: space-empty, 1: space-occupied)
    class_maps = metrics.box.maps  # array of mAP50-95 per class
    names = metrics.names

    print("\n" + "=" * 70)
    print("EVALUATION RESULTS SUMMARY")
    print("=" * 70)
    print(f"Mode:               {eval_type}")
    print(f"Evaluation Time:    {elapsed:.2f} seconds ({elapsed / 60:.2f} minutes)")
    print(f"mAP@50:             {map50:.4f} ({map50 * 100:.2f}%)")
    print(f"mAP@50-95:          {map50_95:.4f} ({map50_95 * 100:.2f}%)")
    print(f"Mean Precision:     {mp:.4f} ({mp * 100:.2f}%)")
    print(f"Mean Recall:        {mr:.4f} ({mr * 100:.2f}%)")
    print("-" * 70)
    print("Per-Class Metrics:")
    class_results = {}
    for idx, name in names.items():
        c_map = float(class_maps[idx]) if idx < len(class_maps) else 0.0
        class_results[name] = {"mAP50-95": c_map}
        print(f"  - Class {idx} ({name:<15}): mAP50-95 = {c_map:.4f}")
    print("=" * 70)

    # Copy plots to output directory
    run_save_dir = Path(metrics.save_dir) if hasattr(metrics, "save_dir") else output_dir / "test_run"
    cm_plot = run_save_dir / "confusion_matrix.png"
    if cm_plot.exists():
        target_cm = output_dir / "confusion_matrix.png"
        shutil.copy2(cm_plot, target_cm)
        print(f"[INFO] Confusion matrix saved to: {target_cm.resolve()}")

    # Save JSON summary
    summary_data = {
        "timestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "evaluation_type": eval_type,
        "split": args.split,
        "model_checkpoint": str(model_path.resolve()),
        "mAP50": round(map50, 4),
        "mAP50_95": round(map50_95, 4),
        "mean_precision": round(mp, 4),
        "mean_recall": round(mr, 4),
        "per_class": class_results,
        "elapsed_seconds": round(elapsed, 2)
    }

    summary_file = output_dir / "evaluation_summary.json"
    with open(summary_file, "w", encoding="utf-8") as f:
        json.dump(summary_data, f, indent=2)

    # Write Markdown Report
    report_md = f"""# AI Parking Stall Model Evaluation Report

- **Date:** {time.strftime("%Y-%m-%d %H:%M:%S UTC", time.gmtime())}
- **Evaluation Type:** `{eval_type}`
- **Evaluated Checkpoint:** `{model_path.name}`
- **Test Dataset:** PKLot v2 640 Held-Out Test Split (1,242 images)

## Overall Performance Metrics

| Metric | Score | Percentage |
| :--- | :---: | :---: |
| **mAP @ 0.50** | `{map50:.4f}` | **{map50 * 100:.2f}%** |
| **mAP @ 0.50:0.95** | `{map50_95:.4f}` | **{map50_95 * 100:.2f}%** |
| **Mean Precision** | `{mp:.4f}` | **{mp * 100:.2f}%** |
| **Mean Recall** | `{mr:.4f}` | **{mr * 100:.2f}%** |

## Class-Level Metrics

| Class ID | Category Name | mAP @ 0.50:0.95 |
| :---: | :--- | :---: |
| `0` | `space-empty` | `{class_maps[0] if len(class_maps) > 0 else 0.0:.4f}` |
| `1` | `space-occupied` | `{class_maps[1] if len(class_maps) > 1 else 0.0:.4f}` |

## Visual Artifacts
- **Confusion Matrix:** `{output_dir.name}/confusion_matrix.png`
- **Machine-Readable JSON:** `{output_dir.name}/evaluation_summary.json`
"""
    report_file = output_dir / "evaluation_report.md"
    report_file.write_text(report_md, encoding="utf-8")

    print(f"[SUCCESS] Evaluation report written to: {report_file.resolve()}\n")
    return summary_data


def main():
    parser = argparse.ArgumentParser(description="Evaluate YOLOv8n on held-out PKLot test split.")
    parser.add_argument("--model", type=str, default=str(DEFAULT_CHECKPOINT), help="Path to model weights (.pt)")
    parser.add_argument("--data", type=str, default=str(DEFAULT_DATA_YAML), help="Path to data.yaml")
    parser.add_argument("--split", type=str, default="test", help="Dataset split to evaluate")
    parser.add_argument("--batch", type=int, default=16, help="Validation batch size")
    parser.add_argument("--imgsz", type=int, default=640, help="Image resolution")
    parser.add_argument("--output-dir", type=str, default=str(DEFAULT_OUTPUT_DIR), help="Output directory for reports")
    parser.add_argument("--smoke-test", action="store_true", help="Run fast verification on small subset")

    args = parser.parse_args()
    evaluate_model(args)


if __name__ == "__main__":
    main()
