package mx.ine.gestiona_t.modules.documentos.service;

import mx.ine.gestiona_t.modules.documentos.integration.RnpClient;
import mx.ine.gestiona_t.modules.documentos.integration.SatClient;
import mx.ine.gestiona_t.modules.documentos.model.Documento;
import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusDocumento;
import mx.ine.gestiona_t.modules.documentos.model.enums.TipoDocumento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ValidacionTipoAService {
    private static final Logger log = LoggerFactory.getLogger(ValidacionTipoAService.class);

    private final RnpClient rnpClient;
    private final SatClient satClient;

    public ValidacionTipoAService(RnpClient rnpClient, SatClient satClient) {
        this.rnpClient = rnpClient;
        this.satClient = satClient;
    }

    // ✅ CAMBIO CLAVE: Se agrega byte[] archivo para uniformidad con la interfaz, aunque no se use aquí.
    public Mono<Map<String, Object>> validar(Documento documento, byte[] archivo) {
        log.info("Validacion Tipo A para documento: {} - Tipo: {}", documento.getId(), documento.getTipoDocumento());
        return switch (documento.getTipoDocumento()) {
            case CEDULA_PROFESIONAL -> validarCedula(documento);
            case RFC, COMPROBANTE_SITUACION_FISCAL -> validarRfc(documento);
            default -> {
                documento.setEstatus(EstatusDocumento.EN_REVISION_MANUAL);
                documento.setMotivoRechazo("Tipo de documento no soportado en validacion Tipo A. Requiere revision manual.");
                yield Mono.just(crearMapaRevision("Tipo de documento no soportado en validacion Tipo A."));
            }
        };
    }

    private Mono<Map<String, Object>> validarCedula(Documento documento) {
        String texto = documento.getTextoExtraido();
        if (texto == null || texto.isEmpty()) {
            return Mono.just(crearMapaRevision("Texto no extraido por OCR. Requiere revision manual."));
        }
        
        String cedula = extraerCedula(texto);
        if (cedula == null) {
            return Mono.just(crearMapaRevision("No se pudo extraer numero de cedula. Requiere revision manual."));
        }

        return rnpClient.validarCedula(cedula, "Aspirante")
            .map(r -> {
                Map<String, Object> resultado = new HashMap<>();
                if (r != null && r.valido()) {
                    documento.setEstatus(EstatusDocumento.VALIDADO_AUTOMATICO);
                    documento.setFechaValidacion(LocalDateTime.now());
                    resultado.put("exitoso", true);
                    resultado.put("mensaje", "Cedula validada en RNP");
                    resultado.put("metodo", "API_RNP");
                    resultado.put("datos", r);
                } else {
                    documento.setEstatus(EstatusDocumento.EN_REVISION_MANUAL);
                    String msg = (r != null) ? r.mensaje() : "No se pudo validar en RNP";
                    documento.setMotivoRechazo(msg + ". Requiere revision manual.");
                    resultado.put("exitoso", false);
                    resultado.put("mensaje", msg + ". Requiere revision manual.");
                    resultado.put("metodo", "REVISION_MANUAL");
                }
                return resultado;
            })
            .onErrorResume(e -> {
                log.error("Error de conexion con RNP para cedula {}: {}", cedula, e.getMessage());
                documento.setEstatus(EstatusDocumento.EN_REVISION_MANUAL);
                documento.setMotivoRechazo("Error de conexion con RNP. Requiere revision manual.");
                return Mono.just(crearMapaRevision("Error de conexion con RNP. Requiere revision manual."));
            });
    }

    private Mono<Map<String, Object>> validarRfc(Documento documento) {
        String texto = documento.getTextoExtraido();
        if (texto == null || texto.isEmpty()) {
            return Mono.just(crearMapaRevision("Texto no extraido por OCR. Requiere revision manual."));
        }
        
        String rfc = extraerRfc(texto);
        if (rfc == null) {
            return Mono.just(crearMapaRevision("No se pudo extraer RFC. Requiere revision manual."));
        }

        return satClient.validarRfc(rfc)
            .map(r -> {
                Map<String, Object> resultado = new HashMap<>();
                if (r != null && r.valido()) {
                    documento.setEstatus(EstatusDocumento.VALIDADO_AUTOMATICO);
                    documento.setFechaValidacion(LocalDateTime.now());
                    resultado.put("exitoso", true);
                    resultado.put("mensaje", "RFC validado en SAT");
                    resultado.put("metodo", "API_SAT");
                } else {
                    documento.setEstatus(EstatusDocumento.EN_REVISION_MANUAL);
                    String msg = (r != null) ? r.mensaje() : "No se pudo validar en SAT";
                    documento.setMotivoRechazo(msg + ". Requiere revision manual.");
                    resultado.put("exitoso", false);
                    resultado.put("mensaje", msg + ". Requiere revision manual.");
                    resultado.put("metodo", "REVISION_MANUAL");
                }
                return resultado;
            })
            .onErrorResume(e -> {
                log.error("Error de conexion con SAT para RFC {}: {}", rfc, e.getMessage());
                documento.setEstatus(EstatusDocumento.EN_REVISION_MANUAL);
                documento.setMotivoRechazo("Error de conexion con SAT. Requiere revision manual.");
                return Mono.just(crearMapaRevision("Error de conexion con SAT. Requiere revision manual."));
            });
    }

    private Map<String, Object> crearMapaRevision(String mensaje) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("exitoso", false);
        resultado.put("mensaje", mensaje);
        resultado.put("metodo", "REVISION_MANUAL");
        return resultado;
    }

    private String extraerCedula(String texto) {
        Pattern p = Pattern.compile("\\b\\d{7,8}\\b");
        Matcher m = p.matcher(texto);
        return m.find() ? m.group() : null;
    }

    private String extraerRfc(String texto) {
        Pattern p = Pattern.compile("\\b[A-Z&Ñ]{3,4}\\d{6}[A-Z0-9]{3}\\b");
        Matcher m = p.matcher(texto);
        return m.find() ? m.group() : null;
    }
}