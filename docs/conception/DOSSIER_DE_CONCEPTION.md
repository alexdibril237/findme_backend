# Dossier de conception — Backend findMe (GeoLink Africa)

Projet 6 — Développeur Backend Java / Spring Boot. Ce document est la source de vérité
architecturale du backend et couvre les livrables L2 attendus par le cahier des charges
(section 4) : diagrammes, MCD/MPD, matrice RBAC, stratégie de sécurité, stratégie
transactionnelle, organisation des packages, conventions REST, stratégie de tests.

Sources utilisées : `PROJET_6_Developpeur_Backend_Java_SpringBoot.pdf` (spécification
normative du contrat d'API, section 2) et `docs/frontend-api-contract.md` (audit du code
frontend réel du Projet 4).

> **Décision de cadrage** : l'audit du frontend (`docs/frontend-api-contract.md`) montre que
> le code actuel du Projet 4 fonctionne en grande partie sur `localStorage` et utilise des
> noms de champs anglais divergents du PDF. Le PDF étant la spécification normative et
> versionnée du Projet 6, **c'est ce contrat (section 2 du PDF) qui est implémenté ici**,
> et non le code frontend existant tel quel. Les deux endpoints RBAC "modifier le rôle" /
> "désactiver un compte" (section 2.4) n'ont pas de route dédiée dans les endpoints figés du
> PDF (section 2.1) : ils sont ajoutés en extension additive (`PATCH /api/admin/users/{id}/role`,
> `PATCH /api/admin/users/{id}/status`), sans toucher aux routes existantes — le frontend actuel
> ne les appelle pas, donc aucun risque de régression.

---

## 1. Diagramme de contexte

```mermaid
graph TB
    FE["Frontend findMe<br/>(Nuxt 3)"]
    B2B["Partenaires B2B<br/>(logistique, fintech, e-commerce)"]

    subgraph GeoLink Backend
        GW["API Gateway<br/>:8080"]
        AUTH["auth-service<br/>:8081"]
        ADDR["address-service<br/>:8082"]
        ADMIN["admin-service<br/>:8083"]
        AUTHDB[("auth_db<br/>PostgreSQL")]
        ADDRDB[("address_db<br/>PostgreSQL")]
        ADMINDB[("admin_db<br/>PostgreSQL")]
    end

    FE -->|HTTPS /api/**| GW
    B2B -->|HTTPS /api/** + OpenAPI| GW

    GW -->|"/api/auth/**, /api/users/**"| AUTH
    GW -->|"/api/addresses/**"| ADDR
    GW -->|"/api/admin/**, /api/support"| ADMIN

    ADMIN -->|"REST synchrone (RestClient)<br/>lecture agrégée"| AUTH
    ADMIN -->|"REST synchrone (RestClient)<br/>lecture agrégée"| ADDR

    AUTH --- AUTHDB
    ADDR --- ADDRDB
    ADMIN --- ADMINDB
```

**Points clés :**
- Un seul point d'entrée public : l'API Gateway (port 8080), seul service exposé côté hôte
  dans `docker-compose.yml`. Les 3 microservices ne sont joignables que sur le réseau Docker
  interne — défense en profondeur en plus de la validation JWT.
- Pattern *database-per-service* strict : aucune base n'est partagée, aucune FK inter-service
  au niveau SQL (`address_db.addresses.user_id` est une référence logique, pas une FK physique
  vers `auth_db.users`).
- `admin-service` ne duplique aucune donnée métier : il interroge `auth-service` et
  `address-service` à la demande (appels REST synchrones) pour construire ses vues agrégées.

---

## 2. Diagramme de composants (architecture Clean/Hexagonale)

Les 3 microservices métier partagent la même stratification en 4 couches. `api-gateway` est
volontairement plus simple (pas de domaine métier propre : routage + sécurité transversale).

