<!-- ============================================================================
  STYLERADAR · MICROSERVICIO DE INVENTARIO POR TALLA (ms-inventario)
  Proyecto      : StyleRadar · Plataforma de moda local geolocalizada para Bogotá
  Asignatura    : DOSW 2026 · Arquitectura de Software
  Año           : 2026
  Servicio/Repo : ms-inventario · StyleRadar1/RST-Inventory-Service
  Squad         : Equipo Restrepo (RST)
============================================================================= -->

<!-- ============================================================================
  SISTEMA DE DISEÑO Y TIPOGRAFÍA OFICIAL
  Fuente primaria  : Poppins (900, 800, 700, 600, 500, 400, 300)
  Fuente monoesp.  : JetBrains Mono
  Paleta oficial   : #5600EF (Primary Violet), #12063A (Deep Indigo), #16A34A (Success Green)
============================================================================= -->

<!-- ============================================================================
  NOTA DE PORTABILIDAD DE RECURSOS
  El logotipo oficial y los badges se cargan mediante rutas HTTPS/CDN portables
  para asegurar su renderizado consistente en GitHub, Azure DevOps y lectores Markdown.
============================================================================= -->

<div align="center">

  <a href="https://github.com/StyleRadar1/RST-Inventory-Service">
    <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/logo/styleradar-logo.svg" width="340" alt="StyleRadar Logo Oficial" />
  </a>

  <h1>STYLE<font color="#5600EF">RADAR</font> &nbsp;·&nbsp; <code>ms-inventario</code></h1>

  <p align="center">
    <img src="https://img.shields.io/badge/BOUNDED_CONTEXT-Inventario_por_talla-12063A?style=for-the-badge" alt="Bounded Context" />
    <img src="https://img.shields.io/badge/PUERTO_OFICIAL-8200-5600EF?style=for-the-badge" alt="Puerto Oficial 8200" />
    <img src="https://img.shields.io/badge/ARQUITECTURA-HEXAGONAL-16A34A?style=for-the-badge" alt="Arquitectura Hexagonal" />
    <img src="https://img.shields.io/badge/SQUAD-Restrepo_(RST)-12063A?style=for-the-badge" alt="Squad Restrepo" />
  </p>

  <p align="center">
    <strong><font color="#5600EF">Descubre tu estilo. En tu ciudad.</font></strong><br />
    <em>Microservicio de inventario multi-sede por talla, cálculo de disponibilidad en tiempo real y reservas transaccionales de StyleRadar.</em>
  </p>

  <p align="center">
    <a href="docs/secciones/03-endpoints-y-enlace-a-swagger.md">
      <img src="https://img.shields.io/badge/DEV_LOCAL-localhost:8200-12063A?style=for-the-badge" alt="DEV Local" />
    </a>
    <a href="docs/secciones/03-endpoints-y-enlace-a-swagger.md">
      <img src="https://img.shields.io/badge/QA_CLOUD-Azure_Container_Apps-5600EF?style=for-the-badge" alt="QA Cloud" />
    </a>
    <a href="docs/secciones/03-endpoints-y-enlace-a-swagger.md">
      <img src="https://img.shields.io/badge/PROD_CLOUD-Azure_Container_Apps-16A34A?style=for-the-badge" alt="PROD Cloud" />
    </a>
    <a href="https://github.com/StyleRadar1/RST-Inventory-Service/actions/workflows/ci.yml">
      <img src="https://img.shields.io/github/actions/workflow/status/StyleRadar1/RST-Inventory-Service/ci.yml?branch=master&label=CI_MASTER&style=for-the-badge" alt="CI Master" />
    </a>
    <a href="https://github.com/StyleRadar1/RST-Inventory-Service/actions/workflows/ci.yml">
      <img src="https://img.shields.io/github/actions/workflow/status/StyleRadar1/RST-Inventory-Service/ci.yml?branch=develop&label=CI_DEVELOP&style=for-the-badge" alt="CI Develop" />
    </a>
    <a href="docs/secciones/08-pruebas-y-cobertura.md">
      <img src="https://img.shields.io/badge/JaCoCo_Coverage-%E2%89%A5_85%25-16A34A?style=for-the-badge&logo=codecov&logoColor=white" alt="JaCoCo Coverage" />
    </a>
    <a href="docs/secciones/09-cicd-pipeline.md">
      <img src="https://img.shields.io/badge/SpotBugs-0_Bugs_(4.8.6.4)-16A34A?style=for-the-badge" alt="SpotBugs Clean" />
    </a>
  </p>

  <p align="center">
    <a href="docs/secciones/05-tecnologias-y-dependencias.md">
      <img src="https://img.shields.io/badge/Java-21_LTS-12063A?style=for-the-badge&logo=openjdk&logoColor=C4B5FD" alt="Java 21" />
    </a>
    <a href="docs/secciones/05-tecnologias-y-dependencias.md">
      <img src="https://img.shields.io/badge/Spring_Boot-3.3.4-12063A?style=for-the-badge&logo=spring&logoColor=C4B5FD" alt="Spring Boot 3.3.4" />
    </a>
    <a href="docs/secciones/05-tecnologias-y-dependencias.md">
      <img src="https://img.shields.io/badge/Spring_Cloud-2023.0.3_Feign-12063A?style=for-the-badge&logo=spring&logoColor=C4B5FD" alt="Spring Cloud 2023.0.3" />
    </a>
    <a href="docs/secciones/05-tecnologias-y-dependencias.md">
      <img src="https://img.shields.io/badge/PostgreSQL-16-12063A?style=for-the-badge&logo=postgresql&logoColor=C4B5FD" alt="PostgreSQL 16" />
    </a>
    <a href="docs/secciones/05-tecnologias-y-dependencias.md">
      <img src="https://img.shields.io/badge/RabbitMQ-3.13.7-12063A?style=for-the-badge&logo=rabbitmq&logoColor=C4B5FD" alt="RabbitMQ 3.13.7" />
    </a>
    <a href="docs/secciones/05-tecnologias-y-dependencias.md">
      <img src="https://img.shields.io/badge/MapStruct-1.5.5.Final-12063A?style=for-the-badge" alt="MapStruct" />
    </a>
    <a href="docs/secciones/05-tecnologias-y-dependencias.md">
      <img src="https://img.shields.io/badge/OpenAPI-springdoc_2.6.0-12063A?style=for-the-badge&logo=swagger&logoColor=C4B5FD" alt="Swagger OpenAPI" />
    </a>
    <a href="docs/secciones/06-como-ejecutar-local-y-docker.md">
      <img src="https://img.shields.io/badge/Docker-OCI_Container-12063A?style=for-the-badge&logo=docker&logoColor=C4B5FD" alt="Docker OCI" />
    </a>
  </p>

