[⬅ Volver al README Principal](../../README.md)

---

## 4. EVENTOS QUE PUBLICA Y CONSUME

La comunicación asíncrona de `ms-playlist` se ejecuta a través de **RabbitMQ 3.13.7** (`5672` en local / `5671` AMQPS con TLS en QA) sobre el exchange principal compartido **`styleradar.exchange`** (`topic`, `durable = true`) en formato JSON UTF-8.

* **Manejo de fallos y colas muertas (DLQ):** La cola consumidora `styleradar.playlist.prenda-retirada.q` está enlazada al Dead Letter Exchange **`styleradar.dlx`** hacia la cola **`styleradar.playlist.dlq`**.
* **Desacoplamiento de tipos Java:** `RabbitMqConfig` elimina la cabecera `__TypeId__` al serializar mensajes salientes para evitar acoplamiento de clases entre microservicios.
* **Resiliencia en entornos sin broker (PROD):** Todos los envíos en `RabbitMQEventPublisher` pasan por el método centralizado `enviar(...)` protegido con `try/catch (AmqpException)`, registrando una advertencia en logs sin interrumpir ni retornar `500` en las operaciones REST cuando RabbitMQ no está disponible.

### Matriz de Eventos AMQP

| Tipo | Routing Key | Cola (Queue) | Microservicio Emisor / Receptor | Cuándo se Dispara / Acción |
| :---: | :--- | :--- | :--- | :--- |
| **PUBLICA** | `styleradar.playlist.creada` | — *(Topic `styleradar.exchange`)* | `ms-playlist` &rarr; `ms-estadisticas` (`:8420`) | Al crear exitosamente una nueva playlist (`POST /api/v1/playlists`). Alimenta el indicador `PLATFORM_PLAYLISTS_CREATED`. |
| **PUBLICA** | `styleradar.playlist.destacada.actualizada` | — *(Topic `styleradar.exchange`)* | `ms-playlist` &rarr; Suscriptores en `styleradar.exchange` | Al fijar (`PUT .../featured`) o remover (`DELETE .../featured`) una playlist como destacada. |
| **PUBLICA** | `styleradar.playlist.prenda.asociada` | — *(Topic `styleradar.exchange`)* | `ms-playlist` &rarr; Suscriptores en `styleradar.exchange` | Al crear una playlist con prendas o al asociar una prenda aprobada (`POST .../garments`). |
| **CONSUME** | `styleradar.catalogo.prenda.retirada` | `styleradar.playlist.prenda-retirada.q` | `ms-item-catalogo` (`:8220`) &rarr; `ms-playlist` | Cuando un almacén o administrador retira una prenda del catálogo; `CatalogoEventsConsumer` marca `disponible = false` en todas las playlists que la contengan. |

<details>
<summary><b>Click para ver Esquema JSON de los Eventos Publicados y Consumidos</b></summary>

#### 1. Evento Publicado: `styleradar.playlist.creada`

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440101",
  "eventType": "styleradar.playlist.creada",
  "occurredAt": "2026-11-24T16:15:00Z",
  "timestamp": "2026-11-24T16:15:00Z",
  "producer": "ms-playlist",
  "data": {
    "playlistId": "b1b7e6d2-4d30-4f8f-9d9c-1f2e3a4b5c6d"
  }
}
```

#### 2. Evento Publicado: `styleradar.playlist.destacada.actualizada`

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440102",
  "eventType": "styleradar.playlist.destacada.actualizada",
  "timestamp": "2026-11-24T16:20:00Z",
  "producer": "ms-playlist",
  "data": {
    "playlistId": "b1b7e6d2-4d30-4f8f-9d9c-1f2e3a4b5c6d",
    "destacada": true
  }
}
```

#### 3. Evento Publicado: `styleradar.playlist.prenda.asociada`

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440103",
  "eventType": "styleradar.playlist.prenda.asociada",
  "timestamp": "2026-11-24T16:21:00Z",
  "producer": "ms-playlist",
  "data": {
    "playlistId": "b1b7e6d2-4d30-4f8f-9d9c-1f2e3a4b5c6d",
    "prendaId": "83114431-f823-47a1-b2db-6a62f70a780c"
  }
}
```

#### 4. Evento Consumido: `styleradar.catalogo.prenda.retirada`

`CatalogoEventsConsumer` lee la clave `prendaId` en formato UUID desde la raíz del mensaje JSON:

```json
{
  "eventId": "evt-cat-ret-8810",
  "eventType": "styleradar.catalogo.prenda.retirada",
  "prendaId": "83114431-f823-47a1-b2db-6a62f70a780c"
}
```

</details>

---

[⬅ Volver al README Principal](../../README.md)
