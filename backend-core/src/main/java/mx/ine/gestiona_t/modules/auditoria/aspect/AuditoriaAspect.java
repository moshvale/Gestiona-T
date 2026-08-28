package mx.ine.gestiona_t.modules.auditoria.aspect;

import jakarta.servlet.http.HttpServletRequest;
import mx.ine.gestiona_t.modules.auditoria.annotation.Auditable;
import mx.ine.gestiona_t.modules.auditoria.dto.request.PublicarEventoRequest;
import mx.ine.gestiona_t.modules.auditoria.model.enums.ActorTipo;
import mx.ine.gestiona_t.modules.auditoria.model.enums.CategoriaEvento;
import mx.ine.gestiona_t.modules.auditoria.model.enums.NivelSeveridad;
import mx.ine.gestiona_t.modules.auditoria.model.enums.TipoEvento;
import mx.ine.gestiona_t.modules.auditoria.service.AuditoriaEventPublisher;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
public class AuditoriaAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaAspect.class);
    private final AuditoriaEventPublisher eventPublisher;

    public AuditoriaAspect(AuditoriaEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Around("@annotation(auditable)")
    public Object auditarOperacion(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long inicio = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.debug("🔍 Interceptando método auditado: {}.{}", className, methodName);

        Object resultado = null;
        boolean exito = true;
        String errorMensaje = null;

        try {
            resultado = joinPoint.proceed();
            return resultado;
        } catch (Throwable t) {
            exito = false;
            errorMensaje = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
            log.warn("⚠️ Método auditado falló: {}.{} - Error: {}", className, methodName, errorMensaje);
            throw t;
        } finally {
            long duracion = System.currentTimeMillis() - inicio;
            try {
                publicarEvento(joinPoint, auditable, exito, errorMensaje, duracion, resultado);
            } catch (Exception e) {
                log.error("❌ Error publicando evento de auditoría para {}.{}: {}", className, methodName, e.getMessage());
            }
        }
    }

    private void publicarEvento(ProceedingJoinPoint joinPoint, Auditable auditable,
                                 boolean exito, String errorMensaje, long duracion, Object resultado) {
        
        HttpServletRequest request = obtenerRequest();
        String ip = request != null ? getClientIp(request) : "DESCONOCIDA";
        String userAgent = request != null ? request.getHeader("User-Agent") : "DESCONOCIDO";
        
        UUID actorId = extraerActorId();
        ActorTipo actorTipo = determinarActorTipo();

        Map<String, Object> datos = new HashMap<>();
        datos.put("metodo", joinPoint.getSignature().getName());
        datos.put("clase", joinPoint.getTarget().getClass().getSimpleName());
        datos.put("exito", exito);
        datos.put("duracion_ms", duracion);
        datos.put("ip_origen", ip);
        
        if (errorMensaje != null) {
            datos.put("error", errorMensaje);
        }

        UUID idEntidad = extraerIdEntidad(joinPoint.getArgs());
        if (idEntidad != null) {
            datos.put("entidadId", idEntidad);
        }

        String descripcion = auditable.descripcion().isEmpty()
                ? String.format("%s ejecutado con %s en %dms", 
                    joinPoint.getSignature().getName(), exito ? "éxito" : "FALLO", duracion)
                : auditable.descripcion();

        NivelSeveridad severidad = exito ? auditable.severidad() : NivelSeveridad.ERROR;
        String moduloOrigen = request != null ? extraerModuloOrigen(request) : null;
        String correlationId = request != null ? extraerCorrelationId(request) : null;
        CategoriaEvento categoriaEvento = extraerCategoriaEvento(auditable.categoria());
        TipoEvento tipoEvento = extraerTipoEvento(auditable.tipo());

        PublicarEventoRequest evento = new PublicarEventoRequest(
                categoriaEvento,
                tipoEvento,
                severidad,
                actorId,
                actorTipo,
                auditable.recurso(),
                descripcion,
                datos,
                moduloOrigen,
                correlationId
        );

        eventPublisher.publicarAsincrono(evento, ip, userAgent);

        log.info("✅ Evento de auditoría publicado: {} | {} | Actor: {} | Duración: {}ms | Éxito: {}",
                auditable.categoria(), auditable.tipo(), actorId, duracion, exito);
    }

    private HttpServletRequest obtenerRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private UUID extraerActorId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() != null && !"anonymousUser".equals(auth.getPrincipal())) {
                String principalStr = auth.getPrincipal().toString();
                try {
                    return UUID.fromString(principalStr);
                } catch (IllegalArgumentException e) {
                    return null; 
                }
            }
        } catch (Exception e) {
            log.debug("No se pudo extraer actorId del contexto de seguridad");
        }
        return null;
    }

    private ActorTipo determinarActorTipo() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getAuthorities() != null) {
                String roles = auth.getAuthorities().toString();
                if (roles.contains("ADMIN")) return ActorTipo.ADMIN_SISTEMA;
                if (roles.contains("ANALISTA")) return ActorTipo.ANALISTA_UR;
                if (roles.contains("CONTRALORIA")) return ActorTipo.CONTRALORIA;
            }
        } catch (Exception e) {
            // Ignorar
        }
        return ActorTipo.ASPIRANTE;
    }

    private UUID extraerIdEntidad(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg instanceof UUID) {
                return (UUID) arg;
            }
        }
        return null;
    }

    private String extraerModuloOrigen(HttpServletRequest request) {
        if (request == null) return null;
        String modulo = request.getHeader("X-Modulo-Origen");
        if (isBlank(modulo)) modulo = request.getHeader("X-Module-Origin");
        if (isBlank(modulo)) modulo = request.getServletPath();
        if (isBlank(modulo)) modulo = request.getRequestURI();
        if (isBlank(modulo)) modulo = request.getContextPath();
        return isBlank(modulo) ? null : modulo;
    }

    private String extraerCorrelationId(HttpServletRequest request) {
        if (request == null) return null;
        String correlationId = request.getHeader("X-Correlation-Id");
        if (isBlank(correlationId)) correlationId = request.getHeader("Correlation-Id");
        if (isBlank(correlationId)) correlationId = request.getHeader("correlationId");
        if (isBlank(correlationId)) correlationId = request.getHeader("X-Request-Id");
        return isBlank(correlationId) ? null : correlationId;
    }

    private CategoriaEvento extraerCategoriaEvento(String categoria) {
        if (isBlank(categoria)) return CategoriaEvento.SISTEMA;
        String normalized = normalizarEnumName(categoria);
        try {
            return CategoriaEvento.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            for (CategoriaEvento cat : CategoriaEvento.values()) {
                if (cat.name().equalsIgnoreCase(categoria.trim())) {
                    return cat;
                }
            }
            return CategoriaEvento.SISTEMA;
        }
    }

    private TipoEvento extraerTipoEvento(String tipo) {
        if (isBlank(tipo)) return TipoEvento.ERROR_SISTEMA;
        String normalized = normalizarEnumName(tipo);
        try {
            return TipoEvento.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return switch (normalized) {
                case "INICIO_CARTA" -> TipoEvento.CARTA_INICIADA;
                case "ACEPTACION_BLOQUE" -> TipoEvento.BLOQUE_ACEPTADO;
                case "ACEPTACION_COMPLETA" -> TipoEvento.BLOQUES_COMPLETADOS;
                case "VALIDACION_EXTERNA" -> TipoEvento.VALIDACION_EXTERNA_OK;
                case "FIRMA_CARTA" -> TipoEvento.FIRMA_FEA_COMPLETADA;
                case "ELIMINACION" -> TipoEvento.DOCUMENTO_ELIMINADO;
                case "VALIDACION_MANUAL" -> TipoEvento.DOCUMENTO_VALIDADO_TIPO_A;
                case "DICTAMEN_REVISION" -> TipoEvento.DOCUMENTO_RECHAZADO;
                default -> TipoEvento.ERROR_SISTEMA;
            };
        }
    }

    private String normalizarEnumName(String valor) {
        return valor == null ? "" : valor.trim().replace(' ', '_').replace('-', '_').toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}