```mermaid
graph LR
    subgraph "web (interfaces)"
        CTRL[Contrôleurs REST]
        DTO[DTO request/response]
        MAP[Mappers DTO ↔ domaine]
        ADVICE["@RestControllerAdvice<br/>(ProblemDetail RFC 7807)"]
    end

    subgraph application
        UC["Use cases<br/>(@Transactional)"]
        PORTIN[Ports d'entrée]
    end

    subgraph domain
        ENT["Entités & value objects<br/>règles métier pures"]
        PORTOUT["Ports de sortie<br/>(interfaces)"]
        DEXC[Exceptions métier]
    end

    subgraph infrastructure
        JPA["Adaptateurs JPA<br/>(Repository impl)"]
        SEC["Sécurité<br/>(JWT, filtres, RBAC)"]
        CLIENT["Clients REST<br/>(inter-services)"]
        CFG[Configuration Spring]
    end

    CTRL --> UC
    CTRL --> DTO
    CTRL --> MAP
    ADVICE -.intercepte.-> CTRL
    UC --> ENT
    UC --> PORTOUT
    UC --> DEXC
    JPA -.implémente.-> PORTOUT
    CLIENT -.implémente.-> PORTOUT
    SEC --> CTRL
    CFG -.assemble via IoC.-> UC
    CFG -.assemble via IoC.-> JPA
```

**Règle de dépendance (inversion de dépendances, SOLID/D)** : `domain` ne dépend de rien
(aucune annotation Spring/JPA). `application` dépend uniquement de `domain` (ports). `web` et
`infrastructure` dépendent de `application`/`domain`, jamais l'inverse. Les adaptateurs
(`infrastructure`) implémentent les ports définis dans `domain`, injectés par Spring IoC —
c'est ce qui permet de tester les use cases avec des mocks Mockito sans démarrer de contexte
Spring ni de base de données.

---

## 3. Diagramme de classes (modèle de domaine)

### 3.1 auth-service

```mermaid
classDiagram
    class User {
        -UserId id
        -Email email
        -String passwordHash
        -String firstName
        -String lastName
        -Role role
        -AccountStatus status
        -Instant createdAt
        -Instant lastLoginAt
        +changePassword(String newHash)
        +recordLogin(Instant when)
        +disable()
        +hasRole(Role r) boolean
    }
    class Email {
        -String value
        +Email(String raw)
    }
    class Role {
        <<enumeration>>
        USER
        ADMIN
        SUPPORT_AGENT
    }
    class AccountStatus {
        <<enumeration>>
        ACTIVE
        DISABLED
    }
    class RefreshToken {
        -TokenId id
        -UserId userId
        -String tokenHash
        -Instant expiresAt
        -boolean revoked
        +isValid(Instant now) boolean
        +revoke()
    }
    class PasswordResetToken {
        -TokenId id
        -UserId userId
        -String tokenHash
        -Instant expiresAt
        -boolean used
        +isUsable(Instant now) boolean
        +markUsed()
    }
    class UserRepositoryPort {
        <<interface>>
        +findByEmail(Email) Optional~User~
        +existsByEmail(Email) boolean
        +save(User) User
    }
    class RefreshTokenRepositoryPort {
        <<interface>>
        +save(RefreshToken)
        +findByTokenHash(String) Optional~RefreshToken~
        +revokeAllForUser(UserId)
    }
    class PasswordHasherPort {
        <<interface>>
        +hash(String raw) String
        +matches(String raw, String hash) boolean
    }
    class JwtIssuerPort {
        <<interface>>
        +issueAccessToken(User) String
        +issueRefreshToken(User) String
        +parseAndValidate(String token) TokenClaims
    }

    User "1" --> "1" Email
    User "1" --> "1" Role
    User "1" --> "1" AccountStatus
    RefreshToken --> User : userId
    PasswordResetToken --> User : userId
    UserRepositoryPort ..> User
    RefreshTokenRepositoryPort ..> RefreshToken
```

### 3.2 address-service

```mermaid
classDiagram
    class Address {
        -AddressId id
        -UserId userId
        -String country
        -String city
        -String district
        -String street
        -String houseNumber
        -String postalCode
        -GeoPoint location
        -String photoUrl
        -Instant createdAt
        -Instant updatedAt
        +belongsTo(UserId) boolean
        +updateDetails(...)
        +attachPhoto(String url)
        +sameIdentityAs(Address other) boolean
    }
    class GeoPoint {
        -double latitude
        -double longitude
    }
    class AddressQuotaPolicy {
        <<domain service>>
        +MAX_ADDRESSES_PER_USER: int = 4
        +ensureCanCreate(long currentCount) void
    }
    class AddressRepositoryPort {
        <<interface>>
        +countByUser(UserId) long
        +existsSameIdentity(UserId, country, city, district, street, houseNumber) boolean
        +findById(AddressId) Optional~Address~
        +findPageByUser(UserId, Pageable, filters) Page~Address~
        +save(Address) Address
        +deleteById(AddressId)
    }
    class PhotoStoragePort {
        <<interface>>
        +store(AddressId, MultipartContent) String
        +validate(MultipartContent) void
    }

    Address "1" --> "1" GeoPoint
    AddressRepositoryPort ..> Address
    AddressQuotaPolicy ..> AddressRepositoryPort : consulte le compte courant
```

