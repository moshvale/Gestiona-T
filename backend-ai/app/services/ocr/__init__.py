"""Servicios de OCR y clasificación de documentos."""
import shutil
import pytesseract
from pathlib import Path

_tesseract_path = shutil.which("tesseract")
if _tesseract_path:
    pytesseract.pytesseract.tesseract_cmd = _tesseract_path
else:
    for _candidate in (
        "/opt/homebrew/bin/tesseract",
        "/usr/local/bin/tesseract",
        r"C:\Program Files\Tesseract-OCR\tesseract.exe",
        r"C:\Program Files (x86)\Tesseract-OCR\tesseract.exe",
    ):
        if Path(_candidate).exists():
            pytesseract.pytesseract.tesseract_cmd = _candidate
            break