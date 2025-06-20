#!/bin/sh

# Запускаем Consul в фоне
consul agent -dev -client=0.0.0.0 &

# Сохраняем PID
CONSUL_PID=$!

# Ждём пока поднимется
echo "Waiting for Consul to be available..."
until curl -s http://localhost:8500/v1/status/leader | grep -q '"'; do
  sleep 1
done

echo "Consul is up. Uploading KV data..."

# Загружаем YAML как строки
consul kv put config/accounts-service/data "$(cat /data/accounts.yaml)"
consul kv put config/blocker-service/data "$(cat /data/blocker.yaml)"
consul kv put config/cash-service/data "$(cat /data/cash.yaml)"
consul kv put config/exchange-generator-service/data "$(cat /data/exchgen.yaml)"
consul kv put config/exchange-service/data "$(cat /data/exchange.yaml)"
consul kv put config/front-ui/data "$(cat /data/front-ui.yaml)"
consul kv put config/notification-service/data "$(cat /data/notification.yaml)"
consul kv put config/transfer-service/data "$(cat /data/transfer.yaml)"


echo "Done. Consul KV loaded."

# Не выходим, а держим консуль живым
wait $CONSUL_PID