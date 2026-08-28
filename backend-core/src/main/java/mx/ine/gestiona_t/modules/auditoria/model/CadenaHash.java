package mx.ine.gestiona_t.modules.auditoria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_cadena_hash", indexes = {
    @Index(name = "idx_cadena_secuencia", columnList = "secuencia", unique = true),
    @Index(name = "idx_cadena_evento", columnList = "eventoId", unique = true)
})
public class CadenaHash {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID eventoId;
    
    @Column(nullable = false, length = 100)
    private String hashEvento;
    
    @Column(length = 100)
    private String hashAnterior;
    
    @Column(nullable = false, unique = true)
    private Long secuencia;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UUID getEventoId() { return eventoId; }
    public void setEventoId(UUID eventoId) { this.eventoId = eventoId; }
    
    public String getHashEvento() { return hashEvento; }
    public void setHashEvento(String hashEvento) { this.hashEvento = hashEvento; }
    
    public String getHashAnterior() { return hashAnterior; }
    public void setHashAnterior(String hashAnterior) { this.hashAnterior = hashAnterior; }
    
    public Long getSecuencia() { return secuencia; }
    public void setSecuencia(Long secuencia) { this.secuencia = secuencia; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}