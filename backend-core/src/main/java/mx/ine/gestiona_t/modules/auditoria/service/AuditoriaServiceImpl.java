package mx.ine.gestiona_t.modules.auditoria.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.ine.gestiona_t.modules.auditoria.dto.request.BuscarEventosRequest;
import mx.ine.gestiona_t.modules.auditoria.dto.request.PublicarEventoRequest;
import mx.ine.gestiona_t.modules.auditoria.dto.response.*;
import mx.ine.gestiona_t.modules.auditoria.model.EventoAuditoria;
import mx.ine.gestiona_t.modules.auditoria.model.enums.NivelSeveridad;
import mx.ine.gestiona_t.modules.auditoria.repository.EventoAuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuditoriaServiceImpl implements AuditoriaService {
    
    private static final Logger log = LoggerFactory.getLogger(AuditoriaServiceImpl.class);
    
    private final EventoAuditoriaRepository eventoRepository;
    private final CadenaHashService cadenaHashService;
    private final ObjectMapper objectMapper;
    
    public AuditoriaServiceImpl(EventoAuditoriaRepository eventoRepository,
                                  CadenaHashService cadenaHashService,
                                  ObjectMapper objectMapper) {
        this.eventoRepository = eventoRepository;
        this.cadenaHashService = cadenaHashService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    @Transactional
    public void publicarEvento(PublicarEventoRequest request, String ip, String userAgent) {
        log.info("Publicando evento: {} - {}", request.categoria(), request.tipoEvento());
        
        try {
            String datosJson = request.datosEvento() != null 
                ? objectMapper.writeValueAsString(request.datosEvento()) 
                : "{}";
            
            String hashDatos = cadenaHashService.calcularHash(datosJson);
            
            EventoAuditoria evento = new EventoAuditoria();
            evento.setCategoria(request.categoria());
            evento.setTipoEvento(request.tipoEvento());
            evento.setSeveridad(request.severidad());
            evento.setActorId(request.actorId());
            evento.setActorTipo(request.actorTipo());
            evento.setIpOrigen(ip != null ? ip : "desconocida");
            evento.setUserAgent(userAgent != null ? userAgent : "desconocido");
            evento.setRecursoAfectado(request.recursoAfectado());
            evento.setDescripcion(request.descripcion());
            evento.setDatosEvento(datosJson);
            evento.setHashDatos(hashDatos);
            evento.setTimestamp(LocalDateTime.now());
            evento.setCorrelationId(request.correlationId());
            evento.setModuloOrigen(request.moduloOrigen());
            evento.setAncladoBlockchain(false);
            
            evento = eventoRepository.save(evento);
            
            String hashPropio = cadenaHashService.calcularYGuardarCadena(evento.getId(), hashDatos);
            evento.setHashPropio(hashPropio);
            
            Optional<EventoAuditoria> ultimoAnterior = eventoRepository.findUltimoEvento();
            if (ultimoAnterior.isPresent() && !ultimoAnterior.get().getId().equals(evento.getId())) {
                evento.setHashAnterior(ultimoAnterior.get().getHashPropio());
            }
            
            eventoRepository.save(evento);
            
            if (request.severidad() == NivelSeveridad.CRITICAL) {
                log.warn("Evento CRITICO registrado: {} - {}", request.categoria(), request.tipoEvento());
            }
            
        } catch (Exception e) {
            log.error("Error publicando evento de auditoria: {}", e.getMessage(), e);
            throw new RuntimeException("Error publicando evento", e);
        }
    }
    
    @Override
    public Page<EventoAuditoriaResponse> buscarEventos(BuscarEventosRequest request) {
        PageRequest pageRequest = PageRequest.of(
            request.pagina(), request.tamanoPagina(), Sort.by(Sort.Direction.DESC, "timestamp")
        );
        
        LocalDateTime desde = request.fechaDesde() != null 
            ? request.fechaDesde() 
            : LocalDateTime.now().minusDays(30);
        LocalDateTime hasta = request.fechaHasta() != null 
            ? request.fechaHasta() 
            : LocalDateTime.now();
        
        Page<EventoAuditoria> page;
        
        if (request.categoria() != null) {
            page = eventoRepository.findByCategoriaAndTimestampBetween(
                request.categoria(), desde, hasta, pageRequest
            );
        } else {
            page = eventoRepository.findByRangoFechas(desde, hasta, pageRequest);
        }
        
        return page.map(this::mapToResponse);
    }
    
    @Override
    public EventoAuditoriaResponse obtenerEvento(UUID id) {
        EventoAuditoria evento = eventoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        return mapToResponse(evento);
    }
    
    @Override
    public Page<EventoAuditoriaResponse> obtenerEventosActor(UUID actorId, int pagina, int tamano) {
        PageRequest pageRequest = PageRequest.of(pagina, tamano);
        List<EventoAuditoria> eventos = eventoRepository.findByActorIdOrderByTimestampDesc(actorId);
        
        int inicio = pagina * tamano;
        int fin = Math.min(inicio + tamano, eventos.size());
        
        List<EventoAuditoriaResponse> sublist = eventos.subList(inicio, fin).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(sublist, pageRequest, eventos.size());
    }
    
    @Override
    public ResumenReporteResponse generarResumen(LocalDateTime desde, LocalDateTime hasta) {
        long total = eventoRepository.countEventosDesde(desde);
        
        Map<String, Long> porCategoria = new HashMap<>();
        for (var cat : mx.ine.gestiona_t.modules.auditoria.model.enums.CategoriaEvento.values()) {
            long count = eventoRepository.countPorCategoriaEnRango(cat, desde, hasta);
            if (count > 0) porCategoria.put(cat.name(), count);
        }
        
        Map<String, Long> porSeveridad = new HashMap<>();
        for (var sev : eventoRepository.contarPorSeveridad(desde)) {
            porSeveridad.put(sev[0].toString(), (Long) sev[1]);
        }
        
        Map<String, Long> porModulo = new HashMap<>();
        for (var mod : eventoRepository.contarPorModulo(desde)) {
            porModulo.put(mod[0] != null ? mod[0].toString() : "SIN_MODULO", (Long) mod[1]);
        }
        
        long criticos = porSeveridad.getOrDefault("CRITICAL", 0L);
        long errores = porSeveridad.getOrDefault("ERROR", 0L);
        
        VerificacionIntegridadResponse integridad = verificarIntegridadCadena();
        
        return new ResumenReporteResponse(
            LocalDateTime.now(), desde, hasta, total,
            porCategoria, porSeveridad, porModulo,
            criticos, errores, integridad.integridadValida()
        );
    }
    
    @Override
    public EstadisticasResponse obtenerEstadisticas() {
        LocalDateTime hoy = LocalDate.now().atStartOfDay();
        LocalDateTime semana = hoy.minusDays(7);
        LocalDateTime mes = hoy.minusDays(30);
        
        long hoyCount = eventoRepository.countEventosDesde(hoy);
        long semanaCount = eventoRepository.countEventosDesde(semana);
        long mesCount = eventoRepository.countEventosDesde(mes);
        
        Map<String, Long> porModulo = new HashMap<>();
        for (var mod : eventoRepository.contarPorModulo(hoy)) {
            porModulo.put(mod[0] != null ? mod[0].toString() : "SIN_MODULO", (Long) mod[1]);
        }
        
        Map<String, Long> porSeveridad = new HashMap<>();
        for (var sev : eventoRepository.contarPorSeveridad(hoy)) {
            porSeveridad.put(sev[0].toString(), (Long) sev[1]);
        }
        
        Map<String, Long> porCategoria = new HashMap<>();
        for (var cat : mx.ine.gestiona_t.modules.auditoria.model.enums.CategoriaEvento.values()) {
            long count = eventoRepository.countPorCategoriaEnRango(cat, hoy, LocalDateTime.now());
            if (count > 0) porCategoria.put(cat.name(), count);
        }
        
        return new EstadisticasResponse(
            hoyCount, semanaCount, mesCount,
            porModulo, porSeveridad, porCategoria,
            cadenaHashService.getLongitudCadena(),
            cadenaHashService.obtenerUltimoHash()
        );
    }
    
    @Override
    public VerificacionIntegridadResponse verificarIntegridadCadena() {
        log.info("Verificando integridad de cadena de auditoria");
        
        List<EventoAuditoria> eventos = eventoRepository.findAll(
            Sort.by(Sort.Direction.ASC, "timestamp")
        );
        
        long validos = 0;
        long invalidos = 0;
        List<String> errores = new ArrayList<>();
        
        String hashEsperadoAnterior = "0000000000000000000000000000000000000000000000000000000000000000";
        
        for (EventoAuditoria evento : eventos) {
            if (evento.getHashAnterior() != null && 
                !evento.getHashAnterior().equals(hashEsperadoAnterior)) {
                invalidos++;
                errores.add("Evento " + evento.getId() + " tiene hash anterior inconsistente");
            } else {
                validos++;
            }
            
            if (evento.getHashPropio() != null) {
                hashEsperadoAnterior = evento.getHashPropio();
            }
        }
        
        boolean valida = invalidos == 0;
        
        return new VerificacionIntegridadResponse(
            valida, eventos.size(), validos, invalidos, errores,
            valida ? "Cadena de auditoria integra" : "Se detectaron inconsistencias en la cadena"
        );
    }
    
    @Override
    public byte[] exportarExcel(LocalDateTime desde, LocalDateTime hasta) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Fecha,Categoria,Tipo,Severidad,Actor,IP,Descripcion,Hash\n");
        
        Page<EventoAuditoria> eventos = eventoRepository.findByRangoFechas(
            desde, hasta, PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "timestamp"))
        );
        
        for (EventoAuditoria e : eventos.getContent()) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                e.getId(),
                e.getTimestamp(),
                e.getCategoria(),
                e.getTipoEvento(),
                e.getSeveridad(),
                e.getActorId(),
                e.getIpOrigen(),
                escaparCsv(e.getDescripcion()),
                e.getHashPropio()
            ));
        }
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    private EventoAuditoriaResponse mapToResponse(EventoAuditoria e) {
        return new EventoAuditoriaResponse(
            e.getId(), e.getCategoria(), e.getTipoEvento(), e.getSeveridad(),
            e.getActorId(), e.getActorTipo(), e.getIpOrigen(), e.getRecursoAfectado(),
            e.getDescripcion(), e.getHashPropio(), e.getHashAnterior(),
            e.getTimestamp(), e.getCorrelationId(), e.getModuloOrigen(),
            e.isAncladoBlockchain()
        );
    }
    
    private String escaparCsv(String texto) {
        if (texto == null) return "";
        return "\"" + texto.replace("\"", "\"\"").replace("\n", " ") + "\"";
    }
}