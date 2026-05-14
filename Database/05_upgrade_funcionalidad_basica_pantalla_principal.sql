USE GestionIncidencias;

ALTER TABLE tecnico
  ADD COLUMN IF NOT EXISTS email VARCHAR(255) NULL;

UPDATE tecnico
SET email = CONCAT(REPLACE(LOWER(nombre), ' ', ''), '@enaire.es')
WHERE email IS NULL OR email = '';

ALTER TABLE tecnico
  MODIFY COLUMN email VARCHAR(255) NOT NULL;

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
  resolved_at TIMESTAMP NULL,
  assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_incidencia_inbox_message (message_id)
);

ALTER TABLE incidencia_inbox
  ADD COLUMN IF NOT EXISTS categoria_id BIGINT NULL;
ALTER TABLE incidencia_inbox
  ADD COLUMN IF NOT EXISTS resuelta BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE incidencia_inbox
  ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMP NULL;
ALTER TABLE incidencia_inbox
  ADD COLUMN IF NOT EXISTS en_progreso BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS categoria (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL,
  abreviatura VARCHAR(20) NOT NULL,
  color_hex VARCHAR(20) NOT NULL DEFAULT '#f3f4f6',
  UNIQUE KEY uq_categoria_nombre (nombre),
  UNIQUE KEY uq_categoria_abreviatura (abreviatura)
);
ALTER TABLE categoria
  ADD COLUMN IF NOT EXISTS color_hex VARCHAR(20) NOT NULL DEFAULT '#f3f4f6';

INSERT INTO categoria (nombre, abreviatura, color_hex)
SELECT 'Infraestructura', 'INF', '#fef3c7'
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE abreviatura = 'INF');

INSERT INTO categoria (nombre, abreviatura, color_hex)
SELECT 'Aplicaciones', 'APP', '#dbeafe'
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE abreviatura = 'APP');

INSERT INTO categoria (nombre, abreviatura, color_hex)
SELECT 'Seguridad', 'SEG', '#fee2e2'
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE abreviatura = 'SEG');

INSERT INTO categoria (nombre, abreviatura, color_hex)
SELECT 'Comunicaciones', 'COM', '#dcfce7'
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE abreviatura = 'COM');

CREATE TABLE IF NOT EXISTS incidencia_nota (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  incidencia_id BIGINT NOT NULL,
  tecnico VARCHAR(150) NOT NULL,
  observacion VARCHAR(500) NOT NULL,
  detalle TEXT NULL,
  accion_realizada VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
