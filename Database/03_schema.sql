USE GestionIncidencias;

CREATE TABLE IF NOT EXISTS mail_management (
  message_id VARCHAR(255) PRIMARY KEY,
  incidencia_generada BOOLEAN NOT NULL DEFAULT FALSE,
  asignada BOOLEAN NOT NULL DEFAULT FALSE,
  tecnico_asignado VARCHAR(150) NOT NULL DEFAULT '',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tecnico (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL,
  email VARCHAR(255) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS incidencia_inbox (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  message_id VARCHAR(255) NOT NULL,
  mailbox VARCHAR(255) NOT NULL,
  received_date_time VARCHAR(100) NOT NULL,
  sender VARCHAR(255) NOT NULL,
  subject VARCHAR(500) NOT NULL,
  summary VARCHAR(1000) NOT NULL,
  tecnico_asignado VARCHAR(150) NOT NULL,
  tecnico_email VARCHAR(255) NOT NULL,
  categoria_id BIGINT NULL,
  prioridad VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
  resuelta BOOLEAN NOT NULL DEFAULT FALSE,
  rechazada BOOLEAN NOT NULL DEFAULT FALSE,
  en_progreso BOOLEAN NOT NULL DEFAULT FALSE,
  resolucion_texto TEXT NULL,
  resuelta_por VARCHAR(255) NULL,
  rechazo_motivo TEXT NULL,
  rechazada_por VARCHAR(255) NULL,
  rechazada_at TIMESTAMP NULL,
  resolved_at TIMESTAMP NULL,
  assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_incidencia_inbox_message (message_id)
);

CREATE TABLE IF NOT EXISTS incidencia_prioridad (
  codigo VARCHAR(20) PRIMARY KEY
);
INSERT IGNORE INTO incidencia_prioridad (codigo) VALUES ('URGENTE'), ('ALTA'), ('NORMAL'), ('BAJA');

CREATE TABLE IF NOT EXISTS incidencia_nota (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  incidencia_id BIGINT NOT NULL,
  tecnico VARCHAR(150) NOT NULL,
  observacion VARCHAR(500) NOT NULL,
  detalle TEXT NULL,
  accion_realizada VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS incidencia_historico (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  incidencia_id BIGINT NOT NULL,
  actor VARCHAR(255) NOT NULL,
  descripcion VARCHAR(1000) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS incidencia_tracking_token (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  incidencia_id BIGINT NOT NULL,
  token VARCHAR(255) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  expires_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_incidencia_tracking_token (token)
);

CREATE TABLE IF NOT EXISTS categoria (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL,
  abreviatura VARCHAR(20) NOT NULL,
  color_hex VARCHAR(20) NOT NULL DEFAULT '#f3f4f6',
  UNIQUE KEY uq_categoria_nombre (nombre),
  UNIQUE KEY uq_categoria_abreviatura (abreviatura)
);
