package mx.ine.gestiona_t.modules.firma.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sellos_digitales", indexes = {
    @Index(name = "idx_sello_documento", columnList = "documentoFirmadoId")
})
public class SelloDigital {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID documentoFirmadoId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String timestampToken;
    
    @Column(nullable = false)
    private LocalDateTime timestampCertificado;
    
    @Column(nullable = false, length = 200)
    private String autoridadTimestamp;
    
    @Column(nullable = false, length = 100)
    private String hashDocumento;
    
    @Column(length = 50)
    private String algoritmoHash;
    
    @Column(length = 50)
    private String algoritmoFirma;
    
    @Column(columnDefinition = "TEXT")
    private String certificadoFirmante;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UUID getDocumentoFirmadoId() { return documentoFirmadoId; }
    public void setDocumentoFirmadoId(UUID documentoFirmadoId) { this.documentoFirmadoId = documentoFirmadoId; }
    
    public String getTimestampToken() { return timestampToken; }
    public void setTimestampToken(String timestampToken) { this.timestampToken = timestampToken; }
    
    public LocalDateTime getTimestampCertificado() { return timestampCertificado; }
    public void setTimestampCertificado(LocalDateTime timestampCertificado) { 
        this.timestampCertificado = timestampCertificado; 
    }
    
    public String getAutoridadTimestamp() { return autoridadTimestamp; }
    public void setAutoridadTimestamp(String autoridadTimestamp) { this.autoridadTimestamp = autoridadTimestamp; }
    
    public String getHashDocumento() { return hashDocumento; }
    public void setHashDocumento(String hashDocumento) { this.hashDocumento = hashDocumento; }
    
    public String getAlgoritmoHash() { return algoritmoHash; }
    public void setAlgoritmoHash(String algoritmoHash) { this.algoritmoHash = algoritmoHash; }
    
    public String getAlgoritmoFirma() { return algoritmoFirma; }
    public void setAlgoritmoFirma(String algoritmoFirma) { this.algoritmoFirma = algoritmoFirma; }
    
    public String getCertificadoFirmante() { return certificadoFirmante; }
    public void setCertificadoFirmante(String certificadoFirmante) { this.certificadoFirmante = certificadoFirmante; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}