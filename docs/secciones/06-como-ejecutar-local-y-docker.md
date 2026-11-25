[⬅ Volver al README Principal](../../README.md)

---

## 6. CÓMO EJECUTAR LOCAL Y CON DOCKER

### Prerrequisitos de Entorno

* **JDK:** Java `21` LTS instalado y configurado en `JAVA_HOME`.
* **Maven:** Apache Maven `3.9+` o el wrapper incluido `./mvnw` (`mvnw.cmd` en Windows).
* **Docker Engine y Docker Compose:** requeridos para ejecución contenerizada y para `PlaylistServiceApplicationIntegrationTest` (Testcontainers).
* **Dependencias en ejecución (solo para perfil `dev` local):** PostgreSQL `16` (`styleradar_playlist`), RabbitMQ `3.13.7` (opcional si no se requiere mensajería local gracias a la tolerancia a `AmqpException`) y `ms-item-catalogo` accesible en `ITEM_CATALOGO_URL` para operaciones de creación/asociación de prendas.

### Ejecución Local con Maven (Perfil Dev)

El perfil `dev` (`src/main/resources/application-dev.yml`) habilita `spring.jpa.show-sql: true` y nivel de log `DEBUG` para `edu.dosw.styleradar.playlist`.

```bash
# 1. Clonar el repositorio y entrar al directorio
git clone https://github.com/StyleRadar1/RST-Playlist-Service.git
cd RST-Playlist-Service

# 2. Crear archivo de variables locales a partir de la plantilla (ajustando credenciales locales)
cp .env.example .env

# 3. Exportar variables en la sesión (Linux / macOS / Git Bash)
set -a && source .env && set +a

# 4. Compilar y ejecutar en perfil dev en el puerto 8260
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

> [!NOTE]
> **Parámetros en host local y comandos equivalentes en Windows PowerShell:**
> En ejecución local fuera de contenedores, el servicio se conecta por defecto a `localhost:5432` (PostgreSQL `styleradar_playlist`), `localhost:5672` (RabbitMQ) y `http://localhost:8220` (`ms-item-catalogo`) `[PENDIENTE: verificar si docker-compose.yml mapea puertos distintos en host]`.
>
> En **Windows PowerShell**, puedes cargar variables y arrancar el servicio así:
> ```powershell
> Copy-Item .env.example .env
> Get-Content .env | ForEach-Object {
>   if ($_ -match '^([^#=]+)=(.*)$') { [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process') }
> }
> .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
> ```

### Ejecución Contenerizada Completa con Docker Compose

Para levantar `ms-playlist` junto con su infraestructura definida en el proyecto `[PENDIENTE: confirmar servicios y puertos exactos en docker-compose.yml]`:

<details>
<summary><b>Click para ver Comandos de Docker Compose y Matriz de Puertos Local vs. Docker</b></summary>

```bash
# Construir la imagen OCI y levantar servicios en segundo plano
docker compose up -d --build

# Verificar el estado de los contenedores
docker compose ps

# Seguir los logs en tiempo real de ms-playlist
docker compose logs -f

# Detener y eliminar contenedores y red (conservar o limpiar volúmenes según necesidad)
docker compose down
```

| Componente | Conexión en Host Local | Conexión dentro de Docker Compose |
| :--- | :--- | :--- |
| **`ms-playlist`** | `http://localhost:8260/api/v1` | `http://ms-playlist:8260/api/v1` *(puerto interno `8260`)* |
| **PostgreSQL 16 (`styleradar_playlist`)** | `localhost:5432` | `postgres:5432` *(`[PENDIENTE: confirmar nombre de servicio BD en docker-compose.yml]`)* |
| **RabbitMQ 3.13.7** | `localhost:5672` | `rabbitmq:5672` *(`[PENDIENTE: confirmar nombre de servicio broker en docker-compose.yml]`)* |
| **`ms-item-catalogo`** | `http://localhost:8220` | `http://ms-item-catalogo:8220` *(`[PENDIENTE: confirmar nombre de servicio en red Docker]`)* |

</details>

### Comprobación de Salud (Health Check)

```bash
curl -i http://localhost:8260/api/v1/health
```

**Salida esperada (`200 OK`):**

```json
{
  "status": "UP",
  "service": "ms-playlist",
  "port": 8260
}
```

*(También disponible el endpoint de Actuator: `GET http://localhost:8260/api/v1/actuator/health`).*

<div align="center">
  <img src="../images/DockerEvidence.png" alt="Evidencia 02 - Contenedor OCI Docker en Ejecución" width="100%" style="border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);" />
  <p><sub><b>Figura 6.1:</b> [PENDIENTE: capturar salida de docker compose ps / curl /api/v1/health y guardarla en docs/images/DockerEvidence.png]</sub></p>
</div>

---

[⬅ Volver al README Principal](../../README.md)
