# Contrat d'API réel du frontend findMe (Projet 4) — Analyse de code

Source analysée : `C:\Users\LENOVO\Documents\formation fullstack\findme` (Nuxt 3).
Objectif : servir de référence pour l'implémentation du backend Spring Boot (`fidme`), qui doit remplacer le
mock server **sans aucune modification du code frontend** (hors variable d'environnement d'URL de base).

## ⚠️ Constat majeur (à lire avant tout le reste)

Le frontend actuel **n'est pas** un client API "propre" qui appelle systématiquement un backend REST. Il a été
construit pour fonctionner en démo autonome, et mélange trois sources de données différentes selon l'écran :

1. **`localStorage` comme source de vérité** pour l'authentification (registre local d'utilisateurs), les
   adresses (`findme_addresses_<userId>`), les messages (`findme_messages_<userId>`, `findme_admin_inbox`) et
   le brouillon d'adresse. La plupart des écrans lisent/écrivent directement dans `localStorage` et **n'appellent
   jamais une API HTTP**.
2. **Un mock Postman distant** (`NUXT_PUBLIC_API_BASE` dans `.env` = `https://…mock.pstmn.io`), utilisé
   uniquement en fallback pour `POST /auth/login` (comptes admin uniquement) et `POST /auth/signup`.
3. **Des routes serveur Nitro locales** (`server/api/**`), qui sont en fait un **second mock**, embarqué dans le
   frontend lui-même, et répondent à `/api/...` en relatif (voir `.env.example`). Certaines (`/api/auth/forgot-password`,
   `/api/support/*`) sont bien appelées par le code ; d'autres (`/api/addresses/*`, `/api/admin/*`) existent mais
   **ne sont appelées par aucun composant/store** que j'ai trouvé (voir section Gaps).

**Conséquence concrète** : brancher un vrai backend Spring Boot sur `NUXT_PUBLIC_API_BASE` ne suffira pas à rendre
le frontend "réel" pour les adresses, les messages ou la connexion utilisateur standard, **tant que le code
frontend n'est pas modifié**, car ces flux ne lisent/écrivent que `localStorage` aujourd'hui. Le cahier des
charges du Projet 6 (PDF) décrit le contrat *cible* (routes `/api/auth/**`, `/api/addresses/**`, etc., avec des
noms de champs français comme `motDePasse`, `prénom`, `nom`) — **ce contrat cible ne correspond pas exactement**
à ce que le code frontend appelle réellement aujourd'hui (routes `/auth/login`, `/auth/signup` en anglais,
`name` au lieu de `prénom`/`nom` séparés, pas d'appel à `/api/addresses` pour la lecture, etc.).

