package mx.ine.gestiona_t.modules.cartadeclaratoria.dto.response;

import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.EstatusCarta;
import java.util.List;

public record EstatusCartaResponse(
    String folio,
    EstatusCarta estatus,
    int bloquesAceptados,
    int bloquesTotal,
    boolean bloquesCompletos,
    boolean validacionExternaOk,
    boolean pdfGenerado,
    boolean firmada,
    List<String> mensajes
) {}