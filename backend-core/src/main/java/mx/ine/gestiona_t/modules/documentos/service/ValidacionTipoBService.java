package mx.ine.gestiona_t.modules.documentos.service;

import mx.ine.gestiona_t.modules.documentos.integration.DocumentosBackendAIClient;
import mx.ine.gestiona_t.modules.documentos.integration.dto.AutenticidadResponse;
import mx.ine.gestiona_t.modules.documentos.model.CatalogoInstitucion;
import mx.ine.gestiona_t.modules.documentos.model.Documento;
import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusDocumento;
import mx.ine.gestiona_t.modules.documentos.repository.CatalogoInstitucionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ValidacionTipoBService {
    private static final Logger log = LoggerFactory.getLogger(ValidacionTipoBService.class);

    @Value("${documentos.autenticidad.threshold:85}")
    private int autenticidadThreshold;

    private final DocumentosBackendAIClient aiClient;
    private final CatalogoInstitucionRepository catalogoRepository;

    public ValidacionTipoBService(DocumentosBackendAIClient aiClient, CatalogoInstitucionRepository catalogoRepository) {
        this.aiClient = aiClient;
        this.catalogoRepository = catalogoRepository;
    }

    // ✅ CAMBIO CLAVE: Recibe el archivo en memoria, evita descarga duplicada de MinIO
    public Mono<Map<String, Object>> validar(Documento documento, byte[] archivo) {
        log.info("Validacion Tipo B para documento: {}", documento.getId());
        
        return aiClient.validarAutenticidad(archivo, documento.getNombreArchivo())
            .map(respuesta -> procesarRespuesta(documento, respuesta))
            .onErrorResume(e -> {
                log.error("Error al conectar con Backend-AI para autenticidad: {}", e.getMessage());
                documento.setEstatus(EstatusDocumento.EN_REVISION_MANUAL);
                documento.setMotivoRechazo("Error de conexion con servicio de IA. Requiere revision manual.");
                return crearResultadoRevisionManual("Error de conexion con servicio de IA.");
            });
    }

    private Map<String, Object> procesarRespuesta(Documento documento, AutenticidadResponse respuesta) {
        Map<String, Object> resultado = new HashMap<>();
        documento.setScoreAutenticidad(respuesta.scoreAutenticidad());

        if (respuesta.scoreAutenticidad() >= autenticidadThreshold && !respuesta.sospechoso()) {
            String institucionDetectada = detectarInstitucion(documento.getTextoExtraido());
            if (institucionDetectada != null && existeEnCatalogo(institucionDetectada)) {
                documento.setEstatus(EstatusDocumento.VALIDADO_AUTOMATICO);
                documento.setFechaValidacion(LocalDateTime.now());
                resultado.put("exitoso", true);
                resultado.put("mensaje", "Documento validado con alta confianza");
            } else {
                documento.setEstatus(EstatusDocumento.EN_REVISION_MANUAL);
                String motivo = institucionDetectada != null 
                    ? "Institucion '" + institucionDetectada + "' no encontrada en catalogo oficial."
                    : "No se pudo detectar una institucion oficial en el texto.";
                documento.setMotivoRechazo(motivo + " Requiere revision manual.");
                resultado.put("exitoso", false);
                resultado.put("mensaje", motivo + " Requiere revision manual.");
            }
        } else if (respuesta.scoreAutenticidad() >= 60) {
            documento.setEstatus(EstatusDocumento.EN_REVISION_MANUAL);
            documento.setMotivoRechazo("Score de autenticidad intermedio (" + respuesta.scoreAutenticidad() + "%). Requiere revision manual.");
            resultado.put("exitoso", false);
            resultado.put("mensaje", "Score intermedio. Requiere revision manual.");
        } else {
            documento.setEstatus(EstatusDocumento.EN_REVISION_MANUAL);
            documento.setMotivoRechazo("Score de autenticidad bajo (" + respuesta.scoreAutenticidad() + "%) o documento sospechoso. Requiere revision manual.");
            resultado.put("exitoso", false);
            resultado.put("mensaje", "Documento con score bajo o sospechoso. Revisión manual requerida.");
        }
        
        resultado.put("metodo", "IA_AUTENTICIDAD");
        resultado.put("score", respuesta.scoreAutenticidad());
        return resultado;
    }

    private Mono<Map<String, Object>> crearResultadoRevisionManual(String mensaje) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("exitoso", false);
        resultado.put("mensaje", mensaje);
        resultado.put("metodo", "REVISION_MANUAL");
        return Mono.just(resultado);
    }

    private String detectarInstitucion(String texto) {
        if (texto == null) return null;
        List<CatalogoInstitucion> instituciones = catalogoRepository.findAll();
        for (CatalogoInstitucion inst : instituciones) {
            if (texto.toLowerCase().contains(inst.getNombre().toLowerCase())) {
                return inst.getNombre();
            }
        }
        return null;
    }

    private boolean existeEnCatalogo(String nombreInstitucion) {
        return !catalogoRepository.buscarPorNombre(nombreInstitucion).isEmpty();
    }
}