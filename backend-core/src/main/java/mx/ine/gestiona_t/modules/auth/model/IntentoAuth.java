package mx.ine.gestiona_t.modules.auth.model;

import jakarta.persistence.*;
import mx.ine.gestiona_t.modules.auth.model.enums.ResultadoIntento;
import mx.ine.gestiona_t.modules.auth.model.enums.TipoIntento;
import java.time.LocalDateTime;

@Entity
@Table(name = "intentos_auth", indexes = {
    @Index(name = "idx_intento_ip", columnList = "ipOrigen"),
    @Index(name = "idx_intento_timestamp", columnList = "timestamp")
})
public class IntentoAuth {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 45)
    private String ipOrigen;
    
    @Column(length = 500)
    private String userAgent;
    
    @Column(length = 18)
    private String curpIntentada;
    
    @Column(length = 100)
    private String correoIntentado;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoIntento tipo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultadoIntento resultado;
    
    @Column(length = 500)
    private String motivoFallo;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getCurpIntentada() { return curpIntentada; }
    public void setCurpIntentada(String curpIntentada) { this.curpIntentada = curpIntentada; }
    
    public String getCorreoIntentado() { return correoIntentado; }
    public void setCorreoIntentado(String correoIntentado) { this.correoIntentado = correoIntentado; }
    
    public TipoIntento getTipo() { return tipo; }
    public void setTipo(TipoIntento tipo) { this.tipo = tipo; }
    
    public ResultadoIntento getResultado() { return resultado; }
    public void setResultado(ResultadoIntento resultado) { this.resultado = resultado; }
    
    public String getMotivoFallo() { return motivoFallo; }
    public void setMotivoFallo(String motivoFallo) { this.motivoFallo = motivoFallo; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}