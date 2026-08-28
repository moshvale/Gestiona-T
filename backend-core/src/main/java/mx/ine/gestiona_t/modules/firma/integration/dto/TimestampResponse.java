package mx.ine.gestiona_t.modules.firma.integration.dto;

public record TimestampResponse(
    boolean exitoso,
    String timestampToken,
    String timestampCertificado,
    String autoridad,
    String mensaje
) {}