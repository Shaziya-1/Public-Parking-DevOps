#!/usr/bin/env python3
"""
scripts/ai_inference_demo.py
=============================================================================
ParkWatch DevOps Project - AI Parking Stall & Vehicle Inference Engine
Student: Sajiya Shaikh | Roll: 24102A2005

Unified Multi-Stage Inference Engine:
1. Vehicle Detection: Pretrained Ultralytics YOLOv8 COCO detector for cars,
   buses, trucks, and motorcycles with class-agnostic NMS.
2. Parking-Space Occupancy:
   - Calibrated Layout: Evaluates physical stall regions from calibration
     registry against vehicle detections via spatial intersection over area.
   - Surveillance Detection: On PKLot surveillance streams, detects stall
     boundaries and occupancy states.
   - Uncalibrated Layout: Reliably reports vehicle counts and flags stall layout.
3. Strict Safety: Completely decoupled from Spring Boot municipal database.
=============================================================================
"""

import os
import sys
import json
import argparse
from pathlib import Path
from PIL import Image

# Prevent Ultralytics from attempting unauthorized pip installs
os.environ["YOLO_AUTOINSTALL"] = "False"

PROJECT_ROOT = Path(__file__).resolve().parent.parent
IMPROVED_MODEL = PROJECT_ROOT / "models" / "parkwatch_yolov8n_improved" / "weights" / "best.pt"
PILOT_MODEL = PROJECT_ROOT / "models" / "parkwatch_yolov8n" / "weights" / "best.pt"
COCO_MODEL = PROJECT_ROOT / "yolov8n.pt"
DEFAULT_MODEL = IMPROVED_MODEL if IMPROVED_MODEL.exists() else PILOT_MODEL
CALIBRATION_FILE = PROJECT_ROOT / "config" / "parking_lot_calibrations.json"

# Vehicle classes in standard COCO (80 classes)
VEHICLE_CLASS_IDS = [2, 3, 5, 7]  # car, motorcycle, bus, truck


def validate_image_path(image_path: Path) -> bool:
    if not image_path.exists():
        print(f"[ERROR] Input image file does not exist: {image_path}")
        return False

    valid_extensions = {".jpg", ".jpeg", ".png", ".bmp", ".webp"}
    if image_path.suffix.lower() not in valid_extensions:
        print(f"[ERROR] Unsupported image format: '{image_path.suffix}'. Expected one of: {valid_extensions}")
        return False

    try:
        with Image.open(image_path) as img:
            img.verify()
        return True
    except Exception as e:
        print(f"[ERROR] Input image appears corrupted: {e}")
        return False


def load_calibrations():
    if not CALIBRATION_FILE.exists():
        return {}
    try:
        with open(CALIBRATION_FILE, "r", encoding="utf-8") as f:
            return json.load(f)
    except Exception as e:
        print(f"[WARN] Failed to read calibration file: {e}")
        return {}


def match_calibrated_stalls(img_shape, calibrations):
    h, w = img_shape[:2]
    ar = w / float(h)

    for key, calib in calibrations.items():
        ref_h, ref_w = calib["target_shape"]
        ref_ar = ref_w / float(ref_h)
        # Check aspect ratio match and rough dimension scale
        if abs(ar - ref_ar) < 0.08:
            scale_x = w / float(ref_w)
            scale_y = h / float(ref_h)
            stalls = []
            for s in calib["stalls"]:
                bx = s["box"]
                stalls.append({
                    "id": s["id"],
                    "box": [
                        int(bx[0] * scale_x),
                        int(bx[1] * scale_y),
                        int(bx[2] * scale_x),
                        int(bx[3] * scale_y)
                    ],
                    "status": "empty",
                    "occupant": None
                })
            return key, calib.get("description", key), stalls, calib
    return None, None, None, None


def is_pklot_image(image_path: Path, img_shape) -> bool:
    h, w = img_shape[:2]
    name = image_path.name.lower()
    # PKLot images are 640x640 and follow the date-time naming convention
    if (w == 640 and h == 640) and ("2012-" in name or "2013-" in name or ".rf." in name or "pklot" in name):
        return True
    return False


