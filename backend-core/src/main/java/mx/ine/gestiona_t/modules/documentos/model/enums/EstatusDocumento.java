package mx.ine.gestiona_t.modules.documentos.model.enums;

public enum EstatusDocumento {
    CARGADO,
    EN_CLASIFICACION,
    EN_OCR,
    EN_VALIDACION_API,
    EN_VALIDACION_IA,
    EN_REVISION_MANUAL,
    VALIDADO_AUTOMATICO,
    VALIDADO_ASISTIDO,
    VALIDADO_MANUAL,
    RECHAZADO,
    INCONSISTENTE,
    SOSPECHOSO
}