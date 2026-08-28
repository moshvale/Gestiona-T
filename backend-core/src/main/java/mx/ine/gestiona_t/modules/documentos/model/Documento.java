package mx.ine.gestiona_t.modules.documentos.model;

import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.documentos.model.enums.CategoriaDocumento; // ✅ NUEVO IMPORT
import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusDocumento;
import mx.ine.gestiona_t.modules.documentos.model.enums.TipoDocumento;
import mx.ine.gestiona_t.modules.documentos.model.enums.TipoValidacion;
import mx.ine.gestiona_t.modules.expedientes.model.ExpedienteLaboral; // ✅ NUEVO IMPORT
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "documentos", indexes = {
    @Index(name = "idx_doc_folio", columnList = "folio"),
    @Index(name = "idx_doc_aspirante", columnList = "aspiranteId"),
    @Index(name = "idx_doc_estatus", columnList = "estatus"),
    // ✅ NUEVOS ÍNDICES: para clasificar documentos por categoría y expediente
    @Index(name = "idx_doc_categoria", columnList = "categoria"),
    @Index(name = "idx_doc_expediente", columnList = "expediente_laboral_id"),
    @Index(name = "idx_doc_base", columnList = "es_documento_base")
})
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID aspiranteId;

    @Column(nullable = false, length = 36)
    private String folio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDocumento tipoDocumento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoValidacion tipoValidacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstatusDocumento estatus;

    @Column(nullable = false, length = 500)
    private String nombreArchivo;

    @Column(nullable = false, length = 500)
    private String storagePath;

    @Column(length = 50)
    private String contentType;

    @Column
    private Long tamanoBytes;

    @Column(columnDefinition = "TEXT")
    private String textoExtraido;

    @Column
    private Double scoreAutenticidad;

    @Column(length = 1000)
    private String motivoRechazo;

    @Column
    private UUID analistaId;

    @Column
    private LocalDateTime fechaCarga;

    @Column
    private LocalDateTime fechaValidacion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_validacion", columnDefinition = "jsonb")
    private Map<String, Object> metadataValidacion = new HashMap<>();

    // ✅ NUEVOS CAMPOS: Clasificación del documento y vinculación con expediente laboral
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaDocumento categoria = CategoriaDocumento.CONCURSO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_laboral_id")
    private ExpedienteLaboral expedienteLaboral;

    @Column(name = "es_documento_base")
    private Boolean esDocumentoBase = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (fechaCarga == null) {
            fechaCarga = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ============================================
    // GETTERS Y SETTERS EXISTENTES
    // ============================================
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getAspiranteId() { return aspiranteId; }
    public void setAspiranteId(UUID aspiranteId) { this.aspiranteId = aspiranteId; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public TipoValidacion getTipoValidacion() { return tipoValidacion; }
    public void setTipoValidacion(TipoValidacion tipoValidacion) { this.tipoValidacion = tipoValidacion; }

    public EstatusDocumento getEstatus() { return estatus; }
    public void setEstatus(EstatusDocumento estatus) { this.estatus = estatus; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }

    public String getTextoExtraido() { return textoExtraido; }
    public void setTextoExtraido(String textoExtraido) { this.textoExtraido = textoExtraido; }

    public Double getScoreAutenticidad() { return scoreAutenticidad; }
    public void setScoreAutenticidad(Double scoreAutenticidad) { this.scoreAutenticidad = scoreAutenticidad; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public UUID getAnalistaId() { return analistaId; }
    public void setAnalistaId(UUID analistaId) { this.analistaId = analistaId; }

    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDateTime fechaCarga) { this.fechaCarga = fechaCarga; }

    public LocalDateTime getFechaValidacion() { return fechaValidacion; }
    public void setFechaValidacion(LocalDateTime fechaValidacion) { this.fechaValidacion = fechaValidacion; }

    public Map<String, Object> getMetadataValidacion() { return metadataValidacion; }
    public void setMetadataValidacion(Map<String, Object> metadataValidacion) { this.metadataValidacion = metadataValidacion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ✅ NUEVOS GETTERS Y SETTERS
    public CategoriaDocumento getCategoria() { return categoria; }
    public void setCategoria(CategoriaDocumento categoria) { this.categoria = categoria; }

    public ExpedienteLaboral getExpedienteLaboral() { return expedienteLaboral; }
    public void setExpedienteLaboral(ExpedienteLaboral expedienteLaboral) { this.expedienteLaboral = expedienteLaboral; }

    public Boolean getEsDocumentoBase() { return esDocumentoBase; }
    public void setEsDocumentoBase(Boolean esDocumentoBase) { this.esDocumentoBase = esDocumentoBase; }
}