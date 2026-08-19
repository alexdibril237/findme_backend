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
│   └── frontend-api-contract.md  # Audit du contrat réel du Projet 4
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
