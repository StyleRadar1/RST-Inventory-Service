[⬅ Volver al README Principal](../../README.md)

---

## 9. CI/CD Y PIPELINE AUTOMATIZADO

Los flujos de Integración Continua y Despliegue Continuo (CI/CD) de `ms-playlist` operan sobre **GitHub Actions**, ejecutando compilación, pruebas automatizadas, verificación de cobertura JaCoCo, análisis estático con SpotBugs y construcción/despliegue de imagen Docker OCI hacia **Azure Container Apps** (`[PENDIENTE: confirmar nombres exactos de los archivos en .github/workflows/]`).

### Badges de Pipelines y Calidad (GitHub Actions & SonarCloud)

<div align="center">
  <p>
    <a href="https://github.com/StyleRadar1/RST-Playlist-Service/actions">
      <img src="https://img.shields.io/badge/CI_MASTER-GitHub_Actions-12063A?style=for-the-badge&logo=githubactions&logoColor=white" alt="CI Master" />
    </a>
    <a href="https://github.com/StyleRadar1/RST-Playlist-Service/actions">
      <img src="https://img.shields.io/badge/CI_DEVELOP-GitHub_Actions-5600EF?style=for-the-badge&logo=githubactions&logoColor=white" alt="CI Develop" />
    </a>
    <a href="../secciones/08-pruebas-y-cobertura.md">
      <img src="https://img.shields.io/badge/JaCoCo-%E2%89%A5_85%25-16A34A?style=for-the-badge&logo=codecov&logoColor=white" alt="JaCoCo 85%" />
    </a>
    <a href="https://github.com/StyleRadar1/RST-Playlist-Service/actions">
      <img src="https://img.shields.io/badge/SonarCloud-%5BPENDIENTE_BADGE%5D-12063A?style=for-the-badge&logo=sonarqubecloud&logoColor=white" alt="SonarCloud" />
    </a>
  </p>
</div>

*(Nota: actualizar los enlaces de los badges con el archivo exacto del workflow en `.github/workflows/` y la clave de proyecto de SonarCloud una vez confirmados `[PENDIENTE: enlaces exactos de workflow y SonarCloud]`).*

### Matriz de Pipelines por Rama Protegida

| Pipeline | Rama / Entorno | Trigger de Ejecución | Quality Gate Requerido | Estado |
| :--- | :--- | :--- | :--- | :---: |
| **CI Pull Request (`[PENDIENTE: nombre workflow]`)** | `feature/*`, `fix/*`, `refactor/*`, `docs/*` &rarr; `develop` | `pull_request` hacia `develop` / `master` | Compilación JDK 21, `./mvnw clean verify`, JaCoCo `≥ 85%`, SpotBugs sin errores | ![PR Gate](https://img.shields.io/badge/Gate-Obligatorio-5600EF?style=flat-square) |
| **CI/CD QA (`[PENDIENTE: nombre workflow]`)** | `develop` &rarr; **QA** (Azure Container Apps) | `push` / merge en `develop` | Pruebas + cobertura `≥ 85%` + SpotBugs + build imagen OCI + despliegue QA | ![QA](https://img.shields.io/badge/QA-Desplegado-16A34A?style=flat-square) |
| **CI/CD Producción (`[PENDIENTE: nombre workflow]`)** | `master` &rarr; **PROD** (Azure Container Apps) | `push` / merge en `master` (con CMV aprobada) | Pruebas + cobertura `≥ 85%` + SpotBugs + build imagen OCI + despliegue PROD | ![PROD](https://img.shields.io/badge/PROD-Desplegado-16A34A?style=flat-square) |

### Diagrama del Flujo Automatizado (Pipeline Stages)

```mermaid
flowchart LR
    A["Push / Pull Request<br/>(GitHub)"] --> B["Compilar<br/>(Maven · Java 21 LTS)"]
    B --> C["Pruebas y JaCoCo<br/>(Cobertura ≥ 85%)"]
    C --> D["Análisis Estático<br/>(SpotBugs / Calidad)"]
    D --> E["Imagen Docker OCI"]
    E --> F["Despliegue Azure<br/>(QA en develop / PROD en master)"]

    classDef step fill:#12063A,stroke:#5600EF,stroke-width:2px,color:#FFFFFF;
    classDef pass fill:#DCFCE7,stroke:#16A34A,stroke-width:2px,color:#0C0C12;

    class A,B,E,F step;
    class C,D pass;
```

### Estructura de Jobs en `.github/workflows/`

`[PENDIENTE: confirmar los nombres literales de los jobs en .github/workflows/*.yml]`

1. **Build & Test (`./mvnw clean verify`):** Configura JDK 21, restaura caché de Maven y ejecuta pruebas unitarias y `PlaylistServiceApplicationIntegrationTest` (con Testcontainers PostgreSQL 16).
2. **Quality & Coverage Gate (JaCoCo + SpotBugs):** Verifica que la cobertura alcance como mínimo el **85 %** y valida el análisis estático de código; cualquier incumplimiento detiene el pipeline antes de empaquetar.
3. **Container Build & Push (Docker OCI):** Construye la imagen del servicio y la publica en el registro de contenedores del proyecto `[PENDIENTE: confirmar registry exacto en workflow, p. ej. GHCR / ACR]`.
4. **Deploy to Azure Container Apps:** Actualiza el contenedor del entorno correspondiente (`develop` &rarr; QA, `master` &rarr; PROD) inyectando las variables de entorno y secretos configurados (`DB_*`, `RABBITMQ_*`, `ITEM_CATALOGO_URL`).

<div align="center">
  <img src="../images/pipeline-ci-cd.png" alt="Evidencia 04 - Ejecución del Pipeline CI/CD en Verde" width="100%" style="border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);" />
  <p><sub><b>Figura 9.1:</b> [PENDIENTE: capturar ejecución en verde del workflow en GitHub Actions y guardarla en docs/images/pipeline-ci-cd.png]</sub></p>
</div>

---

[⬅ Volver al README Principal](../../README.md)
