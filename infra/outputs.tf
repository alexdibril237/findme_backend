output "app_url" {
  description = "URL locale de findme-backend"
  value       = "http://localhost:${var.app_port}"
}

output "prometheus_url" {
  value = "http://localhost:9090"
}

output "alertmanager_url" {
  value = "http://localhost:9093"
}

output "grafana_url" {
  value = "http://localhost:3000"
}

output "network_id" {
  value = docker_network.findme_net.id
}
