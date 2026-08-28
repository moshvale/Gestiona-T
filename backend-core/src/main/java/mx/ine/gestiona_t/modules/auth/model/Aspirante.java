package mx.ine.gestiona_t.modules.auth.model;

import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.auth.model.enums.EstatusAspirante;
import mx.ine.gestiona_t.modules.auth.model.enums.MetodoIdentificacion;
import mx.ine.gestiona_t.modules.auth.model.enums.TipoPersona; // ✅ NUEVO IMPORT

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "aspirantes", indexes = {
    @Index(name = "idx_aspirante_folio", columnList = "folio"),
    @Index(name = "idx_aspirante_curp", columnList = "curp"),
    @Index(name = "idx_aspirante_correo", columnList = "correoElectronico"),
    // ✅ NUEVO ÍNDICE: para búsquedas por número de empleado (solo internos)
    @Index(name = "idx_aspirante_numero_empleado", columnList = "numero_empleado")
})
public class Aspirante {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 36)
    private String folio;

    @Column(nullable = false, length = 200)
    private String nombreCompleto;

    @Column(unique = true, length = 18)
    private String curp;

    @Column(unique = true, length = 13)
    private String rfc;

    @Column(unique = true, nullable = false, length = 100)
    private String correoElectronico;

    @Column(nullable = false, length = 20)
    private String telefonoMovil;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstatusAspirante estatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MetodoIdentificacion metodoIdentificacion;

    @Column(nullable = false)
    private int nivelConfianza;

    @Column
    private LocalDateTime fechaNacimiento;

    @Column(length = 2)
    private String entidadFederativa;

    @Column
    private LocalDateTime fechaRegistro;

    @Column
    private LocalDateTime fechaUltimoAcceso;

    @Column(nullable = false)
    private boolean activo;

    // ✅ NUEVOS CAMPOS: Clasificación del aspirante (EXTERNO/INTERNO) y número de empleado
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_persona", nullable = false, length = 20)
    private TipoPersona tipoPersona = TipoPersona.EXTERNO;

    @Column(name = "numero_empleado", length = 20, unique = true)
    private String numeroEmpleado;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        fechaRegistro = LocalDateTime.now();
        if (folio == null) {
            folio = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ============================================
    // GETTERS Y SETTERS EXISTENTES
    // ============================================
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getCurp() { return curp; }
    public void setCurp(String curp) { this.curp = curp; }

    public String getRfc() { return rfc; }
    public void setRfc(String rfc) { this.rfc = rfc; }

    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    public String getTelefonoMovil() { return telefonoMovil; }
    public void setTelefonoMovil(String telefonoMovil) { this.telefonoMovil = telefonoMovil; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public EstatusAspirante getEstatus() { return estatus; }
    public void setEstatus(EstatusAspirante estatus) { this.estatus = estatus; }

    public MetodoIdentificacion getMetodoIdentificacion() { return metodoIdentificacion; }
    public void setMetodoIdentificacion(MetodoIdentificacion metodoIdentificacion) { this.metodoIdentificacion = metodoIdentificacion; }

    public int getNivelConfianza() { return nivelConfianza; }
    public void setNivelConfianza(int nivelConfianza) { this.nivelConfianza = nivelConfianza; }

    public LocalDateTime getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDateTime fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getEntidadFederativa() { return entidadFederativa; }
    public void setEntidadFederativa(String entidadFederativa) { this.entidadFederativa = entidadFederativa; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getFechaUltimoAcceso() { return fechaUltimoAcceso; }
    public void setFechaUltimoAcceso(LocalDateTime fechaUltimoAcceso) { this.fechaUltimoAcceso = fechaUltimoAcceso; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // ✅ NUEVOS GETTERS Y SETTERS
    public TipoPersona getTipoPersona() { return tipoPersona; }
    public void setTipoPersona(TipoPersona tipoPersona) { this.tipoPersona = tipoPersona; }

    public String getNumeroEmpleado() { return numeroEmpleado; }
    public void setNumeroEmpleado(String numeroEmpleado) { this.numeroEmpleado = numeroEmpleado; }
}