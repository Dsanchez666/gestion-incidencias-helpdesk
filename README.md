# Gestion de Incidencias - Buzon Helpdesk ETNA

Proyecto enterprise con backend Java/Spring y frontend Angular siguiendo Hexagonal Architecture + DDD + TDD.

## Enterprise System Prompt

Referencia obligatoria del proyecto:
- `.codex/enterprise-system-prompt.md`

## Estructura

- `backend/`: dominio, aplicacion e infraestructura (legacy).
- `backend-inc/`: backend limpio para login root/root.
- `frontend/`: arquitectura modular por feature.
- `docs/`: analisis de coherencia e issues.

## Stack

- Backend: Java 22, Spring Boot, Spring Data JPA, Hibernate, PostgreSQL.
- Testing: JUnit5, Mockito, Testcontainers.
- Frontend: Angular, TypeScript, RxJS.
- Tooling: ESLint, Prettier, SonarLint.

## Compilar

Backend (Spring Boot):
```powershell
cd backend-inc
set JAVA_HOME=C:\Program Files\Java\jdk-22
set PATH=%JAVA_HOME%\bin;%PATH%
mvn -DskipTests package
```
Si Maven falla por permisos en `C:\Users\<usuario>\.m2`, usa un repo local:
```powershell
cd backend-inc
set JAVA_HOME=C:\Program Files\Java\jdk-22
set PATH=%JAVA_HOME%\bin;%PATH%
mvn -Dmaven.repo.local=.\.m2 -DskipTests package
```

Frontend (Angular):
```powershell
cd frontend
npm install
npm run build
```

## Arranque (frontend y backend)

Requisitos:
- Java 22 y Maven en PATH.
- Node.js y npm en PATH.
- No se requiere base de datos para el backend-inc.

Backend (puerto 4000 por defecto):
```powershell
cd backend-inc
set JAVA_HOME=C:\Program Files\Java\jdk-22
set PATH=%JAVA_HOME%\bin;%PATH%
mvn spring-boot:run
```
Si Maven falla por permisos en `C:\Users\<usuario>\.m2`, usa un repo local:
```powershell
cd backend-inc
set JAVA_HOME=C:\Program Files\Java\jdk-22
set PATH=%JAVA_HOME%\bin;%PATH%
mvn -Dmaven.repo.local=.\.m2 spring-boot:run
```

Frontend (puerto 3000 por defecto):
```powershell
cd frontend
npm start
```

URLs:
- Frontend: `http://localhost:3000`
- Backend: `http://localhost:4000`

Credenciales de ejemplo:
- `operador` / `operador123`
- `supervisor` / `supervisor123`

Notas:
- El frontend usa `ng serve` con puerto 3000 y configuración de desarrollo (sin optimizaciones).
