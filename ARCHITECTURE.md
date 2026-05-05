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

