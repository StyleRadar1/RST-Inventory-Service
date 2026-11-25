[⬅ Volver al README Principal](../../README.md)

---

## 12. EQUIPO, RESPONSABLES Y ENLACE A LA ÉPICA EN JIRA

### Equipo y Responsables del Microservicio

Desarrollado y mantenido por el **Equipo `Restrepo`** (prefijo `RST`) para la asignatura **DOSW 2026** dentro del ecosistema **StyleRadar** (`Paquete 2: Catálogo`).

<div align="center">
  <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/logo/styleradar-logo.svg" width="260" alt="Logo StyleRadar - Equipo Restrepo" />
</div>

<div align="center">

| Rol | Nombre Completo | Usuario GitHub |
| :---: | :--- | :---: |
| **Líder y apoyo** | `Juan Nicolás Álvarez Muñoz` | [@JUANNICOLASA](https://github.com/JUANNICOLASA) |
| **Backend Developer** | `Daniel Mosquera` | [@Danielmmartinez](https://github.com/Danielmmartinez) |
| **Frontend Developer** | `Carlos Sánchez` | [@Carlossj8](https://github.com/Carlossj8) |

</div>

* **Contacto técnico registrado en OpenAPI:** `daniel.mmartinez@mail.escuelaing.edu.co`

### Enlaces de Gestión, Repositorios y Evidencias

* **Repositorio GitHub:** [StyleRadar1/RST-Inventory-Service](https://github.com/StyleRadar1/RST-Inventory-Service)
* **Organización GitHub:** [StyleRadar1](https://github.com/StyleRadar1)
* **Épica del Microservicio en Jira:** [RES-1 · ms-inventario](https://mail-team-nyjtgqcj.atlassian.net/browse/RES-1)
* **Tablero Scrum del Equipo en Jira:** [Restrepo-StyleRadar (RES)](https://mail-team-nyjtgqcj.atlassian.net/jira/software/projects/RES/boards/76)
* **Documento Técnico del Servicio:** [Documento técnico de ms-inventario](../DocumentoTecnicoInventario.html)
* **Swagger UI (Local):** `http://localhost:8200/api/v1/swagger-ui.html`
* **Especificación OpenAPI JSON (Local):** `http://localhost:8200/api/v1/v3/api-docs`

<div align="center">
  <img src="../images/JiraEvidence.png" alt="Evidencia 05 - Tablero Scrum y seguimiento de la épica RES-1 en Jira" width="100%" style="border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);" />
  <p><sub><b>Figura 12.1:</b> Tablero Scrum del Equipo Restrepo y seguimiento de la épica <code>RES-1</code> (<code>ms-inventario</code>) en Jira. <i>[PENDIENTE: capturar y guardar <code>docs/images/JiraEvidence.png</code>]</i></sub></p>
</div>

### Requerimientos Funcionales Asignados

| Código RF | Nombre del Requerimiento | Actor Principal | Criterio de Aceptación Clave |
| :---: | :--- | :--- | :--- |
| `SR-RF-10` | Filtrar prendas por talla (`RES-102`) | Comprador (`BUYER`) / `ms-item-catalogo` | Retorna los UUID de prendas activas con unidades disponibles (`> 0`) para la talla consultada y filtro opcional por `branchId`. |
| `SR-RF-52` | Registrar tallas disponibles de una prenda (`RES-6`) | Administrador de tienda (`TIENDA` / `STORE_ADMIN`) | Valida compatibilidad de tallas (`ROPA`: `XS`–`XXL`, `CALZADO`: `34`–`45`) y verifica existencia/propiedad de la prenda contra `ms-item-catalogo` (`GET /api/v1/garments/{id}`) antes de registrar el lote. |
| `SR-RF-56` | Actualizar disponibilidad por talla (`RES-7`) | Administrador de tienda (`TIENDA` / `STORE_ADMIN`) | Permite modificar de forma independiente las unidades físicas de una talla (`PUT /{id}/adjustment` o `PATCH .../sizes/{size}`), recalculando estado y emitiendo eventos AMQP. |
| `SR-RF-57` | Ocultar prenda agotada de los resultados disponibles (`RES-8`) | Comprador / `ms-item-catalogo` | Expone `disponibleParaVenta` (`/availability`) y listado de prendas disponibles por sede (`/branches/{branchId}/available`), además de emitir `styleradar.inventario.stock.agotado` al llegar a `0`. |
| `SR-RF-50` / `SR-RF-53` / `SR-RF-59` / `SR-RF-60` | Gestión transaccional, reservas y estados derivados *(soporte de dominio)* | Comprador / `ms-pagos` / Sistema | Bloqueo pesimista para reservas, confirmación/liberación (incluso vía RabbitMQ con idempotencia), umbral de últimas unidades (`≤ 3`) y estado `NUEVA_PRENDA` durante `7` días. |

---

[⬅ Volver al README Principal](../../README.md)
