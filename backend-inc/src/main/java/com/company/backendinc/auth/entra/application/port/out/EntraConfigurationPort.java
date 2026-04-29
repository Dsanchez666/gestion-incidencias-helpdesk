package com.company.backendinc.auth.entra.application.port.out;

import com.company.backendinc.auth.entra.EntraIdConfig;
import java.io.IOException;

/**
 * Output port for loading Entra configuration.
 */
public interface EntraConfigurationPort {
    EntraIdConfig load() throws IOException;
}
