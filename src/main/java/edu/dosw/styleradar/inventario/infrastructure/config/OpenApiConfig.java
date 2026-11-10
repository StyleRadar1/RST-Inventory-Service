package edu.dosw.styleradar.inventario.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                // 1. CLAVE: La ruta relativa "/" obliga a Swagger a consumir el mismo dominio donde está abierto
                // (Si estás en Azure usará https://rst-inventory-service-qa..., si estás en local usará http://localhost:8200)
                .servers(List.of(
                        new Server().url("/").description("Servidor Actual (Local / QA / PROD)")
                ))
                // 2. Información del microservicio para la documentación técnica
                .info(new Info()
                        .title("RST-Inventory-Service")
                        .version("1.0.0")
                        .description("Microservicio de Inventario y Gestión de Stock de StyleRadar. " +
                                "Paquete 2: Catálogo · Equipo Estación RESTrepo.")
                        .contact(new Contact()
                                .name("Equipo Estación RESTrepo")
                                .email("daniel.mmartinez@mail.escuelaing.edu.co")))
                // 3. Soporte para Autenticación JWT en Swagger UI
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingrese el token JWT (Bearer) emitido por ms-auth.")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}