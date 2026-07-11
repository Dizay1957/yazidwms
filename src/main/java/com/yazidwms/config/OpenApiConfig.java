package com.yazidwms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI yazidWmsOpenApi() {
        var schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("YazidWMS API")
                        .version("v1")
                        .description("Enterprise Warehouse Management System API")
                        .contact(new Contact().name("YazidWMS").email("admin@yazidwms.local"))
                        .license(new License().name("Portfolio Project")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .schemaRequirement(schemeName, new SecurityScheme()
                        .name(schemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
