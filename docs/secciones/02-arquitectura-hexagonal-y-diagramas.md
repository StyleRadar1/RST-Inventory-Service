[⬅ Volver al README Principal](../../README.md)

---

## 2. ARQUITECTURA HEXAGONAL Y DIAGRAMAS

El servicio implementa **Arquitectura Hexagonal (Puertos y Adaptadores)** combinada con principios de **Domain-Driven Design (DDD)**. El núcleo de dominio (`Playlist`, `PrendaSnapshot` y reglas invariantes) permanece completamente aislado de Spring Framework, JPA, OpenFeign, RabbitMQ y HTTP, permitiendo evolucionar adaptadores de infraestructura sin alterar la lógica de negocio.

### Diagramas del Microservicio

#### 2.1 Diagrama de Componentes General

<div align="center">
  <img src="../uml/DiagramaComponentesGeneral.png" alt="Diagrama de Componentes General de StyleRadar" width="100%" />
  <p><sub><b>Figura 2.1:</b> [PENDIENTE: agregar archivo docs/uml/DiagramaComponentesGeneral.png en el repositorio]</sub></p>
</div>

##### Justificación y Explicación Técnica del Diagrama de Componentes General

* **Puerta de entrada y enrutamiento:** Todas las peticiones externas ingresan por el **API Gateway**, que autentica el JWT y propaga hacia `ms-playlist` (`:8260/api/v1`) las cabeceras `X-User-Id`, `X-User-Role` y `X-User-Roles`.
* **Comunicación síncrona saliente:** Al crear una playlist o asociar una prenda, `ms-playlist` consulta vía REST/OpenFeign (`GET /api/v1/garments/{id}`) a `ms-item-catalogo` (`:8220`) para verificar existencia, nombre oficial y estado `APROBADA`.
* **Aislamiento de persistencia y caché:** `ms-playlist` persiste exclusivamente en su base dedicada `PostgreSQL 16` (`styleradar_playlist`), respetando *Database-per-Service*. No requiere servidor de caché externo.
* **Mensajería asíncrona:** A través de RabbitMQ (`styleradar.exchange`), `ms-playlist` consume eventos de retiro de prendas desde `ms-item-catalogo` y publica eventos de creación, asociación de prenda y destacado hacia consumidores interesados como `ms-estadisticas` (`:8420`).

#### 2.2 Diagrama de Componentes Específico

<div align="center">
  <img src="../uml/DiagramaComponentesPlaylist.png" alt="Diagrama de Componentes Específico de ms-playlist" width="100%" />
  <p><sub><b>Figura 2.2:</b> [PENDIENTE: agregar archivo docs/uml/DiagramaComponentesPlaylist.png en el repositorio]</sub></p>
</div>

##### Justificación y Explicación Técnica del Diagrama de Componentes Específico

* **Adaptadores de Entrada (*Driving Adapters*):** `PlaylistController` (documentado mediante `PlaylistApi`), `HealthController` (`HealthApi`), `GlobalExceptionHandler` (respuestas RFC 7807) y `CatalogoEventsConsumer` (listener AMQP sobre la cola `styleradar.playlist.prenda-retirada.q`).
* **Puertos de Entrada y Capa de Aplicación:** Cinco interfaces de caso de uso (`CrearPlaylistUseCase`, `ConsultarPlaylistUseCase`, `DestacarPlaylistUseCase`, `AsociarPrendaUseCase`, `InteractuarPlaylistUseCase`) con sus implementaciones `@Service` transaccionales (`*Impl`) y `PlaylistDtoMapper` (MapStruct).
* **Núcleo de Dominio:** Entidades e invariantes en `Playlist` y `PrendaSnapshot` con cero dependencias de infraestructura.
* **Puertos y Adaptadores de Salida (*Driven Adapters*):** `PlaylistRepositoryPort` implementado por `PostgreSQLPlaylistRepositoryAdapter` + `SpringDataPlaylistRepository` + `PlaylistEntityMapper`; `ItemCatalogoPort` implementado por `ItemCatalogoClient` + `ItemCatalogoFeignClient`; y `PlaylistEventPublisherPort` implementado por `RabbitMQEventPublisher`.

