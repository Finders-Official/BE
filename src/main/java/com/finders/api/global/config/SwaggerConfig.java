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
        String userToken = "AccessToken";
        String signupToken = "SignupToken";

        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .addSecurityItem(new SecurityRequirement().addList(userToken))
                .addSecurityItem(new SecurityRequirement().addList(signupToken))
                .components(new Components()
                        .addSecuritySchemes(userToken, new SecurityScheme()
                                .name(userToken)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("일반 회원 AccessToken (USER 권한)"))
                        .addSecuritySchemes(signupToken, new SecurityScheme()
                                .name(signupToken)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("회원가입용 SignupToken (GUEST 권한)")));
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
                    new Server().url("https://finders-api.log8.kr/api").description("Production Server")
            );
            default -> List.of(
                    new Server().url("http://localhost:8080/api").description("Local Server")
            );
        };
    }
}
