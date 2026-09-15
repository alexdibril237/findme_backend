# Infrastructure as Code (Module 7 Partie 2, CHAP 2-4) : provisionne, en conteneurs Docker
# locaux, une instance de findme-backend + PostgreSQL, instrumentée et surveillée par
# Prometheus/Grafana/Alertmanager. Distincte de docker-compose.yml (qui reste l'outil de
# développement local courant) : ce dossier est le livrable pédagogique "IaC" de la formation.

resource "docker_network" "findme_net" {
  name = var.network_name
}

# --- Base de données -------------------------------------------------------

resource "docker_volume" "findme_db_data" {
  name = "findme-monitoring-db-data"
}

resource "docker_image" "findme_db" {
  name = "postgres:16"
}

resource "docker_container" "findme_db" {
  name  = "findme-db-monitoring"
  image = docker_image.findme_db.image_id

  networks_advanced {
    name = docker_network.findme_net.name
  }

  env = [
    "POSTGRES_DB=${var.db_name}",
    "POSTGRES_USER=${var.db_user}",
    "POSTGRES_PASSWORD=${var.db_password}",
  ]

  volumes {
    volume_name    = docker_volume.findme_db_data.name
    container_path = "/var/lib/postgresql/data"
  }

  restart = "unless-stopped"
}

# --- Application -------------------------------------------------------------
# Le conteneur redémarre automatiquement (restart = unless-stopped) jusqu'à ce que
# PostgreSQL soit prêt : la CI/CD gère déjà la même contrainte via un healthcheck Docker
# Compose (voir docker-compose.yml) ; ce dossier reste volontairement simple (pas de
# dépendance de santé Terraform, non supportée nativement par le provider docker).

resource "docker_image" "findme_api" {
  name = var.app_image
}

resource "docker_container" "findme_api" {
  name  = "findme-backend"
  image = docker_image.findme_api.image_id

  depends_on = [docker_container.findme_db]

  networks_advanced {
    name = docker_network.findme_net.name
  }

  env = [
    "DB_HOST=findme-db-monitoring",
    "DB_PORT=5432",
    "DB_NAME=${var.db_name}",
    "DB_USER=${var.db_user}",
    "DB_PASSWORD=${var.db_password}",
    "JWT_SECRET=${var.jwt_secret}",
    "SPRING_PROFILES_ACTIVE=${var.spring_profiles_active}",
    "STORAGE_ROOT=/data/photos",
    "STORAGE_PUBLIC_BASE_URL=http://localhost:${var.app_port}/files",
  ]

  ports {
    internal = 8080
    external = var.app_port
  }

  restart = "unless-stopped"
}

# --- Prometheus ----------------------------------------------------------------

resource "docker_image" "prometheus" {
  name = "prom/prometheus:v2.53.0"
}

resource "docker_container" "prometheus" {
  name  = "prometheus"
  image = docker_image.prometheus.image_id

  depends_on = [docker_container.findme_api]

  networks_advanced {
    name = docker_network.findme_net.name
  }

  ports {
    internal = 9090
    external = 9090
  }

  volumes {
    host_path      = "${path.cwd}/prometheus/prometheus.yml"
    container_path = "/etc/prometheus/prometheus.yml"
    read_only      = true
  }

  volumes {
    host_path      = "${path.cwd}/prometheus/rules.yml"
    container_path = "/etc/prometheus/rules.yml"
    read_only      = true
  }

  restart = "unless-stopped"
}

# --- Alertmanager --------------------------------------------------------------

resource "docker_image" "alertmanager" {
  name = "prom/alertmanager:v0.27.0"
}

resource "docker_container" "alertmanager" {
  name  = "alertmanager"
  image = docker_image.alertmanager.image_id

  networks_advanced {
    name = docker_network.findme_net.name
  }

  ports {
    internal = 9093
    external = 9093
  }

  volumes {
    host_path      = "${path.cwd}/alertmanager/alertmanager.yml"
    container_path = "/etc/alertmanager/alertmanager.yml"
    read_only      = true
  }

  restart = "unless-stopped"
}

# --- Grafana ---------------------------------------------------------------------

resource "docker_image" "grafana" {
  name = "grafana/grafana:11.1.0"
}

resource "docker_container" "grafana" {
  name  = "grafana"
  image = docker_image.grafana.image_id

  depends_on = [docker_container.prometheus]

  networks_advanced {
    name = docker_network.findme_net.name
  }

  ports {
    internal = 3000
    external = 3000
  }

  env = [
    "GF_SECURITY_ADMIN_PASSWORD=${var.grafana_admin_password}",
  ]

  volumes {
    host_path      = "${path.cwd}/grafana/provisioning"
    container_path = "/etc/grafana/provisioning"
    read_only      = true
  }

  restart = "unless-stopped"
}
