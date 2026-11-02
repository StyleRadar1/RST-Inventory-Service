# RST-Inventory-Service

[![CI](https://github.com/StyleRadar1/RST-Inventory-Service/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/StyleRadar1/RST-Inventory-Service/actions/workflows/ci.yml)

## 1. Servicio y bounded context

**ms-inventario** · Microservicio de inventario por talla de StyleRadar.

Bounded context **Inventario**: Inventario por talla de las prendas publicadas por los almacenes aliados.

Paquete 2 Catálogo · puerto `8084` · base de datos `styleradar_inventory`.

## 2. Equipo y responsables

Equipo **Restrepo** (prefijo `RST`).

- Daniel Mosquera (Backend)
- Carlos Sánchez (Frontend)
- Juan Nicolás Álvarez Muñoz (Líder y apoyo)

## 3. Arquitectura hexagonal

El servicio sigue la estructura hexagonal votada por el curso. Las dependencias apuntan hacia el dominio: el dominio no conoce Spring, JPA ni HTTP; los casos de uso y servicios se definen como interfaz con su implementación `<Nombre>Impl`, y los beans se registran en `infrastructure/config/BeanConfig`.

```mermaid
flowchart LR
    C[Cliente / API Gateway] --> R[adapter/in/rest]
    B[RabbitMQ] --> A[adapter/in/amqp]
    R --> PI[domain/port/in]
    A --> PI
    PI --> UC[application/usecase]
    UC --> D[domain/model]
    UC --> PO[domain/port/out]
    PO --> PE[adapter/out/persistence]
    PO --> ME[adapter/out/messaging]
    PE --> DB[(PostgreSQL)]
    ME --> B
```

```text
src/main/java/edu/dosw/styleradar/inventario/
├── domain/            model, event, exception, port/in, port/out
├── application/       usecase, service/impl, dto/request|response|event, mapper/in|out
└── infrastructure/    adapter/in/rest|amqp, adapter/out/persistence|messaging|security|external, config, exception
src/test/java/edu/dosw/styleradar/inventario/   espejo de main
```

## 4. Endpoints y Swagger

Base: `http://localhost:8084/api/v1`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/health` | Estado del servicio |

Swagger UI: `http://localhost:8084/api/v1/swagger-ui.html` · OpenAPI: `http://localhost:8084/api/v1/v3/api-docs`

Requerimientos que cubrirá el servicio:

| Requerimiento | Funcionalidad |
|---|---|
| SR-RF-52 | Registrar tallas disponibles de una prenda |
| SR-RF-56 | Actualizar disponibilidad por talla |
| SR-RF-57 | Ocultar prenda agotada de los resultados disponibles |

## 5. Eventos que publica y consume

**Publica**

| Evento | Consumidores | Descripción |
|---|---|---|
| `StockChangedEvent` | ms-playlist, ms-notificaciones | Cambio en las unidades disponibles de una talla. |
| `PrendaAgotadaEvent` | ms-item-catalogo, ms-playlist | Todas las tallas de una prenda quedaron sin unidades. |

**Consume**

No aplica por ahora.

## 6. Tecnologías y dependencias

| Tecnología | Versión |
|---|---|
| Java (Eclipse Temurin) | 21 |
| Spring Boot | 3.3.4 |
| PostgreSQL | 15.8 |
| Flyway | gestionada por Spring Boot |
| RabbitMQ | 3.13.7 |
| MapStruct | 1.5.5.Final |
| springdoc-openapi | 2.6.0 |
| JUnit 5, Mockito y Testcontainers | gestionadas por Spring Boot |
| JaCoCo | 0.8.12 |
| SpotBugs | 4.8.6.4 |

## 7. Cómo ejecutar

Antes de ejecutar, copia `.env.example` a `.env` y completa `DB_PASSWORD` y `RABBITMQ_PASSWORD`.

**Local** (requiere PostgreSQL y RabbitMQ en ejecución):

```bash
cp .env.example .env
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Docker** (levanta el servicio, PostgreSQL y RabbitMQ con un solo comando):

```bash
cp .env.example .env
docker compose up -d --build
```

## 8. Variables de entorno

Los valores se definen en `.env` a partir de `.env.example`. El archivo `.env` no se versiona.

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `SERVER_PORT` | Puerto HTTP | `8084` |
| `DB_HOST` | Host de PostgreSQL | `localhost` |
| `DB_PORT` | Puerto de PostgreSQL | `5432` |
| `DB_NAME` | Base de datos | `styleradar_inventory` |
| `DB_USER` | Usuario de PostgreSQL | `postgres` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | sin valor |
| `RABBITMQ_HOST` | Host de RabbitMQ | `localhost` |
| `RABBITMQ_PORT` | Puerto de RabbitMQ | `5672` |
| `RABBITMQ_USER` | Usuario de RabbitMQ | `guest` |
| `RABBITMQ_PASSWORD` | Contraseña de RabbitMQ | sin valor |

## 9. Pruebas y cobertura

```bash
./mvnw verify
```

- Pruebas unitarias de dominio y casos de uso con JUnit 5 y Mockito en `src/test/java`, como espejo de `src/main/java`.
- Pruebas de integración de persistencia y mensajería con Testcontainers.
- Cobertura mínima del 85 % de líneas verificada por JaCoCo. Reporte en `target/site/jacoco/index.html`.

## 10. CI/CD

[![CI](https://github.com/StyleRadar1/RST-Inventory-Service/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/StyleRadar1/RST-Inventory-Service/actions/workflows/ci.yml)

El flujo `.github/workflows/ci.yml` se ejecuta en cada push a `master`, `develop` y `feature/**`, y en cada pull request hacia `master` y `develop`:

1. Compilación.
2. Pruebas, cobertura con JaCoCo y análisis estático con SpotBugs (`mvn verify`).
3. Construcción de la imagen Docker.

## 11. Ramas y cómo contribuir

- `master`: versiones estables. Solo recibe cambios por pull request desde `develop`.
- `develop`: integración. Solo recibe cambios por pull request desde ramas `feature/*`.
- `feature/HU-xx-descripcion`: una rama por historia de usuario, creada desde `develop`.

No se hacen commits directos a `master` ni a `develop`. Cada pull request necesita revisión y el pipeline en verde.

## 12. Épica en Jira

[RES-1 · ms-inventario](https://mail-team-nyjtgqcj.atlassian.net/browse/RES-1)
