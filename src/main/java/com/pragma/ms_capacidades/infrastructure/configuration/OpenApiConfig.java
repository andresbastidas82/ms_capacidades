package com.pragma.ms_capacidades.infrastructure.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Capacidades API",
                version = "1.0",
                description = "API de gestión de capacidades"
        )
)
public class OpenApiConfig {
}
