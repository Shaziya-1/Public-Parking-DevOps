# =============================================================================
# health-check.ps1 - Application Health Check Script (Windows PowerShell)
# Public Parking Dashboard
# Student: Sajiya Shaikh | Roll: 24102A2005
#
# Usage:
#   .\scripts\health-check.ps1
#   .\scripts\health-check.ps1 -AppUrl "http://localhost:8080" -MaxRetries 5
# =============================================================================

param(
    [string]$AppUrl = "http://localhost:8080",
    [int]$MaxRetries = 10,
    [int]$RetryDelaySeconds = 5
)

$healthUrl = "$AppUrl/api/v1/health"
$actuatorUrl = "$AppUrl/actuator/health"

Write-Host ""
Write-Host "╔══════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  ParkWatch - Application Health Check            ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "Target URL: $healthUrl" -ForegroundColor Yellow
Write-Host "Max Retries: $MaxRetries | Delay: ${RetryDelaySeconds}s"
Write-Host ""

$attempt = 0
$healthy = $false

while ($attempt -lt $MaxRetries -and -not $healthy) {
    $attempt++
    Write-Host "Attempt [$attempt/$MaxRetries] - Checking health..." -NoNewline

    try {
        $response = Invoke-WebRequest -Uri $healthUrl -TimeoutSec 10 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            $body = $response.Content | ConvertFrom-Json
            Write-Host " ✅ UP" -ForegroundColor Green
            Write-Host ""
            Write-Host "── API Health ────────────────────────────────────" -ForegroundColor Green
            Write-Host "  Status  : $($body.status)" -ForegroundColor Green
            Write-Host "  Service : $($body.service)"
            Write-Host "  Version : $($body.version)"

            # Also check Spring Actuator
            try {
                $actuator = Invoke-WebRequest -Uri $actuatorUrl -TimeoutSec 10 -ErrorAction Stop
                $actuatorBody = $actuator.Content | ConvertFrom-Json
                Write-Host ""
                Write-Host "── Spring Actuator ───────────────────────────────" -ForegroundColor Green
                Write-Host "  Health  : $($actuatorBody.status)" -ForegroundColor Green
            } catch {
                Write-Host "  Actuator endpoint not reachable (may be restricted)." -ForegroundColor Yellow
            }

            $healthy = $true
        }
    } catch {
        Write-Host " ❌ FAILED" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red

        if ($attempt -lt $MaxRetries) {
            Write-Host "  Retrying in ${RetryDelaySeconds}s..." -ForegroundColor Yellow
            Start-Sleep -Seconds $RetryDelaySeconds
        }
    }
}

Write-Host ""
if ($healthy) {
    Write-Host "✅ Health check PASSED. Application is running at $AppUrl" -ForegroundColor Green
    Write-Host ""
    Write-Host "Quick Links:" -ForegroundColor Cyan
    Write-Host "  Dashboard : $AppUrl/"
    Write-Host "  API Health: $healthUrl"
    Write-Host "  Alerts    : $AppUrl/alerts"
    Write-Host "  H2 Console: $AppUrl/h2-console"
    exit 0
} else {
    Write-Host "❌ Health check FAILED after $MaxRetries attempts." -ForegroundColor Red
    Write-Host "   Application may not be running or is not responding." -ForegroundColor Red
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "  1. Check if the application is running:"
    Write-Host "     mvn spring-boot:run"
    Write-Host "     OR: java -jar target\public-parking-dashboard-1.0.0.jar"
    Write-Host "  2. Check Docker container: docker ps"
    Write-Host "  3. Check logs: docker logs public-parking-dashboard-container"
    exit 1
}
