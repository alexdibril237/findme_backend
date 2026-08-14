# Rapport de sécurité — Backend findMe (GeoLink Africa)

Livrable **L9** du Projet 6. Décrit le modèle RBAC, le cycle de vie du JWT et les principales
vulnérabilités traitées (référentiel OWASP Top 10, niveau basique). Complète la section 7 du
[dossier de conception](conception/DOSSIER_DE_CONCEPTION.md).

> Principe directeur (mail du Directeur Technique) : **l'API est la véritable ligne de défense**.
> Toute règle métier ou de sécurité vérifiée côté frontend est **revalidée côté serveur**.

---

## 1. Modèle RBAC

Trois rôles, permissions strictement différenciées, portés par la claim `role` du JWT.

| Ressource / Action | USER | SUPPORT_AGENT | ADMIN |
|---|:---:|:---:|:---:|
| Gérer son profil et ses adresses (max 4) | ✅ | ✅ | ✅ |
| Consulter la liste des utilisateurs / adresses (admin) | ❌ | ❌ | ✅ |
| Consulter et traiter les tickets de support | ❌ | ✅ | ✅ |
| Modifier le rôle / désactiver un compte | ❌ | ❌ | ✅ |

**Mise en œuvre :**
- Autorisation déclarative via `@PreAuthorize("hasRole('ADMIN')")` / `hasAnyRole('ADMIN','SUPPORT_AGENT')`
  sur les contrôleurs `/api/admin/**` (annotation `@EnableMethodSecurity`).
- Propriété des ressources : chaque use case adresse vérifie que `address.userId == currentUserId`
  avant lecture/modification/suppression (sinon `403 ADDRESS_ACCESS_DENIED`), indépendamment du RBAC.
- Le `SUPPORT_AGENT` n'a **aucun** accès à la gestion des utilisateurs (403), seulement au support.

---

## 2. Cycle de vie du JWT

### Access token
- **Format** : JWT signé **HMAC-SHA384** (HS384), secret partagé injecté via `JWT_SECRET` (env).
- **Claims** : `sub` (userId), `email`, `role`, `iat`, `exp`.
- **Durée de vie courte** : 15 minutes (`JWT_ACCESS_TTL_MINUTES`).
- **Émission** : uniquement par l'auth-service, à l'inscription et à la connexion.
- **Validation** : à deux niveaux (défense en profondeur) — l'API Gateway valide en premier niveau,
  puis **chaque microservice revalide** localement le token (signature + expiration) avant de servir
  la requête. Un service compromis en amont ne suffit donc pas à contourner l'autorisation.
- **Stateless** : `SessionCreationPolicy.STATELESS`, aucune session serveur.

### Refresh token
- **Opaque** (non-JWT), généré aléatoirement, **stocké haché** en base (`token_hash`) — jamais en clair.
- **Durée de vie longue** : 7 jours (`JWT_REFRESH_TTL_DAYS`).
- **Rotation** : à chaque `/api/auth/refresh`, l'ancien refresh token est **révoqué** et un nouveau est émis.
- **Détection de réutilisation** : si un refresh token déjà révoqué est présenté (signe de vol),
  **tous** les refresh tokens de l'utilisateur sont révoqués (`revokeAllForUser`).
- **Révocation** : au `logout`, à la réinitialisation du mot de passe et à la désactivation du compte.

### Token de réinitialisation de mot de passe
- Émis par `/api/auth/forgot-password`, stocké **haché**, **à usage unique** (drapeau `used`),
  **expire après 30 minutes** (`JWT_RESET_TTL_MINUTES`).
- Réponse **non énumérante** : `forgot-password` renvoie toujours le même message, que l'email existe ou non.

---

## 3. Vulnérabilités traitées (OWASP Top 10, niveau basique)

| OWASP | Risque | Mesure appliquée |
|---|---|---|
| **A01 – Broken Access Control** | Accès à des ressources d'autrui, escalade de privilèges | RBAC `@PreAuthorize` + vérification systématique de propriété (userId) ; défense en profondeur Gateway + service |
| **A02 – Cryptographic Failures** | Mots de passe / tokens exposés | Mots de passe **BCrypt** ; refresh & reset tokens **hachés** en base ; JWT signé HS384 ; secrets via variables d'environnement |
| **A03 – Injection** | Injection SQL | Spring Data JPA / requêtes paramétrées ; aucune concaténation SQL ; validation Bean Validation des DTO |
| **A04 – Insecure Design** | Règles métier contournables | Règles (quota 4 adresses, unicité, politique de mot de passe) **revalidées serveur** ; contraintes garanties aussi au niveau **base** (unicité, trigger quota) |
| **A05 – Security Misconfiguration** | Secrets par défaut, endpoints exposés | Secrets obligatoires en prod (`JWT_SECRET` requis au démarrage via Compose) ; Actuator limité à `health`/`info` ; CSRF désactivé car API stateless sans cookie |
| **A07 – Identification & Auth Failures** | Énumération de comptes, force brute | `401` **générique** sur identifiants invalides (ne révèle pas l'existence de l'email) ; politique de mot de passe (≥ 8, 1 majuscule, 1 chiffre) revalidée serveur ; tokens à durée de vie courte |
| **A08 – Data Integrity Failures** | Écritures partielles incohérentes | Opérations critiques encapsulées dans des transactions `@Transactional` (création de compte, création d'adresse avec vérif. quota) |
| **A09 – Logging & Monitoring** | Fuite de données sensibles dans les logs | Aucun mot de passe ni token en clair dans les logs ; erreurs normalisées `ProblemDetail` (RFC 7807) sans détail interne ; healthchecks via Actuator |

---

## 4. Politique de mot de passe

- Minimum **8 caractères**, au moins **1 majuscule** et **1 chiffre**.
- Vérifiée côté frontend **et** revalidée côté serveur (`WeakPasswordException` → `400 WEAK_PASSWORD`).
- Hachage **BCrypt** (`BCryptPasswordEncoder`, coût par défaut 10) ; aucun mot de passe en clair ne
  transite en base ni dans les logs.

---

## 5. Gestion des erreurs de sécurité (codes HTTP)

| Situation | Code | errorCode |
|---|---|---|
| Email déjà utilisé (inscription) | `409` | `EMAIL_ALREADY_USED` |
| Identifiants invalides | `401` | `INVALID_CREDENTIALS` (message générique) |
| Token JWT absent / invalide / expiré | `401` | (entry point) |
| Rôle insuffisant | `403` | (access denied handler) |
| Accès à l'adresse d'un autre utilisateur | `403` | `ADDRESS_ACCESS_DENIED` |
| Lien de réinitialisation expiré / déjà utilisé | `400` | `INVALID_OR_EXPIRED_TOKEN` |

---

## 6. Points d'attention / améliorations futures

- **Rate limiting** sur `/api/auth/signin` et `/api/auth/forgot-password` (anti-force brute) — non implémenté (hors périmètre basique).
- **Rotation du secret JWT** (kid + JWKS) pour une révocation globale sans redéploiement.
- **HTTPS / TLS** à terminer au niveau de l'ingress / reverse-proxy en production (non géré par le Compose de démo).
- **Audit trail** persistant des actions sensibles (changement de rôle, désactivation de compte).
