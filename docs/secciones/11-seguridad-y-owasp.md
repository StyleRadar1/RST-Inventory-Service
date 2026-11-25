[⬅ Volver al README Principal](../../README.md)

---

## 11. SEGURIDAD, HABEAS DATA Y ANÁLISIS OWASP TOP 10:2025

`ms-playlist` aplica los lineamientos de protección de datos personales de la **Ley 1581 de 2012 (Habeas Data en Colombia)** y los controles del estándar **OWASP Top 10:2025** desde el diseño del agregado de dominio y sus adaptadores de entrada y salida.

* **Documento técnico detallado de seguridad:** [Análisis de Seguridad y OWASP Top 10:2025 (`ms-playlist`)](../security/analisis-seguridad-owasp-ms-playlist.md) *(`[PENDIENTE: crear el archivo docs/security/analisis-seguridad-owasp-ms-playlist.md con la matriz extendida si el repositorio aún no lo contiene]`)*

### Puntos de Control y Cumplimiento Normativo

1. **Ley 1581 de 2012 / Minimización y Supresión de Datos (Habeas Data):**
   `ms-playlist` no almacena ni procesa datos personales sensibles ni PII (nombres civiles, correos electrónicos, teléfonos, documentos de identidad o fotografías biométricas). Únicamente persiste el identificador opaco `usuario_id` propagado por el API Gateway y el snapshot básico de prendas (`prenda_id`, `nombre_prenda`). Los eventos publicados en RabbitMQ contienen únicamente UUIDs (`playlistId`, `prendaId`), excluyendo cualquier dato personal. El titular puede suprimir sus colecciones o prendas asociadas en cualquier momento vía `DELETE /api/v1/playlists/{id}` y `DELETE /api/v1/playlists/{id}/garments/{garmentId}`.

2. **A01:2025 — Pérdida de Control de Acceso (incluye SSRF):**
   * *Propiedad de recursos:* `Playlist.validarPropietario(usuarioId)` impide que terceros editen, eliminen o modifiquen prendas de una playlist ajena (`403 ACCESO_DENEGADO`).
   * *Privacidad de colecciones:* `Playlist.validarAccesoLectura(usuarioConsultaId)` bloquea el acceso a playlists `PRIVADA` para cualquier usuario distinto al dueño (`403 PLAYLIST_PRIVADA`).
   * *Consulta por usuario:* `GET /api/v1/playlists/users/{userId}` y `GET /api/v1/playlists/users/{userId}/saved` verifican que `{userId}` coincida exactamente con la cabecera `X-User-Id` autenticada (`403 ACCESO_DENEGADO`).
   * *Autorización por rol administrativo:* `PUT` y `DELETE /api/v1/playlists/{id}/featured` exigen explícitamente el rol `STYLERADAR_ADMIN` o `ADMIN_STYLERADAR` (incluyendo variantes `ROLE_*`) leído desde `X-User-Role` / `X-User-Roles` (`403 ACCESO_DENEGADO`).
   * *Mitigación de SSRF:* El cliente `ItemCatalogoFeignClient` únicamente realiza peticiones `GET` hacia la URL base fija configurada por infraestructura (`ITEM_CATALOGO_URL`) concatenando un parámetro tipado `UUID`, sin aceptar URLs arbitrarias desde el cliente.

3. **A02:2025 — Configuración de Seguridad Incorrecta:**
   Desactivación de `spring.jpa.open-in-view` (`false`), validación estricta de esquema (`ddl-auto: validate`), exposición mínima de Actuator (`health, info` únicamente), supresión de cabeceras Java `__TypeId__` en mensajes salientes AMQP y cero credenciales quemadas en `application.yml`.

4. **A03:2025 — Fallas en la Cadena de Suministro de Software:**
   Gestión centralizada de dependencias mediante Maven Wrapper (`./mvnw`) y BOM de Spring Boot, acompañada de análisis estático con SpotBugs en el pipeline de CI/CD (`[PENDIENTE: confirmar si el pipeline ejecuta escaneo de CVEs / OWASP Dependency-Check o Dependabot]`).

5. **A04:2025 — Fallas Criptográficas:**
   Transporte cifrado obligatorio en entornos cloud: conexiones JDBC a PostgreSQL parametrizadas con `DB_SSL_MODE` (`require` recomendado en QA/PROD), conexión RabbitMQ por puerto AMQPS `5671` con `RABBITMQ_SSL_ENABLED=true` y `RABBITMQ_SSL_VERIFY_HOSTNAME=true`, y comunicación HTTPS hacia `ms-item-catalogo`.

6. **A05:2025 — Inyección:**
   Acceso a base de datos exclusivamente mediante Spring Data JPA y consultas JPQL con parámetros nombrados (`@Param("prendaId")`), validación de tipos fuertes (`UUID` en path y DTOs) y reglas `@NotBlank`, `@NotNull`, `@Size` de Jakarta Validation antes de ingresar al dominio.

7. **A06:2025 — Diseño Inseguro:**
   Invariantes críticas aplicadas dentro del agregado `Playlist` (`MAX_PRENDAS = 15`, `MAX_DESTACADAS = 3`, mínimo 1 prenda aprobada al crear, longitud máxima de nombre 100 y descripción 300 caracteres) junto con límite estricto del pool HikariCP (`maximum-pool-size: 3`) para evitar agotamiento de conexiones en el servidor compartido.

8. **A07:2025 — Fallas de Autenticación:**
   Autenticación JWT delegada a `ms-auth` y `API Gateway`. Todo endpoint protegido de `ms-playlist` exige `X-User-Id` no nulo y no vacío, respondiendo `401 USUARIO_NO_AUTENTICADO` si falta o viene en blanco. *(Recomendación de despliegue: mantener `ms-playlist` accesible únicamente a través del API Gateway / red interna de Azure Container Apps para impedir inyección externa directa de cabeceras `X-User-*`).*

9. **A08:2025 — Fallas en la Integridad del Software o de los Datos:**
   Evolución controlada del esquema DDL mediante migraciones versionadas en `Flyway` y consumo seguro en `CatalogoEventsConsumer` sobre `Map<String, Object>` con validación estricta `UUID.fromString(...)`, evitando deserialización polimórfica arbitraria.

10. **A09:2025 — Fallas en el Registro, Alerta y Monitoreo de Seguridad:**
    Registro controlado de incidentes operativos en `GlobalExceptionHandler` (`log.error`), `ItemCatalogoClient` (`log.error`), `RabbitMQEventPublisher` (`log.warn`) y `CatalogoEventsConsumer` (`log.warn` ante payloads inválidos), garantizando ausencia de credenciales o tokens en los logs.

11. **A10:2025 — Manejo Inadecuado de Condiciones Excepcionales:**
    Normalización de todas las respuestas de error en `GlobalExceptionHandler` bajo **RFC 7807** (`application/problem+json` con `code` y `timestamp`, sin exponer *stacktraces* en `500 ERROR_INTERNO`), degradación controlada `503 CATALOGO_NO_DISPONIBLE` ante fallos de `ms-item-catalogo` y captura segura de `AmqpException` en `RabbitMQEventPublisher.enviar(...)` para evitar errores `500` en entornos sin RabbitMQ.

---

[⬅ Volver al README Principal](../../README.md)
