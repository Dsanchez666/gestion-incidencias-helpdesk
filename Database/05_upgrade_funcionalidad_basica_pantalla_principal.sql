USE GestionIncidencias;

ALTER TABLE tecnico
  ADD COLUMN email VARCHAR(255) NULL;
ALTER TABLE tecnico
  ADD COLUMN password_hash VARCHAR(255) NULL;

UPDATE tecnico
SET email = CONCAT(REPLACE(LOWER(nombre), ' ', ''), '@enaire.es')
WHERE email IS NULL OR email = '';

ALTER TABLE tecnico
  MODIFY COLUMN email VARCHAR(255) NOT NULL;

CREATE TABLE IF NOT EXISTS tecnico_password_reset_token (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tecnico_id BIGINT NOT NULL,
  token VARCHAR(255) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  used BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_tecnico_reset_token (token)
);

-- Crear tabla de prioridades
CREATE TABLE IF NOT EXISTS prioridad (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(20) NOT NULL,
  color_hex VARCHAR(20) NOT NULL DEFAULT '#f3f4f6',
  UNIQUE KEY uq_prioridad_nombre (nombre)
);

INSERT INTO prioridad (nombre, color_hex)
SELECT 'URGENTE', '#fee2e2' WHERE NOT EXISTS (SELECT 1 FROM prioridad WHERE nombre = 'URGENTE');
INSERT INTO prioridad (nombre, color_hex)
SELECT 'ALTA', '#fef3c7' WHERE NOT EXISTS (SELECT 1 FROM prioridad WHERE nombre = 'ALTA');
INSERT INTO prioridad (nombre, color_hex)
SELECT 'NORMAL', '#dbeafe' WHERE NOT EXISTS (SELECT 1 FROM prioridad WHERE nombre = 'NORMAL');
INSERT INTO prioridad (nombre, color_hex)
SELECT 'BAJA', '#dcfce7' WHERE NOT EXISTS (SELECT 1 FROM prioridad WHERE nombre = 'BAJA');

-- Agregar columna prioridad_id
-- ALTER TABLE incidencia_inbox ADD COLUMN prioridad_id BIGINT NULL;
-- (Ya existe, comentado)

-- Obtener IDs de prioridades
SET @normal_id = (SELECT id FROM prioridad WHERE nombre='NORMAL' LIMIT 1);
SET @urgente_id = (SELECT id FROM prioridad WHERE nombre='URGENTE' LIMIT 1);
SET @alta_id = (SELECT id FROM prioridad WHERE nombre='ALTA' LIMIT 1);
SET @baja_id = (SELECT id FROM prioridad WHERE nombre='BAJA' LIMIT 1);

-- Migrar datos de prioridad VARCHAR a prioridad_id
UPDATE incidencia_inbox 
SET prioridad_id = CASE 
    WHEN prioridad = 'URGENTE' THEN @urgente_id
    WHEN prioridad = 'ALTA' THEN @alta_id
    WHEN prioridad = 'BAJA' THEN @baja_id
    WHEN prioridad = 'NORMAL' THEN @normal_id
    ELSE @normal_id
  END
WHERE prioridad_id IS NULL;

-- Asegurar que todas tengan prioridad_id (asignar NORMAL a NULL)
UPDATE incidencia_inbox 
SET prioridad_id = @normal_id 
WHERE prioridad_id IS NULL;

-- Dropear la columna prioridad VARCHAR antigua
ALTER TABLE incidencia_inbox
  DROP COLUMN prioridad;

CREATE TABLE IF NOT EXISTS categoria (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL,
  abreviatura VARCHAR(20) NOT NULL,
  color_hex VARCHAR(20) NOT NULL DEFAULT '#f3f4f6',
  UNIQUE KEY uq_categoria_nombre (nombre),
  UNIQUE KEY uq_categoria_abreviatura (abreviatura)
);
-- color_hex ya está en la definición de CREATE TABLE

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
