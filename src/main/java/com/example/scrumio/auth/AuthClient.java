package com.example.scrumio.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthClient {

    private final RestClient restClient;

    public AuthClient(@Value("${auth-service.url}") String authServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(authServiceUrl)
                .build();
    }

    public ResponseEntity<AuthValidationResponse> authenticate(String cookieHeader) {
        return restClient.get()
                .uri("/auth")
                .header("Cookie", cookieHeader)
                .retrieve()
                .toEntity(AuthValidationResponse.class);
    }
}
