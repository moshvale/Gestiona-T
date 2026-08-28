package mx.ine.gestiona_t.modules.firma.integration.dto;

public record BiometriaResponse(
    boolean coincide,
    double scoreCoincidencia,
    String curp,
    String nombreCompleto,
    String fuenteFoto,
    String mensaje
) {}