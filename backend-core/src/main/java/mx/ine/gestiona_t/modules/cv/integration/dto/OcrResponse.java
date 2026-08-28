package mx.ine.gestiona_t.modules.cv.integration.dto;

public record OcrResponse(
    String textoExtraido,
    double confianza,
    String mensaje
) {}