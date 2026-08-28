package mx.ine.gestiona_t.modules.matching.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "resultados_matching")
public class ResultadoMatching {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aspirante_id", nullable = false)
    private UUID aspiranteId;

    @Column(nullable = false)
    private Double score;

    @Column(length = 50, nullable = false)
    private String nivelCompatibilidad;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "fecha_evaluacion", nullable = false)
    private LocalDateTime fechaEvaluacion;

    @PrePersist
    protected void onCreate() {
        fechaEvaluacion = LocalDateTime.now();
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAspiranteId() { return aspiranteId; }
    public void setAspiranteId(UUID aspiranteId) { this.aspiranteId = aspiranteId; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getNivelCompatibilidad() { return nivelCompatibilidad; }
    public void setNivelCompatibilidad(String nivelCompatibilidad) { this.nivelCompatibilidad = nivelCompatibilidad; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public LocalDateTime getFechaEvaluacion() { return fechaEvaluacion; }
    public void setFechaEvaluacion(LocalDateTime fechaEvaluacion) { this.fechaEvaluacion = fechaEvaluacion; }
}