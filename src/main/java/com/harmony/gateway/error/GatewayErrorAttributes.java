package com.harmony.gateway.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;

@Primary
@Component
public class GatewayErrorAttributes extends DefaultErrorAttributes {

    public static final String BODY = "body";
    public static final String STATUS = "status";

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();

        Throwable error = getError(request);

        errorAttributes.put(STATUS, determineHttpStatus(error).value());
        errorAttributes.put(BODY, determineBody(error));

        return errorAttributes;
    }

    private HttpStatus determineHttpStatus(Throwable error) {
        MergedAnnotation<ResponseStatus> responseStatusAnnotation = MergedAnnotations
                .from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);

        if (error instanceof ResponseStatusException) {
            return ((ResponseStatusException) error).getStatus();
        }

        if (error instanceof WebClientResponseException) {
            return ((WebClientResponseException) error).getStatusCode();
        }

        return responseStatusAnnotation.getValue("code", HttpStatus.class).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map determineBody(Throwable error) {
        if (error instanceof BindingResult) {
            return Map.of(BODY, error.getMessage());
        }
        if (error instanceof ResponseStatusException) {
            return Map.of(BODY, Objects.requireNonNull(((ResponseStatusException) error).getReason()));
        }

        if (error instanceof WebClientResponseException) {
            try {
                return new ObjectMapper().readValue(((WebClientResponseException) error).getResponseBodyAsByteArray(), HashMap.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Map.of(BODY, "No body available");
    }
}
