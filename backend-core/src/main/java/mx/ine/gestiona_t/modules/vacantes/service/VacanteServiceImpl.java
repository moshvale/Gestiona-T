package mx.ine.gestiona_t.modules.vacantes.service;

import mx.ine.gestiona_t.modules.vacantes.dto.request.CrearVacanteRequest;
import mx.ine.gestiona_t.modules.vacantes.dto.response.VacanteResumenResponse;
import mx.ine.gestiona_t.modules.vacantes.dto.response.VacanteResponse;
import mx.ine.gestiona_t.modules.vacantes.model.Vacante;
import mx.ine.gestiona_t.modules.vacantes.repository.VacanteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VacanteServiceImpl implements VacanteService {

    private static final Logger log = LoggerFactory.getLogger(VacanteServiceImpl.class);
    private final VacanteRepository vacanteRepository;

    public VacanteServiceImpl(VacanteRepository vacanteRepository) {
        this.vacanteRepository = vacanteRepository;
    }

    @Override
    @Transactional
    public VacanteResponse crearVacante(CrearVacanteRequest request, UUID analistaId) {
        log.info("📋 Creando vacante '{}' - Plaza: {} - Analista: {}", 
                 request.puesto(), request.numeroPlaza(), analistaId);

        if (vacanteRepository.existsByNumeroPlaza(request.numeroPlaza())) {
            throw new RuntimeException("Ya existe una vacante con el número de plaza: " + request.numeroPlaza());
        }

        Vacante vacante = new Vacante();
        mapearRequestAEntidad(request, vacante);
        vacante.setCreadaPor(analistaId);
        vacante.setActiva(true);

        vacante = vacanteRepository.save(vacante);
        log.info("✅ Vacante creada exitosamente - ID: {} - Plaza: {}", vacante.getId(), vacante.getNumeroPlaza());
        return mapToResponse(vacante);
    }

    @Override
    @Transactional(readOnly = true)
    public VacanteResponse obtenerVacante(UUID id) {
        Vacante vacante = vacanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vacante no encontrada"));
        return mapToResponse(vacante);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacanteResumenResponse> listarVacantes() {
        return vacanteRepository.findByActivaTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResumen)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacanteResumenResponse> buscarVacantes(String busqueda) {
        if (busqueda == null || busqueda.trim().isEmpty()) {
            return listarVacantes();
        }
        return vacanteRepository.buscarVacantes(busqueda.trim())
                .stream()
                .map(this::mapToResumen)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VacanteResponse actualizarVacante(UUID id, CrearVacanteRequest request) {
        Vacante vacante = vacanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vacante no encontrada"));

        // Verificar que el nuevo número de plaza no exista en otra vacante
        if (!vacante.getNumeroPlaza().equals(request.numeroPlaza()) 
                && vacanteRepository.existsByNumeroPlaza(request.numeroPlaza())) {
            throw new RuntimeException("Ya existe otra vacante con el número de plaza: " + request.numeroPlaza());
        }

        mapearRequestAEntidad(request, vacante);
        vacante = vacanteRepository.save(vacante);
        log.info("✅ Vacante actualizada - ID: {}", id);
        return mapToResponse(vacante);
    }

    @Override
    @Transactional
    public void desactivarVacante(UUID id) {
        Vacante vacante = vacanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vacante no encontrada"));
        vacante.setActiva(false);
        vacanteRepository.save(vacante);
        log.info("🚫 Vacante desactivada - ID: {} - Plaza: {}", id, vacante.getNumeroPlaza());
    }

    private void mapearRequestAEntidad(CrearVacanteRequest request, Vacante vacante) {
        vacante.setPuesto(request.puesto().trim());
        vacante.setNumeroPlaza(request.numeroPlaza().trim());
        vacante.setNivelTabular(request.nivelTabular().trim());
        vacante.setNumeroVacantes(request.numeroVacantes());
        vacante.setDescripcionFunciones(request.descripcionFunciones());
        vacante.setEscolaridad(request.escolaridad());
        vacante.setExperiencia(request.experiencia());
        vacante.setConocimientos(request.conocimientos());
        vacante.setHabilidades(request.habilidades());
        vacante.setActitudes(request.actitudes());
        vacante.setPercepcionBruta(request.percepcionBruta());
        vacante.setPercepcionNeta(request.percepcionNeta());
        vacante.setCiudadPlaza(request.ciudadPlaza());
        vacante.setUbicacionPlaza(request.ubicacionPlaza());
        vacante.setLugarRecepcionDocumentos(request.lugarRecepcionDocumentos());
        vacante.setFechaExpedicion(request.fechaExpedicion());
        vacante.setFechaInicio(request.fechaInicio());
        vacante.setFechaLimite(request.fechaLimite());
        vacante.setHorarioAtencion(request.horarioAtencion());
        vacante.setPersonaResponsable(request.personaResponsable());
        vacante.setFaseConcurso(request.faseConcurso());
        vacante.setNotaImportante(request.notaImportante());
        vacante.setRequisitos(request.requisitos() != null ? request.requisitos() : List.of());
        vacante.setDocumentacionRequerida(request.documentacionRequerida() != null ? request.documentacionRequerida() : List.of());
    }

    private VacanteResponse mapToResponse(Vacante v) {
        boolean vigente = v.getActiva() && v.getFechaLimite() != null 
                          && !v.getFechaLimite().isBefore(LocalDate.now());
        return new VacanteResponse(
            v.getId(), v.getPuesto(), v.getNumeroPlaza(), v.getNivelTabular(),
            v.getNumeroVacantes(), v.getDescripcionFunciones(), v.getEscolaridad(),
            v.getExperiencia(), v.getConocimientos(), v.getHabilidades(), v.getActitudes(),
            v.getPercepcionBruta(), v.getPercepcionNeta(), v.getCiudadPlaza(),
            v.getUbicacionPlaza(), v.getLugarRecepcionDocumentos(),
            v.getFechaExpedicion(), v.getFechaInicio(), v.getFechaLimite(),
            v.getHorarioAtencion(), v.getPersonaResponsable(), v.getFaseConcurso(),
            v.getNotaImportante(), v.getRequisitos(), v.getDocumentacionRequerida(),
            v.getCreadaPor(), v.getActiva(), v.getCreatedAt(), v.getUpdatedAt(), vigente
        );
    }

    private VacanteResumenResponse mapToResumen(Vacante v) {
        boolean vigente = v.getActiva() && v.getFechaLimite() != null 
                          && !v.getFechaLimite().isBefore(LocalDate.now());
        return new VacanteResumenResponse(
            v.getId(), v.getPuesto(), v.getNumeroPlaza(), v.getNivelTabular(),
            v.getNumeroVacantes(), v.getPercepcionBruta(), v.getPercepcionNeta(),
            v.getCiudadPlaza(), v.getFechaInicio(), v.getFechaLimite(), vigente,
            v.getFaseConcurso()
        );
    }
}