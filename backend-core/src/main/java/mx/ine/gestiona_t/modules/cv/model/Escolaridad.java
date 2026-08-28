package mx.ine.gestiona_t.modules.cv.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.cv.model.enums.NivelEstudio;
import mx.ine.gestiona_t.modules.cv.model.enums.StatusEstudio;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cv_escolaridad")
public class Escolaridad {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id", nullable = false)
    @JsonIgnore
    private CvEstructurado cv;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NivelEstudio nivel;
    
    @Column(nullable = false, length = 200)
    private String institucion;
    
    @Column(length = 100)
    private String titulo;
    
    @Column(length = 20)
    private String cedulaProfesional;
    
    @Column(nullable = false)
    private LocalDate fechaInicio;
    
    @Column
    private LocalDate fechaTermino;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEstudio status;
    
    @Column(length = 500)
    private String documentoSoportePath;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
    
    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public CvEstructurado getCv() { return cv; }
    public void setCv(CvEstructurado cv) { this.cv = cv; }
    
    public NivelEstudio getNivel() { return nivel; }
    public void setNivel(NivelEstudio nivel) { this.nivel = nivel; }
    
    public String getInstitucion() { return institucion; }
    public void setInstitucion(String institucion) { this.institucion = institucion; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getCedulaProfesional() { return cedulaProfesional; }
    public void setCedulaProfesional(String cedulaProfesional) { this.cedulaProfesional = cedulaProfesional; }
    
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public LocalDate getFechaTermino() { return fechaTermino; }
    public void setFechaTermino(LocalDate fechaTermino) { this.fechaTermino = fechaTermino; }
    
    public StatusEstudio getStatus() { return status; }
    public void setStatus(StatusEstudio status) { this.status = status; }
    
    public String getDocumentoSoportePath() { return documentoSoportePath; }
    public void setDocumentoSoportePath(String documentoSoportePath) { this.documentoSoportePath = documentoSoportePath; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}