#### 2.3 Diagrama de Clases del Modelo de Dominio

<div align="center">
  <img src="../uml/DiagramaClasesPlaylist.png" alt="Diagrama de Clases del Modelo de Dominio de ms-playlist" width="100%" />
  <p><sub><b>Figura 2.3:</b> [PENDIENTE: agregar archivo docs/uml/DiagramaClasesPlaylist.png en el repositorio]</sub></p>
</div>

##### Justificación y Explicación Técnica del Modelo de Dominio

* **Agregado Raíz (`Playlist`):** Centraliza todas las invariantes de negocio: `MAX_PRENDAS = 15` (`RNF-12`), `MAX_DESTACADAS = 3` (`SR-RF-72`), nombre obligatorio (1–100 caracteres), descripción opcional (máx. 300 caracteres), creación obligatoria con mínimo una prenda (`crearConPrendas`), prevención de prendas duplicadas, validación de propietario (`validarPropietario`), control de privacidad en lectura (`validarAccesoLectura`) y alternancia idempotente de `likesUsuarios` y `guardadosUsuarios`.
* **Entidad / Snapshot Embebido (`PrendaSnapshot`):** Guarda el identificador inmutable `prendaId`, `nombrePrenda`, `fechaAsociacion` y el indicador `disponible` con transiciones explícitas (`marcarNoDisponible()`, `marcarDisponible()`). Incluye la anotación `@Default` para reconstrucción limpia mediante MapStruct.
* **Enumeraciones:** `VisibilidadPlaylist` (`PUBLICA`, `PRIVADA`) y `Estilo` (`CASUAL`, `FORMAL`, `DEPORTIVO`, `URBANO`).
* **Jerarquía de Excepciones de Dominio:** `DomainException` encapsula el enum estandarizado `CodigoError` y se especializa en `NoAutorizadoException`, `AccesoDenegadoPlaylistException`, `PlaylistPrivadaException`, `PlaylistSinPrendasException`, `DescripcionMuyLargaException`, `LimitePrendasAlcanzadoException`, `PrendaDuplicadaException`, `PrendaNoEncontradaEnPlaylistException`, `LimiteDestacadasSuperadoException`, `ResourceNotFoundException`, `PrendaNoEncontradaEnCatalogoException`, `PrendaNoAprobadaException` y `CatalogoNoDisponibleException`.

#### 2.4 Diagrama de Base de Datos

<div align="center">
  <img src="../uml/DiagramaEntidadRelacionPlaylist.png" alt="Diagrama Entidad Relación de ms-playlist" width="100%" />
  <p><sub><b>Figura 2.4:</b> [PENDIENTE: agregar archivo docs/uml/DiagramaEntidadRelacionPlaylist.png en el repositorio]</sub></p>
</div>

##### Justificación y Explicación Técnica del Modelo de Datos

* **Motor y control de esquema:** Se utiliza `PostgreSQL 16` con migraciones versionadas en `Flyway` (`classpath:db/migration`) y validación estricta de esquema por Hibernate (`ddl-auto: validate`, `open-in-view: false`).
* **Tablas reales y cardinalidades:**
  * `playlists`: clave primaria `id` (`UUID`), `usuario_id` (`VARCHAR(100)`, no nulo), `nombre` (`VARCHAR(100)`, no nulo), `descripcion` (`VARCHAR(300)`), `visibilidad` (`VARCHAR(20)`), `estilo` (`VARCHAR(30)`), `destacada` (`BOOLEAN`), `fecha_creacion` (`TIMESTAMP`, inmutable) y `fecha_actualizacion` (`TIMESTAMP`).
  * `playlist_prendas` (`1:N` vía `@ElementCollection` sobre `PrendaEmbeddable`): `playlist_id` (FK a `playlists`), `prenda_id` (`UUID`), `nombre_prenda` (`VARCHAR(150)`), `fecha_asociacion` (`TIMESTAMP`) y `disponible` (`BOOLEAN`).
  * `playlist_likes` (`1:N` vía `@ElementCollection`): `playlist_id` y `usuario_id` (`VARCHAR(100)`).
  * `playlist_guardados` (`1:N` vía `@ElementCollection`): `playlist_id` y `usuario_id` (`VARCHAR(100)`).
