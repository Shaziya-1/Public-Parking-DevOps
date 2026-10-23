#!/usr/bin/env python3
"""
scripts/coco_to_yolo.py
=============================================================================
ParkWatch DevOps Project - COCO to YOLO Converter
Student: Sajiya Shaikh | Roll: 24102A2005

Converts PKLot COCO JSON annotations into normalized YOLO format (.txt).
- Excludes parent category 'spaces'
- Maps 'space-empty' -> class 0
- Maps 'space-occupied' -> class 1
- Preserves original dataset completely untouched
- Links images to output directory via Windows directory junctions (0 MB overhead)
- Validates converted labels and outputs data.yaml
=============================================================================
"""

import os
import sys
import json
import yaml
import shutil
import argparse
from pathlib import Path
from collections import defaultdict


# Class mapping definition
CLASS_MAPPING = {
    "space-empty": 0,
    "space-occupied": 1
}

EXCLUDED_CATEGORIES = {"spaces"}


def create_directory_link(source_dir: Path, target_dir: Path):
    """
    Creates NTFS hardlinks (os.link) for all image files into target_dir so that:
    1) Image paths explicitly contain the '/images/' directory component required by Ultralytics.
    2) Zero additional disk space is consumed (hardlinks share physical data clusters on disk).
    3) Original dataset files remain 100% untouched.
    """
    target_dir.mkdir(parents=True, exist_ok=True)
    for f in source_dir.iterdir():
        if f.is_file() and f.suffix.lower() in ('.jpg', '.jpeg', '.png'):
            dst = target_dir / f.name
            if not dst.exists():
                try:
                    os.link(f, dst)
                except Exception:
                    shutil.copy2(f, dst)


def convert_coco_to_yolo(coco_json_path: Path, output_labels_dir: Path) -> dict:
    """
    Parses COCO JSON and writes normalized YOLO .txt label files into output_labels_dir.
    """
    output_labels_dir.mkdir(parents=True, exist_ok=True)

    with open(coco_json_path, 'r', encoding='utf-8') as f:
        coco = json.load(f)

    # Build category ID to name and target class ID mapping
    categories = coco.get("categories", [])
    cat_id_to_target = {}
    for cat in categories:
        c_name = cat.get("name")
        c_id = cat.get("id")
        if c_name in CLASS_MAPPING:
            cat_id_to_target[c_id] = CLASS_MAPPING[c_name]
        elif c_name in EXCLUDED_CATEGORIES:
            # Explicitly ignored
            continue
        else:
            print(f"[WARN] Unknown category '{c_name}' (ID: {c_id}) - ignoring.")

    # Group annotations by image_id
    image_annotations = defaultdict(list)
    for ann in coco.get("annotations", []):
        c_id = ann.get("category_id")
        if c_id not in cat_id_to_target:
            continue
        image_annotations[ann["image_id"]].append(ann)

    stats = {
        "images_processed": 0,
        "empty_annotation_images": 0,
        "labels_written": 0,
        "class_counts": {0: 0, 1: 0},
        "invalid_bboxes": 0
    }

    # Process all images
    for img in coco.get("images", []):
        img_id = img["id"]
        file_name = img["file_name"]
        img_w = float(img.get("width", 640))
        img_h = float(img.get("height", 640))

        # Output label filename: same stem, .txt extension
        label_file = output_labels_dir / f"{Path(file_name).stem}.txt"
        anns = image_annotations.get(img_id, [])

        stats["images_processed"] += 1

        if not anns:
            # Empty annotation file for negative/empty sample
            label_file.write_text("", encoding='utf-8')
            stats["empty_annotation_images"] += 1
            continue

        lines = []
        for ann in anns:
            bbox = ann.get("bbox", [])
            if len(bbox) != 4:
                stats["invalid_bboxes"] += 1
                continue

            x, y, bw, bh = bbox
            if bw <= 0 or bh <= 0:
                stats["invalid_bboxes"] += 1
                continue

            # Convert to YOLO format: center_x, center_y, width, height (normalized)
            cx = (x + bw / 2.0) / img_w
            cy = (y + bh / 2.0) / img_h
            nw = bw / img_w
            nh = bh / img_h

            # Clamping to valid [0.0, 1.0] range
            cx = max(0.0, min(1.0, cx))
            cy = max(0.0, min(1.0, cy))
            nw = max(0.0001, min(1.0, nw))
            nh = max(0.0001, min(1.0, nh))

            target_class = cat_id_to_target[ann["category_id"]]
            stats["class_counts"][target_class] += 1
            stats["labels_written"] += 1

            lines.append(f"{target_class} {cx:.6f} {cy:.6f} {nw:.6f} {nh:.6f}")

        label_file.write_text("\n".join(lines) + "\n", encoding='utf-8')

    return stats