**Recommandation** : implémenter le backend selon le contrat *cible* du PDF (section 2 du cahier des charges —
c'est la référence normative, versionnée, faite pour un vrai backend). Le tableau ci-dessous documente le
contrat *actuellement observé dans le code* pour que l'écart soit visible et arbitrable par l'équipe ; il ne
doit pas être copié tel quel sans décision explicite.

---

## 1. Configuration de base

- Variable d'env frontend : `NUXT_PUBLIC_API_BASE` (lue via `useRuntimeConfig().public.apiBase`).
  - `.env` (actif) : `https://0e736129-e57d-4161-af5d-4f0d1d1613e7.mock.pstmn.io` (mock Postman distant).
  - `.env.example` : `/api` (routes Nitro locales relatives).
  - `.env.local` : `http://localhost:3001` (json-server local, lancé via `npm run mock`, sert `mock-server/db.json`).
  - → **Un seul changement de cette variable suffit** à repointer les appels `$fetch(`${api}/...`)` vers le
    vrai backend, **mais seulement pour les flux qui appellent réellement `${api}/...`** (voir constat ci-dessus).
- `json-server` (`npm run mock`) sert `mock-server/db.json` tel quel sur le port 3001, avec les conventions REST
  standard de json-server (pas de fichier de routes custom trouvé) : `GET/POST /addresses`, `GET/PUT/DELETE /addresses/:id`.
  Le fichier ne contient qu'une collection `addresses` (2 entrées), pas de collection `users`.

## 2. Authentification (`app/stores/auth.ts`)

### POST `${api}/auth/login` (fallback mock Postman, comptes admin uniquement)
- Appelé seulement si l'email n'est pas dans le registre local ET matche `isAdminEmail` (contient "admin" ou égal à `adminfindme@gmail.com`).
- Requête : `{ email: string, password: string }`
- Réponse attendue (lue par le store) : `res.data.user` et `res.data.token`, donc enveloppe :
  ```json
  { "success": true, "message": "...", "data": { "user": { "id", "name", "email", "role", "city", "createdAt" }, "token": "string" } }
  ```
  (confirmé par l'implémentation Nitro locale `server/api/auth/login.post.ts`, qui produit exactement cette forme).
- Champ `user.role` : `'user'` ou `'admin'` (anglais, pas `USER`/`ADMIN`/`SUPPORT_AGENT` comme dans le PDF).
- Champ `name` unique (pas de `prénom`/`nom` séparés côté frontend).
- Pas de refresh token côté frontend : un seul `token` est stocké (`findme_token`), aucun appel à un endpoint refresh nulle part dans le code.
- Erreur : le store catch générique → toast `errors.invalid_credentials`, ne lit pas de code d'erreur structuré pour ce endpoint.
- **Le flux de connexion normal (non-admin) ne touche jamais le réseau** : il compare email/mot de passe à un registre stocké dans `localStorage['findme_local_users']` (mot de passe "hashé" par `btoa(email::password)`, donc pas un vrai hash).

### POST `${api}/auth/signup`
- Requête : `{ name: string, email: string, password: string }`
- Réponse : même enveloppe que login, `res.data.user` + `res.data.token`.
- Erreur attendue par le store : `err.data.error.code === 'EMAIL_ALREADY_EXISTS'` → toast dédié. Donc le contrat
  d'erreur *attendu par le code* est `{ "error": { "code": "EMAIL_ALREADY_EXISTS", ... } }` avec un statut HTTP
  d'erreur (le store ne vérifie pas le status code explicitement, seulement `err.data.error.code`).
- Après succès, le store sauvegarde aussi l'utilisateur dans le registre local (`localStorage`) — donc même en
  pointant vers un vrai backend, une reconnexion ultérieure avec le même email/mdp passera par le chemin local
  et ne rappellera jamais l'API.

### POST `${api}/auth/logout`
- Appelé avec header `Authorization: Bearer <token>`, corps vide.
- Réponse ignorée (try/catch, le store nettoie l'état local dans tous les cas).

### Mot de passe oublié / réinitialisation — **100% local, aucun vrai endpoint API métier**
- `forgotPassword(email)` génère un token côté client (`crypto.randomUUID()`), le stocke dans
  `localStorage['findme_reset_<token>']` avec une expiration 1h, puis appelle en best-effort (résultat ignoré)
  `POST /api/auth/forgot-password` **en relatif** (toujours la route Nitro locale, jamais `${api}`) avec
  `{ email, token }` — cette route envoie un email via Resend si `RESEND_API_KEY` est configurée, sinon renvoie
  `{ success: true, sent: false }`. Le lien de réinitialisation généré est `${origin}/auth/reset-password?token=<token>`.
- `resetPassword(token, newPassword)` : vérifie le token **uniquement dans localStorage** (`verifyResetToken`),
  aucun appel réseau. Il n'y a pas d'endpoint `/api/auth/reset-password` appelé par le frontend.
- Il existe aussi une route Nitro `POST /api/auth/verify-reset` (vérification OTP par email/code, stockage
  `useStorage('cache')`) qui ne semble appelée par aucun store/page trouvé dans `app/` — probablement du code mort
  ou un flux OTP non branché.

### `GET/PUT /api/users/me` — **absent du code frontend**
Aucune trace de ces deux endpoints (mentionnés dans le PDF) dans `app/stores` ou `app/pages`. Le profil utilisateur
semble géré uniquement via l'objet `currentUser` reconstruit depuis `localStorage['findme_user']`.

## 3. Adresses (`app/stores/address.ts`)

- Toutes les routes utilisent `${api}/addresses...` avec header `Authorization: Bearer <token>` (si présent).
- **Mais `fetchMyAddresses()` ne fait jamais d'appel réseau** : elle lit `localStorage['findme_addresses_<userId>']`
  et n'appelle l'API que si... en fait jamais — si le local est vide, elle met `addresses.value = []` sans
  fallback réseau (commentaire explicite dans le code : "on ne charge PAS le mock pour éviter des adresses fictives").
- Limite de 4 adresses : vérifiée **côté client uniquement**, avant l'appel réseau (`if (addresses.value.length >= limit) throw ...`), le store ne dépend d'aucun code d'erreur serveur pour cette règle (bien qu'il sache lire `err.data.error.code === 'ADDRESS_LIMIT_REACHED'` s'il apparaissait).

### POST `${api}/addresses`
- Requête : objet `Address` partiel + `{ status: 'pending', addressCode: <généré côté client>, createdAt, updatedAt }`.
- Modèle `Address` (interface TS, `app/stores/address.ts:44-50`) :
  ```ts
  interface Address {
    id: string; userId: string; label: string; country: string; countryCode: string
    city: string; neighborhood: string; street: string; houseNumber: string
    postalCode?: string | null; gps: { latitude: number; longitude: number }; photo?: string
    status: 'verified' | 'pending' | 'draft'
    addressCode: string; createdAt: string; updatedAt: string
  }
  ```
  Champs anglais (`country`, `city`, `street`, `houseNumber`), pas `pays`/`ville`/`rue`/`numéro` comme dans le PDF.
  `gps: { latitude, longitude }` imbriqué, pas des champs plats `latitude`/`longitude`.
  Champ additionnel `label` (nom donné par l'utilisateur, ex. "Domicile"), `countryCode`, `status`, `addressCode`
  (code lisible généré, ex. `FM 4872 AKWA`) — aucun de ces champs n'est dans le modèle `Address` du PDF.
- Réponse lue par le store : `res.data` fusionné avec les données envoyées (id, userId, addressCode, status,
  createdAt, updatedAt pris depuis `res.data` si présents, sinon valeurs générées côté client).
- Le store persiste ensuite systématiquement dans `localStorage`, indépendamment de la réponse serveur.

### PUT `${api}/addresses/:id`
- Requête : `{ ...updates, updatedAt: now }`. Réponse : `res.data` fusionné dans l'état local.

### DELETE `${api}/addresses/:id`
- Pas de corps. Le store retire l'entrée de son état local indépendamment du contenu de la réponse (seul un
  succès HTTP est requis, sinon catch générique → `errors.network`).

### GET `${api}/addresses/:id`
- D'abord recherche locale (`localStorage`) ; sinon appel réseau, lit `res?.data || res` (tolère les deux formes,
  avec ou sans enveloppe `data`).

### Upload photo / export PDF — **aucun appel réseau**
- Pas de `POST /api/addresses/{id}/photo` trouvé dans le code (la photo est vraisemblablement gérée en base64/local,
  champ `photo?: string` sur l'objet Address).
- Pas de `GET /api/addresses/{id}/export` appelé : la génération PDF (`app/composables/usePDF.ts`,
  `generateAddressPDF(address)`) est **100% côté client** avec `jspdf` + `qrcode`, à partir de l'objet `Address`
  déjà en mémoire (aucune requête HTTP pour récupérer des "données formatées pour PDF"). Elle appelle en revanche
  `GET /api/tile?z=&x=&y=` (route Nitro locale, proxy de tuiles OpenStreetMap) pour dessiner un fond de carte —
  hors périmètre du cahier des charges backend.

## 4. Admin (routes Nitro locales `server/api/admin/*` — non consommées par le frontend actuel)

J'ai trouvé les fichiers serveur suivants mais **aucun appel `$fetch`/`useFetch` correspondant** dans `app/pages/admin/*.vue` (elles utilisent `$fetch` mais vers d'autres cibles, à vérifier composant par composant si besoin — non prioritaire car ces routes semblent être des stubs de démonstration) :
- `GET /api/admin/users` → `{ success, data: { users: [{id,name,email,role,city,addressCount,createdAt}], total } }`
- `GET /api/admin/addresses` → `{ success, data: { addresses: [...], total } }`
- `GET /api/admin/dashboard` → `{ success, data: { users:{total,newThisMonth}, addresses:{total,verifiedThisMonth}, support:{open,resolvedThisMonth} } }`
- `POST /api/admin/messages` → body `{ userId, subject, message }` → `{ success, message, data: { id, userId, fromName, fromRole:'admin', subject, body, read:false, createdAt } }`

Ces trois listes admin (`users`, `addresses`, `dashboard`) retournent des **données statiques codées en dur**
(mêmes 3-4 enregistrements à chaque appel), sans pagination réelle ni query params lus. Aucune query param
(`page`, `size`, `sort`, filtres pays/ville) n'est actuellement géré côté Nitro ni envoyé côté frontend pour ces routes.

## 5. Support (`server/api/support/*`, `server/api/messages/*`)

- `POST /api/support` et `POST /api/support/messages` (redondants, mêmes payload/réponse) : body non validé
  dans le store (page `support.vue` **n'appelle en fait ni l'un ni l'autre** — voir Gaps), réponse
  `{ success, message, data: { ticketId: 'tkt_'+timestamp } }`.
- La messagerie utilisateur⇄admin (`app/stores/messages.ts`, page `support.vue`, `dashboard/messages.vue`) est
  **entièrement gérée en `localStorage`** (`sendToAdmin`, `sendToUser`, `fetchMessages`, `fetchAdminInbox`) : pas
  un seul appel réseau. `GET /api/messages` (Nitro, in-memory, filtre par header `x-user-id`) existe mais n'est
  appelé par aucun store/page.

## 6. Rôles et RBAC côté frontend

- Deux rôles seulement dans le code : `'user'` et `'admin'` (chaînes minuscules). **Aucune trace de `SUPPORT_AGENT`**
  dans `app/` (à confirmer par grep élargi si besoin — non trouvé dans stores/middleware).
- `middleware/auth.ts` : redirige vers `/auth/login` si non authentifié ; si route commence par `/admin` et
  `!isAdmin`, redirige vers `/dashboard`. Pas de granularité plus fine (pas de notion de `SUPPORT_AGENT` avec accès
  partiel).
- `middleware/guest.ts` : redirige les utilisateurs déjà connectés loin des pages login/signup vers `/dashboard`.
- Détection admin : email exact `adminfindme@gmail.com` OU commençant par `admin`, **côté client**, ou
  `role !== 'user'` déjà présent sur l'objet utilisateur restauré.
- Token stocké en clair dans `localStorage['findme_token']`, envoyé en header `Authorization: Bearer <token>`
  (computed `authHeaders` dans `auth.ts`, ou `getHeaders()` dans `address.ts` lisant directement `localStorage`).
  Pas de cookie, pas de refresh automatique, pas de gestion d'expiration détectée dans le code frontend (un 401
  ne semble pas intercepté globalement — pas de plugin `$fetch` global trouvé, chaque store gère son propre catch).

## 7. Gaps / zones d'ombre non résolues par la seule lecture du code

1. **Écart contrat PDF vs code réel** (détaillé en intro) : routes, noms de champs (FR vs EN), forme des rôles
   (`USER`/`ADMIN`/`SUPPORT_AGENT` vs `user`/`admin`), présence/absence de refresh token, pagination — le code
   frontend actuel ne respecte pas littéralement le contrat décrit dans le PDF du Projet 6. Il faut une décision
   produit : implémenter le contrat *PDF* (recommandé, c'est la spec normative) et accepter que certains écrans
   du frontend actuel (ceux qui ne tapent que le localStorage) ne "verront" pas le vrai backend tant qu'ils ne
   sont pas eux-mêmes corrigés — ou bien redéfinir le contrat pour coller au code existant. Je n'ai pas tranché.
2. Aucun composant/page appelant `GET /api/users/me`, `PUT /api/users/me`, `POST /api/addresses/{id}/photo`,
   `GET /api/addresses/{id}/export`, `GET /api/admin/support`, `PATCH /api/admin/support/{id}` n'a été trouvé —
   soit ces écrans n'existent pas encore côté frontend, soit ils appellent des routes sous un nom différent que
   je n'ai pas identifié (je n'ai pas exhaustivement lu tous les fichiers sous `app/pages/**`, seulement ceux
   contenant `$fetch`/`useFetch`/mots-clés "support"/"geolocat"). À vérifier si ces écrans existent visuellement.
3. Pas de fichier de routes/middleware custom pour `json-server` trouvé (`mock-server/routes.json` absent) —
   confirmé absent, json-server tourne donc en conventions REST par défaut sur la seule collection `addresses`.
4. Format d'erreur HTTP réel (status codes) non observable depuis le code Nitro local (les handlers ne lèvent
   `createError({statusCode,...})` que pour la validation basique ; le mock Postman distant n'est pas inspectable
   depuis le code source, son schéma exact de réponse d'erreur pour 401/409 est inconnu sans accès à la console Postman).
5. Le champ `photo` sur `Address` n'a pas de contrainte de type/taille visible côté frontend (pas de composant
   d'upload avec validation trouvé dans les fichiers lus) — à vérifier dans les composants de formulaire d'adresse
   (`app/pages/address/create.vue`, `edit/[id].vue`) si un traitement fichier→base64 existe, non lu en détail ici.