package mx.ine.gestiona_t.modules.postulaciones.service;

import mx.ine.gestiona_t.modules.postulaciones.dto.response.PostulacionResponse;
import mx.ine.gestiona_t.modules.postulaciones.model.enums.EstatusPostulacion;
import java.util.List;
import java.util.UUID;

public interface PostulacionService {
    PostulacionResponse postularse(UUID aspiranteId, UUID vacanteId);
    List<PostulacionResponse> obtenerTodasLasPostulaciones();
    List<PostulacionResponse> obtenerMisPostulaciones(UUID aspiranteId);
    List<PostulacionResponse> obtenerPostulacionesPorVacante(UUID vacanteId);
    PostulacionResponse actualizarEstatus(UUID postulacionId, EstatusPostulacion estatus, String observaciones);
}