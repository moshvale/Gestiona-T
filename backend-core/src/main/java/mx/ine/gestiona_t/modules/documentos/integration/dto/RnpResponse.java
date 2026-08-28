package mx.ine.gestiona_t.modules.documentos.integration.dto;

public record RnpResponse(
    boolean valido,
    String numeroCedula,
    String nombre,
    String institucion,
    String fechaExpedicion,
    String mensaje
) {}