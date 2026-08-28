package mx.ine.gestiona_t.modules.auditoria.aspect;

import mx.ine.gestiona_t.modules.auditoria.model.enums.CategoriaEvento;
import mx.ine.gestiona_t.modules.auditoria.model.enums.NivelSeveridad;
import mx.ine.gestiona_t.modules.auditoria.model.enums.TipoEvento;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotacion para interceptar automaticamente operaciones criticas
 * y registrarlas en el sistema de auditoria.
 * 
 * Uso:
 * @Auditable(
 *     categoria = CategoriaEvento.AUTH,
 *     tipo = TipoEvento.LOGIN_EXITOSO,
 *     severidad = NivelSeveridad.INFO
 * )
 * public TokenResponse login(LoginRequest request) { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    CategoriaEvento categoria();
    TipoEvento tipo();
    NivelSeveridad severidad() default NivelSeveridad.INFO;
    String descripcion() default "";
    String recurso() default "";
}