package com.finders.api.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger (OpenAPI 3.0) 설정
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, securityScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Finders API")
                .description("필름 현상소 연결 플랫폼 - Finders API 문서")
                .version("v1.0.0")
                .contact(new Contact()
                        .name("Finders Team")
                        .email("contact@finders.kr"));
    }

    private List<Server> servers() {
        return switch (activeProfile) {
            case "prod" -> List.of(
                    new Server().url("https://finders-api.log8.kr").description("Production Server")
            );
            default -> List.of(
                    new Server().url("http://localhost:8080").description("Local Server")
            );
        };
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("JWT 토큰을 입력하세요. (Bearer 제외)");
    }
}
