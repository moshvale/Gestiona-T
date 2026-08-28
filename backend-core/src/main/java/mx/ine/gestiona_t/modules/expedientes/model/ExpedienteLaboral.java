package mx.ine.gestiona_t.modules.expedientes.model;

import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoContratacion;
import mx.ine.gestiona_t.modules.vacantes.model.Vacante;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "expedientes_laborales")
public class ExpedienteLaboral {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aspirante_id", nullable = false)
    private UUID aspiranteId;

    @Column(name = "numero_empleado", nullable = false, length = 20)
    private String numeroEmpleado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contratacion", nullable = false, length = 50)
    private TipoContratacion tipoContratacion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(nullable = false)
    private Boolean vigente = true;

    @Column(name = "area_adscripcion", length = 200)
    private String areaAdscripcion;

    @Column(name = "puesto_actual", length = 200)
    private String puestoActual;

    @Column(name = "nivel_tabular", length = 20)
    private String nivelTabular;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacante_id")
    private Vacante vacante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "junta_ejecutiva_id")
    private JuntaEjecutiva juntaEjecutiva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocalia_id")
    private Vocalia vocalia;

    @Column(name = "alta_por_usuario_id")
    private UUID altaPorUsuarioId;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

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

    public UUID getAspiranteId() { return aspiranteId; }
    public void setAspiranteId(UUID aspiranteId) { this.aspiranteId = aspiranteId; }

    public String getNumeroEmpleado() { return numeroEmpleado; }
    public void setNumeroEmpleado(String numeroEmpleado) { this.numeroEmpleado = numeroEmpleado; }

    public TipoContratacion getTipoContratacion() { return tipoContratacion; }
    public void setTipoContratacion(TipoContratacion tipoContratacion) { this.tipoContratacion = tipoContratacion; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Boolean getVigente() { return vigente; }
    public void setVigente(Boolean vigente) { this.vigente = vigente; }

    public String getAreaAdscripcion() { return areaAdscripcion; }
    public void setAreaAdscripcion(String areaAdscripcion) { this.areaAdscripcion = areaAdscripcion; }

    public String getPuestoActual() { return puestoActual; }
    public void setPuestoActual(String puestoActual) { this.puestoActual = puestoActual; }

    public String getNivelTabular() { return nivelTabular; }
    public void setNivelTabular(String nivelTabular) { this.nivelTabular = nivelTabular; }

    public Vacante getVacante() { return vacante; }
    public void setVacante(Vacante vacante) { this.vacante = vacante; }

    public JuntaEjecutiva getJuntaEjecutiva() { return juntaEjecutiva; }
    public void setJuntaEjecutiva(JuntaEjecutiva juntaEjecutiva) { this.juntaEjecutiva = juntaEjecutiva; }

    public Vocalia getVocalia() { return vocalia; }
    public void setVocalia(Vocalia vocalia) { this.vocalia = vocalia; }

    public UUID getAltaPorUsuarioId() { return altaPorUsuarioId; }
    public void setAltaPorUsuarioId(UUID altaPorUsuarioId) { this.altaPorUsuarioId = altaPorUsuarioId; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}