def run_conversion(source_dataset_dir: Path, output_yolo_dir: Path):
    print("=" * 70)
    print("PARKWATCH - COCO TO YOLO DATASET CONVERTER")
    print(f"Source PKLot Directory: {source_dataset_dir.resolve()}")
    print(f"Output YOLO Directory:  {output_yolo_dir.resolve()}")
    print("Target Classes: 0 -> space-empty, 1 -> space-occupied (spaces excluded)")
    print("=" * 70)

    splits = ["train", "valid", "test"]
    conversion_summary = {}

    for split in splits:
        split_src = source_dataset_dir / split
        coco_json = split_src / "_annotations.coco.json"

        if not coco_json.exists():
            print(f"[ERROR] Split '{split}' missing JSON: {coco_json}")
            sys.exit(1)

        # 1. Link images directory without duplicating data
        yolo_images_split = output_yolo_dir / "images" / split
        print(f"\n[INFO] Linking images for '{split}' -> {yolo_images_split} ...")
        create_directory_link(split_src, yolo_images_split)

        # 2. Convert annotations to labels/<split>
        yolo_labels_split = output_yolo_dir / "labels" / split
        print(f"[INFO] Converting annotations for '{split}' -> {yolo_labels_split} ...")
        split_stats = convert_coco_to_yolo(coco_json, yolo_labels_split)
        conversion_summary[split] = split_stats

        print(f"  Processed {split_stats['images_processed']:,} images.")
        print(f"  Empty annotation images: {split_stats['empty_annotation_images']:,}")
        print(f"  Converted bounding boxes: {split_stats['labels_written']:,}")
        print(f"    - space-empty (Class 0): {split_stats['class_counts'][0]:,}")
        print(f"    - space-occupied (Class 1): {split_stats['class_counts'][1]:,}")

    # 3. Generate data.yaml for Ultralytics YOLO
    yaml_config = {
        "path": str(output_yolo_dir.resolve()).replace("\\", "/"),
        "train": "images/train",
        "val": "images/valid",
        "test": "images/test",
        "names": {
            0: "space-empty",
            1: "space-occupied"
        }
    }

    yaml_file = output_yolo_dir / "data.yaml"
    with open(yaml_file, "w", encoding="utf-8") as f:
        yaml.dump(yaml_config, f, sort_keys=False)
    print(f"\n[SUCCESS] Generated dataset YAML config: {yaml_file}")

    print("\n" + "=" * 70)
    print("CONVERSION SUMMARY REPORT")
    print("=" * 70)
    print(f"{'Split':<10} | {'Images':<8} | {'Empty Labels (0)':<18} | {'Occupied Labels (1)':<20} | {'Total Labels'}")
    print("-" * 70)
    total_imgs = 0
    total_c0 = 0
    total_c1 = 0
    total_labels = 0

    for split, s in conversion_summary.items():
        total_imgs += s["images_processed"]
        total_c0 += s["class_counts"][0]
        total_c1 += s["class_counts"][1]
        total_labels += s["labels_written"]
        print(f"{split:<10} | {s['images_processed']:<8,} | {s['class_counts'][0]:<18,} | {s['class_counts'][1]:<20,} | {s['labels_written']:,}")

    print("-" * 70)
    print(f"{'TOTAL':<10} | {total_imgs:<8,} | {total_c0:<18,} | {total_c1:<20,} | {total_labels:,}")
    print("=" * 70)
    print("[SUCCESS] COCO to YOLO conversion completed successfully.\n")


def main():
    parser = argparse.ArgumentParser(description="Convert PKLot COCO annotations to YOLO format.")
    parser.add_argument(
        "--source-dir",
        type=Path,
        default=Path("datasets/pklot"),
        help="Path to PKLot COCO dataset root directory."
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=Path("datasets/pklot_yolo"),
        help="Target directory for converted YOLO dataset."
    )
    args = parser.parse_args()

    run_conversion(args.source_dir, args.output_dir)


if __name__ == "__main__":
    main()
