[⬅ Volver al README Principal](../../README.md)

---

## 3. ENDPOINTS Y ENLACE A SWAGGER

### Matriz Oficial de Enlaces por Entorno (PROD, QA, LOCAL)

| Entorno | Estado | Propósito | URL Base del Servicio | Swagger OpenAPI UI | Health Check |
| :--- | :---: | :--- | :--- | :--- | :--- |
| **Producción (PROD)** | Desplegado | Despliegue productivo de StyleRadar desde `master` | `https://rst-inventory-service-prod.redwave-74d6e30f.northcentralus.azurecontainerapps.io/api/v1` | [Swagger UI PROD](https://rst-inventory-service-prod.redwave-74d6e30f.northcentralus.azurecontainerapps.io/api/v1/swagger-ui.html) | [`/api/v1/health`](https://rst-inventory-service-prod.redwave-74d6e30f.northcentralus.azurecontainerapps.io/api/v1/health) |
| **Calidad / Pruebas (QA)** | Desplegado | Pruebas integradas del Sprint desde `develop` | `https://rst-inventory-service-qa.redwave-74d6e30f.northcentralus.azurecontainerapps.io/api/v1` | [Swagger UI QA](https://rst-inventory-service-qa.redwave-74d6e30f.northcentralus.azurecontainerapps.io/api/v1/swagger-ui.html) | [`/api/v1/health`](https://rst-inventory-service-qa.redwave-74d6e30f.northcentralus.azurecontainerapps.io/api/v1/health) |
| **Desarrollo Local (DEV)** | Local | Entorno de desarrollo local | `http://localhost:8200/api/v1` | [Swagger UI Local](http://localhost:8200/api/v1/swagger-ui.html) | [`/api/v1/health`](http://localhost:8200/api/v1/health) |

* **Especificación OpenAPI JSON (Local):** `http://localhost:8200/api/v1/v3/api-docs`
* **Especificación OpenAPI JSON (QA):** `https://rst-inventory-service-qa.redwave-74d6e30f.northcentralus.azurecontainerapps.io/api/v1/v3/api-docs`
* **Arranque en frío (*Cold Start*):** Los ambientes de QA y PROD escalan a cero cuando no reciben tráfico. La primera petición puede responder `503` durante unos minutos mientras el contenedor vuelve a arrancar.

<div align="center">
  <img src="../images/SwaggerEvidence.png" alt="Evidencia 01 - Documentación Swagger OpenAPI UI de ms-inventario" width="100%" style="border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);" />
  <p><sub><b>Figura 3.1:</b> Documentación interactiva Swagger OpenAPI UI de <code>ms-inventario</code> con las rutas oficiales en inglés bajo <code>/api/v1/inventory</code>. <i>[PENDIENTE: capturar y guardar <code>docs/images/SwaggerEvidence.png</code>]</i></sub></p>
</div>

### Tabla de Endpoints Expuestos

| Método | Endpoint | Descripción | Rol Requerido | Códigos HTTP |
| :---: | :--- | :--- | :--- | :---: |
| `GET` | `/api/v1/inventory/garments/{garmentId}` | Consultar existencias consolidadas y desglosadas por sede y talla de una prenda | Autenticado (`BUYER` / `TIENDA`) | `200`, `404` |
| `GET` | `/api/v1/inventory/garments/{garmentId}/availability` | Verificar si la prenda tiene disponibilidad para venta y mostrarse en resultados (`SR-RF-57`) | Interno / Autenticado | `200`, `404` |
| `GET` | `/api/v1/inventory/branches/{branchId}/available` | Listar los UUID de prendas activas y no agotadas de una sede (`SR-RF-57`) | Interno | `200` |
| `GET` | `/api/v1/inventory/sizes/{size}/garments` | Filtrar UUID de prendas disponibles en una talla (parámetro opcional `branchId`) (`SR-RF-10`) | Interno | `200` |
| `POST` | `/api/v1/inventory` | Registrar o fijar existencias iniciales de una talla individual en una sede | `TIENDA`, `ADMIN` | `201`, `400`, `403` |
| `POST` | `/api/v1/inventory/batch` | Registrar un lote de tallas de una prenda validando existencia y almacén contra catálogo (`SR-RF-52`) | `TIENDA`, `ADMIN` | `201`, `400`, `403`, `404`, `503` |
| `PUT` | `/api/v1/inventory/{id}/adjustment` | Ajustar manualmente el stock físico de una talla por ID de `StockItem` | `TIENDA`, `ADMIN` | `200`, `400`, `404`, `422` |
| `PATCH` | `/api/v1/inventory/garments/{garmentId}/branches/{branchId}/sizes/{size}` | Modificar de forma independiente las unidades disponibles de una talla por prenda y sede (`SR-RF-56`) | `TIENDA`, `ADMIN` | `200`, `400`, `403`, `404` |
| `POST` | `/api/v1/inventory/reservations` | Bloquear temporalmente unidades disponibles para un checkout (`SR-RF-53`) | Interno / Autenticado | `200`, `400`, `403`, `404`, `409` |
| `POST` | `/api/v1/inventory/reservations/confirm` | Confirmar salida definitiva de unidades previamente reservadas | Interno | `200`, `400`, `403`, `404`, `409` |
| `POST` | `/api/v1/inventory/reservations/release` | Liberar unidades reservadas y devolverlas al stock disponible | Interno | `200`, `400`, `403`, `404`, `409` |
| `GET` | `/api/v1/health` | Estado del servicio (`UP`, `ms-inventario`, puerto `8200`) | Público | `200`, `500` |

> [!NOTE]
> **Dependencia síncrona con `ms-item-catalogo` y tolerancia a fallos:**
> * El endpoint `POST /api/v1/inventory/batch` consulta de forma síncrona vía OpenFeign a `ms-item-catalogo` (`GET /api/v1/garments/{id}`) con timeouts de conexión de `5000 ms` y lectura de `15000 ms`.
> * Si la prenda no existe en catálogo, retorna `404 PRENDA_NO_ENCONTRADA`; si pertenece a otra sede (`almacenId` distinto de `sedeId`), retorna `403 PRENDA_DE_OTRO_ALMACEN`; y si `ms-item-catalogo` no responde o está en *cold start*, degrada limpiamente devolviendo `503 CATALOGO_NO_DISPONIBLE`.
> * En endpoints internos no acoplados a este flujo (`/inventory`, `/patch`, `/reservations`, `/garments/{id}`), `ms-inventario` opera de forma autónoma sobre `inventario_db`. Además, el entorno Actuator expone `/actuator/health` e `/actuator/info`.

<details>
<summary><b>Click para ver Ejemplos Interactivos de Request y Response JSON (Payloads)</b></summary>

#### 1. Registrar lote de tallas (`POST /api/v1/inventory/batch`)
```bash
curl -X POST http://localhost:8200/api/v1/inventory/batch \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "prendaId": "bb6c7f7f-d743-46f3-aa8d-b6d61ef1cfc4",
    "sedeId": "fc942335-ab30-350a-b6a5-577cf0ca2fb5",
    "tipoPrenda": "ROPA",
    "tallas": [
      { "talla": "S", "cantidad": 6 },
      { "talla": "M", "cantidad": 10 },
      { "talla": "L", "cantidad": 1 },
      { "talla": "XL", "cantidad": 0 }
    ],
    "fechaPublicacion": "2026-11-24T16:50:00Z"
  }'
```

**Respuesta Exitosa (`201 Created`):**
```json
{
  "prendaId": "bb6c7f7f-d743-46f3-aa8d-b6d61ef1cfc4",
  "tipoPrenda": "ROPA",
  "estadoGeneral": "NUEVA_PRENDA",
  "stockDisponibleTotal": 17,
  "sedes": [
    {
      "sedeId": "fc942335-ab30-350a-b6a5-577cf0ca2fb5",
      "activo": true,
      "variantes": [
        { "talla": "S", "stockDisponible": 6, "stockReservado": 0, "estadoVariante": "NUEVA_PRENDA" },
        { "talla": "M", "stockDisponible": 10, "stockReservado": 0, "estadoVariante": "NUEVA_PRENDA" },
        { "talla": "L", "stockDisponible": 1, "stockReservado": 0, "estadoVariante": "ULTIMAS_UNIDADES" },
        { "talla": "XL", "stockDisponible": 0, "stockReservado": 0, "estadoVariante": "AGOTADA" }
      ]
    }
  ]
}
```

#### 2. Reservar stock (`POST /api/v1/inventory/reservations`)
```bash
curl -X POST http://localhost:8200/api/v1/inventory/reservations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "prendaId": "bb6c7f7f-d743-46f3-aa8d-b6d61ef1cfc4",
    "sedeId": "fc942335-ab30-350a-b6a5-577cf0ca2fb5",
    "talla": "M",
    "cantidad": 2,
    "transaccionId": "018e3a2b-5555-7000-8000-000000000005"
  }'
```

**Respuesta Exitosa (`200 OK`):**
```json
{
  "reservaExitosa": true,
  "prendaId": "bb6c7f7f-d743-46f3-aa8d-b6d61ef1cfc4",
  "sedeId": "fc942335-ab30-350a-b6a5-577cf0ca2fb5",
  "talla": "M",
  "unidadesReservadas": 2,
  "stockDisponibleRestante": 8,
  "expiracionReserva": "2026-11-24T17:05:00Z"
}
```

#### 3. Respuesta de Error Estándar RFC 7807 (`409 Conflict` / `application/problem+json`)
```json
{
  "type": "about:blank",
  "title": "Stock insuficiente",
  "status": 409,
  "detail": "Stock disponible insuficiente para la talla XL. Disponible: 0, solicitado: 1",
  "instance": "/api/v1/inventory/reservations",
  "code": "STOCK_INSUFICIENTE",
  "timestamp": "2026-11-24T16:51:00Z"
}
```

</details>

---

[⬅ Volver al README Principal](../../README.md)
