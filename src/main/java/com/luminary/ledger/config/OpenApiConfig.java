package com.luminary.ledger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ledgerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ledger Service API")
                        .description("Double-entry bookkeeping microservice")
                        .version("1.0"));
    }
}
