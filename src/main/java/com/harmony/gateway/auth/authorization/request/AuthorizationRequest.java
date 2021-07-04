package com.harmony.gateway.auth.authorization.request;

import com.harmony.gateway.auth.model.Role;

public class AuthorizationRequest {

    private String authorizationToken;
    private String refreshAuthorizationToken;
    private Role roleRequiredByEndpoint;

    public AuthorizationRequest() {
    }

    public Role getRoleRequiredByEndpoint() {
        return roleRequiredByEndpoint;
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public String getRefreshAuthorizationToken() {
        return refreshAuthorizationToken;
    }
}
