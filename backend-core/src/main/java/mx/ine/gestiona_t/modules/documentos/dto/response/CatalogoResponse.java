package mx.ine.gestiona_t.modules.documentos.dto.response;

public record CatalogoResponse(
    Long id,
    String tipo,
    String nombre,
    String clave,
    String entidadFederativa,
    boolean acreditada,
    String fuenteOficial
) {}