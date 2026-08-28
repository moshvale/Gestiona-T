package mx.ine.gestiona_t.modules.cv.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.cv.model.enums.NivelMando;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cv_experiencia_laboral")
public class ExperienciaLaboral {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id", nullable = false)
    @JsonIgnore
    private CvEstructurado cv;
    
    @Column(nullable = false, length = 200)
    private String institucion;
    
    @Column(length = 13)
    private String rfcInstitucion;
    
    @Column(nullable = false, length = 100)
    private String puesto;
    
    @Column(nullable = false, length = 1000)
    private String funciones;
    
    @Column(nullable = false)
    private LocalDate fechaInicio;
    
    @Column
    private LocalDate fechaTermino;
    
    @Column(nullable = false)
    private boolean actualmenteLaborando;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelMando nivelMando;
    
    @Column(length = 500)
    private String documentoSoportePath;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public CvEstructurado getCv() { return cv; }
    public void setCv(CvEstructurado cv) { this.cv = cv; }
    
    public String getInstitucion() { return institucion; }
    public void setInstitucion(String institucion) { this.institucion = institucion; }
    
    public String getRfcInstitucion() { return rfcInstitucion; }
    public void setRfcInstitucion(String rfcInstitucion) { this.rfcInstitucion = rfcInstitucion; }
    
    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }
    
    public String getFunciones() { return funciones; }
    public void setFunciones(String funciones) { this.funciones = funciones; }
    
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public LocalDate getFechaTermino() { return fechaTermino; }
    public void setFechaTermino(LocalDate fechaTermino) { this.fechaTermino = fechaTermino; }
    
    public boolean isActualmenteLaborando() { return actualmenteLaborando; }
    public void setActualmenteLaborando(boolean actualmenteLaborando) { this.actualmenteLaborando = actualmenteLaborando; }
    
    public NivelMando getNivelMando() { return nivelMando; }
    public void setNivelMando(NivelMando nivelMando) { this.nivelMando = nivelMando; }
    
    public String getDocumentoSoportePath() { return documentoSoportePath; }
    public void setDocumentoSoportePath(String documentoSoportePath) { this.documentoSoportePath = documentoSoportePath; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}