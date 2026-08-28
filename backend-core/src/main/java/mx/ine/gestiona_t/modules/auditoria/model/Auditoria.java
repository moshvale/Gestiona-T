package mx.ine.gestiona_t.modules.auditoria.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 36)
    private String folio;

    @Column(nullable = false, length = 50)
    private String modulo;

    @Column(nullable = false, length = 50)
    private String accion;

    @Column(length = 100)
    private String entidadAfectada;

    @Column
    private java.util.UUID idEntidad;

    @Column(length = 100)
    private String usuarioEjecutor;

    @Column(length = 45)
    private String ipOrigen;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String detalles;

    @Column(length = 100, nullable = false)
    private String hashOperacion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters y Setters (Genera los básicos con tu IDE)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getEntidadAfectada() { return entidadAfectada; }
    public void setEntidadAfectada(String entidadAfectada) { this.entidadAfectada = entidadAfectada; }
    public java.util.UUID getIdEntidad() { return idEntidad; }
    public void setIdEntidad(java.util.UUID idEntidad) { this.idEntidad = idEntidad; }
    public String getUsuarioEjecutor() { return usuarioEjecutor; }
    public void setUsuarioEjecutor(String usuarioEjecutor) { this.usuarioEjecutor = usuarioEjecutor; }
    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }
    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
    public String getHashOperacion() { return hashOperacion; }
    public void setHashOperacion(String hashOperacion) { this.hashOperacion = hashOperacion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}