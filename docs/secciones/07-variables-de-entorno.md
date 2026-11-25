[⬅ Volver al README Principal](../../README.md)

---

## 7. VARIABLES DE ENTORNO

Siguiendo el **Principio III de Twelve-Factor App** (*Configuración en el entorno*), `application.yml` y `docker-compose.yml` externalizan toda la configuración mediante variables de entorno definidas localmente en `.env` (excluido de Git) a partir de `.env.example`.

> [!IMPORTANT]
> **Diferencia crítica entre Host Local, Docker Compose y Nube (QA/PROD):**
> * **Host Local (Maven):** `DB_HOST=localhost` y `RABBITMQ_HOST=localhost` con `RABBITMQ_SSL_ENABLED=false` y puerto `5672`.
> * **Docker Compose:** `docker-compose.yml` inyecta automáticamente `DB_HOST=postgres` y `RABBITMQ_HOST=rabbitmq` sobre `.env` para resolver por la red interna de contenedores; además `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `RABBITMQ_USER` y `RABBITMQ_PASSWORD` se usan tanto en la app como para inicializar `postgres` y `rabbitmq`.
> * **QA / PROD (Azure Container Apps):** El PostgreSQL compartido requiere `DB_POOL_SIZE=3` y `DB_POOL_MIN_IDLE=1`; RabbitMQ opera en **`5671` con `RABBITMQ_SSL_ENABLED=true`**; e **`ITEM_CATALOGO_URL`** debe apuntar al servicio real de catálogo **sin el sufijo `/api/v1`** (porque `ItemCatalogoFeignClient` ya solicita `/api/v1/garments/{id}`).

| Variable | Descripción | Valor Local (Maven) | Valor en Docker Compose | Requerido en QA / Prod |
| :--- | :--- | :--- | :--- | :---: |
| `SERVER_PORT` | Puerto HTTP del servicio (`application.yml`). | `8200` | `8200` | Sí (defecto `8200`) |
| `ITEM_CATALOGO_URL` | URL base de `ms-item-catalogo` para OpenFeign (sin `/api/v1`). | `http://localhost:8220` | `http://host.docker.internal:8220` | Sí |
| `DB_HOST` | Host del servidor PostgreSQL 16. | `localhost` | `postgres` *(forzado en compose)* | Sí |
| `DB_PORT` | Puerto TCP de PostgreSQL 16. | `5432` | `5432` | Sí (defecto `5432`) |
| `DB_NAME` | Nombre de la base de datos en PostgreSQL (`POSTGRES_DB`). | `inventario_db` | `inventario_db` | Sí (defecto `inventario_db`) |
| `DB_USER` | Usuario de PostgreSQL (`POSTGRES_USER`). | `styleradar_user` | `styleradar_user` | Sí (defecto `styleradar_user`) |
| `DB_PASSWORD` | Contraseña de PostgreSQL (`POSTGRES_PASSWORD`). | `<DB_PASSWORD_LOCAL>` | `<DB_PASSWORD_LOCAL>` | Sí *(secreto)* |
| `DB_SSL_MODE` | Parámetro JDBC `sslmode` para PostgreSQL. | `prefer` | `disable` / `prefer` | Sí (`require` o `prefer`) |
| `DB_POOL_SIZE` | Máximo de conexiones simultáneas del pool HikariCP. | `3` | `3` | Sí (`3`) |
| `DB_POOL_MIN_IDLE` | Mínimo de conexiones en espera en HikariCP. | `1` | `1` | Sí (`1`) |
| `DB_INIT_FAIL_TIMEOUT` | Tiempo máximo de espera (ms) al inicializar HikariCP. | `60000` | `60000` | Sí (`60000`) |
| `RABBITMQ_HOST` | Host del broker RabbitMQ 3.13.7. | `localhost` | `rabbitmq` *(forzado en compose)* | Sí |
| `RABBITMQ_PORT` | Puerto AMQP (`5672` plano / `5671` SSL). | `5672` | `5672` | Sí (`5671`) |
| `RABBITMQ_USER` | Usuario de conexión RabbitMQ (`RABBITMQ_DEFAULT_USER`). | `guest` | `guest` / `styleradar` | Sí (`ms_inventario`) |
| `RABBITMQ_PASSWORD` | Contraseña de conexión RabbitMQ (`RABBITMQ_DEFAULT_PASS`). | `guest` | `<RABBIT_PASSWORD>` | Sí *(secreto)* |
| `RABBITMQ_VHOST` | Virtual host del broker RabbitMQ. | `/` | `/` | Sí (`/`) |
| `RABBITMQ_SSL_ENABLED` | Habilita cifrado TLS en la conexión AMQP. | `false` | `false` | Sí (`true`) |
| `RABBITMQ_SSL_VERIFY_HOSTNAME` | Verifica el hostname del certificado TLS en RabbitMQ. | `true` | `true` | Sí (`true`) |

<details>
<summary><b>Click para ver la Plantilla de Archivo .env.example Completa</b></summary>

```dotenv
# ==============================================================================
# PLANTILLA DE VARIABLES DE ENTORNO - ms-inventario (StyleRadar)
# Copiar como .env para ejecución local o Docker Compose (nunca versionar .env)
# ==============================================================================

# --- Servidor HTTP ---
SERVER_PORT=8200

# --- Cliente Feign hacia ms-item-catalogo (sin /api/v1 al final) ---
ITEM_CATALOGO_URL=http://localhost:8220

# --- Base de Datos PostgreSQL 16 ---
DB_HOST=localhost
DB_PORT=5432
DB_NAME=inventario_db
DB_USER=styleradar_user
DB_PASSWORD=cambiar_password_local
DB_SSL_MODE=prefer

# --- Pool HikariCP (límite para PostgreSQL compartido en QA/PROD) ---
DB_POOL_SIZE=3
DB_POOL_MIN_IDLE=1
DB_INIT_FAIL_TIMEOUT=60000

# --- Broker RabbitMQ 3.13.7 ---
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_VHOST=/
RABBITMQ_SSL_ENABLED=false
RABBITMQ_SSL_VERIFY_HOSTNAME=true
```

</details>

---

[⬅ Volver al README Principal](../../README.md)
