[⬅ Volver al README Principal](../../README.md)

---

## 12. EQUIPO, RESPONSABLES Y ENLACE A LA ÉPICA EN JIRA

### Equipo y Responsables del Microservicio

Desarrollado y mantenido por el **Equipo `Restrepo`** (prefijo `RST`, **Paquete 2: Catálogo**) para la asignatura **DOSW 2026** dentro del ecosistema **StyleRadar**.

<div align="center">
  <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/logo/styleradar-logo.svg" width="240" alt="StyleRadar - Equipo Restrepo" />
  <br /><br />

| Rol | Nombre Completo | Usuario GitHub |
| :---: | :--- | :---: |
| **Líder y apoyo** | `Juan Nicolás Álvarez Muñoz` | [@JUANNICOLASA](https://github.com/JUANNICOLASA) |
| **Backend Developer** | `Daniel Mosquera` | [@Danielmmartinez](https://github.com/Danielmmartinez) |
| **Frontend Developer** | `Carlos Sánchez` | [@Carlossj8](https://github.com/Carlossj8) |

</div>

### Enlaces de Gestión, Repositorios y Evidencias

* **Repositorio GitHub:** [StyleRadar1/RST-Playlist-Service](https://github.com/StyleRadar1/RST-Playlist-Service)
* **Organización GitHub:** [StyleRadar1](https://github.com/StyleRadar1)
* **Épica del Microservicio en Jira:** `[PENDIENTE: clave y enlace directo de la épica de ms-playlist en el proyecto RES de Jira]`
* **Tablero Scrum del Equipo:** [Restrepo-StyleRadar (RES)](https://mail-team-nyjtgqcj.atlassian.net/jira/software/projects/RES/boards/76)
* **Swagger UI (Local):** [http://localhost:8260/api/v1/swagger-ui.html](http://localhost:8260/api/v1/swagger-ui.html)
* **Especificación OpenAPI JSON (Local):** [http://localhost:8260/api/v1/v3/api-docs](http://localhost:8260/api/v1/v3/api-docs)

<div align="center">
  <img src="../images/JiraEvidence.png" alt="Evidencia 05 - Tablero Scrum y Seguimiento en Jira" width="100%" style="border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);" />
  <p><sub><b>Figura 12.1:</b> [PENDIENTE: capturar tablero Scrum / épica de ms-playlist en Jira y guardarla en docs/images/JiraEvidence.png]</sub></p>
</div>

### Requerimientos Funcionales Asignados

| Código RF | Nombre del Requerimiento | Actor Principal | Criterio de Aceptación Clave |
| :---: | :--- | :--- | :--- |
| **`SR-RF-69`** | Crear playlist de estilo con al menos una prenda aprobada | Comprador (`BUYER`) | `POST /api/v1/playlists` exige mínimo 1 prenda existente y `APROBADA` en `ms-item-catalogo`; retorna `201` y publica `styleradar.playlist.creada`. |
| **`SR-RF-70`** | Gestionar playlists propias y asociar o retirar prendas | Propietario de playlist (`X-User-Id`) | Solo el creador puede editar (`PUT`), eliminar (`DELETE`) o asociar/retirar prendas en `/garments`; impide duplicados (`422`) y respeta tope de 15 (`RNF-12`). |
| **`SR-RF-71`** | Consultar playlists e interactuar con Me Gusta y Guardados | Comprador / Visitante | Expone listados públicos, populares paginadas, control de acceso en playlists `PRIVADA` (`403 PLAYLIST_PRIVADA`), consulta propia en `/users/{userId}` y toggles en `/likes` y `/saves`. |
| **`SR-RF-72`** | Destacar playlists curadas para Home (máximo 3) | Administrador StyleRadar (`STYLERADAR_ADMIN` / `ADMIN_STYLERADAR`) | Solo roles administrativos pueden ejecutar `PUT` / `DELETE /api/v1/playlists/{id}/featured` (`403 ACCESO_DENEGADO` en otro rol); tope máximo de 3 destacadas (`409 LIMITE_DESTACADAS`). |
| **`RNF-12`** | Límite máximo de 15 prendas por playlist | Sistema (`Playlist`) | Al intentar agregar la prenda 16 al crear o mediante `POST /api/v1/playlists/{id}/garments`, responde `422 LIMITE_PRENDAS_PLAYLIST`. |

---

[⬅ Volver al README Principal](../../README.md)
