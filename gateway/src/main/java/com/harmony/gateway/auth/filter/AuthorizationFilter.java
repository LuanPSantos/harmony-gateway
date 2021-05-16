package com.harmony.gateway.auth.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import static java.util.Objects.requireNonNull;

@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    public final String ROLE_PARAM_KEY = "required-role";
    public final String REFRESH_AUTHORIZATION_TOKEN = "refresh-authorization";

    @Value("${auth.url}")
    private String authUrl;

    public AuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authorization = requireNonNull(exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);
            HttpCookie cookie = requireNonNull(exchange.getRequest().getCookies().get(REFRESH_AUTHORIZATION_TOKEN)).get(0);

            return WebClient
                    .create(authUrl)
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/authorizations")
                            .queryParam(ROLE_PARAM_KEY, config.getRole())
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .cookie(REFRESH_AUTHORIZATION_TOKEN, cookie.getValue())
                    .retrieve()
                    .toBodilessEntity()
                    .map((clientResponse -> {

                        String authorizationToken = requireNonNull(clientResponse.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));

                        exchange
                                .getResponse()
                                .getHeaders()
                                .add(HttpHeaders.AUTHORIZATION, authorizationToken);

                        return exchange;
                    })).flatMap(chain::filter);
        };
    }

    public static class Config {
        private String role;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
