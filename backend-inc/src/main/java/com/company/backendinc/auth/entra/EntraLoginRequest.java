package com.company.backendinc.auth.entra;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EntraLoginRequest {
    @NotBlank
    @Size(max = 150)
    private String username;
    @NotBlank
    @Size(min = 1, max = 300)
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
