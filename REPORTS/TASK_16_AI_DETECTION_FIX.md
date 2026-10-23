# PARKWATCH — AI PARKING OCCUPANCY DETECTION REPAIR & VERIFICATION REPORT
**Task:** End-to-End AI Parking Detection Pipeline Repair, Training, and Verification  
**Author:** Antigravity AI Engineering Suite  
**Date:** October 18, 2026  
**Project:** ParkWatch Municipal Parking DevOps (`C:\Users\Admin\Desktop\Public-Parking-DevOps`)

---

## 1. Executive Summary & Root Cause Analysis

### 1.1 The Problems Encountered
1. **0 Stalls Detected at Standard Confidence (`conf=0.25`):** On clear, visibly occupied parking lot surveillance frames, the AI Analysis page previously reported 0 parking stalls detected.
2. **Dense Wall of Overlapping Red/Green Bounding Boxes:** When confidence was artificially dropped to `0.005`, the system produced 135–172 overlapping and jittery bounding boxes with duplicate stall classifications and inconsistent occupancy percentages.
3. **Frontend/Backend Confidence Mismatch:** The confidence slider displayed `0.25` on the page while the result badge and image overlay displayed `0.03`.

### 1.2 Actual Root Cause
1. **Under-Trained Detection Head (Severe Sub-Sampling):**
   - The initial baseline pilot checkpoint (`models/parkwatch_yolov8n/weights/best.pt`) was trained for only **1 single epoch** on a tiny subsample of **174 images (~2% of the PKLot dataset)**.
   - For a complex multi-object regression task (identifying dozens of small 24x46 pixel parking stalls under varying weather and camera perspectives), 1 epoch was insufficient for bounding-box coordinate regression and classification heads to converge.
   - Consequently, the maximum detection confidence across the entire dataset rarely exceeded `0.007`. At standard confidence `0.25`, the model produced exactly 0 detections.
2. **Artificial Threshold Lowering & High NMS IoU:**
   - In previous iterations, the confidence threshold was lowered to `0.005` to force the model to output *something*.
   - Because the detection head had uncalibrated feature maps, lowering the threshold emitted raw background anchor candidates.
   - Combined with default Non-Maximum Suppression (IoU = 0.70), redundant overlapping anchor boxes across identical parking spaces were drawn, producing the chaotic "box wall".
3. **Thymeleaf Controller Attribute Desynchronization:**
   - In `AiAnalysisController.java`, the HTTP POST handler `analyzeUploadedImage()` received `@RequestParam("conf") Double conf` and passed it to the Python inference script.
   - However, the controller re-populated the Thymeleaf view attributes by calling `populateModelAttributes(model)`, which statically attached `defaultConf` (0.25) to the model.
   - The user's submitted `conf` was never reflected back to the form input. Consequently, the input slider snapped back to displaying `0.25` while the result panel showed `0.03`.

---

## 2. Dataset and Label Verification

### 2.1 PKLot Dataset Structure & Schema
- **Path:** `datasets/pklot_yolo/`
- **Class Mapping:**
  - Class `0`: `space-empty` (Vacant parking stall)
  - Class `1`: `space-occupied` (Stall occupied by a vehicle)
  - Excluded category: `spaces` (COCO super-category excluded during conversion)
- **Label Units:** Normalized YOLO format `[class_id, x_center, y_center, width, height]`.
- **Label Validation:**
  - Bounding boxes represent **individual parking space boundaries** (not arbitrary vehicle contours).
  - All coordinates normalized strictly in `[0.0, 1.0]`.
  - Empty stalls (empty ground markings) are labeled `0`, stalls containing vehicles are labeled `1`.
  - Zero label corruption or out-of-bounds coordinates detected across train/val/test splits.

---

## 3. Training & Evaluation Results

### 3.1 Checkpoint Integrity & Safety
- **Original Checkpoints Preserved:**
  - Backed up to: `models/backup_original_checkpoints/best_pilot_epoch1.pt` (6.24 MB)
  - Original directory: `models/parkwatch_yolov8n/weights/best.pt` (6.24 MB)
  - Neither checkpoint was overwritten or modified.

### 3.2 Improved Fine-Tuned Model
- **Script:** `scripts/train_improved_model.py`
- **Training Configuration:**
  - Base architecture: YOLOv8n (Nano, 3.01M parameters)
  - Training set: 300 stratified PKLot images spanning all cameras (PUCPR, UFPR04, UFPR05) and conditions (Sunny, Rainy, Cloudy)
  - Validation set: 50 stratified PKLot images
  - Epochs: 6
  - Batch size: 16
  - Image size: 416x416
  - Device: Intel Core i5-12450H CPU
- **Resulting Checkpoint:**
  - `models/parkwatch_yolov8n_improved/weights/best.pt` (6.21 MB)

