#!/usr/bin/env bash
# =============================================================================
# health-check.sh - Application Health Check Script (Linux/WSL)
# Public Parking Dashboard
# Student: Sajiya Shaikh | Roll: 24102A2005
# =============================================================================

set -euo pipefail

APP_URL="${1:-http://localhost:8080}"
MAX_RETRIES="${2:-10}"
DELAY="${3:-5}"

HEALTH_URL="${APP_URL}/api/v1/health"

echo ""
echo "══════════════════════════════════════════════════"
echo "  ParkWatch Application Health Check"
echo "══════════════════════════════════════════════════"
echo "  Target: $HEALTH_URL"
echo "  Max Retries: $MAX_RETRIES | Delay: ${DELAY}s"
echo ""

attempt=0
healthy=false

while [ $attempt -lt $MAX_RETRIES ]; do
    attempt=$((attempt + 1))
    printf "Attempt [%d/%d] - Checking health..." "$attempt" "$MAX_RETRIES"

    HTTP_STATUS=$(curl -s -o /tmp/health_response.json -w "%{http_code}" \
                  --connect-timeout 5 --max-time 10 "$HEALTH_URL" || echo "000")

    if [ "$HTTP_STATUS" = "200" ]; then
        echo " ✅ UP"
        echo ""
        echo "── API Health Response ──────────────────────────"
        cat /tmp/health_response.json | python3 -m json.tool 2>/dev/null || cat /tmp/health_response.json
        echo ""
        healthy=true
        break
    else
        echo " ❌ FAILED (HTTP: $HTTP_STATUS)"
        if [ $attempt -lt $MAX_RETRIES ]; then
            echo "  Retrying in ${DELAY}s..."
            sleep "$DELAY"
        fi
    fi
done

echo ""
if [ "$healthy" = "true" ]; then
    echo "✅ Health check PASSED."
    echo "  Application: $APP_URL"
    exit 0
else
    echo "❌ Health check FAILED after $MAX_RETRIES attempts."
    echo ""
    echo "Troubleshooting:"
    echo "  1. Is the app running? Check: docker ps"
    echo "  2. Start app: java -jar target/public-parking-dashboard-1.0.0.jar"
    echo "  3. Logs: docker logs public-parking-dashboard-container"
    exit 1
fi
