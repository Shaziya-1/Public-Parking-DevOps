#!/usr/bin/env python3
"""
scripts/train_ai_model.py
=============================================================================
ParkWatch DevOps Project - YOLOv8n Parking Model Trainer
Student: Sajiya Shaikh | Roll: 24102A2005

Trains or fine-tunes Ultralytics YOLOv8n on PKLot dataset.
- Protects CPU resources by estimating runtime before execution
- Enforces strict split separation (train vs valid, test split untouched)
- Supports configurable epochs, batch size, image size, and pilot mode
- Saves artifacts to ignored models/ directory with configuration audit
=============================================================================
"""

import os
import sys
import json
import time
import argparse
from pathlib import Path
import torch

# Prevent Ultralytics from attempting unauthorized pip installs
os.environ["YOLO_AUTOINSTALL"] = "False"

try:
    from ultralytics import YOLO
except ImportError:
    print("[FATAL] ultralytics package is not installed. Please install it with 'pip install ultralytics'.")
    sys.exit(1)


# Default local checkpoint paths to search if not explicitly specified
DEFAULT_LOCAL_CHECKPOINT = Path(r"C:\Users\Admin\.gemini\antigravity\playground\azure-cassini\backend\yolov8n.pt")


def estimate_resources(data_yaml: Path, epochs: int, batch_size: int, imgsz: int, pilot: bool, fraction: float):
    cpu_cores = os.cpu_count() or 4
    cuda_available = torch.cuda.is_available()
    device = "CUDA (" + torch.cuda.get_device_name(0) + ")" if cuda_available else "CPU (Standard)"

    # Base image counts from dataset
    total_train_imgs = 8691
    if pilot:
        train_imgs = max(10, int(total_train_imgs * fraction))
    else:
        train_imgs = total_train_imgs

    batches_per_epoch = (train_imgs + batch_size - 1) // batch_size
    # Estimated seconds per batch: ~0.8s on modern 8-core CPU, ~0.03s on CUDA
    sec_per_batch = 0.05 if cuda_available else 0.8
    sec_per_epoch = batches_per_epoch * sec_per_batch
    total_est_seconds = sec_per_epoch * epochs

    print("\n" + "=" * 70)
    print("RESOURCE USE & RUNTIME ESTIMATE")
    print("=" * 70)
    print(f"Hardware Device:        {device}")
    print(f"Available CPU Cores:    {cpu_cores}")
    print(f"Training Mode:          {'PILOT (Subsampled ' + str(train_imgs) + ' images)' if pilot else 'FULL DATASET (' + str(train_imgs) + ' images)'}")
    print(f"Configured Epochs:      {epochs}")
    print(f"Batch Size:             {batch_size}")
    print(f"Image Resolution:       {imgsz}x{imgsz}")
    print(f"Batches per Epoch:      {batches_per_epoch}")
    print(f"Estimated Time / Epoch: {sec_per_epoch:.1f} seconds ({sec_per_epoch / 60:.2f} mins)")
    print(f"Estimated Total Time:   {total_est_seconds:.1f} seconds ({total_est_seconds / 60:.2f} mins)")
    print("=" * 70)

    return {
        "device": device,
        "cuda": cuda_available,
        "train_imgs": train_imgs,
        "epochs": epochs,
        "est_total_seconds": total_est_seconds
    }


def find_pretrained_weights(candidate_path: Path) -> Path:
    if candidate_path.exists():
        return candidate_path
    if DEFAULT_LOCAL_CHECKPOINT.exists():
        return DEFAULT_LOCAL_CHECKPOINT
    # Check current directory
    local_yolo = Path("yolov8n.pt")
    if local_yolo.exists():
        return local_yolo
    return None