</div>

---

## <img src="https://img.shields.io/badge/ÍNDICE-5600EF?style=for-the-badge" height="20" align="absmiddle" /> ÍNDICE DE CONTENIDOS

1. [Nombre del servicio y su bounded context](docs/secciones/01-nombre-del-servicio-y-bounded-context.md)
2. [Arquitectura hexagonal con sus diagramas](docs/secciones/02-arquitectura-hexagonal-y-diagramas.md)
3. [Endpoints y enlace a Swagger](docs/secciones/03-endpoints-y-enlace-a-swagger.md)
4. [Eventos que publica y consume](docs/secciones/04-eventos-que-publica-y-consume.md)
5. [Tecnologías y dependencias](docs/secciones/05-tecnologias-y-dependencias.md)
6. [Cómo ejecutar local y con Docker](docs/secciones/06-como-ejecutar-local-y-docker.md)
7. [Variables de entorno](docs/secciones/07-variables-de-entorno.md)
8. [Pruebas y cobertura (≥ 85%)](docs/secciones/08-pruebas-y-cobertura.md)
9. [CI/CD con el badge del pipeline](docs/secciones/09-cicd-pipeline.md)
10. [Ramas y cómo contribuir](docs/secciones/10-ramas-y-como-contribuir.md)
11. [Seguridad, Habeas Data y análisis OWASP Top 10](docs/secciones/11-seguridad-y-owasp.md)
12. [Equipo, responsables y enlace a la épica en Jira](docs/secciones/12-equipo-responsables-y-epica-jira.md)
