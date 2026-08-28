package mx.ine.gestiona_t.modules.documentos.integration.dto;

public record SatResponse(
    boolean valido,
    String rfc,
    String regimen,
    String situacionFiscal,
    String mensaje
) {}