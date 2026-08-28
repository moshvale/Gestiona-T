package mx.ine.gestiona_t.modules.documentos.integration.dto;

public record SfpResponse(
    boolean inhabilitado,
    String motivo,
    String fechaInicioSancion,
    String fechaFinSancion,
    String autoridadSancionadora,
    String mensaje
) {}