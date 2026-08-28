from PIL import Image
import pytesseract

# Crear una imagen de prueba con texto
img = Image.new('RGB', (400, 100), color='white')

# Verificar que Tesseract responde
try:
    version = pytesseract.get_tesseract_version()
    print(f"✅ Tesseract versión: {version}")
    
    # Probar con idioma español
    langs = pytesseract.get_languages()
    print(f"✅ Idiomas disponibles: {langs}")
    
    if 'spa' in langs:
        print("✅ Idioma español (spa) disponible")
    else:
        print("❌ Idioma español NO disponible. Ejecuta: brew install tesseract-lang")
        
except Exception as e:
    print(f"❌ Error: {e}")