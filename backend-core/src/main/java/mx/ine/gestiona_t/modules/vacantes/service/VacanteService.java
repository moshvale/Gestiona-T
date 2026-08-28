package mx.ine.gestiona_t.modules.vacantes.service;

import mx.ine.gestiona_t.modules.vacantes.dto.request.CrearVacanteRequest;
import mx.ine.gestiona_t.modules.vacantes.dto.response.VacanteResumenResponse;
import mx.ine.gestiona_t.modules.vacantes.dto.response.VacanteResponse;

import java.util.List;
import java.util.UUID;

public interface VacanteService {
    VacanteResponse crearVacante(CrearVacanteRequest request, UUID analistaId);
    VacanteResponse obtenerVacante(UUID id);
    List<VacanteResumenResponse> listarVacantes();
    List<VacanteResumenResponse> buscarVacantes(String busqueda);
    VacanteResponse actualizarVacante(UUID id, CrearVacanteRequest request);
    void desactivarVacante(UUID id);
}