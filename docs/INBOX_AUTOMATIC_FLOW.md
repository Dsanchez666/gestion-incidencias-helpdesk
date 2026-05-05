# Flujo Automatico MFA + Bandeja de Entrada

## Objetivo

Mantener autenticacion MFA de EntraID y automatizar la navegacion hasta la bandeja de entrada con gestion de incidencias.

## Flujo funcional

1. `SplashComponent` muestra `Gestion automatica del Buzon de Incidencias` durante 3 segundos.
2. Redireccion automatica a `StartupComponent`.
3. `StartupComponent` consulta `GET /api/auth/entra/status`.
4. Si `loggedIn=false`, redirige automaticamente a `GET /api/auth/entra/login` (MFA).
5. Si `loggedIn=true`, crea sesion frontend y ejecuta `POST /api/mailboxes/graph/user/trace` para cargar carpetas de usuario.
6. Con carpetas cargadas, navega automaticamente a `/inbox`.
7. `InboxComponent` carga mensajes de `Bandeja de entrada` desde `GET /api/inbox/gestion?summaryLength=N`.
8. `N` se toma de `frontend/src/assets/Mailboxes_Conf.json` en `ui.mailPreviewLength` (por defecto `50`).

## Datos mostrados en Inbox

- Fecha y hora de recepcion.
- Usuario de envio.
- Subject.
- Resumen configurable.
- Check de incidencia generada.
- Check de asignada.
- Tecnico asignado (selector).

## Endpoints backend usados

- `GET /api/auth/entra/status`
- `GET /api/auth/entra/login`
- `POST /api/mailboxes/graph/user/trace`
- `GET /api/inbox/gestion?summaryLength=50`
- `PATCH /api/inbox/gestion/{messageId}/incidencia`
- `PATCH /api/inbox/gestion/{messageId}/asignacion`
- `GET /api/tecnicos`

## Persistencia MySQL

Scripts en `Database/`:

- `01_create_database.sql`
- `02_create_user.sql`
- `03_schema.sql`
- `04_seed_tecnicos.sql`

Configuracion actual:

- BBDD: `GestionIncidencias`
- Usuario: `GestorIncidencias`
- Clave: `Gestor123`
