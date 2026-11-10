package edu.dosw.styleradar.inventario.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración OpenAPI 3.0 / Swagger UI para ms-inventario en el puerto oficial :8200.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("StyleRadar · ms-inventario (RST-Inventario-Service)")
                        .version("1.0.0")
                        .description("Microservicio de Inventario y Gestión de Stock Multisede por Talla de StyleRadar. "
                                + "Bounded Context: Inventario y Stock (Puerto Oficial :8200).")
                        .contact(new Contact()
                                .name("Squad Estación RESTrepo - DOSW 2026")
                                .url("https://github.com/StyleRadar1"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8200").description("Entorno Local"),
                        new Server().url("https://styleradar-backend-qa.azurewebsites.net").description("Entorno QA"),
                        new Server().url("https://styleradar-backend-prod.azurewebsites.net").description("Entorno Producción")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Autenticación basada en Bearer JWT")));
    }
}
