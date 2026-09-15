variable "app_image" {
  description = "Image Docker de findme-backend (publiée sur GHCR par la CD, voir .github/workflows/cd.yml)"
  type        = string
  default     = "ghcr.io/alexdibril237/findme_backend:latest"
}

variable "app_port" {
  description = "Port exposé sur l'hôte pour findme-backend"
  type        = number
  default     = 8080
}

variable "network_name" {
  description = "Nom du réseau Docker partagé par l'application et la stack de monitoring"
  type        = string
  default     = "findme-monitoring-network"
}

variable "db_name" {
  type    = string
  default = "findme_db"
}

variable "db_user" {
  type    = string
  default = "findme_user"
}

variable "db_password" {
  description = "Mot de passe PostgreSQL local (stack de monitoring uniquement, jamais utilisé en production)"
  type        = string
  default     = "findme_pass"
  sensitive   = true
}

variable "jwt_secret" {
  description = "Secret JWT (32+ caractères aléatoires) pour l'environnement de monitoring local"
  type        = string
  sensitive   = true
}

variable "spring_profiles_active" {
  description = "Profil Spring actif ; 'training' expose l'endpoint de simulation de panne (CHAP 4)"
  type        = string
  default     = "training"
}

variable "grafana_admin_password" {
  description = "Mot de passe admin Grafana local"
  type        = string
  default     = "admin"
  sensitive   = true
}
