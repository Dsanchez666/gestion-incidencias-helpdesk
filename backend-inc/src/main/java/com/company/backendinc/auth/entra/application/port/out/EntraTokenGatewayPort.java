package com.company.backendinc.auth.entra.application.port.out;

import com.company.backendinc.auth.entra.EntraIdConfig;

/**
 * Output port for remote token acquisition against Entra ID.
 */
public interface EntraTokenGatewayPort {
    EntraTokenResult requestClientCredentialsToken(EntraIdConfig config);

    EntraTokenResult requestPasswordToken(EntraIdConfig config, String username, String password);

    EntraTokenResult exchangeAuthorizationCode(EntraIdConfig config, String code);
}
