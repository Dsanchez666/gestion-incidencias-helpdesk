package com.company.backendinc.auth.entra;

import com.company.backendinc.auth.entra.application.EntraAuthenticationUseCase;
import com.company.backendinc.auth.entra.application.UseCaseResult;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Input adapter exposing the Entra authentication flows.
 */
@RestController
@RequestMapping("/api/auth/entra")
public class EntraIdTestController {
    private final EntraAuthenticationUseCase authenticationUseCase;

    public EntraIdTestController(EntraAuthenticationUseCase authenticationUseCase) {
        this.authenticationUseCase = authenticationUseCase;
    }

    @PostMapping("/app-token")
    public ResponseEntity<EntraLoginResponse> appToken() {
        return toResponse(authenticationUseCase.appToken());
    }

    @PostMapping(value = "/test", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntraLoginResponse> test(@RequestBody EntraLoginRequest request) {
        return toResponse(authenticationUseCase.testCredentials(request));
    }

    @PostMapping(value = "/test", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<EntraLoginResponse> testForm(@ModelAttribute EntraLoginRequest request) {
        return toResponse(authenticationUseCase.testCredentials(request));
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        UseCaseResult<String> result = authenticationUseCase.loginRedirect();
        if (result.body() == null) {
            return ResponseEntity.status(result.status()).build();
        }
        return ResponseEntity.status(result.status())
                .header(HttpHeaders.LOCATION, result.body())
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription) {
        UseCaseResult<String> result = authenticationUseCase.callback(code, state, error, errorDescription);
        return ResponseEntity.status(result.status())
                .contentType(MediaType.TEXT_HTML)
                .body(result.body());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(authenticationUseCase.status());
    }

    private ResponseEntity<EntraLoginResponse> toResponse(UseCaseResult<EntraLoginResponse> result) {
        return ResponseEntity.status(result.status()).body(result.body());
    }
}