def run_training(args):
    print("=" * 70)
    print("PARKWATCH - YOLOV8N PARKING STALL MODEL TRAINING")
    print("=" * 70)

    # 1. Verify data.yaml exists
    data_yaml = Path(args.data)
    if not data_yaml.exists():
        print(f"[ERROR] Dataset configuration file not found: {data_yaml}")
        print("Please run 'python scripts/coco_to_yolo.py' first to generate data.yaml.")
        sys.exit(1)

    # 2. Check pretrained weights
    pretrained_path = find_pretrained_weights(Path(args.weights))
    if not pretrained_path:
        print("[ERROR] Pretrained YOLOv8n weights could not be found locally.")
        print("Expected path or download failed. Please provide valid path via --weights.")
        sys.exit(1)

    print(f"[INFO] Using pretrained checkpoint: {pretrained_path.resolve()}")

    # 3. Resource Estimation
    est = estimate_resources(data_yaml, args.epochs, args.batch, args.imgsz, args.pilot, args.fraction)

    if args.estimate_only:
        print("[INFO] --estimate-only flag specified. Exiting without training.")
        sys.exit(0)

    # Safety guard: Prevent accidental full CPU training if not explicitly confirmed
    if not args.execute:
        print("\n[SAFETY NOTICE]")
        print("Model training was NOT executed because --execute was not provided.")
        print(f"To run this configuration, add the '--execute' flag.")
        print(f"Example Pilot: python scripts/train_ai_model.py --pilot --epochs 1 --execute")
        sys.exit(0)

    # 4. Initialize YOLO model
    print("\n[INFO] Loading YOLO model...")
    model = YOLO(str(pretrained_path))

    # Output directory
    output_project = Path(args.project)
    output_project.mkdir(parents=True, exist_ok=True)

    training_start = time.time()
    print(f"[INFO] Launching training on device: {'cuda' if est['cuda'] else 'cpu'} ...")

    # If pilot mode is requested, use subset fraction
    train_args = {
        "data": str(data_yaml.resolve()),
        "epochs": args.epochs,
        "batch": args.batch,
        "imgsz": args.imgsz,
        "device": 0 if est["cuda"] else "cpu",
        "workers": args.workers,
        "project": str(output_project.resolve()),
        "name": args.name,
        "exist_ok": True,
        "seed": args.seed,
        "deterministic": True,
        "plots": True,
        "val": True,
    }

    if args.pilot:
        train_args["fraction"] = args.fraction

    # Execute training
    results = model.train(**train_args)
    elapsed_time = time.time() - training_start

    print("\n" + "=" * 70)
    print("TRAINING RUN COMPLETE")
    print(f"Elapsed Time: {elapsed_time:.2f} seconds ({elapsed_time / 60:.2f} minutes)")
    save_dir = Path(results.save_dir) if hasattr(results, "save_dir") else output_project / args.name
    print(f"Artifacts Saved To: {save_dir.resolve()}")
    print("=" * 70)

    # Save training metadata
    meta = {
        "timestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "model_architecture": "YOLOv8n",
        "pretrained_weights": str(pretrained_path.name),
        "dataset_yaml": str(data_yaml.resolve()),
        "device": est["device"],
        "epochs": args.epochs,
        "batch_size": args.batch,
        "imgsz": args.imgsz,
        "pilot_mode": args.pilot,
        "fraction": args.fraction if args.pilot else 1.0,
        "elapsed_seconds": round(elapsed_time, 2),
        "weights_path": str((save_dir / "weights" / "best.pt").resolve()),
        "status": "COMPLETED"
    }

    meta_file = save_dir / "training_metadata.json"
    with open(meta_file, "w", encoding="utf-8") as f:
        json.dump(meta, f, indent=2)

    print(f"[SUCCESS] Training metadata written to: {meta_file}")
    return save_dir


def main():
    parser = argparse.ArgumentParser(description="Train or fine-tune YOLOv8n parking occupancy model.")
    parser.add_argument("--data", type=str, default="datasets/pklot_yolo/data.yaml", help="Path to data.yaml")
    parser.add_argument("--weights", type=str, default=str(DEFAULT_LOCAL_CHECKPOINT), help="Path to pretrained weights")
    parser.add_argument("--epochs", type=int, default=1, help="Number of training epochs")
    parser.add_argument("--batch", type=int, default=16, help="Batch size")
    parser.add_argument("--imgsz", type=int, default=640, help="Image resolution")
    parser.add_argument("--workers", type=int, default=2, help="DataLoader workers")
    parser.add_argument("--seed", type=int, default=42, help="Random seed for reproducibility")
    parser.add_argument("--project", type=str, default="models", help="Output directory for model weights")
    parser.add_argument("--name", type=str, default="parkwatch_yolov8n", help="Run name")
    parser.add_argument("--pilot", action="store_true", default=True, help="Run in pilot mode (subsampled training data for CPU safety)")
    parser.add_argument("--fraction", type=float, default=0.02, help="Fraction of training images in pilot mode (default 0.02 = ~174 images)")
    parser.add_argument("--estimate-only", action="store_true", help="Print resource estimate and exit without training")
    parser.add_argument("--execute", action="store_true", help="Explicitly authorize training execution")

    args = parser.parse_args()
    run_training(args)


if __name__ == "__main__":
    main()
