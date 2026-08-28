package mx.ine.gestiona_t.modules.expedientes.service;

import mx.ine.gestiona_t.modules.auth.model.Aspirante;
import mx.ine.gestiona_t.modules.auth.model.enums.TipoPersona;
import mx.ine.gestiona_t.modules.auth.repository.AspiranteRepository;
import mx.ine.gestiona_t.modules.expedientes.dto.request.ActualizarExpedienteLaboralRequest;
import mx.ine.gestiona_t.modules.expedientes.dto.request.CrearExpedienteLaboralRequest;
import mx.ine.gestiona_t.modules.expedientes.dto.response.ExpedienteLaboralResponse;
import mx.ine.gestiona_t.modules.expedientes.model.ExpedienteLaboral;
import mx.ine.gestiona_t.modules.expedientes.model.JuntaEjecutiva;
import mx.ine.gestiona_t.modules.expedientes.model.Vocalia;
import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoContratacion;
import mx.ine.gestiona_t.modules.expedientes.repository.ExpedienteLaboralRepository;
import mx.ine.gestiona_t.modules.expedientes.repository.JuntaEjecutivaRepository;
import mx.ine.gestiona_t.modules.expedientes.repository.VocaliaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mx.ine.gestiona_t.modules.vacantes.model.Vacante;
import mx.ine.gestiona_t.modules.vacantes.repository.VacanteRepository;
import mx.ine.gestiona_t.modules.documentos.repository.DocumentoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpedienteLaboralServiceImpl implements ExpedienteLaboralService {

    private static final Logger log = LoggerFactory.getLogger(ExpedienteLaboralServiceImpl.class);

    private final ExpedienteLaboralRepository expedienteRepository;
    private final AspiranteRepository aspiranteRepository;
    private final JuntaEjecutivaRepository juntaRepository;
    private final VocaliaRepository vocaliaRepository;
    private final VacanteRepository vacanteRepository;
    private final DocumentoRepository documentoRepository;

    public ExpedienteLaboralServiceImpl(ExpedienteLaboralRepository expedienteRepository,
                                        AspiranteRepository aspiranteRepository,
                                        JuntaEjecutivaRepository juntaRepository,
                                        VocaliaRepository vocaliaRepository,
                                        VacanteRepository vacanteRepository,
                                        DocumentoRepository documentoRepository) {
        this.expedienteRepository = expedienteRepository;
        this.aspiranteRepository = aspiranteRepository;
        this.juntaRepository = juntaRepository;
        this.vocaliaRepository = vocaliaRepository;
        this.vacanteRepository = vacanteRepository;
        this.documentoRepository = documentoRepository;
    }

    @Override
    @Transactional
    public ExpedienteLaboralResponse crear(CrearExpedienteLaboralRequest request, UUID usuarioAltaId) {
        log.info("📝 Creando expediente laboral - Aspirante: {} | No.Empleado: {} | Tipo: {}",
                request.aspiranteId(), request.numeroEmpleado(), request.tipoContratacion());

        // 1. Validar que el aspirante exista
        Aspirante aspirante = aspiranteRepository.findById(request.aspiranteId())
                .orElseThrow(() -> {
                    log.error("❌ Aspirante no encontrado: {}", request.aspiranteId());
                    return new RuntimeException("Aspirante no encontrado");
                });

        // 2. Validar que el número de empleado no esté en uso por otro aspirante
        if (expedienteRepository.existsByNumeroEmpleado(request.numeroEmpleado())) {
            log.warn("⚠️ Número de empleado ya existe: {}", request.numeroEmpleado());
            throw new RuntimeException("El número de empleado " + request.numeroEmpleado() + " ya está registrado");
        }

        Vacante vacante = null;
        if (request.vacanteId() != null) {
            vacante = vacanteRepository.findById(request.vacanteId())
                    .orElseThrow(() -> new RuntimeException("Vacante no encontrada"));
            validarDatosContraVacante(vacante, request.puestoActual(), request.nivelTabular());
        }

        // 3. Si es PROCESO_ELECTORAL, validar junta y vocalía
        if (request.tipoContratacion() == TipoContratacion.PROCESO_ELECTORAL) {
            if (request.juntaEjecutivaId() == null || request.vocaliaId() == null) {
                throw new RuntimeException("Para proceso electoral es obligatorio especificar junta ejecutiva y vocalía");
            }
            vocaliaRepository.findById(request.vocaliaId())
                    .filter(v -> v.getJuntaEjecutiva().getId().equals(request.juntaEjecutivaId()))
                    .orElseThrow(() -> new RuntimeException("La vocalía no pertenece a la junta ejecutiva indicada"));
        }

        // 4. Actualizar el aspirante a INTERNO y asignar número de empleado
        aspirante.setTipoPersona(TipoPersona.INTERNO);
        aspirante.setNumeroEmpleado(request.numeroEmpleado());
        aspiranteRepository.save(aspirante);

        // 5. Cerrar expediente vigente previo si existe (un empleado solo puede tener UN vigente)
        expedienteRepository.findVigenteByAspiranteId(request.aspiranteId())
                .ifPresent(vigente -> {
                    log.info("🔄 Cerrando expediente vigente previo: {}", vigente.getId());
                    vigente.setVigente(false);
                    vigente.setFechaFin(LocalDate.now());
                    expedienteRepository.save(vigente);
                });

        // 6. Crear el nuevo expediente
        ExpedienteLaboral expediente = new ExpedienteLaboral();
        expediente.setAspiranteId(request.aspiranteId());
        expediente.setNumeroEmpleado(request.numeroEmpleado());
        expediente.setTipoContratacion(request.tipoContratacion());
        expediente.setFechaInicio(request.fechaInicio());
        expediente.setFechaFin(request.fechaFin());
        expediente.setVigente(true);
        expediente.setAreaAdscripcion(request.areaAdscripcion());
        expediente.setPuestoActual(request.puestoActual());
        expediente.setNivelTabular(request.nivelTabular());
        expediente.setVacante(vacante);
        expediente.setAltaPorUsuarioId(usuarioAltaId);
        expediente.setObservaciones(request.observaciones());

        // Asociar junta y vocalía si aplica
        if (request.juntaEjecutivaId() != null) {
            JuntaEjecutiva junta = juntaRepository.findById(request.juntaEjecutivaId())
                    .orElseThrow(() -> new RuntimeException("Junta ejecutiva no encontrada"));
            expediente.setJuntaEjecutiva(junta);
        }
        if (request.vocaliaId() != null) {
            Vocalia vocalia = vocaliaRepository.findById(request.vocaliaId())
                    .orElseThrow(() -> new RuntimeException("Vocalía no encontrada"));
            expediente.setVocalia(vocalia);
        }

        expediente = expedienteRepository.save(expediente);
        log.info("✅ Expediente laboral creado - ID: {} | No.Empleado: {}", expediente.getId(), expediente.getNumeroEmpleado());

        return mapToResponse(expediente);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpedienteLaboralResponse obtenerPorId(UUID id) {
        ExpedienteLaboral expediente = expedienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expediente laboral no encontrado"));
        return mapToResponse(expediente);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpedienteLaboralResponse obtenerVigentePorAspirante(UUID aspiranteId) {
        return expedienteRepository.findVigenteByAspiranteId(aspiranteId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpedienteLaboralResponse obtenerVigentePorNumeroEmpleado(String numeroEmpleado) {
        return expedienteRepository.findVigenteByNumeroEmpleado(numeroEmpleado)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpedienteLaboralResponse> obtenerHistorialPorAspirante(UUID aspiranteId) {
        return expedienteRepository.findByAspiranteIdOrderByFechaInicioDesc(aspiranteId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpedienteLaboralResponse> listarTodos(Boolean soloVigentes, TipoContratacion tipoFiltro) {
        List<ExpedienteLaboral> expedientes;

        if (tipoFiltro != null) {
            expedientes = expedienteRepository.findByTipoContratacionAndVigenteTrue(tipoFiltro);
        } else if (Boolean.TRUE.equals(soloVigentes)) {
            expedientes = expedienteRepository.findByVigenteTrueOrderByFechaInicioDesc();
        } else {
            expedientes = expedienteRepository.findAll();
        }

        return expedientes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpedienteLaboralResponse> listarPorJuntaEjecutiva(UUID juntaId) {
        return expedienteRepository.findByJuntaEjecutivaAndVigente(juntaId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExpedienteLaboralResponse actualizar(UUID id, ActualizarExpedienteLaboralRequest request) {
        log.info("✏️ Actualizando expediente laboral: {}", id);

        ExpedienteLaboral expediente = expedienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expediente laboral no encontrado"));

        // Validar unicidad del número de empleado si cambia
        if (request.numeroEmpleado() != null && !request.numeroEmpleado().equals(expediente.getNumeroEmpleado())) {
            if (expedienteRepository.existsByNumeroEmpleado(request.numeroEmpleado())) {
                throw new RuntimeException("El número de empleado " + request.numeroEmpleado() + " ya está en uso");
            }
            expediente.setNumeroEmpleado(request.numeroEmpleado());
            aspiranteRepository.findById(expediente.getAspiranteId()).ifPresent(aspirante -> {
                aspirante.setNumeroEmpleado(request.numeroEmpleado());
                aspiranteRepository.save(aspirante);
            });
        }

        if (request.tipoContratacion() != null) expediente.setTipoContratacion(request.tipoContratacion());
        if (request.fechaInicio() != null) expediente.setFechaInicio(request.fechaInicio());
        expediente.setFechaFin(request.fechaFin());
        if (request.areaAdscripcion() != null) expediente.setAreaAdscripcion(request.areaAdscripcion());
        if (request.puestoActual() != null) expediente.setPuestoActual(request.puestoActual());
        if (request.nivelTabular() != null) expediente.setNivelTabular(request.nivelTabular());
        Vacante vacante = expediente.getVacante();
        if (request.vacanteId() != null) {
            vacante = vacanteRepository.findById(request.vacanteId())
                .orElseThrow(() -> new RuntimeException("Vacante no encontrada"));
            expediente.setVacante(vacante);
        }
        if (vacante != null) {
            validarDatosContraVacante(
                    vacante,
                    request.puestoActual() != null ? request.puestoActual() : expediente.getPuestoActual(),
                    request.nivelTabular() != null ? request.nivelTabular() : expediente.getNivelTabular());
        }
        if (request.observaciones() != null) expediente.setObservaciones(request.observaciones());

        // Actualizar junta/vocalía
        if (request.juntaEjecutivaId() != null) {
            JuntaEjecutiva junta = juntaRepository.findById(request.juntaEjecutivaId())
                    .orElseThrow(() -> new RuntimeException("Junta ejecutiva no encontrada"));
            expediente.setJuntaEjecutiva(junta);
        }
        if (request.vocaliaId() != null) {
            Vocalia vocalia = vocaliaRepository.findById(request.vocaliaId())
                    .orElseThrow(() -> new RuntimeException("Vocalía no encontrada"));
            expediente.setVocalia(vocalia);
        }
        if (request.tipoContratacion() != null && request.tipoContratacion() != TipoContratacion.PROCESO_ELECTORAL) {
            expediente.setJuntaEjecutiva(null);
            expediente.setVocalia(null);
        }

        expediente = expedienteRepository.save(expediente);
        log.info("✅ Expediente {} actualizado", id);
        return mapToResponse(expediente);
    }

    @Override
    @Transactional
    public ExpedienteLaboralResponse cerrar(UUID id, UUID usuarioCierreId) {
        log.info("🔒 Cerrando expediente laboral: {} | Usuario: {}", id, usuarioCierreId);

        ExpedienteLaboral expediente = expedienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expediente laboral no encontrado"));

        if (!expediente.getVigente()) {
            throw new RuntimeException("El expediente ya se encuentra cerrado");
        }

        expediente.setVigente(false);
        expediente.setFechaFin(LocalDate.now());
        expediente.setObservaciones(
            (expediente.getObservaciones() != null ? expediente.getObservaciones() + "\n" : "") +
            "[BAJA] Expediente cerrado el " + LocalDate.now() + " por usuario " + usuarioCierreId
        );

        expediente = expedienteRepository.save(expediente);
        log.info("✅ Expediente {} cerrado exitosamente", id);
        return mapToResponse(expediente);
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        log.info("🗑️ Eliminando expediente laboral: {}", id);
        if (!expedienteRepository.existsById(id)) {
            throw new RuntimeException("Expediente laboral no encontrado");
        }
        expedienteRepository.deleteById(id);
        log.info("✅ Expediente {} eliminado", id);
    }

    // ============================================
    // MÉTODO HELPER: Mapear entidad a DTO
    // ============================================
    private ExpedienteLaboralResponse mapToResponse(ExpedienteLaboral e) {
        String nombreAspirante = "Desconocido";
        String correoAspirante = "Desconocido";

        try {
            Aspirante aspirante = aspiranteRepository.findById(e.getAspiranteId()).orElse(null);
            if (aspirante != null) {
                nombreAspirante = aspirante.getNombreCompleto();
                correoAspirante = aspirante.getCorreoElectronico();
            }
        } catch (Exception ex) {
            log.warn("⚠️ No se pudo obtener datos del aspirante {}: {}", e.getAspiranteId(), ex.getMessage());
        }

        String nombreJunta = e.getJuntaEjecutiva() != null ? e.getJuntaEjecutiva().getNombre() : null;
        String nombreVocalia = e.getVocalia() != null ? e.getVocalia().getNombre() : null;
        UUID vacanteId = e.getVacante() != null ? e.getVacante().getId() : null;
        String nombreVacante = e.getVacante() != null ? e.getVacante().getPuesto() : null;
        var soporte = documentoRepository.findFirstByExpedienteLaboral_IdOrderByFechaCargaDesc(e.getId()).orElse(null);

        return new ExpedienteLaboralResponse(
            e.getId(),
            e.getAspiranteId(),
            nombreAspirante,
            correoAspirante,
            e.getNumeroEmpleado(),
            e.getTipoContratacion(),
            e.getFechaInicio(),
            e.getFechaFin(),
            e.getVigente(),
            e.getAreaAdscripcion(),
            e.getPuestoActual(),
            e.getNivelTabular(),
            vacanteId,
            nombreVacante,
            e.getJuntaEjecutiva() != null ? e.getJuntaEjecutiva().getId() : null,
            nombreJunta,
            e.getVocalia() != null ? e.getVocalia().getId() : null,
            nombreVocalia,
            e.getAltaPorUsuarioId(),
            e.getObservaciones(),
            soporte != null ? soporte.getId() : null,
            soporte != null ? soporte.getNombreArchivo() : null,
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }

    private void validarDatosContraVacante(Vacante vacante, String puestoActual, String nivelTabular) {
        if (!coincide(puestoActual, vacante.getPuesto())) {
            throw new RuntimeException("El puesto actual debe coincidir con el puesto publicado en la vacante: " + vacante.getPuesto());
        }
        if (vacante.getNivelTabular() != null && !vacante.getNivelTabular().isBlank()
                && !coincide(nivelTabular, vacante.getNivelTabular())) {
            throw new RuntimeException("El nivel tabular debe coincidir con el publicado en la vacante: " + vacante.getNivelTabular());
        }
    }

    private boolean coincide(String valorCapturado, String valorPublicado) {
        return valorCapturado != null && valorPublicado != null
                && valorCapturado.trim().equalsIgnoreCase(valorPublicado.trim());
    }
}