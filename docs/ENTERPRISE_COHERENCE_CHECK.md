# Enterprise Coherence Check

Fecha: 2026-03-09
Referencia: `.codex/enterprise-system-prompt.md`

## Estado general

El proyecto nuevo **esta mayoritariamente alineado** con el prompt enterprise a nivel de estructura y convenciones de capas.

## Comprobaciones de coherencia

1. Arquitectura hexagonal backend
- Separacion en `domain`, `application`, `infrastructure`.
- Puertos de entrada/salida definidos en dominio.
- Adaptadores REST y persistencia en infraestructura.

2. Reglas DTO y mappers
- Controlador REST usa request/response DTO.
- Mapper dedicado `IncidenciaRestMapper`.

3. Seguridad
- Configuracion de seguridad separada.
- `@RestControllerAdvice` para no filtrar stack traces.
 - Se ha introducido `SecurityConfiguration` con CORS expuesto para soportar los flujos de arranque del frontend.
 - Añadidos `CustomTokenAuthenticationFilter` y `CustomAuthenticationProvider` para aceptar tokens de aplicación y soportar sesiones EntraID.

4. Testing por capas
- Dominio: `EmailValueObjectTest`.
- Aplicacion: `CreateIncidenciaUseCaseTest` con Mockito.
- Infraestructura: `JpaIncidenciaRepositoryAdapterTest` con Testcontainers.

5. Frontend por feature
- `features/incidencia/domain|application|infrastructure|ui`.
- Separacion entre caso de uso, servicio API y componente UI.

6. Tooling
- Configuracion ESLint y Prettier a�adida.
- Recomendacion SonarLint incluida para VS Code.

## Gaps detectados

1. No se ha ejecutado validacion completa de build en este entorno por dependencias locales (Java 21/Node/Angular CLI no verificadas aqui).
2. Falta pipeline CI para forzar checks de calidad, seguridad y pruebas.
3. Falta endurecer seguridad de autenticacion/autorizacion con usuarios/roles enterprise (actualmente base con HTTP Basic).
4. No hay estrategia de migraciones DB versionadas (Flyway/Liquibase).

## Resultado

Acciones recientes sugeridas:
- Revisar y documentar la fuente de verdad para roles (claims de EntraID vs tablas locales) y la política de resolución de perfiles.
- Añadir integración con migraciones DB (Flyway/Liquibase) antes de producción.

Se proponen issues para cerrar los gaps antes de merge/tag de release.
