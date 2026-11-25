[⬅ Volver al README Principal](../../README.md)

---

## 11. SEGURIDAD, HABEAS DATA Y ANÁLISIS OWASP TOP 10

El diseño de `ms-inventario` se ajusta al principio de minimización de datos de la **Ley 1581 de 2012 (Habeas Data - Colombia)** y aplica controles técnicos alineados con **OWASP Top 10:2025**.

* **Documento técnico detallado:** [Análisis de Seguridad y OWASP Top 10:2025](../security/SeguridadYOwaspInventario.md) *(Nota: `[PENDIENTE: crear archivo docs/security/SeguridadYOwaspInventario.md]`)*

### Puntos de Control y Cumplimiento Normativo

1. **Minimización de Datos y Ley 1581 de 2012 (Habeas Data):**
   Bajo el patrón *Database-per-Service*, `ms-inventario` **no almacena datos personales identificables (PII)** de compradores ni administradores (sin nombres, correos electrónicos, documentos, teléfonos, datos biométricos ni medios de pago). Las tablas `inventario_items`, `inventario_tallas` y `eventos_procesados` únicamente guardan identificadores técnicos UUID (`id`, `prenda_id`, `sede_id`, `event_id`), tallas y cantidades numéricas.
2. **A01 · Control de Acceso Roto (*Broken Access Control*):**
   * En el flujo de registro por lote (`POST /api/v1/inventory/batch`), `RegistrarLoteStockUseCaseImpl` valida obligatoriamente contra `ms-item-catalogo` que la prenda pertenezca al almacén (`sedeId`) que intenta registrar existencias (`403 PRENDA_DE_OTRO_ALMACEN` si no coincide).
   * Cuando una sede entra en mora (`styleradar.pagos.suscripcion.suspendida`), el agregado `StockItem` bloquea cualquier mutación (`403 INVENTARIO_SUSPENDIDO`).
   * *Arquitectura de autorización:* `OpenApiConfig` documenta el esquema `bearerAuth` (JWT), delegando actualmente la validación de firma JWT y control de roles (`TIENDA`, `ADMIN`) al **API Gateway** antes de enrutar hacia `:8200` (el código interno no declara `SecurityFilterChain`).
3. **A02 · Fallas Criptográficas y Protección de Secretos (*Cryptographic Failures*):**
   Ninguna credencial de base de datos o mensajería reside en `application.yml` ni en el repositorio (todas se inyectan vía variables de entorno). Las conexiones en QA y PROD utilizan HTTPS en Azure Container Apps, `sslmode` configurable en PostgreSQL (`DB_SSL_MODE`) y **TLS/SSL con verificación de hostname** en RabbitMQ (`RABBITMQ_PORT=5671`, `RABBITMQ_SSL_ENABLED=true`, `RABBITMQ_SSL_VERIFY_HOSTNAME=true`).
4. **A03 · Inyección (*Injection*):**
   Todas las consultas en `SpringDataStockItemRepository` usan Spring Data JPA y JPQL parametrizado (`@Param`) sin concatenación SQL. Los DTOs de entrada aplican Bean Validation (`@Valid`, `@NotNull`, `@NotBlank`, `@NotEmpty`, `@Positive`, `@PositiveOrZero`) y los Value Objects (`Cantidad`, `Talla`) validan tipos y rangos en el dominio.
5. **A04 · Diseño Inseguro (*Insecure Design*):**
   * Consistencia concurrente protegida mediante bloqueo pesimista (`LockModeType.PESSIMISTIC_WRITE`) en `buscarPorPrendaYSedeConBloqueo` para evitar sobreventa ante múltiples reservas simultáneas.
   * Idempotencia garantizada en `eventos_procesados` (`PostgreSqlEventoProcesadoAdapter`) para evitar descuentos o liberaciones duplicadas por reintentos de RabbitMQ.
   * Tolerancia a fallos externos: timeouts explícitos en Feign (`5s` conexión / `15s` lectura), `503 CATALOGO_NO_DISPONIBLE` si catálogo falla, captura de `AmqpException` al publicar eventos y DLQ (`styleradar.inventario.dlq`) con `default-requeue-rejected: false`.
6. **A05 · Configuración de Seguridad Incorrecta (*Security Misconfiguration*):**
   * Exposición mínima de Actuator limitada únicamente a `health` e `info`; endpoint de salud acotado a `/api/v1/health`; `spring.jpa.open-in-view: false`; `hibernate.ddl-auto: validate` apoyado en Flyway; y pool HikariCP acotado (`maximum-pool-size: 3`).
   * *Nota de endurecimiento CORS:* `CorsConfig` configura `allowedOriginPatterns: *` para facilitar pruebas desde Gateway y Swagger UI; en entornos productivos cerrados se recomienda restringir los orígenes permitidos al Gateway y dominio oficial del frontend.
7. **A06 · Componentes Vulnerables y Análisis Estático:**
   Dependencias centralizadas en Spring Boot `3.3.4` y Spring Cloud `2023.0.3`, con verificación automática de **SpotBugs `4.8.6.4`** (`Max` / `High`, actualmente **`0` bugs**) y paso opcional de SonarQube en `ci.yml`.
8. **A09 / A10 · Manejo Seguro de Errores y Trazabilidad:**
   `GlobalExceptionHandler` captura todas las excepciones de dominio, validación, deserialización HTTP e imprevistos (`Exception` $\rightarrow$ `500 ERROR_INTERNO`) entregando respuestas **RFC 7807 (`application/problem+json`)** con `code` y `timestamp`, evitando exponer *stack traces*, nombres de tablas o detalles internos al cliente.

---

[⬅ Volver al README Principal](../../README.md)
