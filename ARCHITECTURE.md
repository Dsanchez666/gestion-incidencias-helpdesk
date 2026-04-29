# Arquitectura del Proyecto (Pilar Hexagonal)

## Objetivo

Este repositorio adopta la **arquitectura hexagonal (Ports & Adapters)** como principio rector para separar:
- la logica de negocio,
- los casos de uso,
- y los mecanismos de entrada/salida (HTTP, configuracion, Graph, Exchange).

La meta es evolucionar cada modulo sin acoplar el dominio al framework ni a integraciones externas.

---

## Estructura actual del repositorio

```text
backend-inc/
  src/main/java/com/company/backendinc/
    auth/
      AuthController
      LoginRequest / LoginResponse
      application/
        AuthenticateUserUseCase
        port/out/
          CredentialsVerifierPort
      entra/
        EntraIdTestController
        adapter/out/
          EntraRemoteTokenGatewayAdapter
        application/
          EntraAuthenticationUseCase
          port/out/
            EntraConfigurationPort
            EntraSessionStorePort
            EntraTokenGatewayPort
            EntraTokenResult
        EntraIdConfig / EntraIdConfigLoader
        EntraTokenStore
        EntraMockTestController
    incidencia/
      Incidencia
      IncidenciaController
      application/
        CreateIncidenciaUseCase
        ListIncidenciasUseCase
        port/out/
          IncidenciaRepositoryPort
      adapter/out/
        InMemoryIncidenciaRepositoryAdapter
    mailbox/
      Mailbox
      MailboxController
      application/
        ListMailboxesUseCase
        TestGraphConnectionUseCase
        TestExchangeConnectionUseCase
        TraceGraphWithAppTokenUseCase
        TraceGraphWithUserTokenUseCase
        port/out/
          MailboxConfigPort
          GraphConnectionPort
          ExchangeConnectionPort
          GraphTracePort
      adapter/out/
        GraphConnectionAdapter
        ExchangeConnectionAdapter
        GraphTraceAdapter
      config/
        MailboxConfig / MailboxEntry / MailboxConfigLoader
      connection/
        MailboxConnectionController
        GraphConnectionTester / ExchangeConnectionTester
        GraphTraceResponse / MailboxFolderResult / MailFolder / ConnectionResult
    config/
      WebConfig

frontend/
  src/app/
    core/auth/
      application/
        login.usecase / logout.usecase / get-token.usecase
        is-authenticated.usecase / initialize-entra-session.usecase
        port/out/
          auth-session.port
          entra-app-token.port
      auth.service / auth.guard / auth.interceptor
      entra-app.service
    features/
      auth/ui/
      mailbox/domain | application | infrastructure | ui/
      incidencia/domain | application | infrastructure | ui/
```

---

## Mapa hexagonal aplicado

## 1) Dominio

El dominio contiene el modelo y reglas de negocio, sin dependencia de HTTP o librerias de infraestructura.

Situacion actual:
- `frontend/src/app/features/incidencia/domain` y `frontend/src/app/features/mailbox/domain` ya separan modelo de UI.
- `backend-inc/src/main/java/com/company/backendinc/incidencia/Incidencia.java` y `backend-inc/src/main/java/com/company/backendinc/mailbox/Mailbox.java` representan entidades del lado servidor.

Direccion objetivo:
- consolidar reglas de negocio en clases de dominio/servicios de aplicacion,
- mantener DTO/controladores solo para transporte.

## 2) Aplicacion (casos de uso)

La capa de aplicacion orquesta el flujo del negocio y habla con puertos.

Situacion actual:
- En frontend existe `core/auth/application` y `features/*/application` con casos de uso orientados a la UI.
- En backend, la orquestacion principal ya se movio a `application`, quedando los controladores como adaptadores finos.

Direccion objetivo:
- mantener la misma disciplina de puertos y adaptadores en nuevas features del frontend y backend.

## 3) Puertos

Los puertos son interfaces que definen contratos de entrada/salida.

En esta base, el patron ya se aprecia en el diseño aunque no todos los contratos estan formalizados como interfaces Java:
- Entrada: endpoints REST (`AuthController`, `EntraIdTestController`, `MailboxController`, `MailboxConnectionController`, `IncidenciaController`).
- Salida: acceso a configuracion JSON (`EntraIdConfigLoader`, `MailboxConfigLoader`) e integraciones externas (`EntraRemoteTokenGatewayAdapter`, `GraphTraceAdapter`, `GraphConnectionAdapter`, `ExchangeConnectionAdapter`, `InMemoryIncidenciaRepositoryAdapter`).

Direccion objetivo:
- explicitar interfaces para puertos de salida (token provider, mailbox gateway, incidencia repository) y desacoplar implementaciones.

