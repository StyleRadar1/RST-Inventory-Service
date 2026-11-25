[⬅ Volver al README Principal](../../README.md)

---

## 8. PRUEBAS Y COBERTURA (≥ 85%)

El aseguramiento de calidad de `ms-playlist` combina pruebas unitarias aisladas sobre dominio y aplicación, pruebas de adaptadores con `MockMvc` y `Mockito`, y pruebas de integración reales contra PostgreSQL usando Testcontainers. Se exige una cobertura mínima de código del **85 %** verificada automáticamente mediante **JaCoCo**.

### Comandos de Ejecución de Pruebas

```bash
# Ejecutar todas las pruebas unitarias e integración (requiere Docker para Testcontainers)
./mvnw test

# Ejecutar verificación completa: compilación, pruebas, reporte JaCoCo y análisis estático
./mvnw clean verify
```

* **Ruta del Reporte HTML de JaCoCo:** `target/site/jacoco/index.html`

### Métricas Actuales de Cobertura en el Repositorio

* **Clases de prueba implementadas en `src/test/java/` (12 archivos):** `PrendaSnapshotTest`, `PlaylistTest`, `UseCasesTest`, `PlaylistControllerTest`, `HealthControllerTest`, `CatalogoEventsConsumerTest`, `GlobalExceptionHandlerTest`, `PostgreSQLPlaylistRepositoryAdapterTest`, `RabbitMQEventPublisherTest`, `ItemCatalogoClientTest`, `RabbitMqConfigTest` y `PlaylistServiceApplicationIntegrationTest`.
* **Pruebas ejecutadas / Fallos:** `[PENDIENTE: ejecutar ./mvnw clean verify en el repositorio y registrar total de pruebas ejecutadas y 0 fallos]`
* **Cobertura JaCoCo verificada:** `[PENDIENTE: registrar porcentaje exacto de líneas e instrucciones desde target/site/jacoco/index.html (meta ≥ 85%)]`
* **Documento de soporte técnico y análisis de seguridad:** [Análisis de Seguridad y OWASP Top 10:2025](../security/analisis-seguridad-owasp-ms-playlist.md) *(`[PENDIENTE: crear archivo docs/security/analisis-seguridad-owasp-ms-playlist.md]`)*
* **Resultado del Build:** `[PENDIENTE: confirmar BUILD SUCCESS tras última ejecución de ./mvnw clean verify]`

<div align="center">
  <img src="../images/JacocoEvidence.png" alt="Evidencia 03 - Reporte Consolidado de Cobertura JaCoCo" width="100%" style="border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);" />
  <p><sub><b>Figura 8.1:</b> [PENDIENTE: capturar reporte target/site/jacoco/index.html y guardarlo en docs/images/JacocoEvidence.png]</sub></p>
</div>

### Pirámide de Pruebas Implementada

| Nivel de Prueba | Alcance | Tecnologías Utilizadas | Ubicación en el Código | Cobertura Meta |
| :--- | :--- | :--- | :--- | :---: |
| **Pruebas Unitarias de Dominio** | Invariantes de `Playlist` (`MAX_PRENDAS = 15`, mínimo 1 prenda, descripción ≤ 300 chars, privacidad, likes, guardados, destacado) y `PrendaSnapshot` | JUnit 5, AssertJ | `src/test/java/edu/dosw/styleradar/playlist/domain/model/` | `≥ 90%` |
| **Pruebas Unitarias de Aplicación** | Orquestación de los 5 casos de uso (`Crear`, `Consultar`, `Destacar`, `AsociarPrenda`, `Interactuar`), validación contra catálogo y disparo de eventos | JUnit 5, Mockito, AssertJ, MapStruct | `src/test/java/edu/dosw/styleradar/playlist/application/usecase/` | `≥ 85%` |
| **Pruebas de Adaptadores e Infraestructura** | Endpoints REST en inglés, autorización de roles `STYLERADAR_ADMIN`/`ADMIN_STYLERADAR`, validación `userId`, códigos RFC 7807, Feign `ItemCatalogoClient`, `RabbitMQEventPublisher` (incluyendo tolerancia `AmqpIOException`), `CatalogoEventsConsumer`, `RabbitMqConfig` y adaptador JPA | JUnit 5, Spring MockMvc, Mockito, AssertJ | `src/test/java/edu/dosw/styleradar/playlist/infrastructure/` | `≥ 85%` |
| **Pruebas de Integración** | Arranque completo del contexto Spring Boot, ejecución de migraciones Flyway y validación Hibernate (`ddl-auto: validate`) frente a PostgreSQL 16 real | Spring Boot Test, Testcontainers (`postgres:16-alpine`), JUnit 5 | `src/test/java/edu/dosw/styleradar/playlist/PlaylistServiceApplicationIntegrationTest.java` | Flujo crítico de arranque y BD |

> [!IMPORTANT]
> **Quality Gate de Cobertura en CI/CD:** El pipeline automatizado falla la compilación durante `./mvnw clean verify` si la cobertura reportada por **JaCoCo** cae por debajo del umbral del **85 %** (`[PENDIENTE: confirmar regla exacta en pom.xml]`), impidiendo fusionar cambios en `develop` o `master` sin respaldo de pruebas automatizadas. Además, `PlaylistServiceApplicationIntegrationTest` está anotado con `@Testcontainers(disabledWithoutDocker = true)` para no bloquear entornos sin daemon Docker local, ejecutándose plenamente en runners con Docker habilitado.

---

[⬅ Volver al README Principal](../../README.md)
