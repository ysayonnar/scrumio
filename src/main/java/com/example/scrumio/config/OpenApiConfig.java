package com.example.scrumio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI scrumioOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Scrumio API")
                .description("REST API for the Scrumio project management system")
                .version("0.0.1-SNAPSHOT"));
    }
}
