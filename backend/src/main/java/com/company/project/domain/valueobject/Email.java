package com.company.project.domain.valueobject;

import com.company.project.domain.exception.DomainValidationException;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public Email {
        String normalized = Objects.requireNonNull(value, "email is required").trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new DomainValidationException("email format is invalid");
        }
        value = normalized;
    }
}
