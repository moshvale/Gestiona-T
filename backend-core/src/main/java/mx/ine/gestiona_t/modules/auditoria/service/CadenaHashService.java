package mx.ine.gestiona_t.modules.auditoria.service;

import mx.ine.gestiona_t.modules.auditoria.model.CadenaHash;
import mx.ine.gestiona_t.modules.auditoria.repository.CadenaHashRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio que gestiona la cadena de hashes para garantizar inmutabilidad.
 * Cada nuevo evento incluye el hash del evento anterior, formando una cadena
 * similar a blockchain.
 */
@Service
public class CadenaHashService {
    
    private static final Logger log = LoggerFactory.getLogger(CadenaHashService.class);
    private static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";
    
    private final CadenaHashRepository cadenaRepository;
    
    public CadenaHashService(CadenaHashRepository cadenaRepository) {
        this.cadenaRepository = cadenaRepository;
    }
    
    @Transactional
    public synchronized String calcularYGuardarCadena(UUID eventoId, String hashDatos) {
        Optional<CadenaHash> ultimoOpt = cadenaRepository.findUltimo();
        
        String hashAnterior = ultimoOpt
            .map(CadenaHash::getHashEvento)
            .orElse(GENESIS_HASH);
        
        Long nuevaSecuencia = cadenaRepository.findSecuenciaMaxima();
        nuevaSecuencia = (nuevaSecuencia == null) ? 1L : nuevaSecuencia + 1L;
        
        String hashPropio = calcularHash(hashDatos + hashAnterior + nuevaSecuencia);
        
        CadenaHash cadena = new CadenaHash();
        cadena.setEventoId(eventoId);
        cadena.setHashEvento(hashPropio);
        cadena.setHashAnterior(hashAnterior);
        cadena.setSecuencia(nuevaSecuencia);
        cadena.setTimestamp(LocalDateTime.now());
        
        cadenaRepository.save(cadena);
        
        log.debug("Cadena hash actualizada. Secuencia: {}, Hash: {}", nuevaSecuencia, hashPropio);
        
        return hashPropio;
    }
    
    public String obtenerUltimoHash() {
        return cadenaRepository.findUltimo()
            .map(CadenaHash::getHashEvento)
            .orElse(GENESIS_HASH);
    }
    
    public long getLongitudCadena() {
        return cadenaRepository.count();
    }
    
    public String calcularHash(String contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenido.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculando hash SHA-256", e);
        }
    }
}