### 3.3 admin-service

```mermaid
classDiagram
    class SupportTicket {
        -TicketId id
        -String name
        -String email
        -String message
        -TicketStatus status
        -Instant createdAt
        -Instant updatedAt
        +markTreated()
        +markPending()
    }
    class TicketStatus {
        <<enumeration>>
        NON_TRAITE
        TRAITE
    }
    class UserSummary {
        <<vue agrégée, pas de persistance>>
        -String id
        -String email
        -String fullName
        -Role role
        -String country
        -String city
    }
    class AddressSummary {
        <<vue agrégée, pas de persistance>>
        -String id
        -String ownerId
        -String country
        -String city
        -String district
    }
    class SupportTicketRepositoryPort {
        <<interface>>
        +findPage(Pageable, statusFilter) Page~SupportTicket~
        +save(SupportTicket) SupportTicket
        +findById(TicketId) Optional~SupportTicket~
    }
    class AuthServiceClientPort {
        <<interface>>
        +findUsersPage(Pageable, filters) Page~UserSummary~
        +updateUserRole(String userId, Role newRole)
        +updateUserStatus(String userId, AccountStatus status)
    }
    class AddressServiceClientPort {
        <<interface>>
        +findAddressesPage(Pageable, filters) Page~AddressSummary~
    }

    SupportTicket --> TicketStatus
    SupportTicketRepositoryPort ..> SupportTicket
    AuthServiceClientPort ..> UserSummary
    AddressServiceClientPort ..> AddressSummary
```

`UserSummary`/`AddressSummary` ne sont **pas** des entités persistées : ce sont des objets de
lecture reconstruits à chaque appel depuis les réponses REST de `auth-service`/`address-service`
(pas de duplication de données métier, conformément à la section 2.3 du cahier des charges).

---

## 4. Diagrammes de séquence

### 4.1 Inscription (signup)

```mermaid
sequenceDiagram
    actor U as Utilisateur
    participant FE as Frontend
    participant GW as API Gateway
    participant A as auth-service
    participant DB as auth_db

    U->>FE: Remplit le formulaire d'inscription
    FE->>GW: POST /api/auth/signup {email, motDePasse, prenom, nom}
    GW->>A: proxy (route non protégée)
    A->>A: Valide le DTO (Bean Validation :<br/>email, mdp ≥8 car., 1 maj., 1 chiffre)
    A->>DB: SELECT ... WHERE email = ? (via unique constraint)
    alt email déjà utilisé
        DB-->>A: contrainte uq_users_email violée
        A-->>GW: 409 Conflict (ProblemDetail)
        GW-->>FE: 409 Conflict
        FE-->>U: "Cet email est déjà utilisé"
    else email disponible
        A->>A: BCrypt.hash(motDePasse)
        A->>DB: INSERT INTO users (...)
        DB-->>A: User créé
        A->>A: Génère access token + refresh token (JwtIssuerPort)
        A->>DB: INSERT INTO refresh_tokens (hash, expiresAt)
        A-->>GW: 201 Created {user, accessToken, refreshToken}
        GW-->>FE: 201 Created
        FE-->>U: Compte créé, connecté
    end
```

### 4.2 Connexion avec émission du JWT (signin)

