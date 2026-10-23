#!/usr/bin/env python3
"""
scripts/verify_pklot_dataset.py
=============================================================================
ParkWatch DevOps Project - PKLot Dataset Verification Script
Student: Sajiya Shaikh | Roll: 24102A2005

Verifies the integrity, split counts, COCO annotation structures,
and bounding-box validity of the PKLot dataset.
=============================================================================
"""

import os
import sys
import json
import argparse
from collections import Counter
from pathlib import Path


def verify_dataset(dataset_dir: Path) -> bool:
    print("=" * 70)
    print("PARKWATCH - PKLOT DATASET INTEGRITY VERIFICATION")
    print(f"Target Directory: {dataset_dir.resolve()}")
    print("=" * 70)

    if not dataset_dir.exists():
        print(f"[FATAL] Dataset directory does not exist: {dataset_dir}")
        return False

    splits = ["train", "valid", "test"]
    overall_valid = True
    split_summaries = {}

    for split in splits:
        split_path = dataset_dir / split
        print(f"\n[INFO] Inspecting split: {split} ...")

        if not split_path.is_dir():
            print(f"  [ERROR] Missing split directory: {split_path}")
            overall_valid = False
            continue

        json_file = split_path / "_annotations.coco.json"
        if not json_file.exists():
            print(f"  [ERROR] Missing COCO annotation file: {json_file}")
            overall_valid = False
            continue

        # Check disk images
        disk_images = {f.name for f in split_path.iterdir() if f.is_file() and f.suffix.lower() in ('.jpg', '.jpeg', '.png')}
        print(f"  Images detected on disk: {len(disk_images):,}")

        # Parse COCO JSON
        try:
            with open(json_file, 'r', encoding='utf-8') as f:
                coco_data = json.load(f)
        except Exception as e:
            print(f"  [ERROR] Failed to parse JSON {json_file}: {e}")
            overall_valid = False
            continue

        # Check top-level keys
        for key in ["images", "annotations", "categories"]:
            if key not in coco_data:
                print(f"  [ERROR] Missing '{key}' in COCO structure.")
                overall_valid = False

        categories = {c["id"]: c["name"] for c in coco_data.get("categories", [])}
        print(f"  Categories detected ({len(categories)}): {categories}")

        # Verify expected categories
        cat_names = set(categories.values())
        if not {"space-empty", "space-occupied"}.issubset(cat_names):
            print(f"  [ERROR] Expected categories 'space-empty' and 'space-occupied' not found: {cat_names}")
            overall_valid = False

        images = coco_data.get("images", [])
        annotations = coco_data.get("annotations", [])

        # Image validation
        image_ids = set()
        image_filenames = set()
        dim_counter = Counter()
        dup_img_ids = 0
        dup_img_files = 0

        for img in images:
            img_id = img.get("id")
            fname = img.get("file_name")
            w = img.get("width")
            h = img.get("height")

            if img_id in image_ids:
                dup_img_ids += 1
            image_ids.add(img_id)

            if fname in image_filenames:
                dup_img_files += 1
            image_filenames.add(fname)

            dim_counter[(w, h)] += 1

        missing_on_disk = image_filenames - disk_images
        unannotated_on_disk = disk_images - image_filenames

        # Annotation validation
        ann_ids = set()
        dup_ann_ids = 0
        orphan_anns = 0
        invalid_bboxes = 0
        out_of_bounds_bboxes = 0
        category_counts = Counter()
        images_with_anns = set()

        img_id_to_dim = {img["id"]: (img.get("width", 640), img.get("height", 640)) for img in images}

        for ann in annotations:
            ann_id = ann.get("id")
            if ann_id in ann_ids:
                dup_ann_ids += 1
            ann_ids.add(ann_id)

            img_id = ann.get("image_id")
            if img_id not in img_id_to_dim:
                orphan_anns += 1
            else:
                images_with_anns.add(img_id)

            cat_id = ann.get("category_id")
            cat_name = categories.get(cat_id, f"Unknown_{cat_id}")
            category_counts[cat_name] += 1

            bbox = ann.get("bbox", [])
            if len(bbox) != 4:
                invalid_bboxes += 1
                continue

            x, y, bw, bh = bbox
            if bw <= 0 or bh <= 0:
                invalid_bboxes += 1

            if img_id in img_id_to_dim:
                iw, ih = img_id_to_dim[img_id]
                # Small tolerance of 5px for edge bounding boxes
                if x < -5 or y < -5 or (x + bw) > (iw + 5) or (y + bh) > (ih + 5):
                    out_of_bounds_bboxes += 1

        images_empty = len(image_ids - images_with_anns)

        # Split check results
        split_valid = (
            len(missing_on_disk) == 0 and
            len(unannotated_on_disk) == 0 and
            dup_img_ids == 0 and
            dup_img_files == 0 and
            dup_ann_ids == 0 and
            orphan_anns == 0 and
            invalid_bboxes == 0 and
            out_of_bounds_bboxes == 0
        )

        if not split_valid:
            overall_valid = False
            print(f"  [FAIL] Split {split} failed validation criteria!")
            if missing_on_disk:
                print(f"    Missing on disk: {len(missing_on_disk)}")
            if unannotated_on_disk:
                print(f"    Unannotated on disk: {len(unannotated_on_disk)}")
            if dup_img_ids or dup_img_files:
                print(f"    Duplicate image records: IDs={dup_img_ids}, Files={dup_img_files}")
            if orphan_anns or invalid_bboxes or out_of_bounds_bboxes:
                print(f"    Annotation errors: Orphan={orphan_anns}, Invalid={invalid_bboxes}, OutOfBounds={out_of_bounds_bboxes}")
        else:
            print(f"  [PASS] Split {split} passed all integrity checks.")

        split_summaries[split] = {
            "images_on_disk": len(disk_images),
            "images_in_json": len(images),
            "dimensions": dict(dim_counter),
            "total_annotations": len(annotations),
            "category_counts": dict(category_counts),
            "images_with_no_annotations": images_empty,
            "status": "VALID" if split_valid else "INVALID"
        }

    # Print final summary table
    print("\n" + "=" * 70)
    print("DATASET VERIFICATION SUMMARY REPORT")
    print("=" * 70)
    print(f"{'Split':<10} | {'Images':<8} | {'Annotations':<12} | {'Empty Stalls':<13} | {'Occupied Stalls':<15} | {'Status'}")
    print("-" * 70)

    total_images = 0
    total_annotations = 0
    total_empty = 0
    total_occupied = 0

    for split, data in split_summaries.items():
        imgs = data["images_in_json"]
        anns = data["total_annotations"]
        empty = data["category_counts"].get("space-empty", 0)
        occ = data["category_counts"].get("space-occupied", 0)

        total_images += imgs
        total_annotations += anns
        total_empty += empty
        total_occupied += occ

        print(f"{split:<10} | {imgs:<8,} | {anns:<12,} | {empty:<13,} | {occ:<15,} | {data['status']}")

    print("-" * 70)
    print(f"{'TOTAL':<10} | {total_images:<8,} | {total_annotations:<12,} | {total_empty:<13,} | {total_occupied:<15,} | {'ALL VALID' if overall_valid else 'FAILED'}")
    print("=" * 70)

    if overall_valid:
        print("[SUCCESS] PKLot dataset verification completed successfully with 0 errors.\n")
        return True
    else:
        print("[FAILURE] PKLot dataset verification detected errors.\n")
        return False


def main():
    parser = argparse.ArgumentParser(description="Verify PKLot dataset structure and annotations.")
    parser.add_argument(
        "--dataset-dir",
        type=Path,
        default=Path("datasets/pklot"),
        help="Path to PKLot root directory containing train, valid, and test folders."
    )
    args = parser.parse_args()

    success = verify_dataset(args.dataset_dir)
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
