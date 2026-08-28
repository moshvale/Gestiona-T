-- ============================================================
-- SEED: Configuracion de Retencion por Categoria
-- ============================================================

INSERT INTO audit_configuracion_retencion (categoria, anios_retencion, activo, descripcion) VALUES
('AUTH', 10, true, 'Eventos de autenticacion y autorizacion'),
('CV', 10, true, 'Eventos de gestion de CV'),
('DOCUMENTOS', 10, true, 'Eventos de validacion documental'),
('MATCHING', 10, true, 'Eventos de matching curricular'),
('CARTA_DECLARATORIA', 15, true, 'Eventos de carta declaratoria (mayor retencion por valor legal)'),
('FIRMA', 15, true, 'Eventos de firma electronica (mayor retencion por valor legal)'),
('SEGURIDAD', 10, true, 'Eventos de seguridad'),
('SISTEMA', 5, true, 'Eventos de sistema'),
('ADMIN', 10, true, 'Eventos administrativos');