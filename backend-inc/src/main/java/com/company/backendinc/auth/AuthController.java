package com.company.backendinc.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String ROOT_USER = "root";
    private static final String ROOT_PASS = "root";

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (ROOT_USER.equals(request.getUsername()) && ROOT_PASS.equals(request.getPassword())) {
            return ResponseEntity.ok(new LoginResponse(request.getUsername(), "login ok"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
