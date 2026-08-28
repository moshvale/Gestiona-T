package mx.ine.gestiona_t.modules.cv.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cv_idiomas")
public class CvIdioma {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id", nullable = false)
    private CvInstitucional cv;

    @Column(length = 100, nullable = false)
    private String idioma;

    @Column(name = "nivel_escritura", length = 50)
    private String nivelEscritura;

    @Column(name = "nivel_lectura", length = 50)
    private String nivelLectura;

    @Column(name = "nivel_conversacion", length = 50)
    private String nivelConversacion;

    // ✅ Campos de auditoría
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

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CvInstitucional getCv() { return cv; }
    public void setCv(CvInstitucional cv) { this.cv = cv; }

    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }

    public String getNivelEscritura() { return nivelEscritura; }
    public void setNivelEscritura(String nivelEscritura) { this.nivelEscritura = nivelEscritura; }

    public String getNivelLectura() { return nivelLectura; }
    public void setNivelLectura(String nivelLectura) { this.nivelLectura = nivelLectura; }

    public String getNivelConversacion() { return nivelConversacion; }
    public void setNivelConversacion(String nivelConversacion) { this.nivelConversacion = nivelConversacion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}