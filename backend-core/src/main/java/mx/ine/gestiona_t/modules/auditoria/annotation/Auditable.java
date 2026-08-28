package mx.ine.gestiona_t.modules.auditoria.annotation;

import mx.ine.gestiona_t.modules.auditoria.model.enums.NivelSeveridad;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String categoria();           // Ej: "DOCUMENTOS", "CARTA_DECLARATORIA"
    String tipo();                // Ej: "ELIMINACION", "FIRMA_CARTA"
    NivelSeveridad severidad() default NivelSeveridad.INFO;
    String recurso() default "";  // Ej: "Documento", "CartaDeclaratoria"
    String descripcion() default "";
}