```mermaid
sequenceDiagram
    actor U as Utilisateur
    participant FE as Frontend
    participant GW as API Gateway
    participant A as auth-service
    participant DB as auth_db

    U->>FE: Saisit email + mot de passe
    FE->>GW: POST /api/auth/signin {email, motDePasse}
    GW->>A: proxy (route non protégée)
    A->>DB: findByEmail(email)
    alt utilisateur introuvable OU mot de passe invalide
        A-->>GW: 401 Unauthorized {message générique}
        GW-->>FE: 401
        Note over A: Message identique dans les deux cas :<br/>ne révèle jamais si l'email existe
    else identifiants valides ET compte ACTIVE
        A->>A: BCrypt.matches(motDePasse, hash)
        A->>A: issueAccessToken(user) (exp. courte, 15 min)
        A->>A: issueRefreshToken(user) (exp. longue, 7 j)
        A->>DB: UPDATE users SET last_login_at = now()
        A->>DB: INSERT INTO refresh_tokens (hash, expiresAt)
        A-->>GW: 200 OK {user, accessToken, refreshToken}
        GW-->>FE: 200 OK
        FE->>FE: Stocke accessToken/refreshToken
    end
```

### 4.3 Création d'une adresse avec vérification du quota

```mermaid
sequenceDiagram
    actor U as Utilisateur (rôle quelconque)
    participant FE as Frontend
    participant GW as API Gateway
    participant AD as address-service
    participant DB as address_db

    U->>FE: Soumet une nouvelle adresse
    FE->>GW: POST /api/addresses (Authorization: Bearer <access token>)
    GW->>GW: Valide signature + expiration JWT
    alt JWT invalide/expiré
        GW-->>FE: 401 Unauthorized
    else JWT valide
        GW->>AD: proxy + Authorization forwardé
        AD->>AD: Filtre JWT local (défense en profondeur) : extrait userId, rôle
        AD->>AD: Valide le DTO (Bean Validation)
        AD->>DB: BEGIN TRANSACTION
        AD->>DB: SELECT COUNT(*) FROM addresses WHERE user_id = :id
        alt count >= 4
            AD-->>GW: 409 Conflict "Limite de 4 adresses atteinte"
            GW-->>FE: 409 Conflict
        else count < 4
            AD->>DB: SELECT ... unicité (pays/ville/quartier/rue/numéro)
            alt adresse déjà existante pour cet utilisateur
                AD-->>GW: 409 Conflict "Adresse déjà enregistrée"
            else adresse nouvelle
                AD->>DB: INSERT INTO addresses (...)
                Note right of DB: Trigger trg_enforce_address_quota<br/>= filet de sécurité DB
                DB-->>AD: Address créée
                AD->>DB: COMMIT
                AD-->>GW: 201 Created {address}
                GW-->>FE: 201 Created
            end
        end
    end
```

### 4.4 Consultation admin agrégée (appel inter-services)

```mermaid
sequenceDiagram
    actor A as Administrateur
    participant FE as Frontend (back-office)
    participant GW as API Gateway
    participant AS as admin-service
    participant AU as auth-service
    participant AD as address-service

    A->>FE: Ouvre le tableau de bord "utilisateurs"
    FE->>GW: GET /api/admin/users?page=0&size=20 (Bearer token rôle ADMIN)
    GW->>GW: Valide JWT (signature + expiration)
    GW->>AS: proxy + Authorization forwardé
    AS->>AS: @PreAuthorize("hasRole('ADMIN')")
    AS->>AU: GET /internal/users?page=0&size=20 (RestClient, timeout 3s)
    alt auth-service indisponible / timeout
        AU--xAS: timeout / connexion refusée
        AS-->>GW: 502/503 ProblemDetail "Service utilisateurs indisponible"
        GW-->>FE: 503
    else réponse reçue
        AU-->>AS: 200 OK Page<UserSummary>
        AS-->>GW: 200 OK Page<UserSummary>
        GW-->>FE: 200 OK
        FE-->>A: Affiche la liste paginée
    end
```

---

## 5. Modèle de données (MCD/MPD)

### 5.1 auth_db

```mermaid
erDiagram
    USERS ||--o{ REFRESH_TOKENS : possede
    USERS ||--o{ PASSWORD_RESET_TOKENS : possede

    USERS {
        uuid id PK
        varchar email UK
        varchar password_hash
        varchar first_name
        varchar last_name
        varchar role
        varchar status
        timestamptz created_at
        timestamptz updated_at
        timestamptz last_login_at
    }
    REFRESH_TOKENS {
        uuid id PK
        uuid user_id FK
        varchar token_hash UK
        timestamptz expires_at
        boolean revoked
        timestamptz created_at
    }
    PASSWORD_RESET_TOKENS {
        uuid id PK
        uuid user_id FK
        varchar token_hash UK
        timestamptz expires_at
        boolean used
        timestamptz created_at
    }
```

