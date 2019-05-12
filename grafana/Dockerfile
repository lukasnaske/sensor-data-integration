FROM grafana/grafana:latest
ADD --chown=grafana:grafana grafana.ini /etc/grafana/grafana.ini
ADD --chown=grafana:grafana ./provisioning /etc/grafana/provisioning
ADD --chown=grafana:grafana ./dashboards /var/lib/grafana/dashboards