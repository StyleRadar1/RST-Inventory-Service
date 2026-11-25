[⬅ Volver al README Principal](../../README.md)

---

## 5. TECNOLOGÍAS Y DEPENDENCIAS

La selección tecnológica de `ms-playlist` está alineada con el estándar del Paquete 2 (Catálogo) de StyleRadar, priorizando tipado fuerte, aislamiento hexagonal, migraciones reproducibles y tolerancia a fallos en red y mensajería.

| Capa / Componente | Tecnología | Versión | Justificación Técnica |
| :--- | :--- | :--- | :--- |
| **Lenguaje Core** | Java LTS | `21` | Aprovecha *records* para DTOs inmutables (`CrearPlaylistRequest`, `PlaylistResponse`, `PrendaCatalogoDto`), colecciones modernas (`getFirst()`) y soporte LTS en contenedores OCI. |
| **Framework Principal** | Spring Boot | `[PENDIENTE: confirmar en pom.xml; estándar RST: 3.3.4]` | Provee inyección de dependencias, configuración externalizada (`application.yml`), soporte nativo para RFC 7807 (`ProblemDetail`) y autoconfiguración de datos y mensajería. |
| **Capa Web y Validación** | Spring Web MVC + Jakarta Validation | Gestionada por Spring Boot | Expone los controladores REST bajo `/api/v1`, deserializa JSON y valida reglas estructurales (`@NotBlank`, `@NotNull`, `@Size`) antes de invocar la capa de aplicación. |
| **Persistencia ORM** | Spring Data JPA / Hibernate | Gestionada por Spring Boot | Implementa el adaptador PostgreSQL con `@ElementCollection`, carga perezosa controlada dentro de límites `@Transactional` (`open-in-view: false`) y validación estricta de esquema (`ddl-auto: validate`). |
| **Pool de Conexiones** | HikariCP | Gestionada por Spring Boot | Configurado con `maximum-pool-size: 3` y `minimum-idle: 1` para operar de forma segura sobre el servidor PostgreSQL compartido de QA y PROD. |
| **Base de Datos** | PostgreSQL | `16` | Garantiza integridad referencial y transacciones ACID sobre playlists, snapshots de prendas, likes y guardados (*Database-per-Service*). |
| **Migraciones de Esquema** | Flyway | Gestionada por Spring Boot | Versiona de forma determinista el esquema DDL en `classpath:db/migration` antes de iniciar Hibernate. |
| **Cliente HTTP Declarativo** | Spring Cloud OpenFeign | Gestionada por Spring Cloud (`[PENDIENTE: versión exacta en pom.xml]`) | Desacopla la consulta síncrona a `ms-item-catalogo` (`GET /api/v1/garments/{id}`) con timeouts explícitos (`connect-timeout: 5000`, `read-timeout: 15000`). |
| **Broker de Mensajería** | RabbitMQ + Spring AMQP | `3.13.7` | Permite publicar eventos de dominio (`styleradar.playlist.creada`, `destacada.actualizada`, `prenda.asociada`) y consumir retiros de prendas (`styleradar.catalogo.prenda.retirada`) con soporte DLQ y TLS (`:5671`). |
| **Mapeo Objeto-Objeto** | MapStruct | `[PENDIENTE: confirmar en pom.xml; estándar RST: 1.5.5.Final]` | Genera en tiempo de compilación `PlaylistDtoMapper` y `PlaylistEntityMapper` sin reflexión ni penalización en arranque. |
| **Documentación API** | springdoc-openapi (Swagger UI) | `[PENDIENTE: confirmar en pom.xml; estándar RST: 2.6.0]` | Genera la especificación OpenAPI (`/api/v1/v3/api-docs`) y la interfaz interactiva (`/api/v1/swagger-ui.html`) a partir de `PlaylistApi` y `HealthApi`. |
| **Observabilidad / Salud** | Spring Boot Actuator + `HealthController` | Gestionada por Spring Boot | Expone `/api/v1/health` y los endpoints de gestión `health` e `info` bajo `/api/v1/actuator`. |
| **Pruebas Unitarias y Web** | JUnit 5 + Mockito + AssertJ + MockMvc | Gestionada por Spring Boot | Permite probar invariantes de dominio, casos de uso, manejo de errores RFC 7807, clientes Feign y controladores REST sin levantar infraestructura externa. |
| **Pruebas de Integración** | Testcontainers (`postgres:16-alpine`) | Gestionada por Spring Boot | Levanta una instancia real de PostgreSQL 16 en Docker durante `PlaylistServiceApplicationIntegrationTest` para validar Flyway y JPA end-to-end. |
| **Cobertura y Análisis Estático** | JaCoCo + SpotBugs | `[PENDIENTE: confirmar versiones en pom.xml; estándar RST: JaCoCo 0.8.12 / SpotBugs 4.8.6.4]` | Verifica la cobertura mínima obligatoria (`≥ 85%`) y detecta defectos estáticos antes de generar la imagen OCI. |

---

[⬅ Volver al README Principal](../../README.md)
