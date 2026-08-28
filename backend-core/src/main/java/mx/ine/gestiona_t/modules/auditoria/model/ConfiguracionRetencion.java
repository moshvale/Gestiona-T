package mx.ine.gestiona_t.modules.auditoria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_configuracion_retencion")
public class ConfiguracionRetencion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50, unique = true)
    private String categoria;
    
    @Column(nullable = false)
    private int aniosRetencion;
    
    @Column(nullable = false)
    private boolean activo;
    
    @Column(length = 500)
    private String descripcion;
    
    @Column
    private LocalDateTime fechaActualizacion;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public int getAniosRetencion() { return aniosRetencion; }
    public void setAniosRetencion(int aniosRetencion) { this.aniosRetencion = aniosRetencion; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}