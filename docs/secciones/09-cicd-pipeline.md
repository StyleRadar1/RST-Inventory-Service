[⬅ Volver al README Principal](../../README.md)

---

## 9. CI/CD CON EL BADGE DEL PIPELINE

El repositorio ejecuta el flujo automatizado **`CI - Integración Continua y Calidad`** definido en `.github/workflows/ci.yml`. Cada *Pull Request* hacia `master`, `main` o `develop`, así como cada *push* sobre ramas `feature/**`, `bugfix/**`, `hotfix/**` o `ci/**` (además de ejecución manual con `workflow_dispatch`), dispara compilación en Java 21, pruebas unitarias e integración, validación estricta de JaCoCo (`≥ 85 %`), análisis estático con SpotBugs (`4.8.6.4`), reporte opcional de SonarQube y construcción de prueba de la imagen Docker.

### Badges de Pipelines y Calidad (GitHub Actions & Calidad)

<div align="center">
  <a href="https://github.com/StyleRadar1/RST-Inventory-Service/actions/workflows/ci.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/StyleRadar1/RST-Inventory-Service/ci.yml?branch=master&label=CI_MASTER&style=for-the-badge" alt="CI Master" />
  </a>
  <a href="https://github.com/StyleRadar1/RST-Inventory-Service/actions/workflows/ci.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/StyleRadar1/RST-Inventory-Service/ci.yml?branch=develop&label=CI_DEVELOP&style=for-the-badge" alt="CI Develop" />
  </a>
  <a href="./08-pruebas-y-cobertura.md">
    <img src="https://img.shields.io/badge/JaCoCo_Coverage-%E2%89%A5_85%25-16A34A?style=for-the-badge&logo=codecov&logoColor=white" alt="JaCoCo Coverage" />
  </a>
  <img src="https://img.shields.io/badge/SpotBugs-0_Bugs_(4.8.6.4)-16A34A?style=for-the-badge" alt="SpotBugs Clean" />
  <img src="https://img.shields.io/badge/SonarQube-Opcional_con_SONAR__TOKEN-5600EF?style=for-the-badge&logo=sonarqube&logoColor=white" alt="SonarQube" />
</div>

### Matriz de Pipelines por Rama Protegida

| Pipeline | Rama / Entorno | Trigger de Ejecución | Quality Gate Requerido | Estado |
| :--- | :--- | :--- | :--- | :---: |
| **`test-and-quality` + `docker-dry-run`** | `develop` / `QA` | `pull_request` hacia `develop` | `clean verify` (Tests + JaCoCo `≥ 85%` + SpotBugs) + Buildx `Dockerfile` | ![CI Develop](https://img.shields.io/github/actions/workflow/status/StyleRadar1/RST-Inventory-Service/ci.yml?branch=develop&style=flat-square&label=develop) |
| **`test-and-quality` + `docker-dry-run`** | `master` / `main` / `PROD` | `pull_request` hacia `master` o `main` | `clean verify` (Tests + JaCoCo `≥ 85%` + SpotBugs) + Buildx `Dockerfile` | ![CI Master](https://img.shields.io/github/actions/workflow/status/StyleRadar1/RST-Inventory-Service/ci.yml?branch=master&style=flat-square&label=master) |
| **`test-and-quality` + `docker-dry-run`** | `feature/**`, `bugfix/**`, `hotfix/**`, `ci/**` | `push` sobre la rama de trabajo / `workflow_dispatch` | `clean verify` (Tests + JaCoCo `≥ 85%` + SpotBugs) + Buildx `Dockerfile` | ![CI Feature](https://img.shields.io/badge/PR_Gate-Activo-5600EF?style=flat-square) |

### Diagrama del Flujo Automatizado (Pipeline Stages)

```mermaid
flowchart LR
    A["Push / PR<br>(GitHub Actions)"] --> B["test-and-quality<br>(JDK 21 Temurin + H2 test)"]
    B --> C["./mvnw -B -ntp clean verify<br>(Tests + JaCoCo ≥ 85% + SpotBugs)"]
    C --> D["Surefire PR Comment +<br>SonarQube (continue-on-error)<br>+ Artifact JaCoCo"]
    D --> E["docker-dry-run<br>(Docker Buildx push=false)"]

    classDef step fill:#12063A,stroke:#5600EF,stroke-width:2px,color:#FFFFFF;
    classDef pass fill:#DCFCE7,stroke:#16A34A,stroke-width:2px,color:#0C0C12;

    class A,B,E step;
    class C,D pass;
```

### Estructura de Jobs en `.github/workflows/ci.yml`

1. **Job 1 · `test-and-quality` (`Build, Tests & Quality Gate` sobre `ubuntu-latest`):**
   * Ejecuta con perfil `SPRING_PROFILES_ACTIVE=test` y base H2 en memoria (`jdbc:h2:mem:testdb`).
   * Realiza checkout con historial completo (`fetch-depth: 0`), instala JDK 21 Temurin con caché Maven y otorga permisos a `mvnw`.
   * Corre `./mvnw -B -ntp clean verify`, ejecutando las 17 suites de pruebas, `jacoco:check` (`≥ 85%`) y `spotbugs:check` (`Max`/`High`).
   * Publica comentarios automáticos de pruebas con `EnricoMi/publish-unit-test-result-action@v2`, corre `sonar:sonar` con `SONAR_TOKEN` (en modo `continue-on-error: true` para no fallar si no hay token configurado) y sube el reporte HTML de JaCoCo (`jacoco-report-${{ github.sha }}`) con retención de 7 días.
2. **Job 2 · `docker-dry-run` (`Validar compilación de Dockerfile`, `needs: test-and-quality`):**
   * Configura Docker Buildx (`docker/setup-buildx-action@v3`) y valida que el `Dockerfile` construye limpiamente (`push: false`, etiqueta `test-image:test`, caché GHA `mode=max`) sin publicar imagen desde el flujo CI de PR.

<div align="center">
  <img src="../images/pipeline-ci-cd.png" alt="Evidencia 04 - Ejecución del pipeline CI/CD en verde en GitHub Actions" width="100%" style="border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);" />
  <p><sub><b>Figura 9.1:</b> Pipeline <code>CI - Integración Continua y Calidad</code> ejecutado exitosamente en GitHub Actions con <code>test-and-quality</code> y <code>docker-dry-run</code> en verde. <i>[PENDIENTE: capturar y guardar <code>docs/images/pipeline-ci-cd.png</code>]</i></sub></p>
</div>

---

[⬅ Volver al README Principal](../../README.md)
