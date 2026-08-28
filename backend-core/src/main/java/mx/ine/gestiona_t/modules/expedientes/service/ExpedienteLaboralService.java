package mx.ine.gestiona_t.modules.expedientes.service;

import mx.ine.gestiona_t.modules.expedientes.dto.request.ActualizarExpedienteLaboralRequest;
import mx.ine.gestiona_t.modules.expedientes.dto.request.CrearExpedienteLaboralRequest;
import mx.ine.gestiona_t.modules.expedientes.dto.response.ExpedienteLaboralResponse;
import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoContratacion;

import java.util.List;
import java.util.UUID;

public interface ExpedienteLaboralService {

    ExpedienteLaboralResponse crear(CrearExpedienteLaboralRequest request, UUID usuarioAltaId);

    ExpedienteLaboralResponse obtenerPorId(UUID id);

    ExpedienteLaboralResponse obtenerVigentePorAspirante(UUID aspiranteId);

    ExpedienteLaboralResponse obtenerVigentePorNumeroEmpleado(String numeroEmpleado);

    List<ExpedienteLaboralResponse> obtenerHistorialPorAspirante(UUID aspiranteId);

    List<ExpedienteLaboralResponse> listarTodos(Boolean soloVigentes, TipoContratacion tipoFiltro);

    List<ExpedienteLaboralResponse> listarPorJuntaEjecutiva(UUID juntaId);

    ExpedienteLaboralResponse actualizar(UUID id, ActualizarExpedienteLaboralRequest request);

    ExpedienteLaboralResponse cerrar(UUID id, UUID usuarioCierreId);

    void eliminar(UUID id);
}