import os
import re
import subprocess
from pathlib import Path

WORKSPACE = Path(r"C:\Users\Admin\Desktop\Public-Parking-DevOps")
REPORTS_DIR = WORKSPACE / "REPORTS"
SUBMISSION_DIR = WORKSPACE / "submission" / "practical_reports"
EDGE_EXE = Path(r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe")

SUBMISSION_DIR.mkdir(parents=True, exist_ok=True)

CSS_PRACTICAL_THEME = """
body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    line-height: 1.55;
    color: #24292e;
    max-width: 900px;
    margin: 0 auto;
    padding: 25px;
    font-size: 11pt;
}
@media print {
    body {
        max-width: 100%;
        padding: 0;
        font-size: 10pt;
    }
    @page {
        margin: 16mm 14mm 16mm 14mm;
        size: A4 portrait;
        @bottom-right {
            content: "Page " counter(page);
            font-size: 8.5pt;
            color: #57606a;
        }
    }
    h1, h2, h3, h4 {
        page-break-after: avoid;
    }
    table, pre, blockquote, .screenshot-container {
        page-break-inside: avoid;
    }
}
.practical-header {
    border-bottom: 2px solid #0969da;
    padding-bottom: 12px;
    margin-bottom: 22px;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    font-size: 9.5pt;
    color: #424a53;
}
.header-badge {
    display: inline-block;
    background: #0969da;
    color: white;
    font-weight: bold;
    padding: 3px 8px;
    border-radius: 4px;
    margin-bottom: 4px;
}
h1 {
    font-size: 20pt;
    color: #0969da;
    margin-top: 0;
    margin-bottom: 12px;
    border-bottom: 1px solid #d0d7de;
    padding-bottom: 8px;
}
h2 {
    font-size: 15pt;
    border-bottom: 1px solid #eaecef;
    padding-bottom: 5px;
    color: #1f2328;
    margin-top: 20px;
    margin-bottom: 10px;
}
h3 {
    font-size: 12pt;
    color: #24292f;
    margin-top: 14px;
    margin-bottom: 8px;
}
p, ul, ol {
    margin-top: 0;
    margin-bottom: 10px;
}
li {
    margin-bottom: 4px;
}
code {
    background-color: #eff1f3;
    padding: 2px 5px;
    border-radius: 4px;
    font-family: "Cascadia Code", Consolas, "Courier New", monospace;
    font-size: 0.9em;
    color: #cf222e;
}
pre {
    background-color: #f6f8fa;
    border: 1px solid #d0d7de;
    border-radius: 6px;
    padding: 10px 12px;
    overflow-x: auto;
    font-family: "Cascadia Code", Consolas, monospace;
    font-size: 8.5pt;
    line-height: 1.4;
    margin: 12px 0;
}
pre code {
    background: none;
    padding: 0;
    color: #1f2328;
}
blockquote {
    border-left: 4px solid #0969da;
    padding: 6px 14px;
    color: #57606a;
    background-color: #f6f8fa;
    margin: 12px 0;
    border-radius: 0 4px 4px 0;
    font-size: 9.5pt;
}
table {
    border-collapse: collapse;
    width: 100%;
    margin: 14px 0;
    font-size: 9pt;
}
th, td {
    border: 1px solid #d0d7de;
    padding: 6px 10px;
    text-align: left;
}
th {
    background-color: #f6f8fa;
    font-weight: 600;
}
tr:nth-child(2n) {
    background-color: #fcfcfc;
}
hr {
    height: 1px;
    background-color: #d0d7de;
    border: none;
    margin: 20px 0;
}
.screenshot-container {
    text-align: center;
    margin: 18px 0;
    page-break-inside: avoid;
}
.screenshot-img {
    max-width: 95%;
    max-height: 480px;
    object-fit: contain;
    border: 1px solid #d0d7de;
    border-radius: 6px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.screenshot-caption {
    font-size: 9pt;
    font-style: italic;
    color: #57606a;
    margin-top: 6px;
}
"""

def resolve_image_path(img_src, report_dir):
    # If path is relative to REPORTS, resolve to absolute file:/// URI
    clean_src = img_src.strip()
    if clean_src.startswith("screenshots/"):
        target_path = report_dir / clean_src
        if target_path.exists():
            return f"file:///{target_path.resolve().as_posix()}"
    elif (report_dir / clean_src).exists():
        return f"file:///{(report_dir / clean_src).resolve().as_posix()}"
    return img_src

def format_inline(text, report_dir):
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

def parse_report_markdown(md_text, report_dir):
    lines = md_text.splitlines()
    html_lines = []
    in_code = False
    in_table = False
    in_list = False
    list_type = None

    def close_table():
        nonlocal in_table
        if in_table:
            html_lines.append("</table>")
            in_table = False

    def close_list():
        nonlocal in_list, list_type
        if in_list:
            html_lines.append(f"</{list_type}>")
            in_list = False
            list_type = None

    for line in lines:
        # Code blocks
        if line.startswith("```"):
            if in_code:
                html_lines.append("</code></pre>")
                in_code = False
            else:
                close_list()
                close_table()
                lang = line[3:].strip()
                html_lines.append(f'<pre><code class="language-{lang}">')
                in_code = True
            continue

        if in_code:
            escaped = line.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            html_lines.append(escaped)
            continue

        # Markdown Images: ![Caption](path)
        img_match = re.match(r"^\s*!\[(.*?)\]\((.*?)\)\s*$", line)
        if img_match:
            close_list()
            close_table()
            caption = img_match.group(1)
            src = img_match.group(2)
            resolved_uri = resolve_image_path(src, report_dir)
            html_lines.append(f'''<div class="screenshot-container">
    <img class="screenshot-img" src="{resolved_uri}" alt="{caption}">
    <div class="screenshot-caption">{caption}</div>
</div>''')
            continue

        # Horizontal rule
        if re.match(r"^(\*{3,}|-{3,}|_{3,})$", line.strip()):
            close_list()
            close_table()
            html_lines.append("<hr/>")
            continue

        # Tables
        if line.strip().startswith("|") and line.strip().endswith("|"):
            close_list()
            cells = [c.strip() for c in line.strip()[1:-1].split("|")]
            if all(re.match(r"^:?-+:?$", c) for c in cells):
                continue
            if not in_table:
                html_lines.append("<table><thead><tr>")
                for c in cells:
                    html_lines.append(f"<th>{format_inline(c, report_dir)}</th>")
                html_lines.append("</tr></thead><tbody>")
                in_table = True
            else:
                html_lines.append("<tr>")
                for c in cells:
                    html_lines.append(f"<td>{format_inline(c, report_dir)}</td>")
                html_lines.append("</tr>")
            continue
        else:
            close_table()

        # Lists (unordered)
        ul_match = re.match(r"^(\s*)[-*+]\s+(.*)$", line)
        if ul_match:
            close_table()
            if not in_list or list_type != "ul":
                close_list()
                html_lines.append("<ul>")
                in_list = True
                list_type = "ul"
            html_lines.append(f"<li>{format_inline(ul_match.group(2), report_dir)}</li>")
            continue

        # Lists (ordered)
        ol_match = re.match(r"^(\s*)\d+\.\s+(.*)$", line)
        if ol_match:
            close_table()
            if not in_list or list_type != "ol":
                close_list()
                html_lines.append("<ol>")
                in_list = True
                list_type = "ol"
            html_lines.append(f"<li>{format_inline(ol_match.group(2), report_dir)}</li>")
            continue

        close_list()

        # Headers
        h_match = re.match(r"^(#{1,6})\s+(.*)$", line)
        if h_match:
            level = len(h_match.group(1))
            text = format_inline(h_match.group(2), report_dir)
            html_lines.append(f"<h{level}>{text}</h{level}>")
            continue

        # Blockquotes
        if line.startswith(">"):
            quote_text = format_inline(line[1:].strip(), report_dir)
            html_lines.append(f"<blockquote>{quote_text}</blockquote>")
            continue

        # Paragraph
        stripped = line.strip()
        if stripped:
            html_lines.append(f"<p>{format_inline(stripped, report_dir)}</p>")

    close_list()
    close_table()
    return "\n".join(html_lines)

def convert_task_report(task_num):
    report_file = REPORTS_DIR / f"TASK_{task_num:02d}_Report.md"
    pdf_file = SUBMISSION_DIR / f"TASK_{task_num:02d}_Report.pdf"

    if not report_file.exists():
        print(f"[ERR] File not found: {report_file}")
        return False, 0

    md_content = report_file.read_text(encoding="utf-8")
    body_html = parse_report_markdown(md_content, REPORTS_DIR)

    header_html = f"""<div class="practical-header">
    <div>
        <span class="header-badge">PRACTICAL REPORT {task_num:02d}</span><br>
        <strong>Department of DevOps & Cloud Engineering</strong><br>
        <strong>Project:</strong> ParkWatch - Public Parking Availability Dashboard
    </div>
    <div style="text-align: right;">
        <strong>Student:</strong> Sajiya Shaikh | <strong>Roll No:</strong> 24102A2005<br>
        <strong>Academic Year:</strong> 2026 | <strong>Milestone:</strong> Task {task_num:02d}
    </div>
</div>"""

    full_html = f"""<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Task {task_num:02d} Practical Report - ParkWatch</title>
<style>{CSS_PRACTICAL_THEME}</style>
</head>
<body>
{header_html}
{body_html}
</body>
</html>"""

    temp_html = SUBMISSION_DIR / f"_temp_task_{task_num:02d}.html"
    temp_html.write_text(full_html, encoding="utf-8")

    cmd = [
        str(EDGE_EXE),
        "--headless",
        "--disable-gpu",
        "--run-all-compositor-stages-before-draw",
        f"--print-to-pdf={pdf_file.resolve()}",
        f"file:///{temp_html.resolve().as_posix()}"
    ]

    subprocess.run(cmd, capture_output=True, text=True)
    if temp_html.exists():
        temp_html.unlink()

    if pdf_file.exists() and pdf_file.stat().st_size > 0:
        size = pdf_file.stat().st_size
        print(f"[OK] Task {task_num:02d}: {pdf_file.name} generated successfully ({size:,} bytes)")
        return True, size
    else:
        print(f"[ERR] Failed to generate {pdf_file.name}")
        return False, 0

def main():
    print("=" * 70)
    print("PARKWATCH - 15 PRACTICAL REPORT PDF GENERATION UTILITY")
    print("=" * 70)

    results = []
    for t in range(1, 16):
        success, size = convert_task_report(t)
        results.append((t, success, size))

    print("\n" + "=" * 70)
    print("GENERATION RECAP:")
    print("=" * 70)
    print(f"{'Task':<10} | {'Status':<12} | {'PDF Size (Bytes)':<18} | {'Filename'}")
    print("-" * 70)
    for t, success, size in results:
        status_str = "SUCCESS" if success else "FAILED"
        fname = f"TASK_{t:02d}_Report.pdf"
        print(f"Task {t:02d}    | {status_str:<12} | {size:<18,} | {fname}")
    print("=" * 70)

if __name__ == "__main__":
    main()
