package mx.ine.gestiona_t.modules.cv.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List; // ✅ AGREGAR ESTA LÍNEA

@Entity
@Table(name = "cv_institucionales")
public class CvInstitucional {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aspirante_id", nullable = false, unique = true)
    private UUID aspiranteId;

    // --- Expectativas Laborales ---
    @Column(length = 100)
    private String entidadPreferida;

    @Column(precision = 10, scale = 2)
    private BigDecimal sueldoDeseado;

    @Column(length = 50)
    private String disponibilidad; // Ej: "Inmediata", "15 días"

    @Column(columnDefinition = "TEXT")
    private String areasInteres;

    // --- Conocimientos de Informática ---
    @Column(length = 255)
    private String sistemasOperativos;

    @Column(length = 255)
    private String lenguajesProgramacion;

    @Column(length = 255)
    private String basesDeDatos;

    // --- Habilidades y Logros (Almacenados como JSON o texto plano) ---
    @Column(columnDefinition = "TEXT")
    private String habilidades; // Ej: JSON array o texto separado por comas

    @Column(columnDefinition = "TEXT")
    private String logrosProfesionales;

    // --- Cursos y Capacitaciones (NUEVO) ---
    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CvCursoCapacitacionInstitucional> cursos = new java.util.ArrayList<>();

    public List<CvCursoCapacitacionInstitucional> getCursos() { return cursos; }
    public void setCursos(List<CvCursoCapacitacionInstitucional> cursos) { this.cursos = cursos; }

    // --- Auditoría ---
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

    public String getEntidadPreferida() { return entidadPreferida; }
    public void setEntidadPreferida(String entidadPreferida) { this.entidadPreferida = entidadPreferida; }

    public BigDecimal getSueldoDeseado() { return sueldoDeseado; }
    public void setSueldoDeseado(BigDecimal sueldoDeseado) { this.sueldoDeseado = sueldoDeseado; }

    public String getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(String disponibilidad) { this.disponibilidad = disponibilidad; }

    public String getAreasInteres() { return areasInteres; }
    public void setAreasInteres(String areasInteres) { this.areasInteres = areasInteres; }

    public String getSistemasOperativos() { return sistemasOperativos; }
    public void setSistemasOperativos(String sistemasOperativos) { this.sistemasOperativos = sistemasOperativos; }

    public String getLenguajesProgramacion() { return lenguajesProgramacion; }
    public void setLenguajesProgramacion(String lenguajesProgramacion) { this.lenguajesProgramacion = lenguajesProgramacion; }

    public String getBasesDeDatos() { return basesDeDatos; }
    public void setBasesDeDatos(String basesDeDatos) { this.basesDeDatos = basesDeDatos; }

    public String getHabilidades() { return habilidades; }
    public void setHabilidades(String habilidades) { this.habilidades = habilidades; }

    public String getLogrosProfesionales() { return logrosProfesionales; }
    public void setLogrosProfesionales(String logrosProfesionales) { this.logrosProfesionales = logrosProfesionales; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}