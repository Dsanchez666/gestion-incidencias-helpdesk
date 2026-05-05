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

## Pantalla principal (rama features/Funcionalidad-basica-pantalla-principal)

- Layout en 4 bloques: `10%` cabecera, `10%` filtros/opciones, `40%` pendientes, resto incidencias asignadas.
- Filtro por defecto: `Ocultar correos Automaticos` para `ServicioNotificacionETNA@enaire.es`.
- Filtros por campo: remitente, subject, resumen, tecnico.
- Filtro global de texto libre.
- Accion de asignacion con boton `+` junto al tecnico seleccionado.
- Accion de asignacion masiva por seleccion multiple de correos.
- Ordenacion en tablas por clic en cabeceras.
- Catalogo de categorias (id, nombre, abreviatura) y asignacion manual por incidencia.
- Boton `Categorizacion Automatica` preparado como placeholder.
- Al asignar:
  - se marca incidencia/asignacion,
  - se persiste en tabla `incidencia_inbox`,
  - se envia email al tecnico (SMTP configurable).

## Perfiles en cabecera y visibilidad

- `ADMIN`: usuario con acceso efectivo al buzon (`Mail.Read` operativo). Ve correos + incidencias.
- `CONSULTA`: usuario autenticado sin acceso efectivo al buzon. Ve solo incidencias.
- `RESOLUTOR`: usuario cuyo identificador coincide con tecnico activo. Ve solo incidencias.

## Endpoints backend usados

- `GET /api/auth/entra/status`
- `GET /api/auth/entra/login`
- `POST /api/mailboxes/graph/user/trace`
- `GET /api/inbox/gestion?summaryLength=50`
- `GET /api/inbox/gestion/context`
- `GET /api/inbox/gestion/incidencias`
- `PATCH /api/inbox/gestion/{messageId}/incidencia`
- `PATCH /api/inbox/gestion/{messageId}/asignacion`
- `POST /api/inbox/gestion/{messageId}/asignar-incidencia`
- `GET /api/tecnicos`

## Persistencia MySQL

Scripts en `Database/`:

- `01_create_database.sql`
- `02_create_user.sql`
- `03_schema.sql`
- `04_seed_tecnicos.sql`

Nuevas tablas/campos:

- `tecnico.email`
- `incidencia_inbox` (persistencia de incidencias asignadas)

Configuracion actual:

- BBDD: `GestionIncidencias`
- Usuario: `GestorIncidencias`
- Clave: `Gestor123`
