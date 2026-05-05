CREATE USER IF NOT EXISTS 'GestorIncidencias'@'localhost' IDENTIFIED BY 'Gestor123';
GRANT ALL PRIVILEGES ON GestionIncidencias.* TO 'GestorIncidencias'@'localhost';
FLUSH PRIVILEGES;
