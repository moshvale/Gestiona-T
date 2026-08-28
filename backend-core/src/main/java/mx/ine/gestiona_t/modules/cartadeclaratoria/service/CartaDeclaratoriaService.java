package mx.ine.gestiona_t.modules.cartadeclaratoria.service;

import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.request.AceptarBloqueRequest;
import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.request.FirmarCartaRequest;
import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.response.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface CartaDeclaratoriaService {
    Mono<CartaDeclaratoriaResponse> iniciarCarta(UUID aspiranteId, String folio);
    Mono<CartaDeclaratoriaResponse> obtenerCarta(String folio);
    
    // ✅ NUEVO: Método para buscar carta por ID del aspirante (uso exclusivo de analistas)
    Mono<CartaDeclaratoriaResponse> obtenerCartaPorAspiranteId(UUID aspiranteId);

    // ✅ NUEVO: Obtener PDF buscando por el ID del aspirante
    Mono<byte[]> obtenerPdfPorAspiranteId(UUID aspiranteId);
    
    Mono<List<BloqueResponse>> obtenerBloques(String folio);
    Mono<AceptacionResponse> aceptarBloque(String folio, AceptarBloqueRequest request, String ip, String userAgent);
    Mono<CartaDeclaratoriaResponse> aceptarTodosBloques(String folio, String ip, String userAgent);
    Mono<Boolean> ejecutarValidacionesExternas(String folio, String curp);
    Mono<List<ValidacionExternaResponse>> obtenerValidaciones(String folio);
    Mono<EstatusCartaResponse> obtenerEstatus(String folio);
    Mono<CartaDeclaratoriaResponse> firmarCarta(String folio, FirmarCartaRequest request, String ip, String userAgent);
    Mono<byte[]> obtenerPdf(String folio);
}