[⬅ Volver al README Principal](../../README.md)

---

## 8. PRUEBAS Y COBERTURA (≥ 85%)

La suite de pruebas de `ms-inventario` valida exhaustivamente el núcleo de dominio, todos los casos de uso, los adaptadores JPA/PostgreSQL, el cliente OpenFeign hacia catálogo, la mensajería RabbitMQ (publicación e idempotencia), los endpoints REST con RFC 7807 y el arranque completo del contexto Spring Boot con Flyway y H2 en modo PostgreSQL. El build exige cobertura mínima del **85 %** (`0.85` en líneas) verificada por **JaCoCo 0.8.12** y cero defectos en **SpotBugs 4.8.6.4**.

### Comandos de Ejecución de Pruebas

```bash
# Linux / macOS / Git Bash: compila, ejecuta las 17 suites, genera reporte JaCoCo, valida >= 85% y corre SpotBugs
./mvnw clean verify
```

```powershell
# Windows PowerShell
.\mvnw.cmd clean verify
```

* **Ruta del Reporte HTML de Cobertura:** `target/site/jacoco/index.html`
* **Reportes Surefire XML:** `target/surefire-reports/*.xml`
* **Reporte SpotBugs XML:** `target/spotbugsXml.xml`

### Métricas Actuales de Cobertura en el Repositorio

* **Umbral mínimo exigido (`jacoco.minimum`):** `85 %` (`0.85`, contador `LINE` sobre `BUNDLE`)
* **Clases de prueba implementadas:** **`17` clases** (`92` métodos `@Test` / `@ParameterizedTest`, equivalentes a **`110` ejecuciones de prueba** contando los 20 casos parametrizados de `TallaTest`)
* **Clases de producción analizadas por JaCoCo:** **`44` clases** (`Analyzed bundle 'RST-Inventory-Service' with 44 classes`)
* **Verificación JaCoCo (`jacoco:check`):** `All coverage checks have been met.` (`≥ 85 %` superado) · `[PENDIENTE: anotar % exacto decimal desde target/site/jacoco/index.html]`
* **Análisis estático SpotBugs (`4.8.6.4` · `Max` / `High`):** **`0` bugs, `0` errores, `0` advertencias** (`BugInstance size is 0`, `Error size is 0`)
* **Resultado de ejecución (`./mvnw clean verify`):** **`BUILD SUCCESS`** (`31.671 s`, ejecutado el `2026-11-24T21:21:55-05:00`)
* **Reporte técnico de seguridad y calidad:** [Análisis de Seguridad, Habeas Data y OWASP Top 10](../security/SeguridadYOwaspInventario.md) *(Nota: `[PENDIENTE: crear docs/security/SeguridadYOwaspInventario.md]`)*

<div align="center">
  <img src="../images/JacocoEvidence.png" alt="Evidencia 03 - Verificación exitosa de JaCoCo (>= 85%) y SpotBugs (0 bugs) en ms-inventario" width="100%" style="border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);" />
  <p><sub><b>Figura 8.1:</b> Ejecución exitosa de <code>./mvnw clean verify</code> validando 44 clases con JaCoCo 0.8.12 (<i>All coverage checks have been met</i>) y SpotBugs 4.8.6.4 (<i>0 bugs, 0 warnings</i>). <i>[PENDIENTE: guardar la captura en <code>docs/images/JacocoEvidence.png</code>]</i></sub></p>
</div>

### Pirámide de Pruebas Implementada

| Nivel de Prueba | Alcance | Tecnologías Utilizadas | Ubicación en el Código | Cobertura Meta |
| :--- | :--- | :--- | :--- | :---: |
| **Pruebas Unitarias · Dominio** | `TallaTest` (20 parametrizados + 3 error), `CantidadTest` (4), `InventarioVarianteTest` (5), `StockItemTest` (12): reglas de tallas, reservas, mora, estados y eventos de dominio. | JUnit 5 (`jupiter-params`), AssertJ | `src/test/java/edu/dosw/styleradar/inventario/domain/` | `≥ 95 %` |
| **Pruebas Unitarias · Aplicación** | `FiltrarPrendasPorTallaTest` (2) y `UseCasesTest` (7): registro individual/lote, validación con catálogo (`404`/`403`), ajuste, reservas con bloqueo, consulta y suspensión. | JUnit 5, Mockito, MapStruct | `src/test/java/edu/dosw/styleradar/inventario/application/` | `≥ 90 %` |
| **Pruebas de Adaptadores e Integración Web/AMQP** | `InventarioRestControllerTest` (20), `GlobalExceptionHandlerTest` (11), `HealthControllerTest` (1), `OrdenPagoEventListenerTest` (6), `SuscripcionEventListenerTest` (3), `ItemCatalogoClientTest` (4), `RabbitMqStockEventPublisherAdapterTest` (5), `PostgreSql*Test` (6). | Spring Boot Test (`@WebMvcTest`, `MockMvc`), Mockito, Feign `Response` | `src/test/java/edu/dosw/styleradar/inventario/infrastructure/` | `≥ 85 %` |
| **Prueba de Contexto Integrado** | `InventarioApplicationIntegrationTest` (1): arranque completo de Spring Boot (`@ActiveProfiles("test")`), validación de esquema Hibernate + migraciones Flyway sobre H2 en modo PostgreSQL. | Spring Boot Test, Flyway, H2 (`MODE=PostgreSQL`), Testcontainers disponible | `src/test/java/edu/dosw/styleradar/inventario/InventarioApplicationIntegrationTest.java` | Arranque limpio |

> [!IMPORTANT]
> **Regla estricta de Quality Gate en `pom.xml` y CI:**
> La ejecución de `./mvnw clean verify` falla automáticamente el build si la cobertura de líneas (`LINE`) medida por **JaCoCo 0.8.12** cae por debajo de `0.85` (excluyendo únicamente `InventoryServiceApplication`, `config/**`, `dto/**` y `persistence/entity/**`), o si **SpotBugs 4.8.6.4** (`effort: Max`, `threshold: High`) detecta al menos un defecto.

---

[⬅ Volver al README Principal](../../README.md)
