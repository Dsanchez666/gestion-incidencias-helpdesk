# Arranque del Proyecto

Estado actual del entorno local:
- JDK 22 en `C:\Program Files\Java\jdk-22`
- Node.js `21.7.3` y npm `10.5.0` (Angular 20 muestra warning por no ser LTS, pero compila)
- No requiere base de datos

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

## Arranque

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

Nota sobre autenticación y configuración:
- El proyecto soporta login interactivo con EntraID. Antes de arrancar el frontend configura `frontend/src/assets/EntraID_Conf.json` y `frontend/src/assets/Mailboxes_Conf.json` (usar `*.example.json` como plantilla).
- El `StartupComponent` llama a `GET /api/auth/entra/status` para decidir el flujo de login; este endpoint está permitido sin autenticación.
- No ejecuto compilaciones por ti; sigue tus propias reglas locales para iniciar servicios.
