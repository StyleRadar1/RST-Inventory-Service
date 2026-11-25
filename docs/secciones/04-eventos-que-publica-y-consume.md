[⬅ Volver al README Principal](../../README.md)

---

## 4. EVENTOS QUE PUBLICA Y CONSUME

La comunicación asíncrona entre microservicios se realiza a través de **RabbitMQ 3.13.7** sobre el exchange oficial **`styleradar.exchange`** (`TopicExchange`) en formato JSON UTF-8 plano **sin cabecera `__TypeId__`**. Los mensajes que fallan durante el procesamiento se reintentan hasta `3` veces con *backoff* exponencial (`1000 ms`, multiplicador `2.0`, máximo `10000 ms`); las excepciones fatales de conversión o resolución (`MessageConversionException`, `AmqpRejectAndDontRequeueException`, `MethodArgumentResolutionException`) se rechazan sin reencolar y se derivan por **`styleradar.dlx`** (`DirectExchange`) hacia la cola **`styleradar.inventario.dlq`**.

### Matriz de Eventos AMQP

| Tipo | Routing Key | Cola (Queue) | Microservicio Emisor / Receptor | Cuándo se Dispara / Acción |
| :---: | :--- | :--- | :--- | :--- |
| **PUBLICA** | `styleradar.inventario.actualizado` | N/A *(publicado al exchange `styleradar.exchange`)* | `ms-inventario` $\rightarrow$ `ms-item-catalogo`, `ms-notifications`, `ms-estadisticas` | Se dispara en toda modificación de stock por talla (registro, lote, ajuste, reserva, confirmación, liberación o descuento). |
| **PUBLICA** | `styleradar.inventario.stock.agotado` | N/A *(publicado al exchange `styleradar.exchange`)* | `ms-inventario` $\rightarrow$ `ms-item-catalogo`, `ms-estadisticas` | Se dispara adicionalmente cuando las unidades disponibles de una talla llegan a `0`. |
| **PUBLICA** | `styleradar.inventario.stock.ultimas-unidades` | N/A *(publicado al exchange `styleradar.exchange`)* | `ms-inventario` $\rightarrow$ `ms-estadisticas`, suscriptores en `styleradar.exchange` | Se dispara adicionalmente cuando las unidades disponibles de una talla quedan entre `1` y el umbral (`≤ 3`). |
| **CONSUME** | `styleradar.pagos.pago.confirmado` | `styleradar.inventario.pago-aprobado.q` *(DLQ: `styleradar.inventario.dlq`)* | `ms-pagos` $\rightarrow$ `ms-inventario` | Confirma la reserva y deduce `stockReservado` (soporta sobre `data` plano o lista `data.items[]`; registra `eventId`/`eventoId` en `eventos_procesados`). |
| **CONSUME** | `styleradar.pagos.suscripcion.suspendida` | `styleradar.inventario.suscripcion-suspendida.q` *(DLQ: `styleradar.inventario.dlq`)* | `ms-pagos` $\rightarrow$ `ms-inventario` | Suspende por mora todo el inventario de la sede (`sedeId` o `tiendaId`), fijando `activo = false` y estado `SUSPENDIDO`. |
| **CONSUME *(Cola lista sin binding)*** | `styleradar.pagos.pago.rechazado` *(pendiente confirmar con `ms-pagos`)* | `styleradar.inventario.pago-rechazado.q` *(DLQ: `styleradar.inventario.dlq`)* | `ms-pagos` $\rightarrow$ `ms-inventario` | Consumidor `onPagoRechazado` implementado para liberar reservas, pero sin `Binding` activo en `RabbitMqConfig` hasta confirmación del emisor. |

> [!NOTE]
> **Idempotencia y tolerancia a fallos del broker:**
> * Los eventos consumidos verifican `eventoId` / `eventId` contra la tabla `eventos_procesados` en PostgreSQL para evitar doble confirmación o liberación ante reentregas.
> * En publicación, `RabbitMqStockEventPublisherAdapter` captura `AmqpException` registrando advertencia en logs para que una indisponibilidad temporal del broker no aborte la transacción local en base de datos.

<details>
<summary><b>Click para ver Esquema JSON del Evento Publicado y Consumido</b></summary>

#### 1. Evento Publicado: `styleradar.inventario.actualizado`
```json
{
  "eventoId": "9c2e8a74-1f3b-4c5d-8e9a-0b1c2d3e4f5a",
  "eventId": "9c2e8a74-1f3b-4c5d-8e9a-0b1c2d3e4f5a",
  "eventType": "styleradar.inventario.actualizado",
  "timestamp": "2026-11-24T16:50:00Z",
  "producer": "ms-inventario",
  "data": {
    "prendaId": "bb6c7f7f-d743-46f3-aa8d-b6d61ef1cfc4",
    "sedeId": "fc942335-ab30-350a-b6a5-577cf0ca2fb5",
    "talla": "M",
    "stockAnterior": 10,
    "stockDisponible": 3,
    "stockReservado": 0,
    "estado": "ACTUALIZADO"
  }
}
```

#### 2. Evento Publicado: `styleradar.inventario.stock.ultimas-unidades` y `styleradar.inventario.stock.agotado`
```json
{
  "eventoId": "a1b2c3d4-e5f6-4789-abcd-111111111111",
  "eventId": "a1b2c3d4-e5f6-4789-abcd-111111111111",
  "eventType": "styleradar.inventario.stock.agotado",
  "timestamp": "2026-11-24T16:50:00Z",
  "producer": "ms-inventario",
  "data": {
    "prendaId": "bb6c7f7f-d743-46f3-aa8d-b6d61ef1cfc4",
    "sedeId": "fc942335-ab30-350a-b6a5-577cf0ca2fb5",
    "talla": "XL",
    "stockRestante": 0,
    "estado": "AGOTADA"
  }
}
```
*(Para `styleradar.inventario.stock.ultimas-unidades`, el campo `estado` toma el valor `"ULTIMAS_UNIDADES"` y `stockRestante` contiene el saldo entre `1` y `3`.)*

#### 3. Evento Consumido: `styleradar.pagos.pago.confirmado` (soporta `data` plano o `data.items[]`)
```json
{
  "eventoId": "evt-pagos-items-1",
  "eventType": "styleradar.pagos.pago.confirmado",
  "data": {
    "sedeId": "fc942335-ab30-350a-b6a5-577cf0ca2fb5",
    "items": [
      {
        "prendaId": "bb6c7f7f-d743-46f3-aa8d-b6d61ef1cfc4",
        "talla": "M",
        "cantidad": 2
      }
    ]
  }
}
```

#### 4. Evento Consumido: `styleradar.pagos.suscripcion.suspendida`
```json
{
  "eventoId": "evt-pagos-susp-1",
  "eventType": "styleradar.pagos.suscripcion.suspendida",
  "data": {
    "sedeId": "fc942335-ab30-350a-b6a5-577cf0ca2fb5"
  }
}
```
*(El consumidor acepta indistintamente `sedeId` o `tiendaId`, y busca `eventoId` o `eventId` en raíz o dentro de `data`.)*

</details>

---

[⬅ Volver al README Principal](../../README.md)