### 3.3 Final Held-Out Test Split Metrics
Evaluated on **1,242 held-out PKLot test images** comprising **70,684 ground truth stall instances** (`scripts/evaluate_on_test_split.py`):

| Evaluation Metric | Original Pilot Model (1 Epoch) | Improved Fine-Tuned Model (6 Epochs) | Improvement / Delta |
| :--- | :---: | :---: | :---: |
| **Precision (P)** | `0.0638` (6.38%) | **`0.7995` (79.95%)** | **+73.57%** |
| **Recall (R)** | `0.3738` (37.38%) | **`0.8328` (83.28%)** | **+45.90%** |
| **mAP @ 0.50** | `0.0568` (5.68%) | **`0.8612` (86.12%)** | **+80.44%** |
| **mAP @ 0.50:0.95** | `0.0200` (2.00%) | **`0.4854` (48.54%)** | **+46.54%** |
| **Evaluation Set** | 1,242 images / 70,684 stalls | 1,242 images / 70,684 stalls | Held-out test split |

---

## 4. Demonstration & Visual Verification

### 4.1 Demonstration Image (`REPORTS/demo_parking_lot.jpg`)
Evaluated at standard confidence `conf=0.25`:
- **Original Pilot Model:**
  - Total Stalls Detected: **0**
  - Average Confidence: `0.000`
  - Output: Empty image with "0 Stalls Detected" header.
- **Improved Fine-Tuned Model:**
  - Total Stalls Detected: **91**
  - Empty Stalls: **88** (96.7%)
  - Occupied Stalls: **3** (3.3%)
  - Estimated Occupancy: **3.3%**
  - Average Confidence: **0.553** (High confidence detection)
  - Visual output: Sharp, tightly fitted green/red bounding boxes aligned directly with marked parking bays.

### 4.2 Held-Out PKLot Samples Visual Verification
- **Sample 1 (`PUCPR_2012-09-12_10-12-04_14`):**
  - Before: 0 stalls
  - After: **82 stalls detected** (15 empty, 67 occupied, 81.7% occupancy, avg conf: 0.542)
- **Sample 2 (`PUCPR_2012-10-24_15-46-30_16`):**
  - Before: 0 stalls
  - After: **90 stalls detected** (11 empty, 79 occupied, 87.8% occupancy, avg conf: 0.576)
- **Sample 3 (`UFPR04_2013-01-16_10-40-02_17`):**
  - Before: 0 stalls
  - After: **109 stalls detected** (6 empty, 103 occupied, 94.5% occupancy, avg conf: 0.597)

Side-by-side comparison artifacts generated at:
- `REPORTS/before_after/demo_frame_comparison.jpg`
- `REPORTS/before_after/pklot_sample1_comparison.jpg`
- `REPORTS/before_after/pklot_sample2_comparison.jpg`
- `REPORTS/before_after/pklot_sample3_comparison.jpg`

---

## 5. Confidence Slider & Pipeline Synchronization

1. **Active Threshold Control:** The confidence slider directly controls the `--conf <float>` argument supplied to the YOLO inference engine.
2. **Display Synchronization:**
   - In `AiAnalysisController.java`, the active confidence is passed to the view as `selectedConf`.
   - In `ai-analysis.html`, `<input type="range">` and the numerical badge display are bound to `${selectedConf != null ? selectedConf : defaultConf}`.
   - The result panel displays `Confidence: <selectedConf>` agreeing with the slider and the header overlay on the generated image (`Conf>=<selectedConf>`).
3. **Honest Zero-Detection Handling:**
   - When no stalls meet the requested confidence threshold (`totalDetected == 0`), the system presents an explicit alert:
     > *"No Reliable Detections Found at Confidence X.XX — The model did not detect parking stalls above the selected threshold. This does not mean the parking lot has no stalls..."*
   - Occupancy is displayed as `N/A (0 Stalls Detected)` instead of presenting a false `0.0%` occupancy calculation.

---

## 6. Regression & Safety Compliance

- **Jenkins on Port 8080:** Untouched and running (Process PID 7052).
- **H2 Database (`./data/parkingdb`):** 100% intact, no tables dropped, no data deleted.
- **Git Repository:** Zero commits or pushes executed.
- **Python Unit Tests:** **7/7 PASSED** (`python -m unittest discover tests`).
- **Maven Test Suite:** **45/45 PASSED**, BUILD SUCCESS (`mvn test`).
- **Spring Boot Packaging:** Repackaged clean artifact `target/public-parking-dashboard-1.0.0.jar`.

---

## 7. Known Limitations
1. **Camera Perspective Sensitivity:** PKLot models are trained on fixed-angle surveillance cameras (elevated 30°–60° angles). Ultra-low ground-level photos or fisheye lenses will show lower recall unless fine-tuned on diverse camera angles.
2. **Extreme Weather / Severe Glare:** While sunny, rainy, and cloudy conditions are included, heavy snow cover obscuring ground paint markings reduces empty stall detection accuracy.
