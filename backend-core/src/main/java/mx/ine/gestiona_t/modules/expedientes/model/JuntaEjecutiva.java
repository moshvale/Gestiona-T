package mx.ine.gestiona_t.modules.expedientes.model;

import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoJunta;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "juntas_ejecutivas")
public class JuntaEjecutiva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoJunta tipo;

    @Column(length = 100)
    private String estado;

    @Column(name = "clave_ine", length = 20, unique = true)
    private String claveIne;

    @Column(nullable = false)
    private Boolean activa = true;

    @OneToMany(mappedBy = "juntaEjecutiva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vocalia> vocalias = new ArrayList<>();

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

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoJunta getTipo() { return tipo; }
    public void setTipo(TipoJunta tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getClaveIne() { return claveIne; }
    public void setClaveIne(String claveIne) { this.claveIne = claveIne; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }

    public List<Vocalia> getVocalias() { return vocalias; }
    public void setVocalias(List<Vocalia> vocalias) { this.vocalias = vocalias; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}