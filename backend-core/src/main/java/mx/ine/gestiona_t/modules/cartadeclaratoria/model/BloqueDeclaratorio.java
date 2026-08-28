package mx.ine.gestiona_t.modules.cartadeclaratoria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bloques_declaratorios")
public class BloqueDeclaratorio {
    
    @Id
    private Integer id;
    
    @Column(nullable = false, length = 100)
    private String titulo;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;
    
    @Column(nullable = false, length = 300)
    private String fundamentoLegal;
    
    @Column(nullable = false)
    private boolean obligatorio;
    
    @Column(nullable = false)
    private Integer orden;
    
    @Column(nullable = false)
    private boolean activo;
    
    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    
    public String getFundamentoLegal() { return fundamentoLegal; }
    public void setFundamentoLegal(String fundamentoLegal) { this.fundamentoLegal = fundamentoLegal; }
    
    public boolean isObligatorio() { return obligatorio; }
    public void setObligatorio(boolean obligatorio) { this.obligatorio = obligatorio; }
    
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}