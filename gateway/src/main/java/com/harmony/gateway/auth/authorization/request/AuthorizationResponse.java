package com.harmony.gateway.auth.authorization.request;

public class AuthorizationResponse {

    private String authorizationToken;

    public AuthorizationResponse() {
    }

    public AuthorizationResponse(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }
}
