package mx.ine.gestiona_t.modules.cv.service;

import mx.ine.gestiona_t.modules.cv.dto.request.*;
import mx.ine.gestiona_t.modules.cv.dto.response.*;
import mx.ine.gestiona_t.modules.cv.model.*;
import mx.ine.gestiona_t.modules.cv.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CvServiceImpl implements CvService {
    
    private static final Logger log = LoggerFactory.getLogger(CvServiceImpl.class);
    
    private final CvEstructuradoRepository cvRepository;
    private final EscolaridadRepository escolaridadRepository;
    private final ExperienciaLaboralRepository experienciaRepository;
    private final CursoCapacitacionRepository cursoRepository;
    private final HabilidadTecnicaRepository habilidadRepository;
    private final ValidacionCvService validacionService;
    private final MinioService minioService;
    
    public CvServiceImpl(CvEstructuradoRepository cvRepository,
                         EscolaridadRepository escolaridadRepository,
                         ExperienciaLaboralRepository experienciaRepository,
                         CursoCapacitacionRepository cursoRepository,
                         HabilidadTecnicaRepository habilidadRepository,
                         ValidacionCvService validacionService,
                         MinioService minioService) {
        this.cvRepository = cvRepository;
        this.escolaridadRepository = escolaridadRepository;
        this.experienciaRepository = experienciaRepository;
        this.cursoRepository = cursoRepository;
        this.habilidadRepository = habilidadRepository;
        this.validacionService = validacionService;
        this.minioService = minioService;
    }
    
    @Override
    @Transactional
    public Mono<CvResponse> crearCv(UUID aspiranteId, String folio) {
        log.info("Creando CV para aspirante: {}", aspiranteId);
        
        if (cvRepository.existsByAspiranteId(aspiranteId)) {
            return Mono.error(new RuntimeException("El aspirante ya tiene un CV registrado"));
        }
        
        CvEstructurado cv = new CvEstructurado();
        cv.setAspiranteId(aspiranteId);
        cv.setFolio(folio);
        cv.setScoreCompletitud(0);
        cv.setCompleto(false);
        cv.setMetodoCaptura("ESTRUCTURADO");
        
        cv = cvRepository.save(cv);
        
        return Mono.just(mapToResponse(cv));
    }
    
    @Override
    public Mono<CvCompletoResponse> obtenerCvCompleto(String folio) {
        log.info("Obteniendo CV completo: {}", folio);
        
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        CvResponse cvResponse = mapToResponse(cv);
        
        List<EscolaridadResponse> escolaridades = cv.getEscolaridades().stream()
            .map(this::mapToEscolaridadResponse)
            .collect(Collectors.toList());
        
        List<ExperienciaResponse> experiencias = cv.getExperiencias().stream()
            .map(this::mapToExperienciaResponse)
            .collect(Collectors.toList());
        
        List<CursoResponse> cursos = cv.getCursos().stream()
            .map(this::mapToCursoResponse)
            .collect(Collectors.toList());
        
        List<HabilidadResponse> habilidades = cv.getHabilidades().stream()
            .map(this::mapToHabilidadResponse)
            .collect(Collectors.toList());
        
        return Mono.just(new CvCompletoResponse(
            cvResponse, escolaridades, experiencias, cursos, habilidades
        ));
    }
    
    @Override
    @Transactional
    public Mono<CvResponse> actualizarCv(String folio) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        int score = validacionService.calcularScoreCompletitud(cv);
        cv.setScoreCompletitud(score);
        cv.setCompleto(score >= 80);
        
        cv = cvRepository.save(cv);
        
        return Mono.just(mapToResponse(cv));
    }
    
    @Override
    @Transactional
    public Mono<EscolaridadResponse> agregarEscolaridad(String folio, EscolaridadRequest request) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        Escolaridad escolaridad = new Escolaridad();
        escolaridad.setCv(cv);
        escolaridad.setNivel(request.nivel());
        escolaridad.setInstitucion(request.institucion());
        escolaridad.setTitulo(request.titulo());
        escolaridad.setCedulaProfesional(request.cedulaProfesional());
        escolaridad.setFechaInicio(request.fechaInicio());
        escolaridad.setFechaTermino(request.fechaTermino());
        escolaridad.setStatus(request.status());
        
        escolaridad = escolaridadRepository.save(escolaridad);
        
        actualizarScoreCv(cv);
        
        return Mono.just(mapToEscolaridadResponse(escolaridad));
    }
    
    @Override
    @Transactional
    public Mono<EscolaridadResponse> actualizarEscolaridad(String folio, UUID id, EscolaridadRequest request) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        Escolaridad escolaridad = escolaridadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Escolaridad no encontrada"));
        
        if (!escolaridad.getCv().getId().equals(cv.getId())) {
            return Mono.error(new RuntimeException("La escolaridad no pertenece a este CV"));
        }
        
        escolaridad.setNivel(request.nivel());
        escolaridad.setInstitucion(request.institucion());
        escolaridad.setTitulo(request.titulo());
        escolaridad.setCedulaProfesional(request.cedulaProfesional());
        escolaridad.setFechaInicio(request.fechaInicio());
        escolaridad.setFechaTermino(request.fechaTermino());
        escolaridad.setStatus(request.status());
        
        escolaridad = escolaridadRepository.save(escolaridad);
        
        actualizarScoreCv(cv);
        
        return Mono.just(mapToEscolaridadResponse(escolaridad));
    }
    
    @Override
    @Transactional
    public Mono<Void> eliminarEscolaridad(String folio, UUID id) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        Escolaridad escolaridad = escolaridadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Escolaridad no encontrada"));
        
        if (!escolaridad.getCv().getId().equals(cv.getId())) {
            return Mono.error(new RuntimeException("La escolaridad no pertenece a este CV"));
        }
        
        escolaridadRepository.delete(escolaridad);
        
        actualizarScoreCv(cv);
        
        return Mono.empty();
    }
    
    @Override
    @Transactional
    public Mono<ExperienciaResponse> agregarExperiencia(String folio, ExperienciaRequest request) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        ExperienciaLaboral experiencia = new ExperienciaLaboral();
        experiencia.setCv(cv);
        experiencia.setInstitucion(request.institucion());
        experiencia.setRfcInstitucion(request.rfcInstitucion());
        experiencia.setPuesto(request.puesto());
        experiencia.setFunciones(request.funciones());
        experiencia.setFechaInicio(request.fechaInicio());
        experiencia.setFechaTermino(request.fechaTermino());
        experiencia.setActualmenteLaborando(request.actualmenteLaborando());
        experiencia.setNivelMando(request.nivelMando());
        
        experiencia = experienciaRepository.save(experiencia);
        
        actualizarScoreCv(cv);
        
        return Mono.just(mapToExperienciaResponse(experiencia));
    }
    
    @Override
    @Transactional
    public Mono<ExperienciaResponse> actualizarExperiencia(String folio, UUID id, ExperienciaRequest request) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        ExperienciaLaboral experiencia = experienciaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Experiencia no encontrada"));
        
        if (!experiencia.getCv().getId().equals(cv.getId())) {
            return Mono.error(new RuntimeException("La experiencia no pertenece a este CV"));
        }
        
        experiencia.setInstitucion(request.institucion());
        experiencia.setRfcInstitucion(request.rfcInstitucion());
        experiencia.setPuesto(request.puesto());
        experiencia.setFunciones(request.funciones());
        experiencia.setFechaInicio(request.fechaInicio());
        experiencia.setFechaTermino(request.fechaTermino());
        experiencia.setActualmenteLaborando(request.actualmenteLaborando());
        experiencia.setNivelMando(request.nivelMando());
        
        experiencia = experienciaRepository.save(experiencia);
        
        actualizarScoreCv(cv);
        
        return Mono.just(mapToExperienciaResponse(experiencia));
    }
    
    @Override
    @Transactional
    public Mono<Void> eliminarExperiencia(String folio, UUID id) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        ExperienciaLaboral experiencia = experienciaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Experiencia no encontrada"));
        
        if (!experiencia.getCv().getId().equals(cv.getId())) {
            return Mono.error(new RuntimeException("La experiencia no pertenece a este CV"));
        }
        
        experienciaRepository.delete(experiencia);
        
        actualizarScoreCv(cv);
        
        return Mono.empty();
    }
    
    @Override
    @Transactional
    public Mono<CursoResponse> agregarCurso(String folio, CursoRequest request) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        CursoCapacitacion curso = new CursoCapacitacion();
        curso.setCv(cv);
        curso.setNombreCurso(request.nombreCurso());
        curso.setInstitucion(request.institucion());
        curso.setDuracionHoras(request.duracionHoras());
        curso.setFechaRealizacion(request.fechaRealizacion());
        
        curso = cursoRepository.save(curso);
        
        actualizarScoreCv(cv);
        
        return Mono.just(mapToCursoResponse(curso));
    }
    
    @Override
    @Transactional
    public Mono<CursoResponse> actualizarCurso(String folio, UUID id, CursoRequest request) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        CursoCapacitacion curso = cursoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        
        if (!curso.getCv().getId().equals(cv.getId())) {
            return Mono.error(new RuntimeException("El curso no pertenece a este CV"));
        }
        
        curso.setNombreCurso(request.nombreCurso());
        curso.setInstitucion(request.institucion());
        curso.setDuracionHoras(request.duracionHoras());
        curso.setFechaRealizacion(request.fechaRealizacion());
        
        curso = cursoRepository.save(curso);
        
        actualizarScoreCv(cv);
        
        return Mono.just(mapToCursoResponse(curso));
    }
    
    @Override
    @Transactional
    public Mono<Void> eliminarCurso(String folio, UUID id) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        CursoCapacitacion curso = cursoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        
        if (!curso.getCv().getId().equals(cv.getId())) {
            return Mono.error(new RuntimeException("El curso no pertenece a este CV"));
        }
        
        cursoRepository.delete(curso);
        
        actualizarScoreCv(cv);
        
        return Mono.empty();
    }
    
    @Override
    @Transactional
    public Mono<HabilidadResponse> agregarHabilidad(String folio, HabilidadRequest request) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        HabilidadTecnica habilidad = new HabilidadTecnica();
        habilidad.setCv(cv);
        habilidad.setTipo(request.tipo());
        habilidad.setNombre(request.nombre());
        habilidad.setNivel(request.nivel());
        habilidad.setFechaCertificacion(request.fechaCertificacion());
        habilidad.setFechaVencimiento(request.fechaVencimiento());
        
        habilidad = habilidadRepository.save(habilidad);
        
        actualizarScoreCv(cv);
        
        return Mono.just(mapToHabilidadResponse(habilidad));
    }
    
    @Override
    @Transactional
    public Mono<HabilidadResponse> actualizarHabilidad(String folio, UUID id, HabilidadRequest request) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        HabilidadTecnica habilidad = habilidadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Habilidad no encontrada"));
        
        if (!habilidad.getCv().getId().equals(cv.getId())) {
            return Mono.error(new RuntimeException("La habilidad no pertenece a este CV"));
        }
        
        habilidad.setTipo(request.tipo());
        habilidad.setNombre(request.nombre());
        habilidad.setNivel(request.nivel());
        habilidad.setFechaCertificacion(request.fechaCertificacion());
        habilidad.setFechaVencimiento(request.fechaVencimiento());
        
        habilidad = habilidadRepository.save(habilidad);
        
        actualizarScoreCv(cv);
        
        return Mono.just(mapToHabilidadResponse(habilidad));
    }
    
    @Override
    @Transactional
    public Mono<Void> eliminarHabilidad(String folio, UUID id) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        HabilidadTecnica habilidad = habilidadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Habilidad no encontrada"));
        
        if (!habilidad.getCv().getId().equals(cv.getId())) {
            return Mono.error(new RuntimeException("La habilidad no pertenece a este CV"));
        }
        
        habilidadRepository.delete(habilidad);
        
        actualizarScoreCv(cv);
        
        return Mono.empty();
    }
    
    @Override
    public Mono<CvUploadResponse> subirCvNoEstructurado(MultipartFile file, UUID aspiranteId) {
        log.info("Subiendo CV no estructurado para aspirante: {}", aspiranteId);
        
        try {
            String fileName = aspiranteId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String objectName = minioService.uploadFile(file, "cv-no-estructurados", fileName);
            
            return Mono.just(new CvUploadResponse(
                objectName,
                file.getOriginalFilename(),
                file.getSize(),
                "UPLOAD_EXITOSO",
                "Archivo subido exitosamente. Pendiente de procesamiento."
            ));
            
        } catch (Exception e) {
            log.error("Error al subir CV: {}", e.getMessage());
            return Mono.error(new RuntimeException("Error al subir archivo"));
        }
    }
    
    @Override
    public Mono<ValidacionCvResponse> validarCv(String folio) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        return Mono.just(validacionService.validarCv(cv));
    }
    
    @Override
    public Mono<ScoreCompletitudResponse> obtenerScoreCompletitud(String folio) {
        CvEstructurado cv = cvRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("CV no encontrado"));
        
        int scoreTotal = validacionService.calcularScoreCompletitud(cv);
        
        return Mono.just(new ScoreCompletitudResponse(
            scoreTotal,
            cv.getEscolaridades().isEmpty() ? 0 : 30,
            cv.getExperiencias().isEmpty() ? 0 : 40,
            cv.getCursos().isEmpty() ? 0 : 15,
            cv.getHabilidades().isEmpty() ? 0 : 15,
            scoreTotal >= 80,
            obtenerMensajeScore(scoreTotal)
        ));
    }
    
    private void actualizarScoreCv(CvEstructurado cv) {
        int score = validacionService.calcularScoreCompletitud(cv);
        cv.setScoreCompletitud(score);
        cv.setCompleto(score >= 80);
        cvRepository.save(cv);
    }
    
    private String obtenerMensajeScore(int score) {
        if (score >= 80) return "CV completo y listo para evaluación";
        if (score >= 50) return "CV parcialmente completo. Complete las secciones faltantes.";
        return "CV incompleto. Agregue más información.";
    }
    
    private CvResponse mapToResponse(CvEstructurado cv) {
        return new CvResponse(
            cv.getId(),
            cv.getFolio(),
            cv.getAspiranteId(),
            cv.getScoreCompletitud(),
            cv.isCompleto(),
            cv.getMetodoCaptura(),
            cv.getFechaCaptura(),
            cv.getFechaUltimaModificacion()
        );
    }
    
    private EscolaridadResponse mapToEscolaridadResponse(Escolaridad e) {
        return new EscolaridadResponse(
            e.getId(), e.getNivel(), e.getInstitucion(), e.getTitulo(),
            e.getCedulaProfesional(), e.getFechaInicio(), e.getFechaTermino(),
            e.getStatus(), e.getDocumentoSoportePath()
        );
    }
    
    private ExperienciaResponse mapToExperienciaResponse(ExperienciaLaboral exp) {
        return new ExperienciaResponse(
            exp.getId(), exp.getInstitucion(), exp.getRfcInstitucion(),
            exp.getPuesto(), exp.getFunciones(), exp.getFechaInicio(),
            exp.getFechaTermino(), exp.isActualmenteLaborando(),
            exp.getNivelMando(), exp.getDocumentoSoportePath()
        );
    }
    
    private CursoResponse mapToCursoResponse(CursoCapacitacion c) {
        return new CursoResponse(
            c.getId(), c.getNombreCurso(), c.getInstitucion(),
            c.getDuracionHoras(), c.getFechaRealizacion(), c.getDocumentoSoportePath()
        );
    }
    
    private HabilidadResponse mapToHabilidadResponse(HabilidadTecnica h) {
        return new HabilidadResponse(
            h.getId(), h.getTipo(), h.getNombre(), h.getNivel(),
            h.getFechaCertificacion(), h.getFechaVencimiento()
        );
    }
}