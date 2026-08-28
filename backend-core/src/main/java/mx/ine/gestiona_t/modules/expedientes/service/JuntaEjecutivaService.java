package mx.ine.gestiona_t.modules.expedientes.service;

import mx.ine.gestiona_t.modules.expedientes.dto.request.CrearJuntaEjecutivaRequest;
import mx.ine.gestiona_t.modules.expedientes.dto.request.CrearVocaliaRequest;
import mx.ine.gestiona_t.modules.expedientes.dto.response.JuntaEjecutivaResponse;
import mx.ine.gestiona_t.modules.expedientes.dto.response.VocaliaResponse;
import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoJunta;

import java.util.List;
import java.util.UUID;

public interface JuntaEjecutivaService {

    JuntaEjecutivaResponse crearJunta(CrearJuntaEjecutivaRequest request);

    List<JuntaEjecutivaResponse> listarJuntas(TipoJunta tipoFiltro);

    JuntaEjecutivaResponse obtenerJunta(UUID id);

    void desactivarJunta(UUID id);

    VocaliaResponse crearVocalia(CrearVocaliaRequest request);

    List<VocaliaResponse> listarVocaliasPorJunta(UUID juntaId);
}