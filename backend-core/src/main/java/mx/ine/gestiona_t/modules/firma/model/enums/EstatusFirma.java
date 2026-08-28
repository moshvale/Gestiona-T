package mx.ine.gestiona_t.modules.firma.model.enums;

public enum EstatusFirma {
    SOLICITADA,
    EN_VALIDACION_IDENTIDAD,
    EN_VALIDACION_BIOMETRIA,
    EN_VALIDACION_OTP,
    EN_FIRMA_DIGITAL,
    EN_TIMESTAMP,
    FIRMADA,
    RECHAZADA,
    EXPIRADA,
    ERROR
}