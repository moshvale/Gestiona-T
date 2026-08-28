package mx.ine.gestiona_t.modules.cartadeclaratoria.model;

import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.EstatusCarta;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.MetodoFirmaCarta;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "cartas_declaratorias", indexes = {
        @Index(name = "idx_carta_folio", columnList = "folio"),
        @Index(name = "idx_carta_aspirante", columnList = "aspirante_id")
})
public class CartaDeclaratoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID aspiranteId;

    @Column(nullable = false, length = 36)
    private String folio;

    @Column(length = 36)
    private String folioCarta;

    @Column(length = 20)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstatusCarta estatus;

    @Column(length = 500)
    private String pdfStoragePath;

    @Column(length = 100)
    private String pdfHash;

    @Column(length = 100)
    private String firmaDigitalHash;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MetodoFirmaCarta metodoFirma;

    @Column
    private LocalDateTime fechaAceptacionCompleta;

    @Column
    private LocalDateTime fechaFirma;

    // ✅ CORREGIDO: Usar Map<String, Object> con @JdbcTypeCode para mapear correctamente a jsonb
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_sesion", columnDefinition = "jsonb")
    private Map<String, Object> metadataSesion = new HashMap<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== Getters y Setters ====================

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getAspiranteId() { return aspiranteId; }
    public void setAspiranteId(UUID aspiranteId) { this.aspiranteId = aspiranteId; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public String getFolioCarta() { return folioCarta; }
    public void setFolioCarta(String folioCarta) { this.folioCarta = folioCarta; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public EstatusCarta getEstatus() { return estatus; }
    public void setEstatus(EstatusCarta estatus) { this.estatus = estatus; }

    public String getPdfStoragePath() { return pdfStoragePath; }
    public void setPdfStoragePath(String pdfStoragePath) { this.pdfStoragePath = pdfStoragePath; }

    public String getPdfHash() { return pdfHash; }
    public void setPdfHash(String pdfHash) { this.pdfHash = pdfHash; }

    public String getFirmaDigitalHash() { return firmaDigitalHash; }
    public void setFirmaDigitalHash(String firmaDigitalHash) { this.firmaDigitalHash = firmaDigitalHash; }

    public MetodoFirmaCarta getMetodoFirma() { return metodoFirma; }
    public void setMetodoFirma(MetodoFirmaCarta metodoFirma) { this.metodoFirma = metodoFirma; }

    public LocalDateTime getFechaAceptacionCompleta() { return fechaAceptacionCompleta; }
    public void setFechaAceptacionCompleta(LocalDateTime fechaAceptacionCompleta) { this.fechaAceptacionCompleta = fechaAceptacionCompleta; }

    public LocalDateTime getFechaFirma() { return fechaFirma; }
    public void setFechaFirma(LocalDateTime fechaFirma) { this.fechaFirma = fechaFirma; }

    // ✅ CORREGIDO: Getter y Setter para Map<String, Object>
    public Map<String, Object> getMetadataSesion() {
        return metadataSesion;
    }

    public void setMetadataSesion(Map<String, Object> metadataSesion) {
        this.metadataSesion = metadataSesion;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}