### 5.2 address_db

```mermaid
erDiagram
    ADDRESSES {
        uuid id PK
        uuid user_id "référence logique vers auth_db.users.id (pas de FK physique)"
        varchar country
        varchar city
        varchar district
        varchar street
        varchar house_number
        varchar postal_code
        double latitude
        double longitude
        varchar photo_url
        timestamptz created_at
        timestamptz updated_at
    }
```
Contrainte `UNIQUE (user_id, country, city, district, street, house_number)` +
trigger `trg_enforce_address_quota` (voir `V2__enforce_address_quota_trigger.sql`).

### 5.3 admin_db

```mermaid
erDiagram
    SUPPORT_TICKETS {
        uuid id PK
        varchar name
        varchar email
        text message
        varchar status
        timestamptz created_at
        timestamptz updated_at
    }
```
`admin_db` ne contient **aucune table users/addresses** — uniquement les tickets de support,
conformément au principe "pas de duplication de données métier".

---

## 6. Matrice RBAC complète

Légende : ✅ autorisé · 🔒 uniquement sur sa propre ressource · ❌ interdit · 🌐 public (pas de JWT requis).

| Endpoint | Méthode | Public | USER | SUPPORT_AGENT | ADMIN |
|---|---|:---:|:---:|:---:|:---:|
| `/api/auth/signup` | POST | 🌐 | 🌐 | 🌐 | 🌐 |
| `/api/auth/signin` | POST | 🌐 | 🌐 | 🌐 | 🌐 |
| `/api/auth/refresh` | POST | 🌐 (token valide requis) | ✅ | ✅ | ✅ |
| `/api/auth/logout` | POST | ❌ | ✅ | ✅ | ✅ |
| `/api/auth/forgot-password` | POST | 🌐 | 🌐 | 🌐 | 🌐 |
| `/api/auth/reset-password` | POST | 🌐 (token à usage unique) | 🌐 | 🌐 | 🌐 |
| `/api/users/me` | GET | ❌ | 🔒 | 🔒 | 🔒 |
| `/api/users/me` | PUT | ❌ | 🔒 | 🔒 | 🔒 |
| `/api/addresses` | GET | ❌ | 🔒 | 🔒 | 🔒 |
| `/api/addresses` | POST | ❌ | 🔒 (quota 4) | 🔒 (quota 4) | 🔒 (quota 4) |
| `/api/addresses/{id}` | GET | ❌ | 🔒 propriétaire | 🔒 propriétaire | 🔒 propriétaire |
| `/api/addresses/{id}` | PUT | ❌ | 🔒 propriétaire | 🔒 propriétaire | 🔒 propriétaire |
| `/api/addresses/{id}` | DELETE | ❌ | 🔒 propriétaire | 🔒 propriétaire | 🔒 propriétaire |
| `/api/addresses/{id}/photo` | POST | ❌ | 🔒 propriétaire | 🔒 propriétaire | 🔒 propriétaire |
| `/api/addresses/{id}/export` | GET | ❌ | 🔒 propriétaire | 🔒 propriétaire | 🔒 propriétaire |
| `/api/admin/users` | GET | ❌ | ❌ | ❌ | ✅ |
| `/api/admin/addresses` | GET | ❌ | ❌ | ❌ | ✅ |
| `/api/support` | POST | 🌐 | 🌐 | 🌐 | 🌐 |
| `/api/admin/support` | GET | ❌ | ❌ | ✅ | ✅ |
| `/api/admin/support/{id}` | PATCH | ❌ | ❌ | ✅ | ✅ |
| `/api/admin/users/{id}/role` *(additif)* | PATCH | ❌ | ❌ | ❌ | ✅ |
| `/api/admin/users/{id}/status` *(additif)* | PATCH | ❌ | ❌ | ❌ | ✅ |

