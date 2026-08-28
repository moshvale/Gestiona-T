package mx.ine.gestiona_t.modules.documentos.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "catalogo_instituciones", indexes = {
    @Index(name = "idx_cat_tipo", columnList = "tipo"),
    @Index(name = "idx_cat_nombre", columnList = "nombre")
})
public class CatalogoInstitucion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 10)
    private String tipo;
    
    @Column(nullable = false, length = 300)
    private String nombre;
    
    @Column(length = 20)
    private String clave;
    
    @Column(length = 100)
    private String entidadFederativa;
    
    @Column(nullable = false)
    private boolean acreditada;
    
    @Column
    private LocalDate fechaActualizacion;
    
    @Column(length = 50)
    private String fuenteOficial;
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    
    public String getEntidadFederativa() { return entidadFederativa; }
    public void setEntidadFederativa(String entidadFederativa) { this.entidadFederativa = entidadFederativa; }
    
    public boolean isAcreditada() { return acreditada; }
    public void setAcreditada(boolean acreditada) { this.acreditada = acreditada; }
    
    public LocalDate getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDate fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    public String getFuenteOficial() { return fuenteOficial; }
    public void setFuenteOficial(String fuenteOficial) { this.fuenteOficial = fuenteOficial; }
}