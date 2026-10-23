# ParkWatch — AI Parking Stall Detection Model Specification

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh | **Roll Number:** 24102A2005  
**Component:** Computer Vision & AI Inference Module  

---

## 1. Dataset Source, Provenance & Attribution

The model is built and evaluated using the **PKLot** parking lot dataset exported in COCO object-detection format:
- **Title:** PKLot — v2 640
- **Originating Institution:** Universidade Federal do Paraná (UFPR), Curitiba, Brazil ([UFPR VRI Parking Database](https://web.inf.ufpr.br/vri/databases/parking-lot-database/))
- **Export Distribution:** Roboflow Public AI Repository (`https://public.roboflow.ai/object-detection/pklot`)
- **Export Date:** January 5, 2021 at 21:55 GMT
- **Licence:** **Creative Commons Attribution 4.0 International (CC BY 4.0)**
- **Citation:**
  ```bibtex
  @article{almeida2015pklot,
    title={PKLot -- A robust dataset for parking lot classification},
    author={Almeida, Paulo and Oliveira, Luiz S and Silva Jr, Edson and Britto Jr, Alceu and Koerich, Alessandro},
    journal={Expert Systems with Applications},
    volume={42},
    number={11},
    pages={4937--4949},
    year={2015},
    publisher={Elsevier}
  }
  ```

---

## 2. Dataset Structure & Empirical Split Statistics

All images are standardized RGB JPEG files at $640 \times 640$ pixels resolution.

| Split | Image Count | Split % | Total Annotations | Empty Stalls (`space-empty`) | Occupied Stalls (`space-occupied`) | Empty Bounding Box Files |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **`train`** | 8,691 | 70.0% | 497,856 | 265,908 (53.4%) | 231,948 (46.6%) | 189 |
| **`valid`** | 2,483 | 20.0% | 143,316 | 73,629 (51.4%) | 69,687 (48.6%) | 59 |
| **`test`** | 1,242 | 10.0% | 70,684 | 36,584 (51.8%) | 34,100 (48.2%) | 26 |
| **Total** | **12,416** | **100%** | **711,856** | **376,121 (52.8%)** | **335,735 (47.2%)** | **274** |

---

## 3. Label Mapping & Conversion

### Category Mapping
The raw COCO JSON defines three categories:
- `id: 0`, `name: "spaces"`, `supercategory: "none"` &mdash; Parent container, **excluded** from detection targets.
- `id: 1`, `name: "space-empty"`, `supercategory: "spaces"` &mdash; **Mapped to Model Class `0` (`space-empty`)**.
- `id: 2`, `name: "space-occupied"`, `supercategory: "spaces"` &mdash; **Mapped to Model Class `1` (`space-occupied`)**.

### Coordinate Normalization Formula
From COCO bounding box $[x, y, w, h]$ to YOLO $[c_x, c_y, n_w, n_h]$:
$$c_x = \frac{x + \frac{w}{2}}{W_{img}}, \quad c_y = \frac{y + \frac{h}{2}}{H_{img}}, \quad n_w = \frac{w}{W_{img}}, \quad n_h = \frac{h}{H_{img}}$$

Negative/empty images are preserved as 0-byte `.txt` label files to provide proper background negative samples.

---

## 4. Model Architecture & Rationale

- **Architecture:** **Ultralytics YOLOv8n (Nano)**
- **Parameters:** $\approx 3.2\text{M}$ parameters (weights file size: $\approx 6.5\text{ MB}$).
- **Input Resolution:** $640 \times 640 \times 3$ RGB.
- **Why YOLOv8n:**
  1. Dense Object Detection: Average surveillance frame contains $\approx 57$ small stall bounding boxes. Anchor-free detection heads handle dense overlapping stalls effectively.
  2. CPU Latency: On host CPU (Intel 12-thread), YOLOv8n forward pass is $\approx 40\text{--}60\text{ ms}$ per image, compared to $>1,500\text{ ms}$ for two-stage models like Faster R-CNN.
  3. Edge Deployment: Small memory footprint enables deployment alongside the Spring Boot application container without starving JVM memory.

---

## 5. Environment & Hardware Requirements

- **Python Runtime:** Python 3.11.9 (`C:\Program Files\Python311\python.exe`).
- **Core Libraries:**
  - `torch` 2.11.0 (CPU execution verified)
  - `torchvision` 0.26.0
  - `ultralytics` 8.4.33
  - `opencv-python` 4.13.0.92
  - `pillow` 12.1.0
  - `pyyaml` 6.0.3
- **Hardware Acceleration:** CPU execution (`torch.cuda.is_available() == False`).

---

## 6. Execution Commands

### A. Dataset Verification
Validates dataset directory integrity and COCO annotation schemas:
```bash
python scripts/verify_pklot_dataset.py
```

### B. COCO to YOLO Conversion
Converts annotations and establishes Windows directory junctions:
```bash
python scripts/coco_to_yolo.py
```

### C. Model Training & Resource Estimation
1. Estimate resource usage and training time without running:
   ```bash
   python scripts/train_ai_model.py --estimate-only
   ```
2. Safe Pilot Run (1 epoch on subsampled pilot set, CPU-friendly):
   ```bash
   python scripts/train_ai_model.py --pilot --epochs 1 --execute
   ```
3. Full Dataset Fine-Tuning (Requires dedicated compute):
   ```bash
   python scripts/train_ai_model.py --epochs 5 --execute
   ```

### D. Model Evaluation on Held-Out Test Split
1. Smoke Test Evaluation ($\approx 25$ test images):
   ```bash
   python scripts/evaluate_ai_model.py --smoke-test
   ```
2. Full Benchmark Evaluation ($1,242$ test images):
   ```bash
   python scripts/evaluate_ai_model.py
   ```

### E. Single Image Camera Inference
```bash
python scripts/ai_inference_demo.py --image datasets/pklot/test/2012-11-06_13_43_31_jpg.rf.00207754dbce0d625d24ca31898fcfd7.jpg
```

---

## 7. Generated Artifact Locations

All generated AI model files and datasets are isolated and excluded from Git via `.gitignore`:

| Artifact | Location | Git Status |
| :--- | :--- | :--- |
| **YOLO Converted Labels** | `datasets/pklot_yolo/labels/` | Ignored (`datasets/`) |
| **Dataset Configuration** | `datasets/pklot_yolo/data.yaml` | Ignored (`datasets/`) |
| **Model Weights** | `models/parkwatch_yolov8n/weights/best.pt` | Ignored (`models/`, `*.pt`) |
| **Training Metadata** | `models/parkwatch_yolov8n/training_metadata.json` | Ignored (`models/`) |
| **Evaluation Metrics** | `REPORTS/ai_evaluation/evaluation_summary.json` | Tracked in reports |
| **Confusion Matrix Plot** | `REPORTS/ai_evaluation/confusion_matrix.png` | Tracked in reports |
| **Annotated Demo Output** | `REPORTS/ai_inference_output.jpg` | Tracked in reports |

---

## 8. Data Separation & System Safety

1. **Strict Decoupling:** AI model predictions evaluate parking spaces from Brazilian surveillance camera feeds. They do **not** reflect or alter live occupancy at the 24 Mumbai municipal lots.
2. **No Automated Mutation:** The inference script does **not** call the REST API `/api/v1/events` or `/api/v1/locations/{id}/occupancy`.
3. **Database Protection:** The H2 database (`data/parkingdb.mv.db`) remains completely untouched during all training and inference operations.

---

## 9. Known Limitations

1. **Angle Sensitivity:** Camera feeds from extreme low-angle perspectives experience stall occlusion behind large vehicles (SUVs, trucks).
2. **Lighting Variations:** Extreme night-time surveillance without auxiliary floodlighting reduces detection confidence for vacant stall markings.
3. **CPU Compute Constraint:** Training on all 497,856 bounding boxes on CPU takes $\approx 20\text{--}25$ minutes per epoch; fine-tuning or transfer learning with small pilot batches is recommended for local development.
