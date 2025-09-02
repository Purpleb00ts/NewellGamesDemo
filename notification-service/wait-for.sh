#!/bin/sh
set -e

echo "Waiting for DB on port 3306..."
while ! nc -z db 3306; do
  sleep 1
done

echo "All dependencies ready. Starting notification-service..."
exec "$@"