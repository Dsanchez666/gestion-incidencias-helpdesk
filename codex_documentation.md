# Codex Documentation Guide (Proyecto Helpdesk)

## Proposito

Este documento define como documentar codigo y cambios en este repositorio para mantener coherencia con el pilar de **arquitectura hexagonal** descrito en `ARCHITECTURE.md`.

Objetivo principal:
- mejorar mantenibilidad,
- acelerar onboarding,
- y dejar claro el rol de cada pieza (dominio, aplicacion, puertos y adaptadores).

---

## Principios de documentacion

1. Documentar la intencion de negocio, no solo el "que hace" tecnico.
2. Explicar dependencias relevantes entre capas.
3. Evitar comentarios redundantes o obvios.
4. Priorizar trazabilidad de decisiones en integraciones externas (Entra, Graph, Exchange).
5. Mantener comentarios y docs sincronizados con endpoints y estructura real.

---

## Convenciones por capa (Hexagonal)

## 1) Dominio

Aplicable a:
- `backend-inc/src/main/java/com/company/backendinc/incidencia/Incidencia.java`
- `backend-inc/src/main/java/com/company/backendinc/mailbox/Mailbox.java`
- `backend-inc/src/main/java/com/company/backendinc/*/application/*`
- `backend-inc/src/main/java/com/company/backendinc/*/adapter/out/*`
- `frontend/src/app/core/auth/application/*`
- `frontend/src/app/features/*/application/*`
- `frontend/src/app/features/*/infrastructure/*`
- `frontend/src/app/features/*/domain/*`

Regla:
- describir reglas de negocio y restricciones del modelo,
- no mencionar detalles de transporte (HTTP) ni framework.

Plantilla recomendada (Java):
```java
/**
 * Entidad de dominio que representa <concepto de negocio>.
 *
 * Reglas:
 * - <regla 1>
 * - <regla 2>
 */
public class NombreEntidad { }
```

Plantilla recomendada (TypeScript):
```ts
/**
 * Modelo de dominio para <concepto> usado en casos de uso de la feature.
 */
export interface NombreModelo {}
```

## 2) Aplicacion (casos de uso)

Aplicable a:
- `frontend/src/app/features/incidencia/application/*`
- `frontend/src/app/features/mailbox/application/*`
- `frontend/src/app/core/auth/application/*`
- futuros `*UseCase` o `*Service` en backend.

Regla:
- documentar orquestacion, precondiciones y resultado funcional.

Plantilla:
```java
/**
 * Caso de uso: <nombre>.
 *
 * Orquesta <pasos de negocio> usando puertos de salida.
 *
 * @param ... datos de entrada del caso de uso
 * @return ... resultado funcional
 */
```

## 3) Adaptadores IN (entrada)

Aplicable a controladores Spring y componentes UI.

Backend:
- `AuthController`
- `EntraIdTestController`
- `MailboxController`
- `MailboxConnectionController`
- `IncidenciaController`

Regla:
- documentar contrato de entrada/salida y codigos esperados,
- evitar meter reglas de negocio complejas en comentarios de controlador.

Plantilla REST:
```java
/**
 * Endpoint de entrada para <caso de uso>.
 *
 * @return ResponseEntity con resultado funcional o error de validacion/integracion.
 */
```

Frontend UI:
- `frontend/src/app/features/*/ui/*`
- `frontend/src/app/core/auth/auth.guard.ts`
- `frontend/src/app/core/auth/auth.interceptor.ts`

Regla:
- documentar eventos de usuario, navegacion y estados de carga/error.

## 4) Adaptadores OUT (salida)

Aplicable a integraciones y carga de configuracion:
- `auth/entra/*`
- `mailbox/connection/*`
- `mailbox/config/*`
- `frontend/src/app/core/auth/*.service.ts`
- `frontend/src/app/features/*/infrastructure/*`

Regla:
- documentar claramente:
- dependencia externa usada,
- formato esperado,
- errores tecnicos traducidos a errores funcionales.

Plantilla:
```java
/**
 * Adaptador de salida para <sistema externo>.
 *
 * Responsabilidad:
 * - <operacion 1>
 * - <operacion 2>
 *
 * Nota: encapsula detalles de protocolo y mapeo de errores.
 */
```

---

## Convenciones de endpoints documentados

Cuando se añada o modifique un endpoint en backend, documentar:
1. Ruta y metodo HTTP.
2. Tipo de autenticacion esperada.
3. Payload de entrada.
4. Respuesta de exito.
5. Errores de negocio/integracion esperables.

Ejemplos actuales relevantes:
- `POST /api/auth/entra/app-token`
- `GET /api/auth/entra/login`
- `GET /api/auth/entra/callback`
- `POST /api/mailboxes/graph/trace`
- `POST /api/mailboxes/graph/user/trace`

---

## Checklist para cambios con Codex

Antes de cerrar una tarea, revisar:
1. ¿La documentacion respeta arquitectura hexagonal (sin acoplar dominio a framework)?
2. ¿Los comentarios explican decisiones y no solo implementacion obvia?
3. ¿Endpoint nuevo/actualizado documentado con contrato minimo?
4. ¿Se eliminaron referencias obsoletas de modulos no existentes?
5. ¿`ARCHITECTURE.md` y este documento siguen consistentes?

---

## Nota adicional sobre cambios de seguridad y CORS

Cuando se introduzcan cambios que afecten a autenticación, autorización o CORS, documenta además:
- Ruta(s) afectadas y método HTTP.
- Si el endpoint debe permitirse sin autenticación para flujos de arranque (`/api/auth/entra/status`, callbacks, etc.).
- Tipo de token esperado (Bearer, app-token con prefijo, etc.) y cómo el frontend debe enviarlo.
- Cambios en cabeceras CORS o en la `CorsConfigurationSource`.

Esto ayuda a mantener sincronizados la configuración del servidor y las expectativas del frontend.


## Alcance

Este documento no reemplaza `ARCHITECTURE.md`.
- `ARCHITECTURE.md`: vision de arquitectura y estructura del sistema.
- `codex_documentation.md`: reglas practicas de documentacion y estilo para cambios de codigo.
