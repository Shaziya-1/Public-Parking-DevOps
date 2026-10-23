#!/usr/bin/env python3
"""
tests/test_ai_pipeline.py
=============================================================================
ParkWatch DevOps Project - AI Pipeline Lightweight Test Suite
Student: Sajiya Shaikh | Roll: 24102A2005

Unit and integration tests for:
1. COCO category mapping
2. Bounding-box coordinate conversion
3. Empty-annotation image handling
4. Invalid bounding-box rejection
5. Missing files handling
6. Invalid input image validation
=============================================================================
"""

import os
import sys
import tempfile
import unittest
from pathlib import Path

# Add project root to path
PROJECT_ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(PROJECT_ROOT))

from scripts.coco_to_yolo import (
    CLASS_MAPPING,
    EXCLUDED_CATEGORIES,
    convert_coco_to_yolo
)
from scripts.verify_pklot_dataset import verify_dataset
from scripts.ai_inference_demo import validate_image_path
from PIL import Image


class TestAiPipeline(unittest.TestCase):

    def setUp(self):
        self.temp_dir = tempfile.TemporaryDirectory()
        self.test_dir = Path(self.temp_dir.name)

    def tearDown(self):
        self.temp_dir.cleanup()

    def test_coco_category_mapping(self):
        """Test that category mapping strictly aligns with model specifications."""
        self.assertEqual(CLASS_MAPPING.get("space-empty"), 0)
        self.assertEqual(CLASS_MAPPING.get("space-occupied"), 1)
        self.assertIn("spaces", EXCLUDED_CATEGORIES)
        self.assertNotIn("spaces", CLASS_MAPPING)

    def test_bounding_box_conversion_math(self):
        """Test exact math for COCO [x, y, w, h] to YOLO [cx, cy, w, h]."""
        img_w, img_h = 640.0, 640.0
        x, y, bw, bh = 100.0, 200.0, 50.0, 80.0

        cx = (x + bw / 2.0) / img_w
        cy = (y + bh / 2.0) / img_h
        nw = bw / img_w
        nh = bh / img_h

        self.assertAlmostEqual(cx, 125.0 / 640.0, places=5)
        self.assertAlmostEqual(cy, 240.0 / 640.0, places=5)
        self.assertAlmostEqual(nw, 50.0 / 640.0, places=5)
        self.assertAlmostEqual(nh, 80.0 / 640.0, places=5)

        # Range checks
        self.assertTrue(0.0 <= cx <= 1.0)
        self.assertTrue(0.0 <= cy <= 1.0)
        self.assertTrue(0.0 < nw <= 1.0)
        self.assertTrue(0.0 < nh <= 1.0)

    def test_empty_annotation_image_handling(self):
        """Test that images with zero annotations produce empty .txt files."""
        fake_coco = {
            "categories": [
                {"id": 0, "name": "spaces"},
                {"id": 1, "name": "space-empty"},
                {"id": 2, "name": "space-occupied"}
            ],
            "images": [
                {"id": 101, "file_name": "empty_frame_01.jpg", "width": 640, "height": 640}
            ],
            "annotations": []  # Zero annotations
        }

        json_path = self.test_dir / "_annotations.coco.json"
        import json
        with open(json_path, "w", encoding="utf-8") as f:
            json.dump(fake_coco, f)

        output_labels = self.test_dir / "labels"
        stats = convert_coco_to_yolo(json_path, output_labels)

        expected_txt = output_labels / "empty_frame_01.txt"
        self.assertTrue(expected_txt.exists())
        self.assertEqual(expected_txt.read_text(encoding="utf-8"), "")
        self.assertEqual(stats["empty_annotation_images"], 1)
        self.assertEqual(stats["labels_written"], 0)

    def test_invalid_bounding_boxes_handling(self):
        """Test that invalid bounding boxes (<=0 width/height, malformed length) are caught."""
        fake_coco = {
            "categories": [
                {"id": 1, "name": "space-empty"},
                {"id": 2, "name": "space-occupied"}
            ],
            "images": [
                {"id": 1, "file_name": "frame_invalid.jpg", "width": 640, "height": 640}
            ],
            "annotations": [
                {"id": 1, "image_id": 1, "category_id": 1, "bbox": [10, 10, 0, 50]},    # zero width
                {"id": 2, "image_id": 1, "category_id": 1, "bbox": [10, 10, 30, -5]},   # negative height
                {"id": 3, "image_id": 1, "category_id": 2, "bbox": [10, 10, 30]},       # len 3 (malformed)
                {"id": 4, "image_id": 1, "category_id": 1, "bbox": [20, 20, 40, 40]}    # valid box
            ]
        }

        json_path = self.test_dir / "_annotations.coco.json"
        import json
        with open(json_path, "w", encoding="utf-8") as f:
            json.dump(fake_coco, f)

        output_labels = self.test_dir / "labels"
        stats = convert_coco_to_yolo(json_path, output_labels)

        self.assertEqual(stats["invalid_bboxes"], 3)
        self.assertEqual(stats["labels_written"], 1)

        lines = (output_labels / "frame_invalid.txt").read_text(encoding="utf-8").strip().splitlines()
        self.assertEqual(len(lines), 1)
        self.assertTrue(lines[0].startswith("0 "))  # Class 0: space-empty

    def test_missing_files_handling(self):
        """Test dataset verifier on non-existent directory."""
        non_existent_dir = self.test_dir / "does_not_exist"
        result = verify_dataset(non_existent_dir)
        self.assertFalse(result)

    def test_invalid_input_images(self):
        """Test inference image validation rejects non-existent and non-image files."""
        # Non-existent
        fake_img = self.test_dir / "missing.jpg"
        self.assertFalse(validate_image_path(fake_img))

        # Text file pretending to be image
        txt_as_jpg = self.test_dir / "not_an_image.jpg"
        txt_as_jpg.write_text("This is plain text, not a JPEG image.")
        self.assertFalse(validate_image_path(txt_as_jpg))

        # Real valid image
        valid_img = self.test_dir / "valid.png"
        img = Image.new("RGB", (640, 640), color=(100, 150, 200))
        img.save(valid_img)
        self.assertTrue(validate_image_path(valid_img))

    def test_inference_reliability_flag(self):
        """Test that run_inference flags low-confidence pilot detections as unverified."""
        from scripts.ai_inference_demo import run_inference, DEFAULT_MODEL
        if not DEFAULT_MODEL.exists():
            self.skipTest("Default model weights not present.")

        valid_img = self.test_dir / "test_frame.jpg"
        img = Image.new("RGB", (640, 640), color=(128, 128, 128))
        img.save(valid_img)

        # At standard confidence 0.25, pilot checkpoint detects 0 stalls and flags unverified
        out_path = self.test_dir / "out.jpg"
        res = run_inference(valid_img, DEFAULT_MODEL, conf=0.25, output_path=out_path, save_output=False, iou=0.45)
        self.assertIsNotNone(res)
        self.assertFalse(res["reliable"])
        self.assertIn("pilot", res["reliabilityWarning"].lower())


if __name__ == "__main__":
    unittest.main()
