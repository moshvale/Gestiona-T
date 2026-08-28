package mx.ine.gestiona_t.modules.cartadeclaratoria.dto.response;

import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.EstatusCarta;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.MetodoFirmaCarta;
import java.time.LocalDateTime;
import java.util.UUID;

public record CartaDeclaratoriaResponse(
    UUID id,
    UUID aspiranteId,
    String folio,
    String folioCarta,
    String version,
    EstatusCarta estatus,
    MetodoFirmaCarta metodoFirma,
    LocalDateTime fechaAceptacionCompleta,
    LocalDateTime fechaFirma,
    LocalDateTime createdAt
) {}