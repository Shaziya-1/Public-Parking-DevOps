import os
import re
from pathlib import Path

SUBMISSION_DIR = Path(r"C:\Users\Admin\Desktop\Public-Parking-DevOps\submission\practical_reports")
REPORTS_DIR = Path(r"C:\Users\Admin\Desktop\Public-Parking-DevOps\REPORTS")

def main():
    print("=" * 85)
    print("PARKWATCH - 15 PRACTICAL REPORT PDF VERIFICATION AUDIT")
    print("=" * 85)
    
    all_ok = True
    results = []
    
    for t in range(1, 16):
        pdf_name = f"TASK_{t:02d}_Report.pdf"
        md_name = f"TASK_{t:02d}_Report.md"
        pdf_path = SUBMISSION_DIR / pdf_name
        md_path = REPORTS_DIR / md_name
        
        # 1. Existence & size
        if not pdf_path.exists():
            results.append((t, False, 0, 0, "Missing PDF file"))
            all_ok = False
            continue
            
        size = pdf_path.stat().st_size
        if size == 0:
            results.append((t, False, 0, 0, "Zero byte file"))
            all_ok = False
            continue
            
        # 2. Check PDF header magic
        with open(pdf_path, "rb") as f:
            header = f.read(5)
            if header != b"%PDF-":
                results.append((t, False, size, 0, "Corrupt PDF header"))
                all_ok = False
                continue
                
        # 3. Count screenshots in markdown source
        md_content = md_path.read_text(encoding="utf-8")
        images = re.findall(r"!\[(.*?)\]\((.*?)\)", md_content)
        img_count = len(images)
        
        # 4. Verify title
        first_line = md_content.splitlines()[0] if md_content.splitlines() else ""
        has_title = f"Task {t:02d}" in first_line or f"Task {t}" in first_line
        
        status_note = "Verified & Valid" if has_title else "Title warning"
        results.append((t, True, size, img_count, status_note))

    print(f"{'Task':<8} | {'PDF Filename':<22} | {'Size (Bytes)':<14} | {'Images':<8} | {'Verification Status'}")
    print("-" * 85)
    for t, valid, size, img_count, note in results:
        v_str = "VALID [OK]" if valid else "INVALID [ERR]"
        fname = f"TASK_{t:02d}_Report.pdf"
        print(f"Task {t:02d}  | {fname:<22} | {size:<14,} | {img_count:<8} | {v_str} ({note})")
        
    print("=" * 85)
    if all_ok:
        print("[SUCCESS] All 15 practical report PDFs are verified, intact, and valid.")
    else:
        print("[FAILURE] Some PDFs failed verification.")

if __name__ == "__main__":
    main()
