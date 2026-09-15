# findMe — Backend 3-tiers (GeoLink Africa)

Backend Java 21 / Spring Boot 4 du portail **findMe**, remplaçant le Mock Server du Projet 4
sans aucune modification du frontend Nuxt 3 (l'URL de base de l'API reste `http://localhost:8080`).

Projet réalisé dans le cadre du **Projet 6 — Développeur Backend Java/Spring Boot** (DHI Academy).
Le contexte métier complet, le cahier des charges et les contraintes sont dans
`PROJET_6_Developpeur_Backend_Java_SpringBoot.pdf`. Le dossier de conception détaillé (diagrammes,
MCD, matrice RBAC, stratégie de sécurité et de tests) est dans
[`docs/conception/DOSSIER_DE_CONCEPTION.md`](docs/conception/DOSSIER_DE_CONCEPTION.md).

## Architecture

Application Spring Boot **unique**, en architecture **3-tiers classique**, adossée à une seule
base PostgreSQL :

```
Frontend Nuxt 3 → findme-backend (:8080)
                     ├── Présentation   (contrôleurs REST, DTO)
                     ├── Métier         (services, règles métier, sécurité JWT)
                     └── Accès données  (repositories JPA)
                              │
                          findme_db (PostgreSQL)
```

- **presentation** : contrôleurs REST (`AuthController`, `UserController`, `AddressController`,
  `AdminUserController`, `AdminAddressController`, `AdminSupportController`, `SupportController`),
  DTO de requête/réponse, mappers, gestion centralisée des erreurs (`GlobalExceptionHandler`).
- **business** : services applicatifs (`AuthService`, `UserService`, `AddressService`,
  `SupportService`, `PhotoStorageService`, `QrCodeService`), règles métier (quota de 4
  adresses/utilisateur, politique de mot de passe), exceptions métier.
- **data** : entités JPA et repositories Spring Data (`UserRepository`, `AddressRepository`,
  `SupportTicketRepository`, ...).
- **security** : JWT (émission/validation), filtre d'authentification, configuration Spring
  Security, CORS.

Détails complets dans le dossier de conception, sections 1, 2 et 9.

## Prérequis

- Java 21
- Docker & Docker Compose
- (Optionnel, pour développer en dehors de Docker) Maven — sinon utiliser le wrapper `./mvnw`

## Lancement (une seule commande)

```bash
cp .env.example .env   # renseigner un JWT_SECRET réel (32+ caractères aléatoires)
docker compose up -d --build
```

Service exposé sur l'hôte :

