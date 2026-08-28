package mx.ine.gestiona_t.modules.cv.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cv_experiencia_laboral")
public class CvExperienciaLaboral {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id", nullable = false)
    private CvInstitucional cv;

    @Column(length = 50, nullable = false)
    private String tipoExperiencia;

    @Column(length = 150, nullable = false)
    private String empresa;

    // La base local conserva esta columna histórica como obligatoria.
    // Se mantiene sincronizada con empresa para soportar ambos esquemas.
    @Column(length = 150, nullable = false)
    private String institucion;


    @Column(length = 150, nullable = false)
    private String puesto;

    @Column(columnDefinition = "TEXT")
    private String funciones;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column
    private LocalDate fechaFin;

    @Column(precision = 10, scale = 2)
    private BigDecimal sueldo;

    // ✅ CLAVE: Sin el atributo 'name'. Hibernate 6 lo convierte a 'actualmente_laborando' automáticamente.
    @Column(nullable = false)
    private boolean actualmenteLaborando;

    // ✅ Campos de auditoría agregados
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
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

    // --- Getters y Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CvInstitucional getCv() { return cv; }
    public void setCv(CvInstitucional cv) { this.cv = cv; }

    public String getTipoExperiencia() { return tipoExperiencia; }
    public void setTipoExperiencia(String tipoExperiencia) { this.tipoExperiencia = tipoExperiencia; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getInstitucion() { return institucion; }
    public void setInstitucion(String institucion) { this.institucion = institucion; }


    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }

    public String getFunciones() { return funciones; }
    public void setFunciones(String funciones) { this.funciones = funciones; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public BigDecimal getSueldo() { return sueldo; }
    public void setSueldo(BigDecimal sueldo) { this.sueldo = sueldo; }

    public boolean isActualmenteLaborando() { return actualmenteLaborando; }
    public void setActualmenteLaborando(boolean actualmenteLaborando) { this.actualmenteLaborando = actualmenteLaborando; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}