Règles transversales :
- 🔒 « propriétaire » : le use case vérifie systématiquement `address.belongsTo(currentUserId)`
  avant lecture/écriture/suppression → sinon `403 Forbidden` (jamais `404`, pour ne pas fuiter
  l'existence de la ressource... *mais* le cahier des charges section 2.2 impose explicitement
  `404 Not Found` pour une adresse inexistante et `403 Forbidden` pour un accès à l'adresse
  d'un autre utilisateur : ces deux cas sont donc bien distingués, dans cet ordre — existence
  vérifiée avant propriété).
- Les rôles sont portés par la claim `role` du JWT et vérifiés via `@PreAuthorize("hasRole('ADMIN')")`
  (ou équivalent) sur chaque contrôleur/méthode sensible — jamais uniquement côté Gateway.

---

## 7. Stratégie de sécurité (JWT & RBAC)

### 7.1 Cycle de vie du token

- **Émission** : uniquement par `auth-service`, à l'issue de `signin` ou `signup`. Deux tokens :
  - **Access token** (JWT signé HMAC-SHA256, claims : `sub`=userId, `email`, `role`, `exp`) —
    durée de vie courte (15 min, configurable via `JWT_ACCESS_TTL_MINUTES`).
  - **Refresh token** (chaîne aléatoire opaque de 256 bits, `SecureRandom` — pas un JWT — dont
    seul le hash SHA-256 est persisté en base `refresh_tokens`, jamais le token en clair) — durée
    de vie longue (7 jours), **rotation à chaque refresh** :
    l'ancien est révoqué (`revoked=true`) et un nouveau est émis, ce qui permet de détecter le
    rejeu d'un refresh token volé (s'il est présenté après avoir déjà été utilisé une fois, tous
    les tokens de l'utilisateur sont révoqués par précaution).
- **Validation** : **défense en profondeur à deux niveaux**, justifiée par le cahier des charges
  (section 2.4 : « validé par chaque service, ou par la Gateway, selon le choix retenu ») :
  1. L'API Gateway valide en premier la signature et l'expiration (rejet rapide, réponse 401
     homogène, évite de solliciter les services métier pour un token manifestement invalide).
  2. Chaque microservice **revalide indépendamment** le même JWT (même secret partagé via la
     variable d'environnement `JWT_SECRET`) avant d'appliquer son propre `@PreAuthorize`. Ce choix
     évite de faire aveuglément confiance au réseau interne Docker (principe *zero trust* même en
     interne) sans nécessiter de bibliothèque partagée versionnée entre services (chaque service
     reste déployable indépendamment — on accepte une petite duplication de code de validation JWT
     au profit de l'indépendance des microservices).
- **Révocation** : `logout` révoque le refresh token courant. `POST /api/admin/users/{id}/status`
  (désactivation de compte) invalide implicitement toute nouvelle tentative de refresh (vérification
  `status == ACTIVE` à chaque `refresh`/`signin`), même si un access token de courte durée reste
  valable jusqu'à son expiration naturelle (compromis assumé : 15 min max, documenté comme
  limitation connue dans le rapport de sécurité L9).
- **Stockage côté client** : hors périmètre backend (frontend), mais le contrat impose que le
  token soit transmis en en-tête `Authorization: Bearer <token>` — jamais en query param (fuite
  possible dans les logs/proxies).

### 7.2 Protection des endpoints

- `SecurityFilterChain` par service : tout endpoint est protégé par défaut (`anyRequest().authenticated()`),
  seules les routes listées en section 6 comme 🌐 sont explicitement `permitAll()`.
- `@PreAuthorize("hasRole('ADMIN')")` / `hasAnyRole('ADMIN','SUPPORT_AGENT')` au niveau méthode
  des contrôleurs `admin-service`, en plus du filtrage par la Gateway sur le préfixe `/api/admin/**`.
- Vérification de propriété (adresses) : effectuée dans la couche `application` (use case), pas
  dans le contrôleur — c'est une règle métier, pas un détail HTTP.

### 7.3 Hashing & secrets

- Mots de passe : `BCrypt` (coût 10, `org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder`),
  jamais loggés (les DTO de requête excluent le mot de passe des `toString()`/logs applicatifs).
- Tokens de réinitialisation : générés aléatoirement (`SecureRandom`, 32 octets, encodés base64url),
  seul leur hash SHA-256 est persisté — usage unique, expiration 30 min.
- Secrets (`JWT_SECRET`, identifiants PostgreSQL) : jamais en dur dans le code ni dans les images
  Docker — injectés via variables d'environnement (`docker-compose.yml` + `.env`, `.env` exclu du
  contrôle de version).

