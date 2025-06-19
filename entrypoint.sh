#!/bin/bash
set -e

echo "[entrypoint] Starting vitrivr-engine-server in config mode..."

ENGINE_DIR=$(ls -d /app/vitrivr/vitrivr-engine-server-* | sort -V | tail -n 1)

"$ENGINE_DIR/bin/vitrivr-engine-server" /app/configs/config-schema.json --mode config
