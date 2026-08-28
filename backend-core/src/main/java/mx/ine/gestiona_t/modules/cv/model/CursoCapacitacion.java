package mx.ine.gestiona_t.modules.cv.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cv_cursos_capacitaciones")
public class CursoCapacitacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id", nullable = false)
    @JsonIgnore
    private CvEstructurado cv;
    
    @Column(nullable = false, length = 200)
    private String nombreCurso;
    
    @Column(nullable = false, length = 200)
    private String institucion;
    
    @Column(nullable = false)
    private int duracionHoras;
    
    @Column(nullable = false)
    private LocalDate fechaRealizacion;
    
    @Column(length = 500)
    private String documentoSoportePath;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public CvEstructurado getCv() { return cv; }
    public void setCv(CvEstructurado cv) { this.cv = cv; }
    
    public String getNombreCurso() { return nombreCurso; }
    public void setNombreCurso(String nombreCurso) { this.nombreCurso = nombreCurso; }
    
    public String getInstitucion() { return institucion; }
    public void setInstitucion(String institucion) { this.institucion = institucion; }
    
    public int getDuracionHoras() { return duracionHoras; }
    public void setDuracionHoras(int duracionHoras) { this.duracionHoras = duracionHoras; }
    
    public LocalDate getFechaRealizacion() { return fechaRealizacion; }
    public void setFechaRealizacion(LocalDate fechaRealizacion) { this.fechaRealizacion = fechaRealizacion; }
    
    public String getDocumentoSoportePath() { return documentoSoportePath; }
    public void setDocumentoSoportePath(String documentoSoportePath) { this.documentoSoportePath = documentoSoportePath; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}