package mx.ine.gestiona_t.modules.postulaciones.model;

import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.postulaciones.model.enums.EstatusPostulacion;
import mx.ine.gestiona_t.modules.postulaciones.model.enums.EstatusFinalSeleccion; // ✅ NUEVO IMPORT

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "postulaciones", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"aspirante_id", "vacante_id"})
})
public class Postulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aspirante_id", nullable = false)
    private UUID aspiranteId;

    @Column(name = "vacante_id", nullable = false)
    private UUID vacanteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstatusPostulacion estatus;

    @Column(name = "fecha_postulacion", nullable = false, updatable = false)
    private LocalDateTime fechaPostulacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Campos de vinculación con CV, documentos y carta
    @Column(name = "carta_declaratoria_id")
    private UUID cartaDeclaratoriaId;

    @Column(name = "cv_completado")
    private Boolean cvCompletado = false;

    @Column(name = "documentos_completos")
    private Boolean documentosCompletos = false;

    // ✅ NUEVOS CAMPOS: Evaluación y dictamen final
    @Column(name = "calificacion_conocimientos")
    private Double calificacionConocimientos;

    @Column(name = "calificacion_psicometrica")
    private Double calificacionPsicometrica;

    @Column(name = "calificacion_entrevista")
    private Double calificacionEntrevista;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus_final_seleccion")
    private EstatusFinalSeleccion estatusFinalSeleccion = EstatusFinalSeleccion.PENDIENTE;

    @Column(name = "dictamen_final", columnDefinition = "TEXT")
    private String dictamenFinal;

    @PrePersist
    protected void onCreate() {
        fechaPostulacion = LocalDateTime.now();
        estatus = EstatusPostulacion.PENDIENTE;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // ============================================
    // GETTERS Y SETTERS EXISTENTES
    // ============================================
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getAspiranteId() { return aspiranteId; }
    public void setAspiranteId(UUID aspiranteId) { this.aspiranteId = aspiranteId; }

    public UUID getVacanteId() { return vacanteId; }
    public void setVacanteId(UUID vacanteId) { this.vacanteId = vacanteId; }

    public EstatusPostulacion getEstatus() { return estatus; }
    public void setEstatus(EstatusPostulacion estatus) { this.estatus = estatus; }

    public LocalDateTime getFechaPostulacion() { return fechaPostulacion; }
    public void setFechaPostulacion(LocalDateTime fechaPostulacion) { this.fechaPostulacion = fechaPostulacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public UUID getCartaDeclaratoriaId() { return cartaDeclaratoriaId; }
    public void setCartaDeclaratoriaId(UUID cartaDeclaratoriaId) { this.cartaDeclaratoriaId = cartaDeclaratoriaId; }

    public Boolean getCvCompletado() { return cvCompletado; }
    public void setCvCompletado(Boolean cvCompletado) { this.cvCompletado = cvCompletado; }

    public Boolean getDocumentosCompletos() { return documentosCompletos; }
    public void setDocumentosCompletos(Boolean documentosCompletos) { this.documentosCompletos = documentosCompletos; }

    // ✅ NUEVOS GETTERS Y SETTERS
    public Double getCalificacionConocimientos() { return calificacionConocimientos; }
    public void setCalificacionConocimientos(Double calificacionConocimientos) { this.calificacionConocimientos = calificacionConocimientos; }

    public Double getCalificacionPsicometrica() { return calificacionPsicometrica; }
    public void setCalificacionPsicometrica(Double calificacionPsicometrica) { this.calificacionPsicometrica = calificacionPsicometrica; }

    public Double getCalificacionEntrevista() { return calificacionEntrevista; }
    public void setCalificacionEntrevista(Double calificacionEntrevista) { this.calificacionEntrevista = calificacionEntrevista; }

    public EstatusFinalSeleccion getEstatusFinalSeleccion() { return estatusFinalSeleccion; }
    public void setEstatusFinalSeleccion(EstatusFinalSeleccion estatusFinalSeleccion) { this.estatusFinalSeleccion = estatusFinalSeleccion; }

    public String getDictamenFinal() { return dictamenFinal; }
    public void setDictamenFinal(String dictamenFinal) { this.dictamenFinal = dictamenFinal; }
}