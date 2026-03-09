package com.company.project.domain;

import com.company.project.domain.exception.DomainValidationException;
import com.company.project.domain.valueobject.Email;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailValueObjectTest {

    @Test
    void shouldNormalizeEmail() {
        Email email = new Email("  USER@ETNA.COM ");
        assertEquals("user@etna.com", email.value());
    }

    @Test
    void shouldRejectInvalidFormat() {
        assertThrows(DomainValidationException.class, () -> new Email("invalid-email"));
    }
}
