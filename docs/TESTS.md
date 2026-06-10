# Tests

## Test Entra ID (backend)

Endpoint:
- `POST http://localhost:4000/api/auth/entra/test`

Body (JSON):
```json
{
  "username": "user",
  "password": "password"
}
```

PowerShell:
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:4000/api/auth/entra/test" `
  -ContentType "application/json" `
  -Body '{"username":"user","password":"password"}' | ConvertTo-Json -Depth 6
```

CMD (curl):
```bat
curl -s -X POST "http://localhost:4000/api/auth/entra/test" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"user\",\"password\":\"password\"}"
```

Respuesta esperada:
```json
{
  "success": true,
  "accessToken": "..."
}
```
o
```json
{
  "success": false,
  "error": "..."
}
```

Notas:
- El test usa `frontend/src/assets/EntraID_Conf.json` para resolver el endpoint de token.
- Si recibes `415 Unsupported Media Type`, revisa el `Content-Type` del request.

## Test Mock Entra ID (backend)

Endpoint:
- `POST http://localhost:4000/api/auth/entra/mock-test`

Body:
- Sin parametros.

PowerShell:
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:4000/api/auth/entra/mock-test" | ConvertTo-Json -Depth 6
```

CMD (curl):
```bat
curl -s -X POST "http://localhost:4000/api/auth/entra/mock-test"
```

Respuesta esperada:
```json
{
  "success": true,
  "accessToken": "..."
}
```
o
```json
{
  "success": false,
  "error": "..."
}
```

Notas:
- Lee el token mock desde `frontend/src/assets/EntraID_Conf.json` (local, no versionado).
- Usa `frontend/src/assets/EntraID_Conf.example.json` como plantilla.

## Comprobacion de estado de sesión (startup)

Endpoint:
- `GET http://localhost:4000/api/auth/entra/status`

Notas:
- Este endpoint está pensado para ser llamado por `StartupComponent` del frontend y puede estar permitido sin token para indicar `loggedIn=false`.

## Test de traza de buzones (user trace)

Endpoint:
- `POST http://localhost:4000/api/mailboxes/graph/user/trace`

Notas:
- Este endpoint permite a la app comprobar carpetas de usuario con el token de aplicación o token de usuario.

## Test asignacion masiva (bulk assign)

Endpoint ejemplo:
```bat
curl -s -X POST "http://localhost:4000/api/inbox/gestion/asignar-incidencias?summaryLength=50" \
  -H "Content-Type: application/json" \
  -d "{\"messageIds\":[\"id1\",\"id2\"],\"tecnicoNombre\":\"juan\",\"prioridad\":\"NORMAL\"}"
```

Notas:
- En caso de error de autorización o validación la respuesta suele ser `400` con cuerpo JSON `{ "error": "mensaje" }`.

## Test MS Graph (buzones)

Endpoint:
- `POST http://localhost:4000/api/mailboxes/graph/test`

PowerShell:
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:4000/api/mailboxes/graph/test" | ConvertTo-Json -Depth 6
```

CMD (curl):
```bat
curl -s -X POST "http://localhost:4000/api/mailboxes/graph/test"
```

Notas:
- Requiere token en `Authorization` (Bearer) con permisos para listar mailFolders.
- Usa la configuracion de `Mailboxes_Conf.json` (local, no versionado).
- Plantilla: `frontend/src/assets/Mailboxes_Conf.example.json`.

## Test Exchange (buzones)

Endpoint:
- `POST http://localhost:4000/api/mailboxes/exchange/test`

PowerShell:
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:4000/api/mailboxes/exchange/test" | ConvertTo-Json -Depth 6
```

CMD (curl):
```bat
curl -s -X POST "http://localhost:4000/api/mailboxes/exchange/test"
```

Notas:
- Requiere token en `Authorization` (Bearer).
- Usa `exchangeEwsUrl` de `Mailboxes_Conf.json`.
