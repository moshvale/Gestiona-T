package mx.ine.gestiona_t.modules.postulaciones.service;

import mx.ine.gestiona_t.modules.auth.model.Aspirante;
import mx.ine.gestiona_t.modules.auth.repository.AspiranteRepository;
import mx.ine.gestiona_t.modules.postulaciones.dto.response.PostulacionResponse;
import mx.ine.gestiona_t.modules.postulaciones.model.Postulacion;
import mx.ine.gestiona_t.modules.postulaciones.model.enums.EstatusPostulacion;
import mx.ine.gestiona_t.modules.postulaciones.repository.PostulacionRepository;
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
public class PostulacionServiceImpl implements PostulacionService {

    private static final Logger log = LoggerFactory.getLogger(PostulacionServiceImpl.class);
    
    private final PostulacionRepository postulacionRepository;
    private final AspiranteRepository aspiranteRepository;
    private final VacanteRepository vacanteRepository;

    public PostulacionServiceImpl(PostulacionRepository postulacionRepository, 
                                  AspiranteRepository aspiranteRepository, 
                                  VacanteRepository vacanteRepository) {
        this.postulacionRepository = postulacionRepository;
        this.aspiranteRepository = aspiranteRepository;
        this.vacanteRepository = vacanteRepository;
    }

    @Override
    @Transactional
    public PostulacionResponse postularse(UUID aspiranteId, UUID vacanteId) {
        log.info("📝 Iniciando postulación - Aspirante: {} | Vacante: {}", aspiranteId, vacanteId);

        if (postulacionRepository.existsByAspiranteIdAndVacanteId(aspiranteId, vacanteId)) {
            log.warn("⚠️ El aspirante {} ya se había postulado a la vacante {}", aspiranteId, vacanteId);
            throw new RuntimeException("Ya te has postulado a esta vacante anteriormente.");
        }
        
        Vacante vacante = vacanteRepository.findById(vacanteId)
                .orElseThrow(() -> {
                    log.error("❌ Vacante no encontrada: {}", vacanteId);
                    return new RuntimeException("Vacante no encontrada");
                });
                
        if (!vacante.getActiva()) {
            log.warn("⚠️ Intento de postulación a vacante inactiva: {}", vacanteId);
            throw new RuntimeException("Esta vacante ya no está disponible.");
        }
        
        if (vacante.getFechaLimite() != null && vacante.getFechaLimite().isBefore(LocalDate.now())) {
            log.warn("⚠️ Intento de postulación a vacante vencida: {} (límite: {})", 
                     vacanteId, vacante.getFechaLimite());
            throw new RuntimeException("Esta vacante ya no está vigente. La fecha límite fue: " + vacante.getFechaLimite());
        }

        aspiranteRepository.findById(aspiranteId)
                .orElseThrow(() -> {
                    log.error("❌ Aspirante no encontrado: {}", aspiranteId);
                    return new RuntimeException("Aspirante no encontrado");
                });

        Postulacion postulacion = new Postulacion();
        postulacion.setAspiranteId(aspiranteId);
        postulacion.setVacanteId(vacanteId);
        
        postulacion = postulacionRepository.save(postulacion);
        
        log.info("✅ Postulación creada exitosamente - ID: {} | Aspirante: {} | Vacante: {} ({}) | Estatus: {}", 
                 postulacion.getId(), aspiranteId, vacanteId, vacante.getPuesto(), postulacion.getEstatus());
        
        return mapToResponse(postulacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponse> obtenerTodasLasPostulaciones() {
        log.info("📂 Consultando todas las postulaciones para analista");
        List<Postulacion> postulaciones = postulacionRepository.findAllByOrderByFechaPostulacionDesc();
        log.info("✅ Postulaciones encontradas: {}", postulaciones.size());
        return postulaciones.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponse> obtenerMisPostulaciones(UUID aspiranteId) {
        log.info("📂 Consultando postulaciones del aspirante: {}", aspiranteId);
        List<Postulacion> postulaciones = postulacionRepository.findByAspiranteIdOrderByFechaPostulacionDesc(aspiranteId);
        log.info("✅ Postulaciones encontradas: {}", postulaciones.size());
        return postulaciones.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponse> obtenerPostulacionesPorVacante(UUID vacanteId) {
        log.info("📂 Consultando postulaciones de la vacante: {}", vacanteId);
        vacanteRepository.findById(vacanteId)
                .orElseThrow(() -> {
                    log.error("❌ Vacante no encontrada: {}", vacanteId);
                    return new RuntimeException("Vacante no encontrada");
                });
        
        List<Postulacion> postulaciones = postulacionRepository.findByVacanteIdOrderByFechaPostulacionDesc(vacanteId);
        log.info("✅ Postulaciones encontradas para vacante {}: {}", vacanteId, postulaciones.size());
        return postulaciones.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PostulacionResponse actualizarEstatus(UUID postulacionId, EstatusPostulacion estatus, String observaciones) {
        log.info("✏️ Actualizando estatus de postulación {} a {} | Observaciones: '{}'", 
                 postulacionId, estatus, observaciones);
        
        Postulacion postulacion = postulacionRepository.findById(postulacionId)
                .orElseThrow(() -> {
                    log.error("❌ Postulación no encontrada: {}", postulacionId);
                    return new RuntimeException("Postulación no encontrada");
                });
        
        EstatusPostulacion estatusAnterior = postulacion.getEstatus();
        postulacion.setEstatus(estatus);
        postulacion.setObservaciones(observaciones != null ? observaciones.trim() : "");
        postulacion = postulacionRepository.save(postulacion);
        
        log.info("✅ Estatus de postulación {} actualizado: {} → {}", postulacionId, estatusAnterior, estatus);
        return mapToResponse(postulacion);
    }

    // ============================================
    // MÉTODO HELPER: Mapear entidad a DTO
    // ============================================
    private PostulacionResponse mapToResponse(Postulacion p) {
        String nombreAspirante = "Desconocido";
        String nombrePuesto = "Desconocido";
        
        try {
            nombreAspirante = aspiranteRepository.findById(p.getAspiranteId())
                    .map(Aspirante::getNombreCompleto)
                    .orElse("Desconocido");
        } catch (Exception e) {
            log.warn("⚠️ No se pudo obtener el nombre del aspirante {}: {}", p.getAspiranteId(), e.getMessage());
        }
        
        try {
            nombrePuesto = vacanteRepository.findById(p.getVacanteId())
                    .map(Vacante::getPuesto)
                    .orElse("Desconocido");
        } catch (Exception e) {
            log.warn("⚠️ No se pudo obtener el nombre de la vacante {}: {}", p.getVacanteId(), e.getMessage());
        }
        
        return new PostulacionResponse(
            p.getId(),
            p.getAspiranteId(),
            nombreAspirante,
            p.getVacanteId(),
            nombrePuesto,
            p.getEstatus(),
            p.getFechaPostulacion(),
            p.getObservaciones(),
            p.getCartaDeclaratoriaId(),
            p.getCvCompletado(),
            p.getDocumentosCompletos(),
            // ✅ NUEVOS CAMPOS
            p.getCalificacionConocimientos(),
            p.getCalificacionPsicometrica(),
            p.getCalificacionEntrevista(),
            p.getEstatusFinalSeleccion(),
            p.getDictamenFinal()
        );
    }
}