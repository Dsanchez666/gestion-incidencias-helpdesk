USE GestionIncidencias;

INSERT INTO tecnico (nombre, email, activo) VALUES
('Tecnico 1', 'tecnico1@enaire.es', TRUE),
('Tecnico 2', 'tecnico2@enaire.es', TRUE),
('Tecnico 3', 'tecnico3@enaire.es', TRUE);

INSERT INTO categoria (nombre, abreviatura, color_hex) VALUES
('Infraestructura', 'INF', '#fef3c7'),
('Aplicaciones', 'APP', '#dbeafe'),
('Seguridad', 'SEG', '#fee2e2'),
('Comunicaciones', 'COM', '#dcfce7');
