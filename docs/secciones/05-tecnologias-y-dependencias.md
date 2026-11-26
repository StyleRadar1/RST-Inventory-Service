[⬅ Volver al README Principal](../../README.md)

---

## 5. TECNOLOGÍAS Y DEPENDENCIAS

La arquitectura tecnológica de `ms-inventario` se define en `pom.xml` sobre **Spring Boot 3.3.4** y **Java 21**, garantizando persistencia transaccional con bloqueo pesimista, migraciones reproducibles, integración HTTP declarativa, mensajería resiliente y verificación estricta de calidad en cada build.

| Capa / Componente | Tecnología | Versión | Justificación Técnica |
| :--- | :--- | :--- | :--- |
| **Lenguaje Core** | Java | `21` (LTS) | Versión *Long-Term Support*; soporta `sealed interface` (`Talla`), `records` (`Cantidad`, DTOs, eventos) y mejoras de rendimiento de la JVM. |
| **Framework Principal** | Spring Boot (`spring-boot-starter-parent`) | `3.3.4` | Provee el contenedor web embebido, validación (`starter-validation`), JPA (`starter-data-jpa`), mensajería (`starter-amqp`) y observabilidad (`starter-actuator`). |
| **Cliente HTTP Declarativo** | Spring Cloud (`spring-cloud-starter-openfeign`) | `2023.0.3` | Implementa `ItemCatalogoFeignClient` para validar existencia y propiedad de almacén de prendas en `ms-item-catalogo` (`GET /api/v1/garments/{id}`) con timeouts `5s`/`15s`. |
| **Base de Datos Relacional** | PostgreSQL (`postgres:16-alpine`) | `16` (driver gestionado por Boot `3.3.4`) | Motor relacional ACID necesario para consistencia en descuentos y reservas concurrentes mediante `LockModeType.PESSIMISTIC_WRITE`. |
| **Pool de Conexiones** | HikariCP | `Gestionada por Spring Boot 3.3.4` | Limitado a `maximum-pool-size: 3` y `minimum-idle: 1` (`idle-timeout: 60000`) para proteger la instancia compartida de PostgreSQL en QA y PROD. |
| **Migraciones de Esquema** | Flyway (`flyway-core`, `flyway-database-postgresql`) | `Gestionada por Spring Boot 3.3.4` | Versiona de forma determinística el esquema en `classpath:db/migration` junto con `hibernate.ddl-auto: validate`. |
| **Mensajería Asíncrona** | RabbitMQ (`rabbitmq:3.13.7-management-alpine` + Spring AMQP) | `3.13.7` | Publica eventos de stock en `styleradar.exchange` y consume eventos de pago y suspensión desde `ms-pagos` con reintentos exponenciales y DLQ (`styleradar.inventario.dlq`). |
| **Mapeo de DTOs y Entidades** | MapStruct (`mapstruct` + `mapstruct-processor`) | `1.5.5.Final` | Genera en compilación `StockItemDtoMapper` y `StockItemEntityMapper` sin penalización por reflexión. |
| **Documentación OpenAPI** | `springdoc-openapi-starter-webmvc-ui` | `2.6.0` | Expone la especificación OpenAPI 3 (`/api/v1/v3/api-docs`) y la interfaz Swagger UI (`/api/v1/swagger-ui.html`) con servidores relativos (`/`). |
| **Pruebas y Entorno Test** | JUnit 5, Mockito, H2 (`test`) y Testcontainers (`postgresql`, `rabbitmq`) | `Gestionada por Spring Boot 3.3.4` | Ejecuta 17 suites unitarias, de slice web (`@WebMvcTest`) y arranque de contexto con Flyway + H2 (`MODE=PostgreSQL`), además de soporte Testcontainers. |
| **Métricas de Cobertura** | JaCoCo (`jacoco-maven-plugin`) | `0.8.12` | Analiza las 44 clases del bundle `RST-Inventory-Service` y exige un mínimo de `85 %` (`0.85` LINE) en fase `verify`. |
| **Análisis Estático** | SpotBugs (`spotbugs-maven-plugin`) | `4.8.6.4` | Ejecuta análisis con `effort: Max` y `threshold: High` en fase `verify`, bloqueando errores o advertencias antes de empaquetar. |
| **Contenerización** | Docker / Docker Compose | `[PENDIENTE: confirmar imagen base en Dockerfile]` | Orquesta localmente el servicio junto con PostgreSQL 16 y RabbitMQ 3.13.7 y genera la imagen OCI desplegable en Azure Container Apps. |

---

[⬅ Volver al README Principal](../../README.md)
