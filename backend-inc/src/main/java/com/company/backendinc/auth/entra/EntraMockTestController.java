package com.company.backendinc.auth.entra;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/entra")
public class EntraMockTestController {
    private static final Logger log = LoggerFactory.getLogger(EntraMockTestController.class);
    private final EntraIdMockConfigLoader mockConfigLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EntraMockTestController(EntraIdMockConfigLoader mockConfigLoader) {
        this.mockConfigLoader = mockConfigLoader;
    }

    @PostMapping("/mock-test")
    public ResponseEntity<EntraMockTestResponse> mockTest() {
        log.info("Entra mock-test request received");
        EntraIdConfig config;
        try {
            config = mockConfigLoader.load();
        } catch (IOException ex) {
            log.error("Error loading EntraID_Conf.json", ex);
            return ResponseEntity.internalServerError()
                    .body(new EntraMockTestResponse("error", null, "No se pudo leer EntraID_Conf.json: " + ex.getMessage()));
        }

        String token = config.getMocktoken();
        log.info("Entra mock token present: {}, length: {}", token != null && !token.isBlank(), token != null ? token.length() : 0);
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new EntraMockTestResponse("invalid_token", null, "mocktoken no configurado en EntraID_Conf.json."));
        }

        if ("REEMPLAZAR_TOKEN".equals(token)) {
            return ResponseEntity.ok(new EntraMockTestResponse("invalid_token", token, "mocktoken pendiente de configurar."));
        }

        EntraJwk jwk = config.getJwk();
        log.info("Entra JWK present: {}, n length: {}, e length: {}",
                jwk != null, jwk != null && jwk.getN() != null ? jwk.getN().length() : 0,
                jwk != null && jwk.getE() != null ? jwk.getE().length() : 0);
        if (jwk == null || jwk.getN() == null || jwk.getE() == null) {
            return ResponseEntity.ok(new EntraMockTestResponse("error", token, "JWK no configurado en EntraID_Conf.json."));
        }

        try {
            logJwtDetails(token);
            if (isValidJwt(token, jwk)) {
                return ResponseEntity.ok(new EntraMockTestResponse("valid_token", token, null));
            }
            return ResponseEntity.ok(new EntraMockTestResponse("invalid_token", token, "Token invalido o expirado."));
        } catch (Exception ex) {
            log.error("Error validating Entra mock token", ex);
            return ResponseEntity.ok(new EntraMockTestResponse("error", token, "Error validando token: " + ex.getMessage()));
        }
    }

    private boolean isValidJwt(String token, EntraJwk jwk) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

        Map<?, ?> header = objectMapper.readValue(headerJson, Map.class);
        Object alg = header.get("alg");
        if (alg == null || !"RS256".equalsIgnoreCase(alg.toString())) {
            return false;
        }

        Map<?, ?> payload = objectMapper.readValue(payloadJson, Map.class);
        Object expValue = payload.get("exp");
        if (expValue == null) {
            return false;
        }

        long exp = ((Number) expValue).longValue();
        long now = Instant.now().getEpochSecond();
        if (exp <= now) {
            return false;
        }

        byte[] signatureBytes = Base64.getUrlDecoder().decode(parts[2]);
        byte[] signedContent = (parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8);

        PublicKey publicKey = buildPublicKey(jwk);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(signedContent);
        return signature.verify(signatureBytes);
    }

    private PublicKey buildPublicKey(EntraJwk jwk) throws Exception {
        BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.getN()));
        BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.getE()));
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private void logJwtDetails(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("JWT format invalid: parts={}", parts.length);
                return;
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            Map<?, ?> header = objectMapper.readValue(headerJson, Map.class);
            Map<?, ?> payload = objectMapper.readValue(payloadJson, Map.class);

            Object alg = header.get("alg");
            Object kid = header.get("kid");
            Object expValue = payload.get("exp");
            long exp = expValue instanceof Number ? ((Number) expValue).longValue() : -1L;
            long now = Instant.now().getEpochSecond();

            log.info("JWT header alg={}, kid={}, exp={}, now={}, expired={}",
                    alg, kid, exp, now, exp > 0 && exp <= now);
        } catch (Exception ex) {
            log.warn("Unable to parse JWT details: {}", ex.getMessage());
        }
    }
}
