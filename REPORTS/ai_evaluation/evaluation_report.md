# AI Parking Stall Model Evaluation Report

- **Date:** 2026-10-17 19:04:18 UTC
- **Evaluation Type:** `SMOKE_TEST (Fast Verification)`
- **Evaluated Checkpoint:** `best.pt`
- **Test Dataset:** PKLot v2 640 Held-Out Test Split (1,242 images)

## Overall Performance Metrics

| Metric | Score | Percentage |
| :--- | :---: | :---: |
| **mAP @ 0.50** | `0.0063` | **0.63%** |
| **mAP @ 0.50:0.95** | `0.0021` | **0.21%** |
| **Mean Precision** | `0.0087` | **0.87%** |
| **Mean Recall** | `0.0500` | **5.00%** |

## Class-Level Metrics

| Class ID | Category Name | mAP @ 0.50:0.95 |
| :---: | :--- | :---: |
| `0` | `space-empty` | `0.0005` |
| `1` | `space-occupied` | `0.0038` |

## Visual Artifacts
- **Confusion Matrix:** `ai_evaluation/confusion_matrix.png`
- **Machine-Readable JSON:** `ai_evaluation/evaluation_summary.json`
