package mx.ine.gestiona_t.modules.firma.model;

import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.firma.model.enums.EstatusFirma;
import mx.ine.gestiona_t.modules.firma.model.enums.NivelFirma;
import mx.ine.gestiona_t.modules.firma.model.enums.TipoDocumentoFirma;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documentos_firmados", indexes = {
    @Index(name = "idx_doc_firmado_folio", columnList = "folioDocumento"),
    @Index(name = "idx_doc_firmado_aspirante", columnList = "aspiranteId"),
    @Index(name = "idx_doc_firmado_estatus", columnList = "estatus")
})
public class DocumentoFirmado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 36)
    private String folioDocumento;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDocumentoFirma tipoDocumento;
    
    @Column(nullable = false)
    private UUID aspiranteId;
    
    @Column(nullable = false, length = 36)
    private String folioAspirante;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NivelFirma nivelFirma;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstatusFirma estatus;
    
    @Column(nullable = false, length = 500)
    private String nombreArchivo;
    
    @Column(nullable = false, length = 500)
    private String storagePathOriginal;
    
    @Column(length = 500)
    private String storagePathFirmado;
    
    @Column(length = 100)
    private String hashOriginal;
    
    @Column(length = 100)
    private String hashFirmado;
    
    @Column(columnDefinition = "jsonb")
    private String metadataFirma;
    
    @Column(length = 100)
    private String motivoRechazo;
    
    @Column
    private LocalDateTime fechaSolicitud;
    
    @Column
    private LocalDateTime fechaFirma;
    
    @Column
    private LocalDateTime fechaExpiracion;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }
        if (folioDocumento == null) {
            folioDocumento = "DOC-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getFolioDocumento() { return folioDocumento; }
    public void setFolioDocumento(String folioDocumento) { this.folioDocumento = folioDocumento; }
    
    public TipoDocumentoFirma getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumentoFirma tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    
    public UUID getAspiranteId() { return aspiranteId; }
    public void setAspiranteId(UUID aspiranteId) { this.aspiranteId = aspiranteId; }
    
    public String getFolioAspirante() { return folioAspirante; }
    public void setFolioAspirante(String folioAspirante) { this.folioAspirante = folioAspirante; }
    
    public NivelFirma getNivelFirma() { return nivelFirma; }
    public void setNivelFirma(NivelFirma nivelFirma) { this.nivelFirma = nivelFirma; }
    
    public EstatusFirma getEstatus() { return estatus; }
    public void setEstatus(EstatusFirma estatus) { this.estatus = estatus; }
    
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    
    public String getStoragePathOriginal() { return storagePathOriginal; }
    public void setStoragePathOriginal(String storagePathOriginal) { this.storagePathOriginal = storagePathOriginal; }
    
    public String getStoragePathFirmado() { return storagePathFirmado; }
    public void setStoragePathFirmado(String storagePathFirmado) { this.storagePathFirmado = storagePathFirmado; }
    
    public String getHashOriginal() { return hashOriginal; }
    public void setHashOriginal(String hashOriginal) { this.hashOriginal = hashOriginal; }
    
    public String getHashFirmado() { return hashFirmado; }
    public void setHashFirmado(String hashFirmado) { this.hashFirmado = hashFirmado; }
    
    public String getMetadataFirma() { return metadataFirma; }
    public void setMetadataFirma(String metadataFirma) { this.metadataFirma = metadataFirma; }
    
    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
    
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    
    public LocalDateTime getFechaFirma() { return fechaFirma; }
    public void setFechaFirma(LocalDateTime fechaFirma) { this.fechaFirma = fechaFirma; }
    
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}