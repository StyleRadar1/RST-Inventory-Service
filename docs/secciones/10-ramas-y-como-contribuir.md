[⬅ Volver al README Principal](../../README.md)

---

## 10. RAMAS Y CÓMO CONTRIBUIR

El flujo de desarrollo en `RST-Inventory-Service` adopta **GitFlow** con ramas protegidas (`master`/`main` para Producción y `develop` para integración continua en QA). Todo cambio se integra exclusivamente mediante **Pull Request** con revisión de pares y pipeline en verde (`test-and-quality` + `docker-dry-run`).

### Estructura Oficial de Ramas

```text
master (main)                       <-- Versiones estables desplegadas en Producción (PROD)
└── develop                         <-- Integración del equipo y despliegue continuo en QA
    ├── feature/HU-xx-descripcion   <-- Nuevas funcionalidades por historia de usuario
    ├── fix/descripcion             <-- Corrección de errores (ej. fix/limitar-pool-hikari)
    ├── bugfix/descripcion          <-- Correcciones disparadoras de CI por push
    ├── hotfix/descripcion          <-- Parches urgentes de producción
    └── refactor/descripcion        <-- Refactorizaciones técnicas (ej. refactor/endpoints-ingles)
```

### Convención de Commits (*Conventional Commits*)

Todo commit sigue el formato estandarizado en español:

```text
<tipo>(<alcance>): <descripción en español>
```

Tipos más usados: `feat`, `fix`, `test`, `docs`, `refactor`, `chore`.

**Ejemplos reales del repositorio:**
* `feat(inventario): validar existencia y sede de prenda contra ms-item-catalogo`
* `fix(config): limitar pool de conexiones hikari en postgresql`
* `refactor(api): dejar solo rutas en inglés bajo /api/v1 y actualizar feign a /garments`
* `docs(readme): estandarizar documentación técnica en 12 secciones`

### Checklist Obligatorio para Pull Requests

- [ ] La rama parte de la versión más reciente de `develop`.
- [ ] Todas las rutas nuevas o modificadas cumplen el estándar oficial: **inglés**, sin `/me` y bajo `/api/v1` (y `/api/v1/health` para salud).
- [ ] `./mvnw clean verify` finaliza con `BUILD SUCCESS`, cobertura JaCoCo `≥ 85 %` y `0` bugs en SpotBugs.
- [ ] No se versionan archivos `.env`, contraseñas ni tokens en el código fuente.
- [ ] Si se agregan propiedades en `application.yml`, eventos RabbitMQ o endpoints, quedan actualizadas las secciones correspondientes en `docs/secciones/`.
- [ ] El PR cuenta con aprobación de otro integrante del equipo antes del *merge*.

---

[⬅ Volver al README Principal](../../README.md)
