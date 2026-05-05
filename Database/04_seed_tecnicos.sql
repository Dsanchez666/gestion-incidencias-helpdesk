USE GestionIncidencias;

INSERT INTO tecnico (nombre, email, activo) VALUES
('Tecnico 1', 'tecnico1@enaire.es', TRUE),
('Tecnico 2', 'tecnico2@enaire.es', TRUE),
('Tecnico 3', 'tecnico3@enaire.es', TRUE);

INSERT INTO categoria (nombre, abreviatura) VALUES
('Infraestructura', 'INF'),
('Aplicaciones', 'APP'),
('Seguridad', 'SEG'),
('Comunicaciones', 'COM');
