package mx.ine.gestiona_t.modules.firma.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "firmas_metadata", indexes = {
    @Index(name = "idx_firma_meta_documento", columnList = "documentoFirmadoId")
})
public class FirmaMetadata {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID documentoFirmadoId;
    
    @Column(nullable = false, length = 45)
    private String ipOrigen;
    
    @Column(length = 500)
    private String userAgent;
    
    @Column(length = 100)
    private String geolocalizacion;
    
    @Column(length = 100)
    private String dispositivoId;
    
    @Column(columnDefinition = "TEXT")
    private String datosBiometricos;
    
    @Column(length = 100)
    private String otpHash;
    
    @Column(length = 100)
    private String certificadoSerial;
    
    @Column(length = 200)
    private String certificadoSubject;
    
    @Column
    private LocalDateTime certificadoValidoHasta;
    
    @Column
    private Double scoreCoincidenciaBiometrica;
    
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
    
    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getGeolocalizacion() { return geolocalizacion; }
    public void setGeolocalizacion(String geolocalizacion) { this.geolocalizacion = geolocalizacion; }
    
    public String getDispositivoId() { return dispositivoId; }
    public void setDispositivoId(String dispositivoId) { this.dispositivoId = dispositivoId; }
    
    public String getDatosBiometricos() { return datosBiometricos; }
    public void setDatosBiometricos(String datosBiometricos) { this.datosBiometricos = datosBiometricos; }
    
    public String getOtpHash() { return otpHash; }
    public void setOtpHash(String otpHash) { this.otpHash = otpHash; }
    
    public String getCertificadoSerial() { return certificadoSerial; }
    public void setCertificadoSerial(String certificadoSerial) { this.certificadoSerial = certificadoSerial; }
    
    public String getCertificadoSubject() { return certificadoSubject; }
    public void setCertificadoSubject(String certificadoSubject) { this.certificadoSubject = certificadoSubject; }
    
    public LocalDateTime getCertificadoValidoHasta() { return certificadoValidoHasta; }
    public void setCertificadoValidoHasta(LocalDateTime certificadoValidoHasta) { 
        this.certificadoValidoHasta = certificadoValidoHasta; 
    }
    
    public Double getScoreCoincidenciaBiometrica() { return scoreCoincidenciaBiometrica; }
    public void setScoreCoincidenciaBiometrica(Double scoreCoincidenciaBiometrica) { 
        this.scoreCoincidenciaBiometrica = scoreCoincidenciaBiometrica; 
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}