## 4) Adaptadores

### Adaptadores IN (entrada)
- Controllers Spring REST en `backend-inc/.../auth`, `.../mailbox`, `.../incidencia`.
- Componentes UI Angular en `frontend/src/app/features/*/ui`.

### Adaptadores OUT (salida)
- Integracion con Microsoft Entra ID: `auth/entra/*`.
- Integracion con Graph y Exchange: `mailbox/connection/*`.
- Carga de configuracion desde ficheros JSON: `mailbox/config/*`, `auth/entra/*ConfigLoader`.

---

## Flujos principales

## Flujo 1: Token app-to-app y prueba de Graph
1. Frontend solicita token a backend (`/api/auth/entra/app-token`).
2. Backend obtiene token en Entra ID.
3. Frontend llama test de buzones (`/api/mailboxes/...`).
4. Backend consulta Graph/Exchange para buzones configurados en `Mailboxes_Conf.json`.

## Flujo 2: Login interactivo y trazas de Graph
1. Usuario inicia login interactivo (`/api/auth/entra/login`).
2. Entra redirige a callback (`/api/auth/entra/callback`).
3. Backend guarda token temporal en `EntraTokenStore`.
4. Endpoint de traza (`/api/mailboxes/graph/user/trace`) usa ese token para listar carpetas y devolver trazas.

## Flujo 2.1: Flujo exacto actualmente en produccion local (MFA compatible)
1. El usuario abre `http://localhost:3000/` (pantalla `StartupComponent`).
2. Frontend consulta `GET /api/auth/entra/status`:
- Si `loggedIn=true`, crea sesion local frontend y navega a `/buzones`.
- Si `loggedIn=false`, muestra boton `Iniciar Sesion con Microsoft`.
3. Al pulsar `Iniciar Sesion con Microsoft`, frontend redirige a `GET /api/auth/entra/login`.
4. Backend construye URL OAuth2 authorize contra Entra:
- `response_type=code`
- `redirect_uri=http://localhost:4000/api/auth/entra/callback`
- `scope=openid profile email offline_access User.Read Mail.Read Mail.Read.Shared`
- `prompt=select_account`
5. El usuario completa login y MFA en Microsoft.
6. Entra llama a `GET /api/auth/entra/callback?code=...`.
7. Backend intercambia `code` por token delegado y guarda token en `EntraTokenStore` con expiracion.
8. Usuario vuelve a `http://localhost:3000/` (enlace del callback).
9. Startup reejecuta `GET /api/auth/entra/status`, detecta `loggedIn=true`, fija token local para pasar `authGuard` y entra en `/buzones`.
10. Desde `/buzones`, el boton `Carpetas usuario` llama `POST /api/mailboxes/graph/user/trace`.
11. Backend lee token de `EntraTokenStore` y consulta Graph `GET /users/{mailbox}/mailFolders?$top=200`.
12. Backend devuelve:
- `success=true` con carpetas cuando hay permisos efectivos.
- `success=false` o `status=error` por buzón cuando hay `403 ErrorAccessDenied`.

## Flujo 3: Gestion de incidencias
1. Frontend (feature `incidencia`) ejecuta caso de uso de aplicacion.
2. Infraestructura frontend invoca API backend.
3. Backend procesa en controlador de incidencias y responde al cliente.

---

## Decisiones de arquitectura

- Spring Boot y Angular son **adaptadores**, no centro del dominio.
- En frontend, `core/auth/application` y `features/*/application` orquestan los casos de uso; `*.service.ts` e infraestructura concreta quedan como adaptadores de salida.
- El dominio debe permanecer libre de dependencias de transporte e integracion.
- Las integraciones con Entra/Graph/Exchange deben encapsularse detras de puertos para facilitar pruebas y sustituciones.
- La configuracion externa (`EntraID_Conf.json`, `Mailboxes_Conf.json`) es parte de infraestructura y no debe condicionar el dominio.

---

## Estado actual y siguiente paso natural

Estado actual:
- Base funcional de auth, buzones e incidencias.
- Controladores reducidos a adaptadores de entrada finos.
- La mayor parte de la orquestacion ya vive en `application`.
- Flujo delegado MFA compatible operativo: login interactivo + callback + `graph/user/trace`.

Siguiente paso natural para reforzar hexagonal:
1. Extraer tests auxiliares restantes a adaptadores o helpers estrictamente de infraestructura.
2. Añadir puertos de entrada si se quiere forzar aun mas el desacoplamiento entre controladores y casos de uso.
3. Aplicar el mismo patron de documentacion y comentarios al frontend si se amplian nuevas features.

Con esto, el proyecto conserva su pilar hexagonal y reduce acoplamiento a medida que crece.
