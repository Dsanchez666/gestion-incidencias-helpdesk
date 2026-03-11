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
