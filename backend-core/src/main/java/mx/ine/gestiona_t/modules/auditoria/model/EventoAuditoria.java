package mx.ine.gestiona_t.modules.auditoria.model;

import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.auditoria.model.enums.ActorTipo;
import mx.ine.gestiona_t.modules.auditoria.model.enums.CategoriaEvento;
import mx.ine.gestiona_t.modules.auditoria.model.enums.NivelSeveridad;
import mx.ine.gestiona_t.modules.auditoria.model.enums.TipoEvento;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_eventos", indexes = {
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_categoria", columnList = "categoria"),
    @Index(name = "idx_audit_tipo", columnList = "tipoEvento"),
    @Index(name = "idx_audit_actor", columnList = "actorId"),
    @Index(name = "idx_audit_severidad", columnList = "severidad"),
    @Index(name = "idx_audit_recurso", columnList = "recursoAfectado"),
    @Index(name = "idx_audit_correlation", columnList = "correlationId")
})
public class EventoAuditoria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaEvento categoria;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoEvento tipoEvento;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelSeveridad severidad;
    
    @Column
    private UUID actorId;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ActorTipo actorTipo;
    
    @Column(nullable = false, length = 45)
    private String ipOrigen;
    
    @Column(length = 500)
    private String userAgent;
    
    @Column(length = 100)
    private String recursoAfectado;
    
    @Column(nullable = false, length = 1000)
    private String descripcion;
    
    @Column(columnDefinition = "jsonb")
    private String datosEvento;
    
    @Column(length = 100)
    private String hashDatos;
    
    @Column(length = 100)
    private String hashAnterior;
    
    @Column(nullable = false, length = 100)
    private String hashPropio;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 100)
    private String correlationId;
    
    @Column(length = 50)
    private String moduloOrigen;
    
    @Column(nullable = false)
    private boolean ancladoBlockchain;
    
    @Column(length = 200)
    private String transaccionBlockchain;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public CategoriaEvento getCategoria() { return categoria; }
    public void setCategoria(CategoriaEvento categoria) { this.categoria = categoria; }
    
    public TipoEvento getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(TipoEvento tipoEvento) { this.tipoEvento = tipoEvento; }
    
    public NivelSeveridad getSeveridad() { return severidad; }
    public void setSeveridad(NivelSeveridad severidad) { this.severidad = severidad; }
    
    public UUID getActorId() { return actorId; }
    public void setActorId(UUID actorId) { this.actorId = actorId; }
    
    public ActorTipo getActorTipo() { return actorTipo; }
    public void setActorTipo(ActorTipo actorTipo) { this.actorTipo = actorTipo; }
    
    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getRecursoAfectado() { return recursoAfectado; }
    public void setRecursoAfectado(String recursoAfectado) { this.recursoAfectado = recursoAfectado; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getDatosEvento() { return datosEvento; }
    public void setDatosEvento(String datosEvento) { this.datosEvento = datosEvento; }
    
    public String getHashDatos() { return hashDatos; }
    public void setHashDatos(String hashDatos) { this.hashDatos = hashDatos; }
    
    public String getHashAnterior() { return hashAnterior; }
    public void setHashAnterior(String hashAnterior) { this.hashAnterior = hashAnterior; }
    
    public String getHashPropio() { return hashPropio; }
    public void setHashPropio(String hashPropio) { this.hashPropio = hashPropio; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public String getModuloOrigen() { return moduloOrigen; }
    public void setModuloOrigen(String moduloOrigen) { this.moduloOrigen = moduloOrigen; }
    
    public boolean isAncladoBlockchain() { return ancladoBlockchain; }
    public void setAncladoBlockchain(boolean ancladoBlockchain) { this.ancladoBlockchain = ancladoBlockchain; }
    
    public String getTransaccionBlockchain() { return transaccionBlockchain; }
    public void setTransaccionBlockchain(String transaccionBlockchain) { this.transaccionBlockchain = transaccionBlockchain; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}