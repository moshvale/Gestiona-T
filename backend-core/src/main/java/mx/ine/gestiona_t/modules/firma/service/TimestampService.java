package mx.ine.gestiona_t.modules.firma.service;

import mx.ine.gestiona_t.modules.firma.integration.TimestampAuthorityClient;
import mx.ine.gestiona_t.modules.firma.integration.dto.TimestampResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Service
public class TimestampService {
    
    private static final Logger log = LoggerFactory.getLogger(TimestampService.class);
    private final TimestampAuthorityClient tsaClient;
    
    public TimestampService(TimestampAuthorityClient tsaClient) {
        this.tsaClient = tsaClient;
    }
    
    public Mono<TimestampResponse> solicitarTimestamp(String hashDocumento) {
        log.info("Solicitando timestamp para documento: {}", hashDocumento);
        return tsaClient.solicitarTimestamp(hashDocumento);
    }
    
    public LocalDateTime parseTimestamp(String timestampStr) {
        try {
            return LocalDateTime.parse(timestampStr);
        } catch (Exception e) {
            log.warn("Error parseando timestamp: {}", e.getMessage());
            return LocalDateTime.now();
        }
    }
}