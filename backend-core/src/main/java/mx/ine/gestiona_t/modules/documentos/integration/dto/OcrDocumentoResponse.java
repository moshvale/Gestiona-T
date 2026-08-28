package mx.ine.gestiona_t.modules.documentos.integration.dto;

import java.util.Map;

public record OcrDocumentoResponse(
    String textoExtraido,
    double confianza,
    String tipoDetectado,
    Map<String, Object> camposExtraidos,
    String mensaje
) {}