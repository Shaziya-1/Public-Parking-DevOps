# =============================================================================
# health-check.ps1 - Application Health Check Script (Windows PowerShell)
# Public Parking Dashboard
# Student: Sajiya Shaikh | Roll: 24102A2005
#
# Usage:
#   .\scripts\health-check.ps1
#   .\scripts\health-check.ps1 -AppUrl "http://localhost:80" -MaxRetries 5
# =============================================================================

param(
    [string]$AppUrl = "http://localhost:80",
    [int]$Port = 0,
    [int]$MaxRetries = 10,
    [int]$RetryDelaySeconds = 3
)

if ($Port -gt 0) {
    $AppUrl = "http://localhost:$Port"
}

$healthUrl = "$AppUrl/api/v1/health"
$actuatorUrl = "$AppUrl/actuator/health"

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  ParkWatch - Application Health Check" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Target URL : $healthUrl" -ForegroundColor Yellow
Write-Host "Actuator   : $actuatorUrl" -ForegroundColor Yellow
Write-Host "Max Retries: $MaxRetries | Delay: ${RetryDelaySeconds}s"
Write-Host ""

$attempt = 0
$healthy = $false

while (($attempt -lt $MaxRetries) -and (-not $healthy)) {
    $attempt++
    Write-Host "Attempt [$attempt/$MaxRetries] - Checking health... " -NoNewline

    try {
        $response = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 10 -Method Get -ErrorAction Stop
        if ($response -and $response.status -eq "UP") {
            Write-Host "[OK] UP" -ForegroundColor Green
            Write-Host ""
            Write-Host "-- API Health Details ----------------------------" -ForegroundColor Green
            Write-Host "  Status  : $($response.status)" -ForegroundColor Green
            Write-Host "  Service : $($response.service)"
            Write-Host "  Version : $($response.version)"

            try {
                $actuator = Invoke-RestMethod -Uri $actuatorUrl -TimeoutSec 10 -Method Get -ErrorAction Stop
                Write-Host ""
                Write-Host "-- Spring Actuator -------------------------------" -ForegroundColor Green
                Write-Host "  Health  : $($actuator.status)" -ForegroundColor Green
                if ($actuator.components) {
                    Write-Host "  Database: $($actuator.components.db.status)"
                    Write-Host "  Disk    : $($actuator.components.diskSpace.status)"
                }
            } catch {
                Write-Host "  Actuator endpoint unreachable or restricted: $($_.Exception.Message)" -ForegroundColor Yellow
            }

            $healthy = $true
            break
        } else {
            Write-Host "[WARN] Unexpected response: $response" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "[FAILED]" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    }

    if (-not $healthy -and ($attempt -lt $MaxRetries)) {
        Write-Host "  Retrying in ${RetryDelaySeconds}s..." -ForegroundColor Yellow
        Start-Sleep -Seconds $RetryDelaySeconds
    }
}

Write-Host ""
if ($healthy) {
    Write-Host "[SUCCESS] Health check PASSED. Application is running at $AppUrl" -ForegroundColor Green
    Write-Host ""
    Write-Host "Quick Links:" -ForegroundColor Cyan
    Write-Host "  Dashboard : $AppUrl/"
    Write-Host "  API Health: $healthUrl"
    Write-Host "  Actuator  : $actuatorUrl"
    exit 0
} else {
    Write-Host "[FAILED] Health check FAILED after $MaxRetries attempts." -ForegroundColor Red
    Write-Host "Application may not be running or is not responding." -ForegroundColor Red
    exit 1
}
