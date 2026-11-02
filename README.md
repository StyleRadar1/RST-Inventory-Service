# RST-Inventory-Service

[![CI](https://github.com/StyleRadar1/RST-Inventory-Service/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/StyleRadar1/RST-Inventory-Service/actions/workflows/ci.yml)

## 1. Servicio y bounded context

**ms-inventario**: microservicio de inventario por talla de StyleRadar.

Su responsabilidad dentro de la plataforma es: inventario por talla de las prendas publicadas por los almacenes aliados. Forma parte del Paquete 2 Catálogo y atiende en el puerto `8084`.

## 2. Equipo y responsables

Equipo Restrepo (prefijo `RST`).

- Daniel Mosquera (Backend)
- Carlos Sánchez (Frontend)
- Juan Nicolás Álvarez Muñoz (Líder y apoyo)

## 3. Arquitectura hexagonal

El servicio sigue la arquitectura hexagonal acordada por el curso. Las reglas del negocio se mantienen aisladas de las herramientas externas, como la base de datos, el broker de mensajería o la API REST, de modo que estas pueden cambiar sin afectar la lógica principal.

```mermaid
flowchart LR
    C[API Gateway] --> R[Adaptador REST]
    B[RabbitMQ] --> A[Adaptador de eventos]
    R --> PI[Puertos de entrada]
    A --> PI
    PI --> UC[Casos de uso]
    UC --> D[Dominio]
    UC --> PO[Puertos de salida]
    PO --> PE[Adaptador de persistencia]
    PO --> ME[Adaptador de mensajería]
    PE --> DB[(PostgreSQL)]
    ME --> B
```

## 4. Endpoints y Swagger

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/health` | Estado del servicio |

La documentación de la API está disponible en `/api/v1/swagger-ui.html`.

Funcionalidades que cubrirá el servicio:

| Requerimiento | Funcionalidad |
|---|---|
| SR-RF-52 | Registrar tallas disponibles de una prenda |
| SR-RF-56 | Actualizar disponibilidad por talla |
| SR-RF-57 | Ocultar prenda agotada de los resultados disponibles |

## 5. Eventos que publica y consume

**Publica**

| Evento | Consumidores | Propósito |
|---|---|---|
| `StockChangedEvent` | ms-playlist, ms-notificaciones | Cambio en las unidades disponibles de una talla. |
| `PrendaAgotadaEvent` | ms-item-catalogo, ms-playlist | Todas las tallas de una prenda quedaron sin unidades. |

**Consume**

Por ahora el servicio no tiene eventos de este tipo.

## 6. Tecnologías y dependencias

| Tecnología | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.4 |
| PostgreSQL | 15.8 |
| RabbitMQ | 3.13.7 |
| MapStruct | 1.5.5.Final |
| springdoc-openapi | 2.6.0 |
| JaCoCo | 0.8.12 |
| SpotBugs | 4.8.6.4 |

## 7. Cómo ejecutar

El servicio, junto con su base de datos y RabbitMQ, se levanta con Docker a partir de un archivo `.env` creado desde `.env.example`:

```bash
docker compose up -d --build
```

## 8. Variables de entorno

Las variables se definen en `.env`, que no se versiona. La plantilla `.env.example` indica cuáles se requieren.

| Variable | Descripción |
|---|---|
| `SERVER_PORT` | Puerto del servicio |
| `DB_HOST`, `DB_PORT`, `DB_NAME` | Conexión a PostgreSQL |
| `DB_USER`, `DB_PASSWORD` | Credenciales de PostgreSQL |
| `RABBITMQ_HOST`, `RABBITMQ_PORT` | Conexión a RabbitMQ |
| `RABBITMQ_USER`, `RABBITMQ_PASSWORD` | Credenciales de RabbitMQ |

## 9. Pruebas y cobertura

Las pruebas unitarias validan el dominio y los casos de uso, y las de integración validan la persistencia y la mensajería. Se exige una cobertura mínima del 85 %, verificada en cada ejecución del pipeline.

## 10. CI/CD

[![CI](https://github.com/StyleRadar1/RST-Inventory-Service/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/StyleRadar1/RST-Inventory-Service/actions/workflows/ci.yml)

Cada cambio enviado a `master`, `develop` o a una rama `feature/*`, así como cada pull request, pasa por un pipeline que compila el proyecto, ejecuta las pruebas, verifica la cobertura, realiza el análisis estático del código y construye la imagen Docker.

## 11. Ramas y cómo contribuir

- `master`: versiones estables.
- `develop`: integración del trabajo del equipo.
- `feature/HU-xx-descripcion`: una rama por historia de usuario, creada desde `develop`.

Los cambios llegan a `develop` y `master` únicamente por pull request, con revisión y el pipeline en verde.

## 12. Épica en Jira

[RES-1 · ms-inventario](https://mail-team-nyjtgqcj.atlassian.net/browse/RES-1)
