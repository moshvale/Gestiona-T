package mx.ine.gestiona_t.modules.documentos.model;

import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusRevision;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "revisiones_manuales", indexes = {
    @Index(name = "idx_rev_documento", columnList = "documentoId"),
    @Index(name = "idx_rev_analista", columnList = "analistaId"),
    @Index(name = "idx_rev_estatus", columnList = "estatus")
})
public class RevisionManual {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID documentoId;
    
    @Column
    private UUID analistaId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstatusRevision estatus;
    
    @Column(columnDefinition = "TEXT")
    private String dictamen;
    
    @Column(length = 1000)
    private String motivo;
    
    @Column
    private Integer prioridad;
    
    @Column
    private LocalDateTime fechaAsignacion;
    
    @Column
    private LocalDateTime fechaDictamen;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (estatus == null) {
            estatus = EstatusRevision.PENDIENTE;
        }
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UUID getDocumentoId() { return documentoId; }
    public void setDocumentoId(UUID documentoId) { this.documentoId = documentoId; }
    
    public UUID getAnalistaId() { return analistaId; }
    public void setAnalistaId(UUID analistaId) { this.analistaId = analistaId; }
    
    public EstatusRevision getEstatus() { return estatus; }
    public void setEstatus(EstatusRevision estatus) { this.estatus = estatus; }
    
    public String getDictamen() { return dictamen; }
    public void setDictamen(String dictamen) { this.dictamen = dictamen; }
    
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    
    public Integer getPrioridad() { return prioridad; }
    public void setPrioridad(Integer prioridad) { this.prioridad = prioridad; }
    
    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
    
    public LocalDateTime getFechaDictamen() { return fechaDictamen; }
    public void setFechaDictamen(LocalDateTime fechaDictamen) { this.fechaDictamen = fechaDictamen; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}