package mx.ine.gestiona_t.modules.cv.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cv_cursos_capacitaciones_institucionales")
public class CvCursoCapacitacionInstitucional {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id", nullable = false)
    private CvInstitucional cv;

    @Column(length = 200, nullable = false)
    private String nombreCurso;

    @Column(length = 200, nullable = false)
    private String institucion;

    @Column(nullable = false)
    private Integer duracionHoras;

    @Column(name = "fecha_realizacion", nullable = false)
    private LocalDate fechaRealizacion;

    @Column(name = "documento_soporte_path", length = 500)
    private String documentoSoportePath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public CvInstitucional getCv() { return cv; }
    public void setCv(CvInstitucional cv) { this.cv = cv; }
    public String getNombreCurso() { return nombreCurso; }
    public void setNombreCurso(String nombreCurso) { this.nombreCurso = nombreCurso; }
    public String getInstitucion() { return institucion; }
    public void setInstitucion(String institucion) { this.institucion = institucion; }
    public Integer getDuracionHoras() { return duracionHoras; }
    public void setDuracionHoras(Integer duracionHoras) { this.duracionHoras = duracionHoras; }
    public LocalDate getFechaRealizacion() { return fechaRealizacion; }
    public void setFechaRealizacion(LocalDate fechaRealizacion) { this.fechaRealizacion = fechaRealizacion; }
    public String getDocumentoSoportePath() { return documentoSoportePath; }
    public void setDocumentoSoportePath(String documentoSoportePath) { this.documentoSoportePath = documentoSoportePath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; } // ✅ 'A' mayúscula
}