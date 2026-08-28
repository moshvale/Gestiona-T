package mx.ine.gestiona_t.modules.cartadeclaratoria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "aceptaciones_bloques", indexes = {
    @Index(name = "idx_aceptacion_carta", columnList = "cartaId"),
    @Index(name = "idx_aceptacion_bloque", columnList = "bloqueId")
})
public class AceptacionBloque {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID cartaId;
    
    @Column(nullable = false)
    private Integer bloqueId;
    
    @Column(nullable = false)
    private boolean aceptado;
    
    @Column(nullable = false)
    private LocalDateTime timestampAceptacion;
    
    @Column(nullable = false, length = 45)
    private String ipOrigen;
    
    @Column(length = 500)
    private String userAgent;
    
    @Column(length = 100)
    private String hashTextoBloque;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UUID getCartaId() { return cartaId; }
    public void setCartaId(UUID cartaId) { this.cartaId = cartaId; }
    
    public Integer getBloqueId() { return bloqueId; }
    public void setBloqueId(Integer bloqueId) { this.bloqueId = bloqueId; }
    
    public boolean isAceptado() { return aceptado; }
    public void setAceptado(boolean aceptado) { this.aceptado = aceptado; }
    
    public LocalDateTime getTimestampAceptacion() { return timestampAceptacion; }
    public void setTimestampAceptacion(LocalDateTime timestampAceptacion) { 
        this.timestampAceptacion = timestampAceptacion; 
    }
    
    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getHashTextoBloque() { return hashTextoBloque; }
    public void setHashTextoBloque(String hashTextoBloque) { this.hashTextoBloque = hashTextoBloque; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}