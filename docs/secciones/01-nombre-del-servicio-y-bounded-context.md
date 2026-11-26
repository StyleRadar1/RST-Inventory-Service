[⬅ Volver al README Principal](../../README.md)

---

## 1. NOMBRE DEL SERVICIO Y SU BOUNDED CONTEXT

* **Identificador de Servicio:** `ms-inventario` (`RST-Inventory-Service`)
* **Paquete Arquitectónico:** `Paquete 2: Catálogo`
* **Puerto de Escucha Oficial:** `8200`
* **Base de Datos Asignada:** `PostgreSQL 16` (`inventario_db`), aislada bajo el patrón *Database-per-Service*.
* **Caché:** No aplica (el servicio no utiliza caché distribuida en memoria como Redis; persiste y consulta directamente en `PostgreSQL 16` con bloqueos pesimistas en reservas).
* **Bounded Context:** `Inventario por talla`

### Responsabilidades del Dominio

Este microservicio encapsula exclusivamente la lógica de negocio del inventario multi-sede por talla de las prendas publicadas por los almacenes aliados de StyleRadar, garantizando consistencia transaccional ACID en registros, ajustes, reservas, cálculo de estados (`DISPONIBLE`, `ULTIMAS_UNIDADES`, `NUEVA_PRENDA`, `AGOTADA`, `SUSPENDIDO`) e idempotencia ante eventos asíncronos.

| Qué HACE este Microservicio (Alcance) | Qué NO HACE este Microservicio (Límites) |
| :--- | :--- |
| • Registra existencias iniciales individuales y por lote de tallas tipificadas (`ROPA`: `XS`–`XXL`; `CALZADO`: `34`–`45`), validando existencia y sede en `ms-item-catalogo`. | • NO autentica usuarios ni emite tokens JWT (delegado a `ms-auth` / API Gateway). |
| • Actualiza existencias físicas por variante/talla e informa cambios de disponibilidad y filtrado por talla. | • NO publica, modera ni edita fichas de prendas (delegado a `ms-item-catalogo` en `:8220`). |
| • Gestiona el ciclo completo de reservas (`reservar`, `confirmar`, `liberar`) con bloqueo pesimista (`PESSIMISTIC_WRITE`). | • NO gestiona playlists de estilo (delegado a `ms-playlist` en `:8260`). |
| • Calcula estados de disponibilidad, emite eventos de stock actualizado, últimas unidades (`≤ 3`) y agotado (`0`), y suspende sedes por mora al recibir eventos de `ms-pagos`. | • NO procesa pagos ni pasarelas ni accede a bases de datos de otros microservicios. |

### Catálogo de Requerimientos Funcionales Oficiales

1. **`SR-RF-10` (`RES-102`)** · *Filtrar prendas por talla (apoyo)*: consulta de identificadores de prendas con existencias físicas disponibles en una talla específica y filtro opcional por sede (`GET /api/v1/inventory/sizes/{size}/garments?branchId=...`).
2. **`SR-RF-52` (`RES-6`)** · *Registrar tallas disponibles de una prenda*: asociación inicial individual o en lote de tallas compatibles por tipo de prenda (`ROPA` vs `CALZADO`) validando propiedad de almacén contra `ms-item-catalogo` (`POST /api/v1/inventory`, `POST /api/v1/inventory/batch`).
3. **`SR-RF-56` (`RES-7`)** · *Actualizar disponibilidad por talla*: modificación directa e independiente del stock físico de una talla por ID de ítem o por prenda y sede (`PUT /api/v1/inventory/{id}/adjustment`, `PATCH /api/v1/inventory/garments/{garmentId}/branches/{branchId}/sizes/{size}`).
4. **`SR-RF-57` (`RES-8`)** · *Ocultar prenda agotada de los resultados disponibles*: verificación de disponibilidad consolidada para venta y listado de prendas no agotadas por sede (`GET /api/v1/inventory/garments/{garmentId}`, `GET /api/v1/inventory/garments/{garmentId}/availability`, `GET /api/v1/inventory/branches/{branchId}/available`), además de emisión del evento `styleradar.inventario.stock.agotado`.
5. **Capacidades de Dominio Complementarias (`SR-RF-50`, `SR-RF-53`, `SR-RF-59`, `SR-RF-60`)**: bloqueo temporal, confirmación y liberación de reservas (`POST /api/v1/inventory/reservations`, `/confirm`, `/release`), umbral de últimas unidades (`≤ 3`) y ventana de novedad (`NUEVA_PRENDA` durante `7` días desde publicación).

### Contexto de Marca / Arquetipo

<div align="center">
  <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/antonia.jpg" width="130" style="border-radius:10px;" alt="Antonia (Sastre) - Arquetipo Tiendas y Descubrimiento" />
  <p><b>Antonia (Sastre)</b> · <i>Tiendas &amp; Descubrimiento de Stock Real</i></p>
</div>

Dentro del ecosistema **StyleRadar** (`Paquete 2: Catálogo`), `ms-inventario` habilita la promesa central de inventario multi-sede en tiempo real para Bogotá: asegura que los compradores descubran únicamente prendas con stock real disponible en la tienda física seleccionada y resguarda la disponibilidad durante el proceso de checkout.

---

[⬅ Volver al README Principal](../../README.md)
