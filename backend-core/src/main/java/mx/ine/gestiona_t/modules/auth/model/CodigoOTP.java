package mx.ine.gestiona_t.modules.auth.model;

import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.auth.model.enums.CanalOTP;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "codigos_otp", indexes = {
    @Index(name = "idx_otp_aspirante", columnList = "aspiranteId"),
    @Index(name = "idx_otp_expiracion", columnList = "fechaExpiracion")
})
public class CodigoOTP {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID aspiranteId;
    
    @Column(nullable = false)
    private String codigoHash;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CanalOTP canal;
    
    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;
    
    @Column(nullable = false)
    private boolean utilizado;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UUID getAspiranteId() { return aspiranteId; }
    public void setAspiranteId(UUID aspiranteId) { this.aspiranteId = aspiranteId; }
    
    public String getCodigoHash() { return codigoHash; }
    public void setCodigoHash(String codigoHash) { this.codigoHash = codigoHash; }
    
    public CanalOTP getCanal() { return canal; }
    public void setCanal(CanalOTP canal) { this.canal = canal; }
    
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    
    public boolean isUtilizado() { return utilizado; }
    public void setUtilizado(boolean utilizado) { this.utilizado = utilizado; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}