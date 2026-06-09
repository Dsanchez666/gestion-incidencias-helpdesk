package com.company.backendinc.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Custom authentication provider that accepts application tokens (entra, basic-session, etc.)
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = authentication.getName();

        if (token == null || token.isEmpty()) {
            throw new BadCredentialsException("Invalid token");
        }

        // Accept tokens that start with known prefixes
        if (token.startsWith("entra-") || token.startsWith("basic-session-")) {
            // Create authenticated token with default authority
            return new PreAuthenticatedAuthenticationToken(
                    token,
                    token,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }

        throw new BadCredentialsException("Invalid token format");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
