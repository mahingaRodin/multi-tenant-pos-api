package com.msp.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI api() {
                final String schemeName = "Bearer Authentication";

                return new OpenAPI()
                                .info(new Info()
                                                .title("MultiTenant SaaS POS Service API")
                                                .description("API documentation for MSP")
                                                .version("v1"))
                                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                                .components(new io.swagger.v3.oas.models.Components()
                                                .addSecuritySchemes(schemeName,
                                                                new SecurityScheme()
                                                                                .name(schemeName)
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")));
        }
}