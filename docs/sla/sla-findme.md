# SLA — findme-backend

Document produit dans le cadre du **Module 7 DevOps — Partie 2 (Infrastructure as Code &
Monitoring orientés SLA)**, DHI Academy. Il applique au backend findMe la chaîne
**SLA → SLO → KPI → Métriques → Instrumentation → Monitoring → Alerting → Vérification**
enseignée sur le projet fil rouge "TaskFlow API", adaptée aux endpoints réels de findMe
(authentification, adresses, administration/support — voir le contrat complet dans
`docs/frontend-api-contract.md` et `PROJET_6_Developpeur_Backend_Java_SpringBoot.pdf`).

## Engagement (SLA)

- **Disponibilité** : 99,5 % sur une fenêtre mensuelle.
- **Temps de réponse** : p95 < 300 ms sur les endpoints applicatifs (`/api/**`).
- **Taux d'erreur** : < 1 % des requêtes en erreur serveur (HTTP 5xx).

Ces seuils sont un point de départ réaliste pour une API CRUD adossée à une seule base
PostgreSQL (architecture 3-tiers, voir `docs/conception/DOSSIER_DE_CONCEPTION.md`), pas une
promesse contractuelle définitive : ils doivent être révisés à la lumière des données
réellement observées une fois le service en charge (25 000 utilisateurs actifs visés à
terme, cf. cahier des charges Projet 6).

## Objectifs internes (SLO)

Toujours plus stricts que le SLA, pour absorber une marge d'erreur avant que le SLA ne soit
techniquement rompu :

- Disponibilité : **99,7 %**
- p95 : **< 250 ms**
- Taux d'erreur : **< 0,5 %**

## KPI et métriques associées

| KPI | Métrique Prometheus (Micrometer / Spring Boot Actuator) | Seuil d'alerte | Fréquence |
|---|---|---|---|
| Availability | `up{job="findme-backend"}` | `up == 0` pendant 1 min | 15 s |
| Response Time (p95) | `http_server_requests_seconds_bucket` | p95 > 300 ms pendant 5 min | 15 s |
| Error Rate | `http_server_requests_seconds_count{status=~"5.."}` | > 1 % pendant 5 min | 15 s |
| Throughput | `http_server_requests_seconds_count` | informatif (pas d'alerte directe) | 15 s |
| Resource Utilization | `process_cpu_usage`, `jvm_memory_used_bytes` | CPU > 80 % pendant 5 min | 15 s |

Le **Throughput** seul ne déclenche jamais d'alerte : c'est une métrique de contexte, pas de
conformité. C'est sa combinaison avec la latence ou le taux d'erreur qui devient un signal
réel (charge élevée + dégradation = incident probable, charge élevée seule = simple pic
d'usage).

Une fréquence de collecte de 15 secondes est un compromis raisonnable pour une application
de cette taille : descendre à 1 seconde multiplierait le volume de séries temporelles
stockées par Prometheus sans gain proportionnel de réactivité pour ce cas d'usage.

## Matrice complète SLA → SLO → KPI → Alerte

| SLA | SLO | KPI | Seuil d'alerte | Alerte | Sévérité | Action attendue |
|---|---|---|---|---|---|---|
| Disponibilité 99,5 %/mois | 99,7 % | Availability | `up == 0` pendant 1 min | `ServiceDown` | critique | Investiguer immédiatement, restaurer le service |
| p95 < 300 ms | p95 < 250 ms | Response Time | p95 > 300 ms pendant 5 min | `HighLatency` | critique | Vérifier CPU/mémoire, identifier l'endpoint concerné |
| Taux d'erreur < 1 % | < 0,5 % | Error Rate | > 1 % pendant 5 min | `ErrorRateHigh` | critique | Consulter les logs applicatifs, identifier la cause |
| Absorption de charge | Pas de dégradation sous charge normale | Throughput | informatif | — | — | Corréler avec latence/erreurs en cas de pic |
| Ressources maîtrisées | CPU < 80 % | Resource Utilization | > 80 % pendant 5 min | `HighCpuUsage` | warning | Surveiller, envisager un dimensionnement si récurrent |

Cette matrice est la source de vérité pour les règles d'alerte Prometheus
(`infra/prometheus/rules.yml`) et pour l'organisation du dashboard Grafana
(`infra/grafana/provisioning/dashboards/findme-sla.json`) : chaque panneau et chaque règle
doit pouvoir être relié à une ligne précise de ce tableau.

## Endpoints couverts

Les métriques `http_server_requests_seconds_*` sont collectées automatiquement par Spring
Boot Actuator/Micrometer pour tous les endpoints exposés par le backend, notamment :

- **Authentification** : `POST /api/auth/signup`, `/signin`, `/refresh`, `/logout`,
  `/forgot-password`, `/reset-password`, `GET/PUT /api/users/me`.
- **Adresses** (cœur métier, cible de l'exercice de simulation de panne — voir CHAP 4) :
  `GET/POST /api/addresses`, `GET/PUT/DELETE /api/addresses/{id}`,
  `POST /api/addresses/{id}/photo`, `GET /api/addresses/{id}/export`.
- **Administration/Support** : `GET /api/admin/users`, `/api/admin/addresses`,
  `POST /api/support`, `GET/PATCH /api/admin/support/**`.

Le taux d'erreur et la latence p95 du SLA sont calculés **tous endpoints confondus**
(`job="findme-backend"`), conformément à la métrique Prometheus par défaut. Une
décomposition par endpoint (label `uri`) est disponible dans Grafana pour le diagnostic,
sans faire partie du SLA global.

## Points d'attention

- Un SLA sans seuil chiffré n'en est pas un : chaque ligne de ce document est mesurable
  directement par une requête PromQL.
- La disponibilité et la latence sont deux dimensions **indépendantes** du SLA : une
  dégradation de performance sans interruption de service viole le SLO de latence sans
  nécessairement violer le SLA de disponibilité (voir `infra/prometheus/rules.yml`, alertes
  `ServiceDown` et `HighLatency` distinctes).
- Le pourcentage p95 est préféré à une moyenne : une latence moyenne basse peut masquer une
  minorité de requêtes très dégradées (notamment l'upload de photo, plus lent par nature que
  les endpoints CRUD simples).
- L'endpoint `/actuator/prometheus` n'est jamais exposé publiquement sans restriction
  réseau : dans `infra/`, seul le réseau Docker interne y accède (voir
  `infra/prometheus/prometheus.yml`).
