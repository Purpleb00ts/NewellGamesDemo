#!/bin/sh
set -e

# Wait for notification service
echo "Waiting for notification-service on port 8081..."
while ! nc -z notification-service 8081; do
  sleep 1
done

# Wait for database
echo "Waiting for db on port 3306..."
while ! nc -z db 3306; do
  sleep 1
done

echo "All dependencies are up! Starting app..."
exec "$@"