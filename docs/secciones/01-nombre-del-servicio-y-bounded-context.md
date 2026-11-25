[⬅ Volver al README Principal](../../README.md)

---

## 1. NOMBRE DEL SERVICIO Y SU BOUNDED CONTEXT

* **Identificador de Servicio:** `ms-playlist` (`RST-Playlist-Service`)
* **Paquete Arquitectónico:** `Paquete 2: Catálogo`
* **Puerto de Escucha Oficial:** `8260` (con prefijo de rutas `/api/v1`)
* **Base de Datos Asignada:** `PostgreSQL 16` (`styleradar_playlist`), aislada bajo el patrón *Database-per-Service* y gestionada con `Flyway`
* **Caché:** No aplica (el servicio no utiliza Redis ni caché distribuida; opera directamente con persistencia ACID en PostgreSQL)
* **Bounded Context:** `Playlists de estilo y colecciones temáticas de prendas de vestir`

### Responsabilidades del Dominio

Este microservicio encapsula exclusivamente la creación, curaduría, asociación de prendas aprobadas, consulta, interacciones sociales (likes y guardados) y gestión de colecciones destacadas dentro del ecosistema StyleRadar.

| Qué HACE este Microservicio (Alcance) | Qué NO HACE este Microservicio (Límites) |
| :--- | :--- |
| • Crea, edita y elimina playlists públicas o privadas exigiendo al menos una prenda aprobada (máx. 15 prendas por playlist). | • NO autentica usuarios ni emite/valida tokens JWT (delegado a `ms-auth` y al `API Gateway`, que propaga `X-User-Id`, `X-User-Role` y `X-User-Roles`). |
| • Valida en línea que cada prenda asociada exista y esté en estado `APROBADA` consultando a `ms-item-catalogo` (`:8220`) vía OpenFeign. | • NO publica, edita, modera ni administra el catálogo maestro ni fotos/precios de prendas (delegado a `ms-item-catalogo` en `:8220`). |
| • Gestiona interacciones sociales por usuario (`likes` y `saves`) y ordena listas públicas populares paginadas. | • NO gestiona tallas ni unidades de inventario por sede (delegado a `ms-inventario` en `:8200`). |
| • Administra el tope máximo de 3 playlists destacadas en Home restringido a roles `STYLERADAR_ADMIN` o `ADMIN_STYLERADAR`. | • NO calcula métricas globales ni tableros agregados (delegado a `ms-estadisticas` en `:8420`, al cual alimenta mediante eventos). |
| • Marca prendas como no disponibles (`disponible = false`) dentro de las playlists cuando recibe el evento de retiro desde catálogo. | • NO accede directamente a bases de datos de otros microservicios. |

### Catálogo de Requerimientos Funcionales Oficiales

1. **`SR-RF-69`** — *Crear playlist de estilo con al menos una prenda aprobada* &rarr; `POST /api/v1/playlists` (valida existencia y estado `APROBADA` contra `ms-item-catalogo` y exige mínimo 1 prenda).
2. **`SR-RF-70`** — *Gestionar playlists propias y asociar o retirar prendas* &rarr; `PUT /api/v1/playlists/{id}`, `DELETE /api/v1/playlists/{id}`, `POST /api/v1/playlists/{id}/garments` y `DELETE /api/v1/playlists/{id}/garments/{garmentId}`.
3. **`SR-RF-71`** — *Consultar playlists e interactuar con Me Gusta y Guardados* &rarr; `GET /api/v1/playlists`, `GET /api/v1/playlists/{id}`, `GET /api/v1/playlists/popular`, `GET /api/v1/playlists/users/{userId}`, `GET /api/v1/playlists/users/{userId}/saved`, `POST /api/v1/playlists/{id}/likes` y `POST /api/v1/playlists/{id}/saves`.
4. **`SR-RF-72`** — *Destacar y consultar playlists curadas para Home (máximo 3)* &rarr; `GET /api/v1/playlists/featured`, `PUT /api/v1/playlists/{id}/featured` y `DELETE /api/v1/playlists/{id}/featured` (restringido a `STYLERADAR_ADMIN` / `ADMIN_STYLERADAR`).
5. **`RNF-12`** — *Capacidad máxima de prendas por playlist (15 prendas)* &rarr; Regla de invariante aplicada en el agregado `Playlist` al crear o asociar prendas (`LIMITE_PRENDAS_PLAYLIST`).

### Contexto de Marca / Arquetipo

Dentro de la narrativa y arquetipos oficiales de StyleRadar, `ms-playlist` materializa el dominio de **Mateo (Calle)**, orientado a la curaduría de tendencias urbanas, colecciones de inspiración y conexión directa entre las prendas aprobadas de los almacenes aliados y las preferencias de los compradores en Bogotá.

<div align="center">

| Antonia (Sastre) | Mateo (Calle) | Aura (Futuro) | Nora (Rebelde) | Tomás (Esencial) |
| :---: | :---: | :---: | :---: | :---: |
| <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/antonia.jpg" width="110" style="border-radius:8px;" alt="Antonia" /><br>**Sastre**<br><sub>Tiendas & Descubrimiento</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/mateo.jpg" width="110" style="border-radius:8px;" alt="Mateo" /><br>**Calle**<br><sub>Playlists & Cultura Urbana</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/aura.jpg" width="110" style="border-radius:8px;" alt="Aura" /><br>**Futuro**<br><sub>Probador Virtual AI</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/nora.jpg" width="110" style="border-radius:8px;" alt="Nora" /><br>**Rebelde**<br><sub>Segunda Vida & Vintage</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/tomas.jpg" width="110" style="border-radius:8px;" alt="Tomás" /><br>**Esencial**<br><sub>Fundaciones & Donaciones</sub> |

</div>

---

[⬅ Volver al README Principal](../../README.md)
