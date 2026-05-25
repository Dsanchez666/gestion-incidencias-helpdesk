package com.company.backendinc.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetRequest {
    @NotBlank
    @Size(max = 255)
    private String userOrEmail;

    public String getUserOrEmail() {
        return userOrEmail;
    }

    public void setUserOrEmail(String userOrEmail) {
        this.userOrEmail = userOrEmail;
    }
}

