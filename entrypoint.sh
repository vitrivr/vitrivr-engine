#!/bin/bash
set -e

echo "[entrypoint] Starting vitrivr-engine-server in config mode..."
/app/vitrivr/vitrivr-engine-server-0.1.9/bin/vitrivr-engine-server /app/configs/config-schema.json --mode config
