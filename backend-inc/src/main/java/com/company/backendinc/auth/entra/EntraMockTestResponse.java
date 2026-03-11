package com.company.backendinc.auth.entra;

public class EntraMockTestResponse {
    private String result;
    private String accessToken;
    private String error;

    public EntraMockTestResponse() {
    }

    public EntraMockTestResponse(String result, String accessToken, String error) {
        this.result = result;
        this.accessToken = accessToken;
        this.error = error;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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