---

## 8. Stratégie de gestion des transactions

- **Périmètre `@Transactional`** : posé au niveau de la couche `application` (use cases), jamais
  dans les contrôleurs ni les repositories — un use case = une unité transactionnelle cohérente.
  Exemples : `SignupUseCase` (vérification unicité + insertion utilisateur + première émission de
  refresh token), `CreateAddressUseCase` (comptage du quota + vérification d'unicité + insertion,
  dans la même transaction pour éviter une *race condition* entre la lecture du compte et l'écriture).
- **Isolation** : niveau par défaut PostgreSQL (`READ COMMITTED`) jugé suffisant car le quota de 4
  adresses est protégé par un filet de sécurité supplémentaire au niveau base (trigger
  `trg_enforce_address_quota`, section 5.2) qui absorbe le cas limite d'une double requête
  concurrente sur le même compte (la transaction perdante échoue proprement avec une exception
  applicative traduite en `409 Conflict`, plutôt que de produire une 5ᵉ adresse).
- **Erreurs partielles (admin-service)** : les appels REST vers `auth-service`/`address-service`
  ne sont **jamais** inclus dans une transaction locale (ce sont des lectures, pas des écritures
  distribuées — pas de need de saga/2PC). En cas d'échec/timeout d'un service appelé, `admin-service`
  retourne une erreur `503 Service Unavailable` avec un `ProblemDetail` explicite, sans jamais
  planter (`RestClient` configuré avec un timeout de connexion et de lecture de 3s, capturé par un
  `try/catch` dédié dans l'adaptateur `infrastructure/client`).
- **Rollback** : toute exception métier non contrôlée (`RuntimeException`) déclenche un rollback
  automatique Spring ; les exceptions métier attendues (ex. `EmailAlreadyUsedException`,
  `AddressQuotaExceededException`) sont volontairement des `RuntimeException` pour bénéficier de ce
  comportement par défaut, traduites ensuite en code HTTP par le `@RestControllerAdvice`.

---

## 9. Organisation des packages (arborescence type)

Identique pour `auth-service`, `address-service`, `admin-service` (illustration avec `auth-service`) :

```
auth-service/
└── src/main/java/com/geolink/findme/auth/
    ├── AuthServiceApplication.java
    ├── domain/
    │   ├── model/            # User, Email, Role, AccountStatus, RefreshToken, PasswordResetToken
    │   ├── port/              # UserRepositoryPort, RefreshTokenRepositoryPort, PasswordHasherPort, JwtIssuerPort
    │   └── exception/         # EmailAlreadyUsedException, InvalidCredentialsException, ...
    ├── application/
    │   └── usecase/           # SignupUseCase, SigninUseCase, RefreshTokenUseCase, LogoutUseCase,
    │                           # ForgotPasswordUseCase, ResetPasswordUseCase, GetMyProfileUseCase, UpdateMyProfileUseCase
    ├── infrastructure/
    │   ├── persistence/        # UserJpaEntity, UserJpaRepository, UserRepositoryAdapter, ...
    │   ├── security/            # JwtService (impl JwtIssuerPort), JwtAuthenticationFilter, SecurityConfig
    │   └── config/               # OpenApiConfig, BeanConfig (PasswordEncoder, RestClient si besoin)
    └── web/
        ├── controller/          # AuthController, UserController
        ├── dto/                 # SignupRequest, SigninRequest, AuthResponse, UserProfileResponse, ...
        ├── mapper/              # UserWebMapper
        └── advice/               # GlobalExceptionHandler (ProblemDetail)
```

`api-gateway` (structure allégée, pas de couche domaine métier) :
```
api-gateway/
└── src/main/java/com/geolink/findme/gateway/
    ├── ApiGatewayApplication.java
    ├── config/        # RouteProperties (mapping préfixe → base URL), RestClientConfig
    ├── security/      # GatewayJwtFilter (validation signature/expiration avant proxy)
    └── web/           # ProxyController (routage générique par préfixe de chemin)
```

---

## 10. Conventions REST retenues

- **Nommage des ressources** : substantifs pluriels (`/api/addresses`), imbrication limitée à un
  niveau pour les sous-ressources (`/api/addresses/{id}/photo`).
- **Verbes HTTP** : `GET` (lecture, idempotent), `POST` (création ou action non idempotente comme
  l'upload), `PUT` (remplacement complet d'une ressource existante), `PATCH` (modification
  partielle — utilisé pour le statut d'un ticket ou d'un compte), `DELETE` (suppression).
- **Codes de statut** : `200` lecture/mise à jour réussie, `201` création (avec header `Location`),
  `204` suppression réussie sans corps, `400` requête invalide (Bean Validation), `401` non
  authentifié/token invalide, `403` authentifié mais non autorisé, `404` ressource inexistante,
  `409` conflit métier (email dupliqué, quota, adresse dupliquée), `422` (réservé pour une
  validation métier complexe multi-champs si besoin futur), `500` erreur non anticipée.
- **Pagination** : paramètres `page` (0-indexé) et `size`, mappés directement sur `Pageable` de
  Spring Data ; `sort` au format `champ,asc|desc`. Réponse de liste = `Page<T>` sérialisé par
  Spring (`content`, `totalElements`, `totalPages`, `number`, `size`) — pas d'enveloppe `{success,data}`
  personnalisée (voir décision de cadrage en tête de document).
- **Filtrage** : query params nommés d'après le domaine métier (`pays`, `ville`, `quartier`,
  `statut`), toujours optionnels, combinables.
- **Erreurs** : format homogène `ProblemDetail` (RFC 7807) via `@RestControllerAdvice` central par
  service — `type`, `title`, `status`, `detail`, `instance`, et une extension `errorCode` (ex.
  `EMAIL_ALREADY_USED`, `ADDRESS_QUOTA_EXCEEDED`) exploitable par le frontend sans parser le message.
- **Versionnement** : non appliqué dans cette itération (contrat figé, un seul consommateur
  officiel) ; si nécessaire plus tard, stratégie recommandée = préfixe d'URI (`/api/v2/...`)
  plutôt qu'un header, pour rester lisible par les partenaires B2B.
- **DTO systématiques** : aucune entité JPA ni objet de domaine n'est jamais sérialisé directement
  ; chaque contrôleur ne connaît que des DTO `web/dto`, mappés explicitement (mappers manuels,
  pas de bibliothèque de mapping magique, pour rester lisible en contexte pédagogique).

---

## 11. Stratégie de tests

**Pyramide visée** (par service) :

```mermaid
graph TD
    E2E["Tests bout-en-bout manuels<br/>(Postman / Swagger UI, soutenance)"]
    IT["Tests d'intégration (Testcontainers)<br/>endpoints critiques : signup, signin, création adresse + quota,<br/>accès interdit à l'adresse d'autrui, admin agrégé"]
    UNIT["Tests unitaires (JUnit 5 + Mockito)<br/>use cases, règles métier du domaine, mappers"]

    UNIT --> IT --> E2E
```

- **Tests unitaires** (majorité du volume) : ciblent `domain` et `application`, sans contexte
  Spring (`@ExtendWith(MockitoExtension.class)`), ports mockés. Couvrent explicitement les cas
  limites du cahier des charges (email dupliqué → 409, identifiants invalides → 401, quota
  dépassé → 409, accès à l'adresse d'autrui → 403, adresse inexistante → 404, token de
  réinitialisation expiré/déjà utilisé → 410/400, upload non conforme → 400).
- **Tests d'intégration** : `@SpringBootTest` + Testcontainers PostgreSQL (une instance par
  classe de test, migrations Flyway réellement exécutées) pour les endpoints critiques listés
  ci-dessus + vérification de la RBAC (`@WithMockUser`/JWT généré en test) sur les routes
  `/api/admin/**`.
- **Objectif de couverture** : ≥ 80 % sur `domain`+`application` (logique métier), ≥ 60 % global
  par service. Mesuré via `jacoco-maven-plugin` (rapport HTML dans `target/site/jacoco`), ajouté
  en semaine 4 avec les tests eux-mêmes.
- **Hors périmètre non-obligatoire** : tests de charge/performance (non demandés par le cahier
  des charges), circuit breaker (bonus explicitement optionnel).
