package mx.ine.gestiona_t.modules.auditoria.dto.response;

import java.util.List;

public record VerificacionIntegridadResponse(
    boolean integridadValida,
    long totalEventosVerificados,
    long eventosConHashValido,
    long eventosConHashInvalido,
    List<String> erroresDetectados,
    String mensaje
) {}