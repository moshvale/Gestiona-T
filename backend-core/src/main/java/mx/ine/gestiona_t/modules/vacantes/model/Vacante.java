package mx.ine.gestiona_t.modules.vacantes.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vacantes")
public class Vacante {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String puesto;

    @Column(name = "numero_plaza", nullable = false, unique = true)
    private String numeroPlaza;

    @Column(name = "nivel_tabular", nullable = false)
    private String nivelTabular;

    @Column(name = "numero_vacantes", nullable = false)
    private Integer numeroVacantes;

    @Column(name = "descripcion_funciones", columnDefinition = "TEXT", nullable = false)
    private String descripcionFunciones;

    @Column(name = "escolaridad", nullable = false)
    private String escolaridad;

    @Column(name = "experiencia")
    private String experiencia;

    @Column(name = "conocimientos")
    private String conocimientos;

    @Column(name = "habilidades")
    private String habilidades;

    @Column(name = "actitudes")
    private String actitudes;

    @Column(name = "percepcion_bruta", precision = 12, scale = 2, nullable = false)
    private BigDecimal percepcionBruta;

    @Column(name = "percepcion_neta", precision = 12, scale = 2, nullable = false)
    private BigDecimal percepcionNeta;

    @Column(name = "ciudad_plaza", nullable = false)
    private String ciudadPlaza;

    @Column(name = "ubicacion_plaza", columnDefinition = "TEXT", nullable = false)
    private String ubicacionPlaza;

    @Column(name = "lugar_recepcion_documentos", columnDefinition = "TEXT", nullable = false)
    private String lugarRecepcionDocumentos;

    @Column(name = "fecha_expedicion", nullable = false)
    private LocalDate fechaExpedicion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_limite", nullable = false)
    private LocalDate fechaLimite;

    @Column(name = "horario_atencion")
    private String horarioAtencion;

    @Column(name = "persona_responsable")
    private String personaResponsable;

    @Column(name = "fase_concurso")
    private String faseConcurso;

    @Column(name = "nota_importante", columnDefinition = "TEXT")
    private String notaImportante;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "vacantes_requisitos", joinColumns = @JoinColumn(name = "vacante_id"))
    @Column(name = "requisito", columnDefinition = "TEXT")
    private List<String> requisitos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "vacantes_documentacion", joinColumns = @JoinColumn(name = "vacante_id"))
    @Column(name = "documento", columnDefinition = "TEXT")
    private List<String> documentacionRequerida = new ArrayList<>();

    @Column(name = "creada_por", nullable = false)
    private UUID creadaPor;

    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

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

    // ===== Getters y Setters =====
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }

    public String getNumeroPlaza() { return numeroPlaza; }
    public void setNumeroPlaza(String numeroPlaza) { this.numeroPlaza = numeroPlaza; }

    public String getNivelTabular() { return nivelTabular; }
    public void setNivelTabular(String nivelTabular) { this.nivelTabular = nivelTabular; }

    public Integer getNumeroVacantes() { return numeroVacantes; }
    public void setNumeroVacantes(Integer numeroVacantes) { this.numeroVacantes = numeroVacantes; }

    public String getDescripcionFunciones() { return descripcionFunciones; }
    public void setDescripcionFunciones(String descripcionFunciones) { this.descripcionFunciones = descripcionFunciones; }

    public String getEscolaridad() { return escolaridad; }
    public void setEscolaridad(String escolaridad) { this.escolaridad = escolaridad; }

    public String getExperiencia() { return experiencia; }
    public void setExperiencia(String experiencia) { this.experiencia = experiencia; }

    public String getConocimientos() { return conocimientos; }
    public void setConocimientos(String conocimientos) { this.conocimientos = conocimientos; }

    public String getHabilidades() { return habilidades; }
    public void setHabilidades(String habilidades) { this.habilidades = habilidades; }

    public String getActitudes() { return actitudes; }
    public void setActitudes(String actitudes) { this.actitudes = actitudes; }

    public BigDecimal getPercepcionBruta() { return percepcionBruta; }
    public void setPercepcionBruta(BigDecimal percepcionBruta) { this.percepcionBruta = percepcionBruta; }

    public BigDecimal getPercepcionNeta() { return percepcionNeta; }
    public void setPercepcionNeta(BigDecimal percepcionNeta) { this.percepcionNeta = percepcionNeta; }

    public String getCiudadPlaza() { return ciudadPlaza; }
    public void setCiudadPlaza(String ciudadPlaza) { this.ciudadPlaza = ciudadPlaza; }

    public String getUbicacionPlaza() { return ubicacionPlaza; }
    public void setUbicacionPlaza(String ubicacionPlaza) { this.ubicacionPlaza = ubicacionPlaza; }

    public String getLugarRecepcionDocumentos() { return lugarRecepcionDocumentos; }
    public void setLugarRecepcionDocumentos(String lugarRecepcionDocumentos) { this.lugarRecepcionDocumentos = lugarRecepcionDocumentos; }

    public LocalDate getFechaExpedicion() { return fechaExpedicion; }
    public void setFechaExpedicion(LocalDate fechaExpedicion) { this.fechaExpedicion = fechaExpedicion; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }

    public String getHorarioAtencion() { return horarioAtencion; }
    public void setHorarioAtencion(String horarioAtencion) { this.horarioAtencion = horarioAtencion; }

    public String getPersonaResponsable() { return personaResponsable; }
    public void setPersonaResponsable(String personaResponsable) { this.personaResponsable = personaResponsable; }

    public String getFaseConcurso() { return faseConcurso; }
    public void setFaseConcurso(String faseConcurso) { this.faseConcurso = faseConcurso; }

    public String getNotaImportante() { return notaImportante; }
    public void setNotaImportante(String notaImportante) { this.notaImportante = notaImportante; }

    public List<String> getRequisitos() { return requisitos; }
    public void setRequisitos(List<String> requisitos) { this.requisitos = requisitos; }

    public List<String> getDocumentacionRequerida() { return documentacionRequerida; }
    public void setDocumentacionRequerida(List<String> documentacionRequerida) { this.documentacionRequerida = documentacionRequerida; }

    public UUID getCreadaPor() { return creadaPor; }
    public void setCreadaPor(UUID creadaPor) { this.creadaPor = creadaPor; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}