def run_inference(image_path: Path, model_path: Path, conf: float, output_path: Path, save_output: bool, iou: float = 0.20):
    print("=" * 70)
    print("PARKWATCH - UNIFIED AI PARKING OCCUPANCY INFERENCE")
    print("=" * 70)

    # 1. Validate image
    if not validate_image_path(image_path):
        sys.exit(1)

    # 2. Check model checkpoints
    if not model_path.exists():
        print(f"[ERROR] Stall model checkpoint not found at: {model_path}")
        sys.exit(1)

    print(f"Input Image:        {image_path.resolve()}")
    print(f"Stall Checkpoint:   {model_path.resolve()}")
    print(f"Vehicle Checkpoint: {COCO_MODEL.resolve() if COCO_MODEL.exists() else 'Unavailable'}")
    print(f"Confidence Level:   {conf}")
    print(f"NMS IoU Threshold:  {iou}")
    print("=" * 70)

    import cv2
    import numpy as np
    from ultralytics import YOLO

    img_cv = cv2.imread(str(image_path.resolve()))
    if img_cv is None:
        print(f"[ERROR] Failed to load image with OpenCV: {image_path}")
        sys.exit(1)

    h_img, w_img = img_cv.shape[:2]

    # Pre-match camera perspective against calibration registry
    calibrations = load_calibrations()
    calib_key, calib_desc, calibrated_stalls, target_calib = match_calibrated_stalls(img_cv.shape, calibrations)

    # Determine perspective-optimized inference resolution and sensitivity
    v_imgsz = target_calib.get("inference_imgsz", 640) if target_calib else 640
    # For overhead lot (Image 1), conf 0.20 reliably captures subtle dark-colored vehicles
    v_conf = min(conf, 0.20) if calib_key == "image1_parking_lot" else conf

    # STAGE 1: Real Vehicle Detection using YOLOv8 COCO
    vehicles = []
    vehicle_confidences = []

    if COCO_MODEL.exists():
        v_model = YOLO(str(COCO_MODEL.resolve()))
        v_results = v_model.predict(
            source=str(image_path.resolve()),
            imgsz=v_imgsz,
            conf=v_conf,
            iou=0.45,
            agnostic_nms=True,
            classes=VEHICLE_CLASS_IDS,
            device="cpu",
            verbose=False
        )[0]

        raw_vehicles = []
        if v_results.boxes is not None and len(v_results.boxes) > 0:
            for b in v_results.boxes:
                cls_id = int(b.cls[0].item())
                confidence = float(b.conf[0].item())
                v_name = v_model.names.get(cls_id, "vehicle")
                x1, y1, x2, y2 = [int(v) for v in b.xyxy[0].tolist()]
                if x2 > x1 and y2 > y1:
                    raw_vehicles.append({
                        "class": v_name,
                        "conf": confidence,
                        "box": [x1, y1, x2, y2]
                    })

        # Remove duplicate multi-class detections of the exact same physical vehicle
        raw_vehicles.sort(key=lambda v: v["conf"], reverse=True)
        for v in raw_vehicles:
            v_box = v["box"]
            v_area = (v_box[2] - v_box[0]) * (v_box[3] - v_box[1])
            is_dup = False
            for kept in vehicles:
                k_box = kept["box"]
                k_area = (k_box[2] - k_box[0]) * (k_box[3] - k_box[1])
                xA = max(v_box[0], k_box[0])
                yA = max(v_box[1], k_box[1])
                xB = min(v_box[2], k_box[2])
                yB = min(v_box[3], k_box[3])
                inter = max(0, xB - xA) * max(0, yB - yA)
                iou_overlap = inter / float(v_area + k_area - inter) if (v_area + k_area - inter) > 0 else 0
                if iou_overlap >= 0.60:
                    is_dup = True
                    break
            if not is_dup:
                vehicles.append(v)
                vehicle_confidences.append(v["conf"])

    vehicles_detected_count = len(vehicles)

    # STAGE 2: Parking-Space Region Identification & Occupancy Evaluation
    mode = "UNCALIBRATED"
    stalls_list = []
    empty_count = 0
    occupied_count = 0
    is_reliable = True
    reliability_warning = None

    if calibrated_stalls is not None and len(calibrated_stalls) > 0:
        mode = "CALIBRATED"
        stalls_list = calibrated_stalls

        # Evaluate Occupancy via Vehicle-to-Stall Overlap (IoA & Centroid)
        # Sort vehicles by confidence to match highest-confidence vehicle first
        sorted_v = sorted(vehicles, key=lambda v: v["conf"], reverse=True)
        assigned_vehicles = set()

        for s in stalls_list:
            s_box = s["box"]
            s_area = (s_box[2] - s_box[0]) * (s_box[3] - s_box[1])
            best_score = 0.0
            best_v_idx = -1

            for v_idx, v in enumerate(sorted_v):
                if v_idx in assigned_vehicles:
                    continue
                v_box = v["box"]
                xA = max(s_box[0], v_box[0])
                yA = max(s_box[1], v_box[1])
                xB = min(s_box[2], v_box[2])
                yB = min(s_box[3], v_box[3])
                inter = max(0, xB - xA) * max(0, yB - yA)
                ioa = inter / float(s_area) if s_area > 0 else 0.0

                v_cx = (v_box[0] + v_box[2]) / 2.0
                v_cy = (v_box[1] + v_box[3]) / 2.0
                in_center = (s_box[0] <= v_cx <= s_box[2]) and (s_box[1] <= v_cy <= s_box[3])

                if (in_center and ioa >= 0.08) or ioa >= 0.18:
                    score = ioa + (0.5 if in_center else 0.0)
                    if score > best_score:
                        best_score = score
                        best_v_idx = v_idx

            if best_v_idx != -1:
                s["status"] = "occupied"
                s["occupant"] = sorted_v[best_v_idx]["class"]
                assigned_vehicles.add(best_v_idx)
                occupied_count += 1
            else:
                s["status"] = "empty"
                s["occupant"] = None
                empty_count += 1

        total_detected = len(stalls_list)
        occupancy_pct = (occupied_count / total_detected * 100) if total_detected > 0 else 0.0
        avg_conf = (sum(vehicle_confidences) / len(vehicle_confidences)) if vehicle_confidences else 0.0
        min_conf = min(vehicle_confidences) if vehicle_confidences else 0.0
        max_conf = max(vehicle_confidences) if vehicle_confidences else 0.0

    elif is_pklot_image(image_path, img_cv.shape):
        mode = "PKLOT_SURVEILLANCE"
        stall_model = YOLO(str(model_path.resolve()))
        s_results = stall_model.predict(
            source=str(image_path.resolve()),
            conf=conf,
            iou=iou,
            agnostic_nms=True,
            device="cpu",
            verbose=False
        )[0]

        s_boxes = s_results.boxes if (s_results and len(s_results) > 0) else []
        confidences = []

        if s_boxes is not None and len(s_boxes) > 0:
            for b in s_boxes:
                cls_id = int(b.cls[0].item())
                confidence = float(b.conf[0].item())
                x1, y1, x2, y2 = [int(v) for v in b.xyxy[0].tolist()]
                if x2 <= x1 or y2 <= y1:
                    continue
                confidences.append(confidence)
                if cls_id == 0:
                    empty_count += 1
                    status = "empty"
                else:
                    occupied_count += 1
                    status = "occupied"

                stalls_list.append({
                    "id": f"S{len(stalls_list)+1}",
                    "box": [x1, y1, x2, y2],
                    "status": status,
                    "conf": confidence
                })

        total_detected = len(stalls_list)
        occupancy_pct = (occupied_count / total_detected * 100) if total_detected > 0 else 0.0
        avg_conf = (sum(confidences) / len(confidences)) if confidences else 0.0
        min_conf = min(confidences) if confidences else 0.0
        max_conf = max(confidences) if confidences else 0.0

    else:
        # Uncalibrated image: report vehicle detections honestly without drawing arbitrary pavement boxes
        mode = "UNCALIBRATED"
        total_detected = 0
        empty_count = 0
        occupied_count = 0
        occupancy_pct = 0.0
        avg_conf = (sum(vehicle_confidences) / len(vehicle_confidences)) if vehicle_confidences else 0.0
        min_conf = min(vehicle_confidences) if vehicle_confidences else 0.0
        max_conf = max(vehicle_confidences) if vehicle_confidences else 0.0

        if vehicles_detected_count > 0:
            is_reliable = True
            reliability_warning = (
                f"Parking Stall Layout Uncalibrated: Detected {vehicles_detected_count} physical vehicles. "
                "Because this camera perspective is not in the stall calibration registry, slot-by-slot "
                "boundaries are withheld to avoid arbitrary pavement boxes. Calibrate stall polygons for space occupancy."
            )
        else:
            is_reliable = False
            reliability_warning = (
                f"Zero parking stalls detected at confidence threshold {conf:.2f}. "
                "No stalls or vehicles met the current threshold. (Pilot or baseline weights may require lower sensitivity, "
                "or verify that the camera perspective matches surveillance parking lot layouts)."
            )

    # STAGE 3: Draw Professional Multi-Layer Visualization
    # Layer A: Parking Stall Outlines (Green = Empty, Red = Occupied)
    for s in stalls_list:
        x1, y1, x2, y2 = s["box"]
        status = s["status"]
        color = (0, 0, 220) if status == "occupied" else (0, 200, 0)
        cv2.rectangle(img_cv, (x1, y1), (x2, y2), color, 3)
        s_lbl = f"{s['id']}: {status.upper()}"
        cv2.putText(img_cv, s_lbl, (x1 + 4, y1 + 18), cv2.FONT_HERSHEY_SIMPLEX, 0.42, color, 2, cv2.LINE_AA)

    # Layer B: Vehicle Bounding Boxes (Cyan / Gold) around actual vehicles
    for v in vehicles:
        x1, y1, x2, y2 = v["box"]
        cv2.rectangle(img_cv, (x1, y1), (x2, y2), (255, 200, 0), 2)  # Cyan
        v_lbl = f"{v['class']} {v['conf']:.2f}"
        cv2.putText(img_cv, v_lbl, (x1 + 4, y2 - 6), cv2.FONT_HERSHEY_SIMPLEX, 0.40, (255, 200, 0), 1, cv2.LINE_AA)

    # Layer C: Professional Header Banner
    cv2.rectangle(img_cv, (0, 0), (w_img, 42), (20, 20, 20), -1)
    if mode == "CALIBRATED":
        header_text = (
            f"ParkWatch AI | Calibrated Stalls: {total_detected} (Empty: {empty_count}, Occupied: {occupied_count}) | "
            f"Vehicles: {vehicles_detected_count} | Occupancy: {occupancy_pct:.1f}% | Conf>={conf:.2f}"
        )
    elif mode == "PKLOT_SURVEILLANCE":
        header_text = (
            f"ParkWatch AI | PKLot Surveillance: {total_detected} Stalls (Empty: {empty_count}, Occupied: {occupied_count}) | "
            f"Occupancy: {occupancy_pct:.1f}% | Conf>={conf:.2f}"
        )
    else:
        header_text = (
            f"ParkWatch AI | Vehicles Detected: {vehicles_detected_count} (Stall Layout Uncalibrated) | Conf>={conf:.2f}"
        )

    cv2.putText(img_cv, header_text, (12, 28), cv2.FONT_HERSHEY_SIMPLEX, 0.58, (255, 255, 255), 2, cv2.LINE_AA)

    # Save output image
    if save_output:
        output_path.parent.mkdir(parents=True, exist_ok=True)
        cv2.imwrite(str(output_path.resolve()), img_cv)
        print(f"[SUCCESS] Annotated visual output saved to: {output_path.resolve()}")

    # Print Summary Report
    print("\n" + "=" * 70)
    print("DETECTION RESULTS")
    print("=" * 70)
    print(f"Inference Mode:             {mode}")
    print(f"Physical Vehicles Detected: {vehicles_detected_count}")
    print(f"Total Parking Stalls:       {total_detected}")
    print(f"  - Empty Stalls:           {empty_count}")
    print(f"  - Occupied Stalls:        {occupied_count}")
    print(f"Estimated Lot Occupancy:    {occupancy_pct:.1f}%")
    print(f"Reliability Status:         {'RELIABLE' if is_reliable else 'UNVERIFIED'}")
    if reliability_warning:
        print(f"Reliability Warning:        {reliability_warning}")
    print("-" * 70)

    # Structured JSON output for web integration
    json_result = {
        "status": "SUCCESS",
        "vehiclesDetected": vehicles_detected_count,
        "totalDetected": total_detected,
        "emptyCount": empty_count,
        "occupiedCount": occupied_count,
        "occupancyPct": round(occupancy_pct, 1),
        "minConf": round(min_conf, 4),
        "maxConf": round(max_conf, 4),
        "avgConf": round(avg_conf, 4),
        "calibrationMode": mode,
        "reliable": is_reliable,
        "reliabilityWarning": reliability_warning,
        "outputFile": str(output_path.resolve()) if save_output else None
    }
    print(f"__JSON_RESULT_START__{json.dumps(json_result)}__JSON_RESULT_END__")
    return json_result


def main():
    parser = argparse.ArgumentParser(description="Run parking stall and vehicle detection on a surveillance camera image.")
    parser.add_argument("--image", "-i", type=Path, required=True, help="Path to input surveillance camera image")
    parser.add_argument("--model", "-m", type=Path, default=DEFAULT_MODEL, help="Path to trained YOLOv8 model weights (.pt)")
    parser.add_argument("--conf", "-c", type=float, default=0.25, help="Confidence threshold (default: 0.25)")
    parser.add_argument("--iou", type=float, default=0.20, help="NMS IoU threshold for overlapping box suppression (default: 0.20)")
    parser.add_argument("--output", "-o", type=Path, default=Path("REPORTS/ai_inference_output.jpg"), help="Output path for annotated image")
    parser.add_argument("--no-save", action="store_true", help="Do not save output image to disk")
    parser.add_argument("--json", action="store_true", help="Output machine-readable JSON")

    args = parser.parse_args()
    run_inference(args.image, args.model, args.conf, args.output, not args.no_save, args.iou)


if __name__ == "__main__":
    main()