* **Privacidad y cumplimiento:** Siguiendo *Database-per-Service* y minimización de datos, `ms-playlist` únicamente almacena el identificador opaco `usuario_id` y la referencia `prenda_id` con su snapshot de nombre; no almacena correos, nombres reales, teléfonos ni contraseñas.

#### 2.5 Diagrama de Despliegue General

<div align="center">
  <img src="../uml/DiagramaDespliegueGeneral.png" alt="Diagrama de Despliegue General de StyleRadar" width="100%" />
  <p><sub><b>Figura 2.5:</b> [PENDIENTE: agregar archivo docs/uml/DiagramaDespliegueGeneral.png en el repositorio]</sub></p>
</div>

##### Justificación y Explicación Técnica del Despliegue

* **Capa Cliente e Ingress:** El tráfico entra por el API Gateway hacia los contenedores de **Azure Container Apps** en QA y PROD (con escalado a cero por inactividad, lo que introduce arranque en frío en la primera petición).
* **Topología específica de `ms-playlist`:** El contenedor escucha en el puerto `8260` (`/api/v1`), se comunica vía HTTPS por OpenFeign con el contenedor de `ms-item-catalogo` (`ITEM_CATALOGO_URL`), persiste en el servidor **PostgreSQL 16** compartido con un pool acotado de HikariCP (`maximum-pool-size: 3`, `minimum-idle: 1`) para evitar agotamiento de conexiones, y se conecta en QA a RabbitMQ por puerto TLS `5671` (en entornos como PROD sin RabbitMQ activo, `RabbitMQEventPublisher` captura `AmqpException` para no degradar la API REST).

#### 2.6 Diagrama de Secuencia de Flujos Críticos

<div align="center">
  <img src="../uml/DiagramaSecuenciaPlaylist.png" alt="Diagrama de Secuencia de Flujos Críticos de ms-playlist" width="100%" />
  <p><sub><b>Figura 2.6:</b> [PENDIENTE: agregar archivo docs/uml/DiagramaSecuenciaPlaylist.png en el repositorio]</sub></p>
</div>

##### Justificación y Explicación Técnica de los Flujos Críticos

1. **Flujo principal — Creación de playlist (`POST /api/v1/playlists`):** `PlaylistController` valida `X-User-Id` y el DTO `@Valid` (`CrearPlaylistRequest`). `CrearPlaylistUseCaseImpl` exige al menos 1 `prendaId` y máximo 15; por cada UUID consulta `ItemCatalogoClient` &rarr; `GET /api/v1/garments/{id}` en `ms-item-catalogo`. Si alguna no existe lanza `404 PRENDA_NO_ENCONTRADA`; si no está `APROBADA` lanza `422 PRENDA_NO_APROBADA`; si catálogo falla lanza `503 CATALOGO_NO_DISPONIBLE`. Con todas aprobadas, construye el agregado `Playlist`, guarda en PostgreSQL y publica `styleradar.playlist.creada` más `styleradar.playlist.prenda.asociada` por cada prenda, retornando `201 Created`.
2. **Flujo administrativo — Destacar playlist (`PUT /api/v1/playlists/{id}/featured`):** `PlaylistController` valida `X-User-Id` (`401` si falta) y verifica en `X-User-Role` / `X-User-Roles` que el usuario posea `STYLERADAR_ADMIN` o `ADMIN_STYLERADAR` (`403 ACCESO_DENEGADO` en otro caso). `DestacarPlaylistUseCaseImpl` consulta `countDestacadas()` en PostgreSQL; si ya existen 3 lanza `409 LIMITE_DESTACADAS`. De lo contrario marca `destacada = true`, guarda y publica `styleradar.playlist.destacada.actualizada`.
3. **Flujo transversal — Manejo centralizado de errores RFC 7807:** Toda excepción de validación (`MethodArgumentNotValidException`), de parámetros (`MethodArgumentTypeMismatchException`), de autorización o de dominio (`DomainException`) es interceptada por `GlobalExceptionHandler` (`@RestControllerAdvice`), devolviendo `application/problem+json` con `status`, `title`, `detail`, `code` (UPPER_SNAKE_CASE) y `timestamp` (ISO-8601).

