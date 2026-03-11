package com.company.backendinc.auth.entra;

public class EntraLoginResponse {
    private boolean success;
    private String accessToken;
    private String error;

    public EntraLoginResponse() {
    }

    public EntraLoginResponse(boolean success, String accessToken, String error) {
        this.success = success;
        this.accessToken = accessToken;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
