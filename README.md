[![CI](https://github.com/apchavez/quarkus-react-fullstack-k8s/actions/workflows/ci.yml/badge.svg)](https://github.com/apchavez/quarkus-react-fullstack-k8s/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=apchavez_quarkus-react-fullstack-k8s&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=apchavez_quarkus-react-fullstack-k8s)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=apchavez_quarkus-react-fullstack-k8s&metric=coverage)](https://sonarcloud.io/summary/new_code?id=apchavez_quarkus-react-fullstack-k8s)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=apchavez_quarkus-react-fullstack-k8s&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=apchavez_quarkus-react-fullstack-k8s)

# Product Management Platform

Fullstack application for product administration built as a portfolio project to demonstrate end-to-end development: Java 21 REST API with hexagonal architecture, React frontend, and a complete Kubernetes deployment.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21 · Quarkus 3 · MongoDB · Redis · MapStruct · Lombok |
| Frontend | React 18 · TypeScript · Vite · Material UI |
| Infrastructure | Docker · Kubernetes · GitHub Actions |

---

## Architecture

```mermaid
flowchart LR
    Browser([Browser]) --> React[React + Vite\nFrontend]
    React -->|REST| Nginx[nginx\nReverse Proxy]
    Nginx --> Quarkus[Quarkus REST\nBackend]
    Quarkus -->|cache-aside| Redis[(Redis\nCache)]
    Quarkus --> Mongo[(MongoDB\nPersistence)]
```

The backend follows **Hexagonal Architecture (Ports & Adapters)**:

- **Domain layer** — Product entity and port contracts (repository interfaces)
- **Application layer** — Use cases for CRUD operations
- **Infrastructure layer** — MongoDB adapter, Redis cache adapter, REST controller

The frontend is a single-page application built with React + Vite, communicating with the backend through a REST API.

Both services are independently containerized and orchestrated via Kubernetes or Docker Compose.

---

## Repository Structure

```text
product-management/
├── api/         Java + Quarkus backend
│   ├── src/
│   ├── Dockerfile
│   └── build.gradle
├── web/         React + Vite frontend
│   ├── src/
│   ├── Dockerfile
│   └── nginx.conf
├── k8s/                            Kubernetes manifests (full stack)
│   ├── issuer.yaml
│   ├── secret.yaml
│   ├── configmap.yaml
│   ├── mongo.yaml
│   ├── redis.yaml
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── web-deployment.yaml
│   ├── web-service.yaml
│   └── ingress.yaml
├── docker/
│   └── gateway.conf                nginx gateway (Docker Compose)
├── postman/
│   ├── quarkus-react-fullstack-k8s.postman_collection.json
│   ├── quarkus-react-fullstack-k8s.local.postman_environment.json
│   └── quarkus-react-fullstack-k8s.k8s.postman_environment.json
├── .github/workflows/
│   ├── docker-publish.yml          Backend CI/CD
│   └── docker-publish-web.yml      Frontend CI/CD
├── docker-compose.yml
└── README.md
```

---

## Getting Started

### Docker Compose (recommended for local dev)

```bash
docker compose up --build
```

- Backend API: `http://localhost:8080`
- Frontend: `http://localhost:3000`

### Kubernetes

```bash
kubectl apply -f k8s/
```

Add `product.local` to `/etc/hosts` pointing to your Ingress controller IP, then access the app at `http://product.local`.

---

## Testing

```bash
# Backend
cd api
./gradlew test

# Frontend
cd web
npm test
```

Both services have independent test suites. The backend covers use cases, persistence adapters, and REST endpoints. All tests run locally without Docker or external services.

See [`api/README.md`](api/README.md) for full coverage details and test descriptions.

---

## CI/CD

GitHub Actions runs tests and publishes Docker images to GHCR:

| Workflow | Trigger | What it does |
|---|---|---|
| `ci.yml` | Every push / PR to `main` | Backend tests + JaCoCo coverage gate; frontend typecheck, tests, and coverage; SonarCloud (on main) |
| `docker-publish.yml` | Push / PR to `main` (`api/**`) | Backend tests + coverage, then builds and pushes `ghcr.io/apchavez/product-api` |
| `docker-publish-web.yml` | Push / PR to `main` (`web/**`) | Frontend typecheck, tests, coverage, build, then pushes `ghcr.io/apchavez/product-web` |

---

## Postman

The `postman/` folder contains the collection and two environments.

| File | Description |
|---|---|
| `quarkus-react-fullstack-k8s.postman_collection.json` | Main collection (10 requests) |
| `quarkus-react-fullstack-k8s.local.postman_environment.json` | Local environment via Docker Compose |
| `quarkus-react-fullstack-k8s.k8s.postman_environment.json` | Kubernetes environment (`product.local`) |

Import all three files into Postman, select the appropriate environment, and run the requests in order — `01 - Create Product` automatically captures `productId` for subsequent requests.

> For K8s: add `product.local` to `/etc/hosts` pointing to the Ingress controller IP before running the collection.

---

## What This Project Demonstrates

- Fullstack development: Java backend + React frontend as independent services
- Hexagonal architecture on Quarkus with MongoDB persistence and Redis caching
- Complete Kubernetes manifests: ConfigMap, Secret, Deployments, Services, Ingress
- Multi-stage Docker builds for both backend and frontend
- Independent CI/CD pipelines per service (backend and frontend published separately to GHCR)

---

## Detailed Documentation

See [`api/README.md`](api/README.md) for complete backend setup, endpoints, and deployment instructions.

---

## Related Projects

| Project | Description |
|---|---|
| [spring-webflux-hexagonal-arch](https://github.com/apchavez/spring-webflux-hexagonal-arch) | Java 21 reactive REST API with Spring Boot WebFlux, hexagonal architecture, PostgreSQL, and Kubernetes deployment |
| [clean-arch-azure-functions-java](https://github.com/apchavez/clean-arch-azure-functions-java) | Java 21 serverless platform on Azure Functions with Clean Architecture |
---

## License

[MIT](LICENSE)
