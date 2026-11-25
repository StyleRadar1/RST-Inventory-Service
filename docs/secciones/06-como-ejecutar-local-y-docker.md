[⬅ Volver al README Principal](../../README.md)

---

## 6. CÓMO EJECUTAR LOCAL Y CON DOCKER

### Prerrequisitos de Entorno

* **JDK:** Java `21` (LTS).
* **Apache Maven:** `3.9+` (o wrapper incluido `./mvnw` / `mvnw.cmd`).
* **Docker Engine y Docker Compose** activos en el equipo.
* **PostgreSQL 16** y **RabbitMQ 3.13.7** disponibles si se ejecuta directamente con Maven en el host, además del archivo `.env` configurado en la raíz del repositorio.

### Ejecución Local con Maven (Perfil Dev)

```bash
# 1. Clonar el repositorio oficial
git clone https://github.com/StyleRadar1/RST-Inventory-Service.git
cd RST-Inventory-Service

# 2. Crear el archivo .env (asegura DB_HOST=localhost, DB_NAME=inventario_db, DB_USER=styleradar_user y RABBITMQ_HOST=localhost)
cp .env.example .env

# 3. Verificar que PostgreSQL (puerto 5432) y RabbitMQ (puerto 5672) estén activos en localhost
# 4. Ejecutar el microservicio con perfil dev (muestra SQL y logs DEBUG del dominio/adaptadores)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

> [!NOTE]
> **Parámetros de conexión en host local y comandos equivalentes en Windows PowerShell:**
> * En ejecución directa en host (`application.yml`), el servicio escucha en **`localhost:8200`** y busca por defecto PostgreSQL en `localhost:5432/inventario_db` (usuario `styleradar_user`), RabbitMQ en `localhost:5672` (`guest`/`guest`, vhost `/`) y `ms-item-catalogo` en `http://localhost:8220`.
> * **Importante:** En el `docker-compose.yml` actual, los servicios `postgres` y `rabbitmq` no publican los puertos `5432` ni `5672` hacia el host (solo `8200` en `ms-inventario` y `15672` en `rabbitmq`). Si corres el `.jar` con Maven en el host en lugar de Docker Compose, asegúrate de tener PostgreSQL/RabbitMQ escuchando localmente en `5432`/`5672` o expón esos puertos en un override local.
> * En **Windows PowerShell**:
> ```powershell
> Copy-Item .env.example .env
> .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
> Invoke-RestMethod -Uri http://localhost:8200/api/v1/health
> ```

### Ejecución Contenerizada Completa con Docker Compose

El archivo `docker-compose.yml` levanta la pila completa (`ms-inventario`, `ms-inventario-db` y `ms-inventario-rabbitmq`) con *healthchecks* antes de iniciar el microservicio:

<details>
<summary><b>Click para ver Comandos de Docker Compose y Tabla de Puertos/Redes</b></summary>

```bash
# Construir la imagen de ms-inventario y levantar los 3 contenedores
docker compose up -d --build

# Verificar que los contenedores estén saludables (healthy / Up)
docker compose ps

# Consultar logs en vivo de ms-inventario
docker compose logs -f ms-inventario

# Detener y remover los contenedores (conservando el volumen db-data)
docker compose down
```

| Componente | Conexión en Host Local (Maven) | Conexión dentro de Docker Compose |
| :--- | :--- | :--- |
| **API `ms-inventario`** | `http://localhost:8200/api/v1` | `http://ms-inventario:8200/api/v1` *(publicado en host `${SERVER_PORT:-8200}:8200`)* |
| **PostgreSQL 16 (`ms-inventario-db`)** | `jdbc:postgresql://localhost:5432/inventario_db` | `jdbc:postgresql://postgres:5432/${DB_NAME}` *(red interna Compose; `DB_HOST=postgres`)* |
| **RabbitMQ 3.13.7 (`ms-inventario-rabbitmq`)** | `localhost:5672` / UI `http://localhost:15672` | `rabbitmq:5672` *(red interna Compose; `RABBITMQ_HOST=rabbitmq`; UI `15672:15672`)* |
| **Cliente `ms-item-catalogo`** | `http://localhost:8220` | `http://host.docker.internal:8220` *(o URL del servicio en red/nube)* |

</details>

### Comprobación de Salud (Health Check)

```bash
curl -i http://localhost:8200/api/v1/health
```

**Respuesta esperada (`200 OK`):**
```json
{
  "status": "UP",
  "service": "ms-inventario",
  "port": 8200
}
```
*(También están expuestos `/actuator/health` y `/actuator/info` vía Spring Boot Actuator).*

<div align="center">
  <img src="../images/DockerEvidence.png" alt="Evidencia 02 - Contenedor OCI Docker de ms-inventario en ejecución" width="100%" style="border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);" />
  <p><sub><b>Figura 6.1:</b> Contenedor OCI Docker de <code>ms-inventario</code> junto a PostgreSQL y RabbitMQ en ejecución (<code>docker compose ps</code>). <i>[PENDIENTE: capturar y guardar <code>docs/images/DockerEvidence.png</code>]</i></sub></p>
</div>

---

[⬅ Volver al README Principal](../../README.md)
