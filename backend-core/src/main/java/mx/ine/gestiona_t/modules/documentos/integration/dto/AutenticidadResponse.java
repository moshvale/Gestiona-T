package mx.ine.gestiona_t.modules.documentos.integration.dto;

public record AutenticidadResponse(
    double scoreAutenticidad,
    boolean sospechoso,
    String[] alteracionesDetectadas,
    String mensaje
) {}