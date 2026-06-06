package org.example.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 统一配置（各微服务引入 common 后自动生效）
 */
@Configuration
@ConditionalOnClass(OpenAPI.class)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(@Value("${spring.application.name:psych-api}") String appName) {
        return new OpenAPI()
                .info(new Info()
                        .title("听心 · " + appName)
                        .description("高校心理预约系统 RESTful API")
                        .version("1.0")
                        .contact(new Contact().name("HeartListen Team")))
                .components(new Components().addSecuritySchemes("BearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}
