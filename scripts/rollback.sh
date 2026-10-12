#!/usr/bin/env bash
# =============================================================================
# rollback.sh - Docker Container Rollback Script (Linux/WSL)
# Public Parking Dashboard
# Student: Sajiya Shaikh | Roll: 24102A2005
#
# Usage: ./scripts/rollback.sh <rollback-image-tag>
# Example: ./scripts/rollback.sh 1.0.0-15
# =============================================================================

set -euo pipefail

ROLLBACK_TAG="${1:-}"
APP_NAME="public-parking-dashboard"
CONTAINER_NAME="${APP_NAME}-container"
APP_PORT="${APP_PORT:-8080}"
VOLUME_NAME="parking-data"

echo "════════════════════════════════════════════════════"
echo "  ParkWatch Container Rollback Procedure"
echo "════════════════════════════════════════════════════"

if [ -z "$ROLLBACK_TAG" ]; then
    echo "Usage: $0 <rollback-image-tag>"
    echo "Example: $0 1.0.0-15"
    echo ""
    echo "Available local tags:"
    docker images "${APP_NAME}" --format "  {{.Tag}}\t{{.CreatedAt}}" || true
    exit 1
fi

IMAGE_NAME="${APP_NAME}:${ROLLBACK_TAG}"

echo "  Rollback target: $IMAGE_NAME"
echo ""

# Verify the rollback image exists
if ! docker image inspect "$IMAGE_NAME" &>/dev/null; then
    echo "❌ ERROR: Image '$IMAGE_NAME' not found locally."
    echo "   Available images:"
    docker images "${APP_NAME}" --format "    {{.Tag}}" || true
    exit 1
fi

# Save current container state for reference
echo "── Step 1: Recording current container state"
docker ps --filter "name=${CONTAINER_NAME}" --format "  {{.Image}}\t{{.Status}}" || true

# Stop current container
echo ""
echo "── Step 2: Stopping current container"
docker stop "${CONTAINER_NAME}" 2>/dev/null && echo "  Container stopped." || echo "  Container was not running."

# Remove current container
echo ""
echo "── Step 3: Removing current container"
docker rm "${CONTAINER_NAME}" 2>/dev/null && echo "  Container removed." || echo "  Container not found."

# Start rollback container
echo ""
echo "── Step 4: Starting rollback container (${IMAGE_NAME})"
docker run -d \
    --name "${CONTAINER_NAME}" \
    -p "${APP_PORT}:8080" \
    -v "${VOLUME_NAME}:/app/data" \
    --restart unless-stopped \
    "${IMAGE_NAME}"

echo "  Container started."

# Health check
echo ""
echo "── Step 5: Health check (30s timeout)"
sleep 15

HEALTH_URL="http://localhost:${APP_PORT}/api/v1/health"
MAX_WAIT=6
attempt=0
while [ $attempt -lt $MAX_WAIT ]; do
    attempt=$((attempt + 1))
    printf "  Attempt [%d/%d]..." "$attempt" "$MAX_WAIT"
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 "$HEALTH_URL" || echo "000")
    if [ "$STATUS" = "200" ]; then
        echo " ✅ HEALTHY"
        echo ""
        echo "════════════════════════════════════════════════════"
        echo "  ✅ Rollback COMPLETE: $IMAGE_NAME is running"
        echo "  URL: http://localhost:${APP_PORT}"
        echo "════════════════════════════════════════════════════"
        exit 0
    else
        echo " waiting... (HTTP $STATUS)"
        sleep 5
    fi
done

echo ""
echo "❌ Rollback container is NOT healthy. Manual investigation required."
echo "   Logs:"
docker logs "${CONTAINER_NAME}" --tail 30 || true
exit 1
