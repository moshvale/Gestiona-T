package mx.ine.gestiona_t.modules.documentos.dto.response;

import java.util.UUID;

public record UploadResponse(
    UUID documentoId,
    String folio,
    String nombreArchivo,
    Long tamanoBytes,
    String storagePath,
    String mensaje
) {}