| Service | URL | Swagger UI |
|---|---|---|
| findme-backend (point d'entrée unique) | http://localhost:8080 | http://localhost:8080/swagger-ui.html |

Le frontend Nuxt 3 se branche en pointant sa variable d'environnement d'URL de base sur
`http://localhost:8080`.

## Développement local (sans Docker pour le code, DB en conteneur)

```bash
docker compose up -d findme-db
./mvnw spring-boot:run
```
(`DB_HOST=localhost` et `JWT_SECRET` doivent être exportés dans l'environnement — voir les valeurs
par défaut dans `src/main/resources/application.yml`).

## Structure du dépôt

```
fidme/
├── src/main/java/com/geolink/findme/
│   ├── presentation/   # Contrôleurs REST, DTO, mappers, gestion des erreurs
│   ├── business/         # Services, règles métier, exceptions
│   ├── data/               # Entités JPA, repositories Spring Data
│   ├── security/            # JWT, filtre d'authentification, config Spring Security, CORS
│   └── config/                # OpenAPI, fichiers statiques (photos)
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/       # Migrations Flyway
├── docs/
│   ├── conception/           # Dossier de conception complet (L2)
│   ├── sla/                  # SLA/SLO/KPI/métriques (Module 7 Partie 2)
│   └── frontend-api-contract.md  # Audit du contrat réel du Projet 4
├── infra/                    # IaC Terraform + monitoring (Module 7 Partie 2)
│   ├── prometheus/           # Config de scrape + règles d'alerte
│   ├── alertmanager/         # Routage des alertes
│   └── grafana/provisioning/ # Datasource + dashboard SLA
├── docker-compose.yml
├── Dockerfile
└── pom.xml                   # POM unique (application mono-module)
```

## Tests

```bash
./mvnw test
```

Tests unitaires (JUnit 5 + Mockito) sur les règles métier et les services applicatifs, tests
d'intégration (Testcontainers PostgreSQL) sur les endpoints critiques. Stratégie détaillée dans le
dossier de conception, section 11.

## Sécurité

JWT signé (HMAC-SHA256), access token courte durée + refresh token opaque avec rotation, RBAC à
trois rôles (USER, ADMIN, SUPPORT_AGENT), mots de passe hashés BCrypt. Détails complets dans le
dossier de conception (section 7) et le rapport de sécurité (`docs/SECURITE.md`).

## CI/CD

Pipeline GitHub Actions (Module 7 DHI Academy) :

```
push/PR sur main
  → CI : secret scan (Gitleaks) → build → tests unitaires → tests d'intégration
    (Testcontainers) → Quality Gate JaCoCo (65% lignes / 55% branches)
  → CodeQL (SAST, à chaque push/PR + planifié le lundi)
  → Dependabot (SCA, hebdomadaire, PR automatiques)
  → CD (si CI verte) : build Docker → scan Trivy (bloquant HIGH/CRITICAL)
    → publication GHCR (tag SHA + latest) → déploiement Render → smoke test /actuator/health
```

Le déploiement automatisé ne concerne que l'environnement de **test**. Chaque image est publiée
avec un tag SHA immuable (`ghcr.io/alexdibril237/findme_backend:sha-<commit>`), jamais uniquement
`latest`, afin de permettre un rollback fiable.

**Rollback** : en cas d'échec du smoke test ou d'incident après déploiement, redéclencher
manuellement le déploiement Render avec l'`imageUrl` pointant vers le tag SHA du commit précédent
connu pour être stable (visible dans l'onglet **Packages** du dépôt GitHub).

**Secrets/variables GitHub à configurer** (Settings → Secrets and variables → Actions) :
- `RENDER_DEPLOY_HOOK_URL` (secret) : URL de Deploy Hook du service Render (Settings du service →
  Deploy Hook sur render.com). Le service Render doit être configuré pour tirer l'image
  `ghcr.io/alexdibril237/findme_backend:latest`.
- `RENDER_TEST_URL` (variable) : URL publique de l'environnement de test.

## Infrastructure as Code & Monitoring (Module 7 Partie 2)

En complément de Render (déploiement réel), le dossier `infra/` fournit une stack de
monitoring **locale et reproductible**, décrite en Terraform (provider Docker), conforme au
Module 7 Partie 2 (DHI Academy) : `PROJET_6...` remplacé ici par
`Module 7 DevOps - Partie 2 - Infrastructure as Code & Monitoring orientés SLA.pdf`.

Chaîne complète : **SLA → SLO → KPI → Métriques → Instrumentation → Monitoring → Alerting →
Vérification**, documentée dans [`docs/sla/sla-findme.md`](docs/sla/sla-findme.md).

```
findme-backend (Micrometer) → /actuator/prometheus → Prometheus (scrape 15s)
                                                          │
                                              ┌───────────┴───────────┐
                                              ▼                       ▼
                                          Grafana                Alertmanager
                                     (dashboard SLA)          (règles → notifications)
```

### Lancer la stack

```bash
cd infra
cp terraform.tfvars.example terraform.tfvars   # renseigner un JWT_SECRET réel
terraform init
terraform plan
terraform apply
```

| Service | URL |
|---|---|
| findme-backend | http://localhost:8080 |
| Prometheus | http://localhost:9090 |
| Grafana (admin / mot de passe défini dans terraform.tfvars) | http://localhost:3000 |
| Alertmanager | http://localhost:9093 |

`terraform destroy` détruit proprement l'ensemble (réseau, conteneurs, volume PostgreSQL
dédié à cette stack — indépendant de celui de `docker-compose.yml`).

### Tester les alertes (fault injection)

L'image déployée avec `SPRING_PROFILES_ACTIVE=training` (valeur par défaut de cette stack)
expose un endpoint de simulation de panne, **inexistant dans tout autre profil** :

```bash
# Injecter 500ms de latence + 20% d'erreurs sur /api/addresses/**
curl -X POST "http://localhost:8080/api/addresses/_simulate?delayMs=500&errorRate=0.2"

# Observer l'alerte passer de "pending" à "firing" après 5 minutes
curl http://localhost:9090/api/v1/alerts

# Revenir à la normale
curl -X POST "http://localhost:8080/api/addresses/_simulate?delayMs=0&errorRate=0"
```

Les règles d'alerte (`infra/prometheus/rules.yml`) et le dashboard Grafana
(`infra/grafana/provisioning/dashboards/findme-sla.json`) sont tous deux dérivés de la
matrice SLA → KPI de `docs/sla/sla-findme.md` — aucun seuil n'y est choisi arbitrairement.
