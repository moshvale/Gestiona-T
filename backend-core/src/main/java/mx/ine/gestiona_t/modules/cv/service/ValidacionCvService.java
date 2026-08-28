package mx.ine.gestiona_t.modules.cv.service;

import mx.ine.gestiona_t.modules.cv.dto.response.ValidacionCvResponse;
import mx.ine.gestiona_t.modules.cv.model.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidacionCvService {
    
    public ValidacionCvResponse validarCv(CvEstructurado cv) {
        List<String> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();
        List<String> sugerencias = new ArrayList<>();
        
        // Validar escolaridad
        validarEscolaridad(cv.getEscolaridades(), errores, advertencias);
        
        // Validar experiencia
        validarExperiencia(cv.getExperiencias(), errores, advertencias);
        
        // Validar cursos
        validarCursos(cv.getCursos(), errores, advertencias, sugerencias);
        
        // Validar habilidades
        validarHabilidades(cv.getHabilidades(), errores, sugerencias);
        
        // Calcular score
        int score = calcularScoreCompletitud(cv);
        
        boolean completo = errores.isEmpty() && score >= 80;
        
        return new ValidacionCvResponse(
            errores.isEmpty(),
            score,
            completo,
            errores,
            advertencias,
            sugerencias
        );
    }
    
    private void validarEscolaridad(List<Escolaridad> escolaridades, 
                                     List<String> errores, List<String> advertencias) {
        if (escolaridades.isEmpty()) {
            errores.add("Debe registrar al menos un nivel de estudio");
            return;
        }
        
        for (Escolaridad e : escolaridades) {
            if (e.getFechaInicio() != null && e.getFechaTermino() != null && e.getFechaInicio().isAfter(e.getFechaTermino())) {
                errores.add("Fecha de inicio posterior a fecha de término en: " + e.getInstitucion());
            }
            
            if (e.getFechaInicio() == null) {
                advertencias.add("Fecha de inicio faltante en: " + e.getInstitucion());
            }
            
            if (e.getCedulaProfesional() != null && !e.getCedulaProfesional().matches("\\d{7,8}")) {
                advertencias.add("Formato de cédula profesional inválido en: " + e.getTitulo());
            }
        }
    }
    
    private void validarExperiencia(List<ExperienciaLaboral> experiencias, 
                                     List<String> errores, List<String> advertencias) {
        if (experiencias.isEmpty()) {
            advertencias.add("No se ha registrado experiencia laboral");
            return;
        }
        
        for (ExperienciaLaboral exp : experiencias) {
            if (!exp.isActualmenteLaborando() && exp.getFechaTermino() == null) {
                errores.add("Fecha de término requerida para: " + exp.getPuesto());
            }
            
            if (exp.getFechaInicio() != null && exp.getFechaTermino() != null && exp.getFechaInicio().isAfter(exp.getFechaTermino())) {
                errores.add("Fecha de inicio posterior a fecha de término en: " + exp.getPuesto());
            }
            
            if (exp.getFechaInicio() != null) {
                long meses = ChronoUnit.MONTHS.between(exp.getFechaInicio(), 
                    exp.isActualmenteLaborando() ? LocalDate.now() : exp.getFechaTermino());
                
                if (meses < 1) {
                    advertencias.add("Periodo muy corto en: " + exp.getPuesto());
                }
            }
        }
    }
    
    private void validarCursos(List<CursoCapacitacion> cursos, 
                                List<String> errores, List<String> advertencias, 
                                List<String> sugerencias) {
        if (cursos.isEmpty()) {
            sugerencias.add("Se recomienda agregar cursos de capacitación relevantes");
            return;
        }
        
        for (CursoCapacitacion c : cursos) {
            if (c.getDuracionHoras() < 8) {
                advertencias.add("Curso con duración menor a 8 horas: " + c.getNombreCurso());
            }
        }
    }
    
    private void validarHabilidades(List<HabilidadTecnica> habilidades, 
                                     List<String> errores, List<String> sugerencias) {
        if (habilidades.isEmpty()) {
            sugerencias.add("Se recomienda agregar habilidades técnicas e idiomas");
        }
    }
    
    public int calcularScoreCompletitud(CvEstructurado cv) {
        int score = 0;
        
        // Escolaridad (30 puntos)
        if (!cv.getEscolaridades().isEmpty()) {
            score += 30;
        }
        
        // Experiencia (40 puntos)
        if (!cv.getExperiencias().isEmpty()) {
            score += 40;
        }
        
        // Cursos (15 puntos)
        if (!cv.getCursos().isEmpty()) {
            score += 15;
        }
        
        // Habilidades (15 puntos)
        if (!cv.getHabilidades().isEmpty()) {
            score += 15;
        }
        
        return score;
    }
}