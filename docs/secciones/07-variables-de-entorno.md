[⬅ Volver al README Principal](../../README.md)

---

## 7. VARIABLES DE ENTORNO

Siguiendo el **Principio III de The Twelve-Factor App** (*Almacenar la configuración en el entorno*), `ms-playlist` no incluye credenciales ni URLs fijas de ambiente en el código fuente. Toda su parametrización reside en `src/main/resources/application.yml` y se inyecta mediante variables de entorno (`.env` local no versionado, GitHub Secrets/Variables y variables de contenedor en Azure Container Apps).

> [!IMPORTANT]
> **Diferencia crítica entre Host Local, Docker Compose y QA/PROD:**
> * En **Host Local** (Maven), `DB_HOST`, `RABBITMQ_HOST` e `ITEM_CATALOGO_URL` apuntan a `localhost`.
> * Dentro de **Docker Compose**, `localhost` refiere al propio contenedor; por ello deben usar el **nombre del servicio** en la red interna de Docker (`postgres`, `rabbitmq`, `http://ms-item-catalogo:8220`).
> * En **QA y PROD**, `ITEM_CATALOGO_URL` debe configurarse **sin `/api/v1` al final** (porque `ItemCatalogoFeignClient` ya antepone `/api/v1/garments/{id}`), `RABBITMQ_PORT` debe ser **`5671`** con **`RABBITMQ_SSL_ENABLED=true`**, y `DB_POOL_SIZE` debe mantenerse en **`3`** para proteger el servidor PostgreSQL compartido.

| Variable | Descripción | Valor Local (Maven) | Valor en Docker Compose | Requerido en QA / Prod |
| :--- | :--- | :--- | :--- | :---: |
| `SERVER_PORT` | Puerto HTTP donde escucha `ms-playlist` | `8260` | `8260` | Opcional (`8260` por defecto) |
| `ITEM_CATALOGO_URL` | URL base de `ms-item-catalogo` **sin `/api/v1`** | `http://localhost:8220` | `http://ms-item-catalogo:8220` | **Sí** (URL Azure Container App de catálogo) |
| `DB_HOST` | Host del servidor PostgreSQL | `localhost` | `postgres` *(`[PENDIENTE: confirmar nombre en compose]`)* | **Sí** |
| `DB_PORT` | Puerto de conexión PostgreSQL | `5432` | `5432` | Opcional (`5432` por defecto) |
| `DB_NAME` | Nombre de la base de datos aislada | `styleradar_playlist` | `styleradar_playlist` | **Sí** |
| `DB_USER` | Usuario de PostgreSQL | `postgres` | `postgres` | **Sí** |
| `DB_PASSWORD` | Contraseña de PostgreSQL | `<clave_local>` *(por defecto vacío)* | `<clave_docker>` | **Sí** |
| `DB_SSL_MODE` | Modo SSL de JDBC PostgreSQL (`prefer`, `require`, `disable`) | `prefer` | `prefer` | Recomendado `require` |
| `DB_POOL_SIZE` | Máximo de conexiones simultáneas de HikariCP | `3` | `3` | Recomendado `3` |
| `DB_POOL_MIN_IDLE` | Mínimo de conexiones inactivas en HikariCP | `1` | `1` | Recomendado `1` |
| `DB_INIT_FAIL_TIMEOUT` | Tiempo máximo (ms) para fallar si el pool inicial no conecta | `60000` | `60000` | Recomendado `60000` |
| `RABBITMQ_HOST` | Host del broker RabbitMQ | `localhost` | `rabbitmq` *(`[PENDIENTE: confirmar nombre en compose]`)* | **Sí** en QA (en PROD sin broker se degrada sin error) |
| `RABBITMQ_PORT` | Puerto AMQP (`5672` plano, `5671` TLS) | `5672` | `5672` | **Sí** en QA (`5671`) |
| `RABBITMQ_USER` | Usuario asignado al servicio en RabbitMQ | `guest` | `guest` | **Sí** en QA |
| `RABBITMQ_PASSWORD` | Contraseña de RabbitMQ | `<clave_rabbit>` *(por defecto vacío)* | `<clave_rabbit>` | **Sí** en QA |
| `RABBITMQ_VHOST` | Virtual host de RabbitMQ | `/` | `/` | Opcional (`/` por defecto) |
| `RABBITMQ_SSL_ENABLED` | Habilita TLS/AMQPS hacia RabbitMQ | `false` | `false` | **Sí** en QA (`true`) |
| `RABBITMQ_SSL_VERIFY_HOSTNAME` | Valida el hostname del certificado TLS de RabbitMQ | `true` | `true` | Recomendado `true` en QA |

<details>
<summary><b>Click para ver la Plantilla de Archivo .env.example Completa</b></summary>

```dotenv
# ==============================================================================
# Plantilla de Variables de Entorno · ms-playlist (RST-Playlist-Service)
# Copiar como .env para desarrollo local (nunca versionar secretos reales)
# ==============================================================================

# Servidor HTTP
SERVER_PORT=8260

# Integración síncrona con ms-item-catalogo (SIN /api/v1 al final)
ITEM_CATALOGO_URL=http://localhost:8220

# Persistencia PostgreSQL 16 (Database-per-Service)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=styleradar_playlist
DB_USER=postgres
DB_PASSWORD=changeme_local_db_password
DB_SSL_MODE=prefer
DB_POOL_SIZE=3
DB_POOL_MIN_IDLE=1
DB_INIT_FAIL_TIMEOUT=60000

# Mensajería RabbitMQ 3.13.7 (En QA/PROD con TLS usar puerto 5671 y SSL=true)
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=changeme_local_rabbit_password
RABBITMQ_VHOST=/
RABBITMQ_SSL_ENABLED=false
RABBITMQ_SSL_VERIFY_HOSTNAME=true
```

</details>

---

[⬅ Volver al README Principal](../../README.md)
