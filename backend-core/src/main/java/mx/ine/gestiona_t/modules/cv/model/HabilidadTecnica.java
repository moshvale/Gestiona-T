package mx.ine.gestiona_t.modules.cv.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.cv.model.enums.NivelHabilidad;
import mx.ine.gestiona_t.modules.cv.model.enums.TipoHabilidad;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cv_habilidades_tecnicas")
public class HabilidadTecnica {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id", nullable = false)
    @JsonIgnore
    private CvEstructurado cv;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoHabilidad tipo;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelHabilidad nivel;
    
    @Column
    private LocalDate fechaCertificacion;
    
    @Column
    private LocalDate fechaVencimiento;
    
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
    
    public TipoHabilidad getTipo() { return tipo; }
    public void setTipo(TipoHabilidad tipo) { this.tipo = tipo; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public NivelHabilidad getNivel() { return nivel; }
    public void setNivel(NivelHabilidad nivel) { this.nivel = nivel; }
    
    public LocalDate getFechaCertificacion() { return fechaCertificacion; }
    public void setFechaCertificacion(LocalDate fechaCertificacion) { this.fechaCertificacion = fechaCertificacion; }
    
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}