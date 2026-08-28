package mx.ine.gestiona_t.modules.expedientes.service;

import mx.ine.gestiona_t.modules.expedientes.dto.request.CrearJuntaEjecutivaRequest;
import mx.ine.gestiona_t.modules.expedientes.dto.request.CrearVocaliaRequest;
import mx.ine.gestiona_t.modules.expedientes.dto.response.JuntaEjecutivaResponse;
import mx.ine.gestiona_t.modules.expedientes.dto.response.VocaliaResponse;
import mx.ine.gestiona_t.modules.expedientes.model.JuntaEjecutiva;
import mx.ine.gestiona_t.modules.expedientes.model.Vocalia;
import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoJunta;
import mx.ine.gestiona_t.modules.expedientes.repository.JuntaEjecutivaRepository;
import mx.ine.gestiona_t.modules.expedientes.repository.VocaliaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JuntaEjecutivaServiceImpl implements JuntaEjecutivaService {

    private static final Logger log = LoggerFactory.getLogger(JuntaEjecutivaServiceImpl.class);

    private final JuntaEjecutivaRepository juntaRepository;
    private final VocaliaRepository vocaliaRepository;

    public JuntaEjecutivaServiceImpl(JuntaEjecutivaRepository juntaRepository,
                                     VocaliaRepository vocaliaRepository) {
        this.juntaRepository = juntaRepository;
        this.vocaliaRepository = vocaliaRepository;
    }

    @Override
    @Transactional
    public JuntaEjecutivaResponse crearJunta(CrearJuntaEjecutivaRequest request) {
        log.info("🏛️ Creando junta ejecutiva: {} | Tipo: {}", request.nombre(), request.tipo());

        JuntaEjecutiva junta = new JuntaEjecutiva();
        junta.setNombre(request.nombre().trim());
        junta.setTipo(request.tipo());
        junta.setEstado(request.estado());
        junta.setClaveIne(request.claveIne());

        junta = juntaRepository.save(junta);
        log.info("✅ Junta ejecutiva creada: {}", junta.getId());
        return mapJuntaToResponse(junta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JuntaEjecutivaResponse> listarJuntas(TipoJunta tipoFiltro) {
        List<JuntaEjecutiva> juntas;
        if (tipoFiltro != null) {
            juntas = juntaRepository.findByTipoAndActivaTrue(tipoFiltro);
        } else {
            juntas = juntaRepository.findByActivaTrueOrderByNombreAsc();
        }
        return juntas.stream().map(this::mapJuntaToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JuntaEjecutivaResponse obtenerJunta(UUID id) {
        JuntaEjecutiva junta = juntaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Junta ejecutiva no encontrada"));
        return mapJuntaToResponse(junta);
    }

    @Override
    @Transactional
    public void desactivarJunta(UUID id) {
        JuntaEjecutiva junta = juntaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Junta ejecutiva no encontrada"));
        junta.setActiva(false);
        juntaRepository.save(junta);
        log.info("🔒 Junta ejecutiva {} desactivada", id);
    }

    @Override
    @Transactional
    public VocaliaResponse crearVocalia(CrearVocaliaRequest request) {
        log.info("🏛️ Creando vocalía: {} | Junta: {}", request.nombre(), request.juntaEjecutivaId());

        JuntaEjecutiva junta = juntaRepository.findById(request.juntaEjecutivaId())
                .orElseThrow(() -> new RuntimeException("Junta ejecutiva no encontrada"));

        Vocalia vocalia = new Vocalia();
        vocalia.setNombre(request.nombre().trim());
        vocalia.setJuntaEjecutiva(junta);

        vocalia = vocaliaRepository.save(vocalia);
        log.info("✅ Vocalía creada: {}", vocalia.getId());
        return mapVocaliaToResponse(vocalia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VocaliaResponse> listarVocaliasPorJunta(UUID juntaId) {
        return vocaliaRepository.findByJuntaEjecutivaIdAndActivaTrue(juntaId)
                .stream()
                .map(this::mapVocaliaToResponse)
                .collect(Collectors.toList());
    }

    private JuntaEjecutivaResponse mapJuntaToResponse(JuntaEjecutiva j) {
        return new JuntaEjecutivaResponse(
            j.getId(), j.getNombre(), j.getTipo(), j.getEstado(), j.getClaveIne(), j.getActiva()
        );
    }

    private VocaliaResponse mapVocaliaToResponse(Vocalia v) {
        return new VocaliaResponse(
            v.getId(),
            v.getNombre(),
            v.getJuntaEjecutiva().getId(),
            v.getJuntaEjecutiva().getNombre(),
            v.getActiva()
        );
    }
}