package mx.ine.gestiona_t.modules.documentos.model;

import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusExpediente;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "expedientes_digitales", indexes = {
    @Index(name = "idx_exp_folio", columnList = "folio"),
    @Index(name = "idx_exp_aspirante", columnList = "aspiranteId")
})
public class ExpedienteDigital {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private UUID aspiranteId;
    
    @Column(nullable = false, length = 36)
    private String folio;
    
    @Column(nullable = false)
    private int documentosTotales;
    
    @Column(nullable = false)
    private int documentosValidados;
    
    @Column(nullable = false)
    private int documentosRechazados;
    
    @Column(nullable = false)
    private int documentosEnRevision;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstatusExpediente estatusGeneral;
    
    @Column(nullable = false)
    private boolean sfpVerificado;
    
    @Column
    private Boolean sfpHabilitado;
    
    @Column
    private LocalDateTime fechaVerificacionSfp;
    
    @Column
    private LocalDateTime fechaUltimaActualizacion;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        fechaUltimaActualizacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        fechaUltimaActualizacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getAspiranteId() { return aspiranteId; }
    public void setAspiranteId(UUID aspiranteId) { this.aspiranteId = aspiranteId; }
    
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    
    public int getDocumentosTotales() { return documentosTotales; }
    public void setDocumentosTotales(int documentosTotales) { this.documentosTotales = documentosTotales; }
    
    public int getDocumentosValidados() { return documentosValidados; }
    public void setDocumentosValidados(int documentosValidados) { this.documentosValidados = documentosValidados; }
    
    public int getDocumentosRechazados() { return documentosRechazados; }
    public void setDocumentosRechazados(int documentosRechazados) { this.documentosRechazados = documentosRechazados; }
    
    public int getDocumentosEnRevision() { return documentosEnRevision; }
    public void setDocumentosEnRevision(int documentosEnRevision) { this.documentosEnRevision = documentosEnRevision; }
    
    public EstatusExpediente getEstatusGeneral() { return estatusGeneral; }
    public void setEstatusGeneral(EstatusExpediente estatusGeneral) { this.estatusGeneral = estatusGeneral; }
    
    public boolean isSfpVerificado() { return sfpVerificado; }
    public void setSfpVerificado(boolean sfpVerificado) { this.sfpVerificado = sfpVerificado; }
    
    public Boolean getSfpHabilitado() { return sfpHabilitado; }
    public void setSfpHabilitado(Boolean sfpHabilitado) { this.sfpHabilitado = sfpHabilitado; }
    
    public LocalDateTime getFechaVerificacionSfp() { return fechaVerificacionSfp; }
    public void setFechaVerificacionSfp(LocalDateTime fechaVerificacionSfp) { this.fechaVerificacionSfp = fechaVerificacionSfp; }
    
    public LocalDateTime getFechaUltimaActualizacion() { return fechaUltimaActualizacion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}