### Estructura Oficial de Paquetes en `src/main/java/edu/dosw/styleradar/playlist/`

```text
src/main/java/edu/dosw/styleradar/playlist/
├── PlaylistServiceApplication.java
├── domain/
│   ├── model/
│   │   ├── Default.java
│   │   ├── Estilo.java
│   │   ├── Playlist.java
│   │   ├── PrendaSnapshot.java
│   │   └── VisibilidadPlaylist.java
│   ├── exception/
│   │   ├── AccesoDenegadoPlaylistException.java
│   │   ├── CatalogoNoDisponibleException.java
│   │   ├── CodigoError.java
│   │   ├── DescripcionMuyLargaException.java
│   │   ├── DomainException.java
│   │   ├── LimiteDestacadasSuperadoException.java
│   │   ├── LimitePrendasAlcanzadoException.java
│   │   ├── NoAutorizadoException.java
│   │   ├── PlaylistPrivadaException.java
│   │   ├── PlaylistSinPrendasException.java
│   │   ├── PrendaDuplicadaException.java
│   │   ├── PrendaNoAprobadaException.java
│   │   ├── PrendaNoEncontradaEnCatalogoException.java
│   │   ├── PrendaNoEncontradaEnPlaylistException.java
│   │   └── ResourceNotFoundException.java
│   └── port/
│       ├── in/
│       │   ├── AsociarPrendaUseCase.java
│       │   ├── ConsultarPlaylistUseCase.java
│       │   ├── CrearPlaylistUseCase.java
│       │   ├── DestacarPlaylistUseCase.java
│       │   └── InteractuarPlaylistUseCase.java
│       └── out/
│           ├── ItemCatalogoPort.java
│           ├── PlaylistEventPublisherPort.java
│           ├── PlaylistRepositoryPort.java
│           └── PrendaCatalogoInfo.java
├── application/
│   ├── dto/
│   │   ├── AgregarPrendaRequest.java
│   │   ├── CrearPlaylistRequest.java
│   │   ├── EditarPlaylistRequest.java
│   │   ├── PageResponse.java
│   │   ├── PlaylistResponse.java
│   │   ├── PrendaResponse.java
│   │   └── mapper/
│   │       └── PlaylistDtoMapper.java
│   └── usecase/
│       ├── AsociarPrendaUseCaseImpl.java
│       ├── ConsultarPlaylistUseCaseImpl.java
│       ├── CrearPlaylistUseCaseImpl.java
│       ├── DestacarPlaylistUseCaseImpl.java
│       └── InteractuarPlaylistUseCaseImpl.java
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   ├── amqp/
    │   │   │   └── CatalogoEventsConsumer.java
    │   │   └── rest/
    │   │       ├── HealthController.java
    │   │       ├── PlaylistController.java
    │   │       └── doc/
    │   │           ├── HealthApi.java
    │   │           └── PlaylistApi.java
    │   └── out/
    │       ├── external/
    │       │   ├── ItemCatalogoClient.java
    │       │   ├── ItemCatalogoFeignClient.java
    │       │   └── PrendaCatalogoDto.java
    │       ├── messaging/
    │       │   └── RabbitMQEventPublisher.java
    │       └── persistence/
    │           ├── PostgreSQLPlaylistRepositoryAdapter.java
    │           ├── SpringDataPlaylistRepository.java
    │           ├── entity/
    │           │   ├── PlaylistEntity.java
    │           │   └── PrendaEmbeddable.java
    │           └── mapper/
    │               └── PlaylistEntityMapper.java
    ├── config/
    │   ├── BeanConfig.java
    │   ├── FeignConfig.java
    │   ├── OpenApiConfig.java
    │   └── RabbitMqConfig.java
    └── exception/
        └── GlobalExceptionHandler.java
```

---

[⬅ Volver al README Principal](../../README.md)
