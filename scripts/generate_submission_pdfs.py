import os
import re
import sys
import subprocess
from pathlib import Path

WORKSPACE = Path(r"C:\Users\Admin\Desktop\Public-Parking-DevOps")
SUBMISSION_DIR = WORKSPACE / "submission"
EDGE_EXE = Path(r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe")

SUBMISSION_DIR.mkdir(parents=True, exist_ok=True)

CSS_REPORT_THEME = """
body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    line-height: 1.6;
    color: #24292e;
    max-width: 900px;
    margin: 0 auto;
    padding: 30px;
    font-size: 13pt;
}
@media print {
    body {
        max-width: 100%;
        padding: 0;
        font-size: 11pt;
    }
    @page {
        margin: 18mm 15mm 18mm 15mm;
        size: A4 portrait;
        @bottom-right {
            content: counter(page);
        }
    }
    h1, h2, h3 {
        page-break-after: avoid;
    }
    table, pre, blockquote {
        page-break-inside: avoid;
    }
}
h1 {
    font-size: 24pt;
    border-bottom: 2px solid #0366d6;
    padding-bottom: 8px;
    color: #0366d6;
    margin-top: 24px;
}
h2 {
    font-size: 18pt;
    border-bottom: 1px solid #eaecef;
    padding-bottom: 6px;
    color: #24292e;
    margin-top: 22px;
}
h3 {
    font-size: 14pt;
    color: #1b1f23;
    margin-top: 18px;
}
h4 {
    font-size: 12pt;
    color: #24292e;
    margin-top: 14px;
}
p, ul, ol {
    margin-top: 0;
    margin-bottom: 14px;
}
code {
    background-color: #f6f8fa;
    padding: 2px 5px;
    border-radius: 4px;
    font-family: "Cascadia Code", Consolas, "Courier New", monospace;
    font-size: 0.9em;
    color: #d73a49;
}
pre {
    background-color: #f6f8fa;
    border: 1px solid #e1e4e8;
    border-radius: 6px;
    padding: 12px;
    overflow-x: auto;
    font-family: "Cascadia Code", Consolas, "Courier New", monospace;
    font-size: 9.5pt;
    line-height: 1.45;
}
pre code {
    background: none;
    padding: 0;
    color: #24292e;
}
blockquote {
    border-left: 4px solid #0366d6;
    padding: 8px 16px;
    color: #444d56;
    background-color: #f1f8ff;
    margin: 14px 0;
    border-radius: 0 4px 4px 0;
}
table {
    border-collapse: collapse;
    width: 100%;
    margin: 16px 0;
    font-size: 10pt;
}
th, td {
    border: 1px solid #dfe2e5;
    padding: 8px 12px;
    text-align: left;
}
th {
    background-color: #f6f8fa;
    font-weight: 600;
}
tr:nth-child(2n) {
    background-color: #fafbfc;
}
hr {
    height: 1px;
    background-color: #e1e4e8;
    border: none;
    margin: 24px 0;
}
.badge {
    display: inline-block;
    padding: 2px 8px;
    font-size: 0.85em;
    font-weight: bold;
    border-radius: 4px;
    background: #28a745;
    color: white;
}
"""

CSS_SLIDES_THEME = """
body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    line-height: 1.6;
    color: #24292e;
    margin: 0;
    padding: 0;
    background-color: #f0f2f5;
}
.slide {
    background-color: white;
    width: 900px;
    min-height: 520px;
    margin: 25px auto;
    padding: 40px 50px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    border-radius: 8px;
    border-top: 6px solid #0366d6;
    box-sizing: border-box;
    page-break-after: always;
}
@media print {
    body {
        background-color: white;
    }
    .slide {
        margin: 0;
        width: 100%;
        min-height: 100vh;
        box-shadow: none;
        border-radius: 0;
        border-top: 6px solid #0366d6;
        padding: 30px 40px;
        page-break-after: always;
    }
    @page {
        size: A4 landscape;
        margin: 10mm;
    }
}
h2 {
    color: #0366d6;
    font-size: 22pt;
    margin-top: 0;
    border-bottom: 2px solid #eaecef;
    padding-bottom: 8px;
}
h3 {
    color: #24292e;
    font-size: 16pt;
    margin-top: 10px;
}
p, ul, ol {
    font-size: 12pt;
    margin-bottom: 12px;
}
li {
    margin-bottom: 6px;
}
code {
    background-color: #f6f8fa;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: "Cascadia Code", Consolas, monospace;
    font-size: 0.9em;
    color: #d73a49;
}
pre {
    background-color: #f6f8fa;
    border: 1px solid #e1e4e8;
    border-radius: 6px;
    padding: 10px;
    font-family: "Cascadia Code", Consolas, monospace;
    font-size: 9pt;
}
table {
    border-collapse: collapse;
    width: 100%;
    margin: 12px 0;
    font-size: 9.5pt;
}
th, td {
    border: 1px solid #dfe2e5;
    padding: 6px 10px;
}
th {
    background-color: #f1f8ff;
    color: #0366d6;
}
"""

def markdown_to_html(md_text, is_presentation=False):
    lines = md_text.splitlines()
    html_lines = []
    in_code_block = False
    in_table = False
    in_list = False
    list_type = None
    slide_open = False
    
    def close_table_if_open():
        nonlocal in_table
        if in_table:
            html_lines.append("</table>")
            in_table = False

    def close_list_if_open():
        nonlocal in_list, list_type
        if in_list:
            html_lines.append(f"</{list_type}>")
            in_list = False
            list_type = None

    for i, line in enumerate(lines):
        # Code blocks
        if line.startswith("```"):
            if in_code_block:
                html_lines.append("</code></pre>")
                in_code_block = False
            else:
                close_list_if_open()
                close_table_if_open()
                lang = line[3:].strip()
                html_lines.append(f"<pre><code class=\"language-{lang}\">")
                in_code_block = True
            continue

        if in_code_block:
            # Escape HTML characters inside code blocks
            escaped = line.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            html_lines.append(escaped)
            continue

        # Presentation slide splitting
        if is_presentation:
            if line.startswith("## Slide ") or (line.startswith("## ") and "Slide" in line):
                close_list_if_open()
                close_table_if_open()
                if slide_open:
                    html_lines.append("</div>") # close previous slide
                html_lines.append("<div class=\"slide\">")
                slide_open = True

        # Horizontal rule
        if re.match(r"^(\*{3,}|-{3,}|_{3,})$", line.strip()):
            close_list_if_open()
            close_table_if_open()
            html_lines.append("<hr/>")
            continue

        # Tables
        if line.strip().startswith("|") and line.strip().endswith("|"):
            close_list_if_open()
            cells = [c.strip() for c in line.strip()[1:-1].split("|")]
            # Check if it's separator row
            if all(re.match(r"^:?-+:?$", c) for c in cells):
                continue
            if not in_table:
                html_lines.append("<table>")
                html_lines.append("<thead><tr>")
                for c in cells:
                    formatted_c = inline_format(c)
                    html_lines.append(f"<th>{formatted_c}</th>")
                html_lines.append("</tr></thead><tbody>")
                in_table = True
            else:
                html_lines.append("<tr>")
                for c in cells:
                    formatted_c = inline_format(c)
                    html_lines.append(f"<td>{formatted_c}</td>")
                html_lines.append("</tr>")
            continue
        else:
            close_table_if_open()

        # Lists (unordered)
        list_match = re.match(r"^(\s*)[-*+]\s+(.*)$", line)
        if list_match:
            close_table_if_open()
            if not in_list or list_type != "ul":
                close_list_if_open()
                html_lines.append("<ul>")
                in_list = True
                list_type = "ul"
            content = inline_format(list_match.group(2))
            html_lines.append(f"<li>{content}</li>")
            continue

        # Lists (ordered)
        num_match = re.match(r"^(\s*)\d+\.\s+(.*)$", line)
        if num_match:
            close_table_if_open()
            if not in_list or list_type != "ol":
                close_list_if_open()
                html_lines.append("<ol>")
                in_list = True
                list_type = "ol"
            content = inline_format(num_match.group(2))
            html_lines.append(f"<li>{content}</li>")
            continue

        close_list_if_open()

        # Headers
        h_match = re.match(r"^(#{1,6})\s+(.*)$", line)
        if h_match:
            level = len(h_match.group(1))
            text = inline_format(h_match.group(2))
            html_lines.append(f"<h{level}>{text}</h{level}>")
            continue

        # Blockquotes
        if line.startswith(">"):
            quote_text = inline_format(line[1:].strip())
            html_lines.append(f"<blockquote>{quote_text}</blockquote>")
            continue

        # Normal paragraph
        stripped = line.strip()
        if stripped:
            html_lines.append(f"<p>{inline_format(stripped)}</p>")

    close_list_if_open()
    close_table_if_open()
    if is_presentation and slide_open:
        html_lines.append("</div>")

    return "\n".join(html_lines)

def inline_format(text):
    # Escape basic HTML
    text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    # Bold
    text = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", text)
    # Italic
    text = re.sub(r"\*(.+?)\*", r"<em>\1</em>", text)
    # Inline code
    text = re.sub(r"`([^`]+)`", r"<code>\1</code>", text)
    # Links
    text = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", r'<a href="\2">\1</a>', text)
    return text

def convert_md_to_pdf(md_path, pdf_path, is_presentation=False):
    print(f"[PDF] Processing: {md_path.name} -> {pdf_path.name}")
    md_content = md_path.read_text(encoding="utf-8")
    body_html = markdown_to_html(md_content, is_presentation=is_presentation)
    css = CSS_SLIDES_THEME if is_presentation else CSS_REPORT_THEME
    title = md_path.stem.replace("_", " ").title()

    full_html = f"""<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>{title}</title>
    <style>{css}</style>
</head>
<body>
{body_html}
</body>
</html>
"""
    temp_html_path = SUBMISSION_DIR / f"{pdf_path.stem}.html"
    temp_html_path.write_text(full_html, encoding="utf-8")

    cmd = [
        str(EDGE_EXE),
        "--headless",
        "--disable-gpu",
        "--run-all-compositor-stages-before-draw",
        f"--print-to-pdf={pdf_path.resolve()}",
        f"file:///{temp_html_path.resolve().as_posix()}"
    ]
    
    result = subprocess.run(cmd, capture_output=True, text=True)
    if pdf_path.exists() and pdf_path.stat().st_size > 0:
        print(f"[SUCCESS] Generated: {pdf_path.name} ({pdf_path.stat().st_size:,} bytes)")
    else:
        print(f"[ERROR] Failed to generate {pdf_path.name}: {result.stderr}")

def main():
    print("=" * 70)
    print("PARKWATCH - SUBMISSION PDF GENERATION UTILITY")
    print("=" * 70)
    
    tasks = [
        (
            WORKSPACE / "REPORTS" / "FINAL_PROJECT_COMPLETION_REPORT.md",
            SUBMISSION_DIR / "ParkWatch_Final_Project_Report.pdf",
            False
        ),
        (
            WORKSPACE / "docs" / "final_report_and_viva.md",
            SUBMISSION_DIR / "ParkWatch_Master_Academic_Report_and_Viva_Guide.pdf",
            False
        ),
        (
            WORKSPACE / "docs" / "presentation_slides.md",
            SUBMISSION_DIR / "ParkWatch_Presentation_Slides.pdf",
            True
        ),
        (
            WORKSPACE / "REPORTS" / "TASK_16_AI_INTEGRATION_VERIFICATION.md",
            SUBMISSION_DIR / "ParkWatch_Task_16_AI_Integration_Report.pdf",
            False
        ),
        (
            WORKSPACE / "REPORTS" / "TASK_17_DASHBOARD_AUDIT.md",
            SUBMISSION_DIR / "ParkWatch_Task_17_Dashboard_Audit_Report.pdf",
            False
        )
    ]
    
    for md_file, pdf_file, is_pres in tasks:
        if md_file.exists():
            convert_md_to_pdf(md_file, pdf_file, is_presentation=is_pres)
        else:
            print(f"[WARN] Source file missing: {md_file}")

    print("\n[SUMMARY] Submission folder contents:")
    for f in SUBMISSION_DIR.glob("*.pdf"):
        print(f" - {f.name}: {f.stat().st_size:,} bytes")
    print("=" * 70)

if __name__ == "__main__":
    main()
