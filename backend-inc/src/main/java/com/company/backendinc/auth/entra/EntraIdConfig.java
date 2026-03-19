package com.company.backendinc.auth.entra;

public class EntraIdConfig {
    private String tenantId;
    private String clientId;
    private String clientSecret;
    private String authorityUrl;
    private String pingUrl;
    private String scope;
    private String redirectUri;
    private String mocktoken;
    private EntraJwk jwk;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAuthorityUrl() {
        return authorityUrl;
    }

    public void setAuthorityUrl(String authorityUrl) {
        this.authorityUrl = authorityUrl;
    }

    public String getPingUrl() {
        return pingUrl;
    }

    public void setPingUrl(String pingUrl) {
        this.pingUrl = pingUrl;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getMocktoken() {
        return mocktoken;
    }

    public void setMocktoken(String mocktoken) {
        this.mocktoken = mocktoken;
    }

    public EntraJwk getJwk() {
        return jwk;
    }

    public void setJwk(EntraJwk jwk) {
        this.jwk = jwk;
    }
}
