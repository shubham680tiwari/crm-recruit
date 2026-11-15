package com.recruitcrm.candidatemanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI (Swagger) documentation served at /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI candidateManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Candidate Manager API")
                        .version("1.0.0")
                        .description("REST API for managing recruitment candidates — "
                                + "supports CRUD, keyword search, and multi-criteria filtering.")
                        .contact(new Contact()
                                .name("Shubham Tiwari")));
    }
}
