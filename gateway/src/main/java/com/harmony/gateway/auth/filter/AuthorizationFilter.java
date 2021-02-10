package com.harmony.gateway.auth.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    @Value("${auth.url}")
    private String authUrl;

    public AuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            return WebClient.create(authUrl + "/authorizations")
                    .method(HttpMethod.GET)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .toBodilessEntity()
                    .map((voidResponseEntity -> {
                        String newAuthorization = voidResponseEntity
                                .getHeaders()
                                .getFirst(HttpHeaders.AUTHORIZATION);

                        exchange
                                .getResponse()
                                .getHeaders()
                                .add(HttpHeaders.AUTHORIZATION, newAuthorization);

                        return exchange;
                    })).flatMap(chain::filter);
        };
    }

    public static class Config {

    }
}
