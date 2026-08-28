package mx.ine.gestiona_t.modules.firma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class PdfSignatureService {
    
    private static final Logger log = LoggerFactory.getLogger(PdfSignatureService.class);
    
    public byte[] aplicarSelloDigital(byte[] pdfOriginal, String hashDocumento, 
                                        String timestampToken, String metadata) {
        log.info("Aplicando sello digital al PDF");
        
        // En produccion se usaria iText + Bouncy Castle para aplicar el sello
        // Aqui simulamos la operacion
        String selloInfo = String.format(
            "SELLO DIGITAL GESTIONA-T%n" +
            "Hash: %s%n" +
            "Timestamp: %s%n" +
            "Metadata: %s%n" +
            "Fecha: %s%n",
            hashDocumento, timestampToken, metadata, java.time.LocalDateTime.now()
        );
        
        byte[] pdfFirmado = new byte[pdfOriginal.length + selloInfo.length()];
        System.arraycopy(pdfOriginal, 0, pdfFirmado, 0, pdfOriginal.length);
        System.arraycopy(selloInfo.getBytes(StandardCharsets.UTF_8), 0, 
                         pdfFirmado, pdfOriginal.length, selloInfo.length());
        
        return pdfFirmado;
    }
    
    public String calcularHash(byte[] contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenido);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculando hash", e);
        }
    }
}