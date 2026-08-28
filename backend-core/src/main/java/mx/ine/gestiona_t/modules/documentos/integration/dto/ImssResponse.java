package mx.ine.gestiona_t.modules.documentos.integration.dto;

public record ImssResponse(
    boolean valido,
    String registroPatronal,
    String nombreEmpresa,
    String mensaje
) {}