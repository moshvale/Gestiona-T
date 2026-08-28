package mx.ine.gestiona_t.modules.expedientes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vocalias", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre", "junta_ejecutiva_id"})
})
public class Vocalia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "junta_ejecutiva_id", nullable = false)
    private JuntaEjecutiva juntaEjecutiva;

    @Column(nullable = false)
    private Boolean activa = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public JuntaEjecutiva getJuntaEjecutiva() { return juntaEjecutiva; }
    public void setJuntaEjecutiva(JuntaEjecutiva juntaEjecutiva) { this.juntaEjecutiva = juntaEjecutiva; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}