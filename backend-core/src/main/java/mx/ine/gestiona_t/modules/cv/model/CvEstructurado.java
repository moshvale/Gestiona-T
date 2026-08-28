package mx.ine.gestiona_t.modules.cv.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cv_estructurados", indexes = {
    @Index(name = "idx_cv_folio", columnList = "folio"),
    @Index(name = "idx_cv_aspirante", columnList = "aspiranteId")
})
public class CvEstructurado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID aspiranteId;
    
    @Column(nullable = false, length = 36)
    private String folio;
    
    @Column(nullable = false)
    private int scoreCompletitud;
    
    @Column(nullable = false)
    private LocalDateTime fechaCaptura;
    
    @Column(nullable = false)
    private LocalDateTime fechaUltimaModificacion;
    
    @Column(nullable = false)
    private boolean completo;
    
    @Column(length = 50)
    private String metodoCaptura;
    
    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Escolaridad> escolaridades = new ArrayList<>();
    
    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExperienciaLaboral> experiencias = new ArrayList<>();
    
    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CursoCapacitacion> cursos = new ArrayList<>();
    
    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HabilidadTecnica> habilidades = new ArrayList<>();
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        fechaCaptura = LocalDateTime.now();
        fechaUltimaModificacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        fechaUltimaModificacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getAspiranteId() { return aspiranteId; }
    public void setAspiranteId(UUID aspiranteId) { this.aspiranteId = aspiranteId; }
    
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    
    public int getScoreCompletitud() { return scoreCompletitud; }
    public void setScoreCompletitud(int scoreCompletitud) { this.scoreCompletitud = scoreCompletitud; }
    
    public LocalDateTime getFechaCaptura() { return fechaCaptura; }
    public void setFechaCaptura(LocalDateTime fechaCaptura) { this.fechaCaptura = fechaCaptura; }
    
    public LocalDateTime getFechaUltimaModificacion() { return fechaUltimaModificacion; }
    public void setFechaUltimaModificacion(LocalDateTime fechaUltimaModificacion) { 
        this.fechaUltimaModificacion = fechaUltimaModificacion; 
    }
    
    public boolean isCompleto() { return completo; }
    public void setCompleto(boolean completo) { this.completo = completo; }
    
    public String getMetodoCaptura() { return metodoCaptura; }
    public void setMetodoCaptura(String metodoCaptura) { this.metodoCaptura = metodoCaptura; }
    
    public List<Escolaridad> getEscolaridades() { return escolaridades; }
    public void setEscolaridades(List<Escolaridad> escolaridades) { this.escolaridades = escolaridades; }
    
    public List<ExperienciaLaboral> getExperiencias() { return experiencias; }
    public void setExperiencias(List<ExperienciaLaboral> experiencias) { this.experiencias = experiencias; }
    
    public List<CursoCapacitacion> getCursos() { return cursos; }
    public void setCursos(List<CursoCapacitacion> cursos) { this.cursos = cursos; }
    
    public List<HabilidadTecnica> getHabilidades() { return habilidades; }
    public void setHabilidades(List<HabilidadTecnica> habilidades) { this.habilidades = habilidades; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}