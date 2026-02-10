resource "google_monitoring_dashboard" "server" {
  project        = var.project_id
  dashboard_json = file("${path.module}/dashboards/finders-server-dashboard.json")
}
