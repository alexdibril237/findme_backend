# findMe — Backend microservices (GeoLink Africa)

Backend Java 21 / Spring Boot 4 du portail **findMe**, remplaçant le Mock Server du Projet 4
sans aucune modification du frontend Nuxt 3 (seule l'URL de base de l'API change).

Projet réalisé dans le cadre du **Projet 6 — Développeur Backend Java/Spring Boot** (DHI Academy).
Le contexte métier complet, le cahier des charges et les contraintes sont dans
`PROJET_6_Developpeur_Backend_Java_SpringBoot.pdf`. Le dossier de conception détaillé (diagrammes,
MCD, matrice RBAC, stratégie de sécurité et de tests) est dans
[`docs/conception/DOSSIER_DE_CONCEPTION.md`](docs/conception/DOSSIER_DE_CONCEPTION.md).

## Architecture

```
Frontend Nuxt 3 → API Gateway (:8080) → auth-service (:8081)     → auth_db (PostgreSQL)
                                       → address-service (:8082) → address_db (PostgreSQL)
                                       → admin-service (:8083)   → admin_db (PostgreSQL)
```

- **api-gateway** : point d'entrée unique, routage par préfixe de chemin, validation JWT de premier niveau.
- **auth-service** : comptes utilisateurs, authentification JWT (access + refresh token), RBAC.
- **address-service** : CRUD des adresses, quota de 4 adresses/utilisateur, upload photo, export QR code.
- **admin-service** : vues agrégées utilisateurs/adresses (appels REST vers les deux autres services), support client.

Chaque microservice suit une architecture Clean/Hexagonale : `domain` (entités, règles métier
pures) → `application` (use cases) → `infrastructure` (JPA, sécurité, clients REST) → `web`
(contrôleurs REST, DTO). Détails complets dans le dossier de conception, section 9.

## Prérequis

- Java 21
- Docker & Docker Compose
- (Optionnel, pour développer en dehors de Docker) Maven — sinon utiliser le wrapper `./mvnw`

## Lancement (une seule commande)

```bash
cp .env.example .env   # renseigner un JWT_SECRET réel (32+ caractères aléatoires)
docker compose up -d --build
```

Services exposés sur l'hôte :

| Service | URL | Swagger UI |
|---|---|---|
| API Gateway (point d'entrée frontend) | http://localhost:8080 | — |
| auth-service | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
| address-service | http://localhost:8082 | http://localhost:8082/swagger-ui.html |
| admin-service | http://localhost:8083 | http://localhost:8083/swagger-ui.html |

Le frontend Nuxt 3 se branche en pointant sa variable d'environnement d'URL de base sur
`http://localhost:8080`.

## Développement local (sans Docker pour le code, DB en conteneur)

```bash
docker compose up -d auth-db address-db admin-db
./mvnw -pl auth-service -am spring-boot:run
```
(remplacer `auth-service` par le module souhaité ; `DB_HOST=localhost` et `JWT_SECRET` doivent être
exportés dans l'environnement — voir les valeurs par défaut dans chaque `application.yml`).

## Structure du dépôt

```
fidme/
├── auth-service/       # Authentification, utilisateurs, JWT, RBAC
├── address-service/     # Adresses, quota, photos, export
├── admin-service/        # Administration, vues agrégées, support
├── api-gateway/            # Passerelle unique
├── docs/
│   ├── conception/           # Dossier de conception complet (L2)
│   └── frontend-api-contract.md  # Audit du contrat réel du Projet 4
├── docker-compose.yml
└── pom.xml                   # POM parent multi-module
```

## Tests

```bash
./mvnw test
```

Tests unitaires (JUnit 5 + Mockito) sur le domaine et les use cases, tests d'intégration
(Testcontainers PostgreSQL) sur les endpoints critiques. Stratégie détaillée dans le dossier de
conception, section 11.

## Sécurité

JWT signé (HMAC-SHA256), access token courte durée + refresh token opaque avec rotation, RBAC à
trois rôles (USER, ADMIN, SUPPORT_AGENT), mots de passe hashés BCrypt. Détails complets dans le
dossier de conception (section 7) et le rapport de sécurité (`docs/SECURITE.md`).
