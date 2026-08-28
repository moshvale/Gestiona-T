package mx.ine.gestiona_t.modules.cv.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import mx.ine.gestiona_t.modules.auth.model.Aspirante;
import mx.ine.gestiona_t.modules.auth.repository.AspiranteRepository;
import mx.ine.gestiona_t.modules.cv.dto.request.CvInstitucionalRequest;
import mx.ine.gestiona_t.modules.cv.dto.response.CvInstitucionalResponse;
import mx.ine.gestiona_t.modules.cv.model.*;
import mx.ine.gestiona_t.modules.cv.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class CvInstitucionalServiceImpl implements CvInstitucionalService {

    private static final Logger log = LoggerFactory.getLogger(CvInstitucionalServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CvInstitucionalRepository cvRepository;
    private final CvFormacionAcademicaRepository formacionRepository;
    private final CvExperienciaLaboralRepository experienciaRepository;
    private final CvIdiomaRepository idiomaRepository;
    private final CvCursoCapacitacionInstitucionalRepository cursoRepository;
    private final AspiranteRepository aspiranteRepository;

    public CvInstitucionalServiceImpl(CvInstitucionalRepository cvRepository,
                                      CvFormacionAcademicaRepository formacionRepository,
                                      CvExperienciaLaboralRepository experienciaRepository,
                                      CvIdiomaRepository idiomaRepository,
                                      CvCursoCapacitacionInstitucionalRepository cursoRepository,
                                      AspiranteRepository aspiranteRepository) {
        this.cvRepository = cvRepository;
        this.formacionRepository = formacionRepository;
        this.experienciaRepository = experienciaRepository;
        this.idiomaRepository = idiomaRepository;
        this.cursoRepository = cursoRepository;
        this.aspiranteRepository = aspiranteRepository;
    }

    @Override
    @Transactional
    public CvInstitucionalResponse guardarOActualizarCv(UUID aspiranteId, CvInstitucionalRequest request) {
        log.info("Guardando/Actualizando CV para aspirante: {}", aspiranteId);

        CvInstitucional cv = cvRepository.findByAspiranteId(aspiranteId)
                .orElseGet(() -> {
                    CvInstitucional nuevo = new CvInstitucional();
                    nuevo.setAspiranteId(aspiranteId);
                    return nuevo;
                });

        // --- Datos básicos ---
        cv.setEntidadPreferida(request.entidadPreferida());
        cv.setSueldoDeseado(request.sueldoDeseado());
        cv.setDisponibilidad(request.disponibilidad());
        cv.setAreasInteres(request.areasInteres());
        cv.setSistemasOperativos(request.sistemasOperativos());
        cv.setLenguajesProgramacion(request.lenguajesProgramacion());
        cv.setBasesDeDatos(request.basesDeDatos());
        cv.setHabilidades(request.habilidades());
        cv.setLogrosProfesionales(request.logrosProfesionales());

        cv = cvRepository.save(cv);
        UUID cvId = cv.getId();

        // --- Formación Académica ---
        formacionRepository.deleteByCvId(cvId);
        if (request.formacionAcademica() != null) {
            for (CvInstitucionalRequest.FormacionRequest f : request.formacionAcademica()) {
                CvFormacionAcademica formacion = new CvFormacionAcademica();
                formacion.setCv(cv);
                formacion.setNivel(f.nivel());
                formacion.setCarrera(f.carrera());
                formacion.setInstitucion(f.institucion());
                formacion.setFechaInicio(f.fechaInicio());
                formacion.setFechaFin(f.fechaFin());
                formacion.setCedulaProfesional(f.cedulaProfesional());
                formacion.setEstatus(f.estatus());
                formacionRepository.save(formacion);
            }
        }

        // --- Experiencia Laboral ---
        experienciaRepository.deleteByCvId(cvId);
        if (request.experienciaLaboral() != null) {
            for (CvInstitucionalRequest.ExperienciaRequest e : request.experienciaLaboral()) {
                CvExperienciaLaboral exp = new CvExperienciaLaboral();
                exp.setCv(cv);
                exp.setTipoExperiencia(e.tipoExperiencia());
                exp.setEmpresa(e.empresa());
                exp.setInstitucion(e.empresa());
                exp.setPuesto(e.puesto());
                exp.setFunciones(e.funciones());
                exp.setFechaInicio(e.fechaInicio());
                exp.setFechaFin(e.fechaFin());
                exp.setSueldo(e.sueldo());
                exp.setActualmenteLaborando(e.actualmenteLaborando());
                experienciaRepository.save(exp);
            }
        }

        // --- Cursos y Capacitaciones ---
        cursoRepository.deleteByCvId(cvId);
        if (request.cursos() != null) {
            for (CvInstitucionalRequest.CursoRequest c : request.cursos()) {
                CvCursoCapacitacionInstitucional curso = new CvCursoCapacitacionInstitucional();
                curso.setCv(cv);
                curso.setNombreCurso(c.nombreCurso());
                curso.setInstitucion(c.institucion());
                curso.setDuracionHoras(c.duracionHoras());
                curso.setFechaRealizacion(c.fechaRealizacion());
                curso.setDocumentoSoportePath(c.documentoSoportePath());
                cursoRepository.save(curso);
            }
        }

        // --- Idiomas ---
        idiomaRepository.deleteByCvId(cvId);
        if (request.idiomas() != null) {
            for (CvInstitucionalRequest.IdiomaRequest i : request.idiomas()) {
                CvIdioma idioma = new CvIdioma();
                idioma.setCv(cv);
                idioma.setIdioma(i.idioma());
                idioma.setNivelEscritura(i.nivelEscritura());
                idioma.setNivelLectura(i.nivelLectura());
                idioma.setNivelConversacion(i.nivelConversacion());
                idiomaRepository.save(idioma);
            }
        }

        int score = calcularScoreCompletitud(cv, cvId);
        log.info("✅ CV guardado con score de completitud: {}%", score);
        return mapToResponse(cv, cvId, score);
    }

    @Override
    @Transactional(readOnly = true)
    public CvInstitucionalResponse obtenerCvPorAspirante(UUID aspiranteId) {
        CvInstitucional cv = cvRepository.findByAspiranteId(aspiranteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV no encontrado"));
        int score = calcularScoreCompletitud(cv, cv.getId());
        return mapToResponse(cv, cv.getId(), score);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCv(UUID aspiranteId) {
        return cvRepository.existsByAspiranteId(aspiranteId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarPdfCv(UUID aspiranteId) {
        log.info("Generando PDF del CV para aspirante: {}", aspiranteId);
        
        CvInstitucionalResponse cv = obtenerCvPorAspirante(aspiranteId);
        Aspirante aspirante = aspiranteRepository.findById(aspiranteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aspirante no encontrado"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fuentes
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(136, 19, 55)); // Color INE
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(136, 19, 55));
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // Encabezado
            Paragraph title = new Paragraph("CURRÍCULUM VITAE INSTITUCIONAL", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            String nombreAspirante = aspirante.getNombreCompleto() != null ? aspirante.getNombreCompleto().toUpperCase() : "ASPIRANTE";
            Paragraph name = new Paragraph(nombreAspirante, headerFont);
            name.setAlignment(Element.ALIGN_CENTER);
            name.setSpacingAfter(20f);
            document.add(name);

            // 1. Expectativas
            document.add(new Paragraph("1. EXPECTATIVAS Y DATOS GENERALES", subHeaderFont));
            document.add(new Paragraph(" ", normalFont));
            if (cv.entidadPreferida() != null && !cv.entidadPreferida().isBlank()) document.add(new Paragraph("Entidad Preferida: " + cv.entidadPreferida(), normalFont));
            if (cv.disponibilidad() != null && !cv.disponibilidad().isBlank()) document.add(new Paragraph("Disponibilidad: " + cv.disponibilidad(), normalFont));
            if (cv.areasInteres() != null && !cv.areasInteres().isBlank()) document.add(new Paragraph("Áreas de Interés: " + cv.areasInteres(), normalFont));
            if (cv.sueldoDeseado() != null) document.add(new Paragraph("Sueldo Deseado: $" + cv.sueldoDeseado(), normalFont));
            document.add(new Paragraph(" ", normalFont));

            // 2. Formación Académica
            document.add(new Paragraph("2. FORMACIÓN ACADÉMICA", subHeaderFont));
            document.add(new Paragraph(" ", normalFont));
            if (cv.formacionAcademica() != null && !cv.formacionAcademica().isEmpty()) {
                for (var f : cv.formacionAcademica()) {
                    document.add(new Paragraph("• " + f.nivel() + " en " + f.carrera(), boldFont));
                    document.add(new Paragraph("  Institución: " + f.institucion() + " | Estatus: " + f.estatus(), normalFont));
                    String periodo = f.fechaInicio() != null ? f.fechaInicio().format(DATE_FORMATTER) : "";
                    periodo += f.fechaFin() != null ? " a " + f.fechaFin().format(DATE_FORMATTER) : " - Actual";
                    document.add(new Paragraph("  Periodo: " + periodo, normalFont));
                    if (f.cedulaProfesional() != null && !f.cedulaProfesional().isBlank()) {
                        document.add(new Paragraph("  Cédula Profesional: " + f.cedulaProfesional(), normalFont));
                    }
                    document.add(new Paragraph(" ", normalFont));
                }
            } else {
                document.add(new Paragraph("No registra formación académica.", normalFont));
                document.add(new Paragraph(" ", normalFont));
            }

            // 3. Experiencia Laboral
            document.add(new Paragraph("3. EXPERIENCIA LABORAL", subHeaderFont));
            document.add(new Paragraph(" ", normalFont));
            if (cv.experienciaLaboral() != null && !cv.experienciaLaboral().isEmpty()) {
                for (var e : cv.experienciaLaboral()) {
                    document.add(new Paragraph("• " + e.puesto() + " en " + e.empresa(), boldFont));
                    document.add(new Paragraph("  Tipo: " + e.tipoExperiencia() + (e.actualmenteLaborando() ? " (Actualmente laborando)" : ""), normalFont));
                    String periodoExp = e.fechaInicio() != null ? e.fechaInicio().format(DATE_FORMATTER) : "";
                    periodoExp += e.fechaFin() != null ? " a " + e.fechaFin().format(DATE_FORMATTER) : " - Actual";
                    document.add(new Paragraph("  Periodo: " + periodoExp, normalFont));
                    if (e.funciones() != null && !e.funciones().isBlank()) {
                        document.add(new Paragraph("  Funciones: " + e.funciones(), normalFont));
                    }
                    document.add(new Paragraph(" ", normalFont));
                }
            } else {
                document.add(new Paragraph("No registra experiencia laboral.", normalFont));
                document.add(new Paragraph(" ", normalFont));
            }

            // 4. Cursos y Capacitaciones
            document.add(new Paragraph("4. CURSOS Y CAPACITACIONES", subHeaderFont));
            document.add(new Paragraph(" ", normalFont));
            if (cv.cursos() != null && !cv.cursos().isEmpty()) {
                for (var c : cv.cursos()) {
                    document.add(new Paragraph("• " + c.nombreCurso(), boldFont));
                    document.add(new Paragraph("  Institución: " + c.institucion() + " | Duración: " + c.duracionHoras() + " horas", normalFont));
                    String fechaCurso = c.fechaRealizacion() != null ? c.fechaRealizacion().format(DATE_FORMATTER) : "";
                    document.add(new Paragraph("  Fecha: " + fechaCurso, normalFont));
                    document.add(new Paragraph(" ", normalFont));
                }
            } else {
                document.add(new Paragraph("No registra cursos o capacitaciones.", normalFont));
                document.add(new Paragraph(" ", normalFont));
            }

            // 5. Idiomas e Informática
            document.add(new Paragraph("5. IDIOMAS E INFORMÁTICA", subHeaderFont));
            document.add(new Paragraph(" ", normalFont));
            if (cv.idiomas() != null && !cv.idiomas().isEmpty()) {
                document.add(new Paragraph("Idiomas:", boldFont));
                for (var i : cv.idiomas()) {
                    document.add(new Paragraph("  - " + i.idioma() + " (Escritura: " + i.nivelEscritura() + ", Lectura: " + i.nivelLectura() + ", Conversación: " + i.nivelConversacion() + ")", normalFont));
                }
                document.add(new Paragraph(" ", normalFont));
            }
            if (cv.sistemasOperativos() != null && !cv.sistemasOperativos().isBlank()) document.add(new Paragraph("Sistemas Operativos: " + cv.sistemasOperativos(), normalFont));
            if (cv.lenguajesProgramacion() != null && !cv.lenguajesProgramacion().isBlank()) document.add(new Paragraph("Lenguajes de Programación: " + cv.lenguajesProgramacion(), normalFont));
            if (cv.basesDeDatos() != null && !cv.basesDeDatos().isBlank()) document.add(new Paragraph("Bases de Datos: " + cv.basesDeDatos(), normalFont));
            document.add(new Paragraph(" ", normalFont));

            // 6. Habilidades y Logros
            document.add(new Paragraph("6. HABILIDADES Y LOGROS PROFESIONALES", subHeaderFont));
            document.add(new Paragraph(" ", normalFont));
            if (cv.habilidades() != null && !cv.habilidades().isBlank()) document.add(new Paragraph("Habilidades: " + cv.habilidades(), normalFont));
            if (cv.logrosProfesionales() != null && !cv.logrosProfesionales().isBlank()) document.add(new Paragraph("Logros: " + cv.logrosProfesionales(), normalFont));

            document.close();
            log.info("✅ PDF generado exitosamente");
            return out.toByteArray();

        } catch (DocumentException e) {
            log.error("Error al generar el PDF", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al generar el documento PDF");
        }
    }

    private int calcularScoreCompletitud(CvInstitucional cv, UUID cvId) {
        int score = 0;

        if (!formacionRepository.findByCvId(cvId).isEmpty()) score += 15;
        if (!experienciaRepository.findByCvId(cvId).isEmpty()) score += 20;
        if (!cursoRepository.findByCvId(cvId).isEmpty()) score += 15;
        if (!idiomaRepository.findByCvId(cvId).isEmpty()) score += 10;

        boolean tieneInfo = (cv.getSistemasOperativos() != null && !cv.getSistemasOperativos().isBlank())
                || (cv.getLenguajesProgramacion() != null && !cv.getLenguajesProgramacion().isBlank())
                || (cv.getBasesDeDatos() != null && !cv.getBasesDeDatos().isBlank());
        if (tieneInfo) score += 10;

        boolean tieneHabilidades = (cv.getHabilidades() != null && !cv.getHabilidades().isBlank())
                || (cv.getLogrosProfesionales() != null && !cv.getLogrosProfesionales().isBlank());
        if (tieneHabilidades) score += 10;

        boolean tieneExpectativas = (cv.getEntidadPreferida() != null && !cv.getEntidadPreferida().isBlank())
                || (cv.getDisponibilidad() != null && !cv.getDisponibilidad().isBlank())
                || (cv.getAreasInteres() != null && !cv.getAreasInteres().isBlank());
        if (tieneExpectativas) score += 10;

        if (cv.getSueldoDeseado() != null) score += 10;

        return Math.min(score, 100);
    }

    private CvInstitucionalResponse mapToResponse(CvInstitucional cv, UUID cvId, int score) {
        List<CvInstitucionalResponse.FormacionResponse> formaciones = formacionRepository.findByCvId(cvId)
                .stream().map(f -> new CvInstitucionalResponse.FormacionResponse(
                        f.getId(), f.getNivel(), f.getCarrera(), f.getInstitucion(),
                        f.getFechaInicio(), f.getFechaFin(), f.getCedulaProfesional(), f.getEstatus()
                )).toList();

        List<CvInstitucionalResponse.ExperienciaResponse> experiencias = experienciaRepository.findByCvId(cvId)
                .stream().map(e -> new CvInstitucionalResponse.ExperienciaResponse(
                        e.getId(), e.getTipoExperiencia(), e.getEmpresa(), e.getPuesto(),
                        e.getFunciones(), e.getFechaInicio(), e.getFechaFin(), e.getSueldo(), e.isActualmenteLaborando()
                )).toList();

        List<CvInstitucionalResponse.CursoResponse> cursos = cursoRepository.findByCvId(cvId)
                .stream().map(c -> new CvInstitucionalResponse.CursoResponse(
                        c.getId(), c.getNombreCurso(), c.getInstitucion(),
                        c.getDuracionHoras(), c.getFechaRealizacion(), c.getDocumentoSoportePath()
                )).toList();

        List<CvInstitucionalResponse.IdiomaResponse> idiomas = idiomaRepository.findByCvId(cvId)
                .stream().map(i -> new CvInstitucionalResponse.IdiomaResponse(
                        i.getId(), i.getIdioma(), i.getNivelEscritura(), i.getNivelLectura(), i.getNivelConversacion()
                )).toList();

        return new CvInstitucionalResponse(
                cv.getId(), cv.getAspiranteId(),
                cv.getEntidadPreferida(), cv.getSueldoDeseado(), cv.getDisponibilidad(), cv.getAreasInteres(),
                cv.getSistemasOperativos(), cv.getLenguajesProgramacion(), cv.getBasesDeDatos(),
                cv.getHabilidades(), cv.getLogrosProfesionales(),
                score, cv.getCreatedAt(), cv.getUpdatedAt(),
                formaciones, experiencias, idiomas, cursos
        );
    }
}