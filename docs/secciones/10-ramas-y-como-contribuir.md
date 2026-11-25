[⬅ Volver al README Principal](../../README.md)

---

## 10. RAMAS Y CÓMO CONTRIBUIR

El repositorio `RST-Playlist-Service` adopta el modelo **GitFlow** acordado por el Equipo Restrepo (`RST`). Las ramas principales **`master`** (Producción) y **`develop`** (Integración / QA) están protegidas: ningún desarrollador hace *push* directo sobre ellas; toda integración ingresa exclusivamente mediante **Pull Request** revisado, con el pipeline en verde y aprobación previa. Para promover cambios desde `develop` hacia `master` se tramita además la solicitud oficial de CMV.

### Estructura Oficial de Ramas

```text
master                                  <-- Versiones estables en Producción (CMV aprobada)
└── develop                             <-- Integración continua y despliegue automático a QA
    ├── feature/HU-xx-descripcion       <-- Nuevas funcionalidades o historias de usuario
    ├── fix/descripcion-corta           <-- Correcciones de defectos, seguridad o configuración
    ├── refactor/descripcion-corta      <-- Mejoras estructurales / estandarización de API
    └── docs/descripcion-corta          <-- Actualización de documentación y evidencias
```

### Convención de Commits (*Conventional Commits*)

Todo *commit* debe redactarse siguiendo el formato estándar:

```text
<tipo>(<alcance>): <descripción clara en español>
```

Tipos admitidos: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `ci`.

**Ejemplos reales del repositorio `ms-playlist`:**

* `fix(db): limitar pool de conexiones hikari a 3 en application.yml`
* `fix(security): validar rol admin de StyleRadar al destacar playlists`
* `refactor(api): dejar solo rutas en ingles, actualizar feign a garments y swagger en ingles`
* `feat(events): publicar evento styleradar.playlist.creada al crear playlist`

### Checklist Obligatorio para Pull Requests

- [ ] La rama parte de la versión más reciente de `develop` (`git pull origin develop`).
- [ ] Todos los endpoints nuevos o modificados cumplen el estándar: rutas en inglés bajo `/api/v1`, sin `/me` y sin verbos en la URI.
- [ ] Todas las pruebas pasan (`./mvnw clean verify`) y la cobertura **JaCoCo** es mayor o igual al **85 %**.
- [ ] El análisis estático (**SpotBugs**) finaliza sin hallazgos bloqueantes.
- [ ] No se incluyen credenciales, contraseñas, tokens ni archivos `.env` en el control de versiones.
- [ ] La documentación Swagger (`PlaylistApi`, `HealthApi`, `OpenApiConfig`) y los archivos de `docs/` reflejan cualquier cambio de contrato REST o AMQP.
- [ ] El Pull Request cuenta con al menos una aprobación (*Approve*) de otro integrante del equipo antes del *merge*.

---

[⬅ Volver al README Principal](../../README.md)
