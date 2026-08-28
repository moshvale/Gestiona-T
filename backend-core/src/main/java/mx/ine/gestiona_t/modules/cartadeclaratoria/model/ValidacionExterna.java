package mx.ine.gestiona_t.modules.cartadeclaratoria.model;

import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.TipoValidacionExterna;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "validaciones_externas_carta", indexes = {
    @Index(name = "idx_val_ext_carta", columnList = "cartaId")
})
public class ValidacionExterna {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID cartaId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoValidacionExterna tipoValidacion;
    
    @Column(nullable = false)
    private boolean resultado;
    
    @Column(columnDefinition = "TEXT")
    private String respuestaApi;
    
    @Column(length = 1000)
    private String mensaje;
    
    @Column
    private LocalDateTime fechaConsulta;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (fechaConsulta == null) {
            fechaConsulta = LocalDateTime.now();
        }
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UUID getCartaId() { return cartaId; }
    public void setCartaId(UUID cartaId) { this.cartaId = cartaId; }
    
    public TipoValidacionExterna getTipoValidacion() { return tipoValidacion; }
    public void setTipoValidacion(TipoValidacionExterna tipoValidacion) { 
        this.tipoValidacion = tipoValidacion; 
    }
    
    public boolean isResultado() { return resultado; }
    public void setResultado(boolean resultado) { this.resultado = resultado; }
    
    public String getRespuestaApi() { return respuestaApi; }
    public void setRespuestaApi(String respuestaApi) { this.respuestaApi = respuestaApi; }
    
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    
    public LocalDateTime getFechaConsulta() { return fechaConsulta; }
    public void setFechaConsulta(LocalDateTime fechaConsulta) { this.fechaConsulta = fechaConsulta; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}