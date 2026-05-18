package com.msp.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI api() {

                final String schemeName = "Bearer Authentication";

                Server productionServer = new Server();

                productionServer.setUrl("https://pos.185.229.227.70.nip.io/msp");

                productionServer.setDescription("Production Server");

                return new OpenAPI()

                        .info(new Info()
                                .title("MultiTenant SaaS POS Service API")
                                .description("API documentation for MSP")
                                .version("v1"))

                        .servers(List.of(productionServer))

                        .addSecurityItem(
                                new SecurityRequirement().addList(schemeName)
                        )

                        .components(
                                new Components()
                                        .addSecuritySchemes(
                                                schemeName,

                                                new SecurityScheme()
                                                        .name(schemeName)
                                                        .type(SecurityScheme.Type.HTTP)
                                                        .scheme("bearer")
                                                        .bearerFormat("JWT")
                                        )
                        );
        }
}