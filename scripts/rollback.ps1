# =============================================================================
# rollback.ps1 - Docker Container Rollback Script (Windows PowerShell)
# Public Parking Dashboard
# Student: Sajiya Shaikh | Roll: 24102A2005
#
# Usage:
#   .\scripts\rollback.ps1 -RollbackTag "1.0.0-previous"
#   .\scripts\rollback.ps1 -RollbackTag "1.0.0-previous" -AppPort 8081
# =============================================================================

param(
    [string]$RollbackTag = "1.0.0-previous",
    [string]$AppName = "public-parking-dashboard",
    [string]$ContainerName = "public-parking-dashboard-container",
    [int]$AppPort = 8081,
    [string]$VolumeName = "parking-data"
)

$imageName = "${AppName}:${RollbackTag}"

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  ParkWatch Container Rollback Procedure" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "Target Image : $imageName" -ForegroundColor Yellow
Write-Host "Container    : $ContainerName" -ForegroundColor Yellow
Write-Host "Host Port    : $AppPort" -ForegroundColor Yellow
Write-Host ""

# Check if image exists
$imageCheck = docker images -q $imageName
if (-not $imageCheck) {
    Write-Host "[ERROR] Image '$imageName' not found locally." -ForegroundColor Red
    Write-Host "Available images:"
    docker images $AppName
    exit 1
}

# Step 1: Record current state
Write-Host "-- Step 1: Inspecting current container state" -ForegroundColor Cyan
docker ps --filter "name=$ContainerName"

# Step 2: Stop current container
Write-Host ""
Write-Host "-- Step 2: Stopping current container" -ForegroundColor Cyan
docker stop $ContainerName 2>$null | Out-Null
Write-Host "  Container stopped."

# Step 3: Remove current container
Write-Host ""
Write-Host "-- Step 3: Removing current container" -ForegroundColor Cyan
docker rm $ContainerName 2>$null | Out-Null
Write-Host "  Container removed."

# Step 4: Run rollback container
Write-Host ""
Write-Host "-- Step 4: Starting rollback container ($imageName)" -ForegroundColor Cyan
$runOutput = docker run -d `
    --name $ContainerName `
    -p "${AppPort}:8080" `
    -v "${VolumeName}:/app/data" `
    --restart unless-stopped `
    $imageName

Write-Host "  Container started: $runOutput"

# Step 5: Health Check
Write-Host ""
Write-Host "-- Step 5: Verifying health on port $AppPort" -ForegroundColor Cyan
Start-Sleep -Seconds 10

$healthy = $false
$healthUrl = "http://localhost:${AppPort}/api/v1/health"
for ($attempt = 1; $attempt -le 6; $attempt++) {
    Write-Host "  Attempt [$attempt/6] - Checking $healthUrl... " -NoNewline
    try {
        $res = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 5 -ErrorAction Stop
        if ($res.status -eq "UP") {
            Write-Host "[OK] UP" -ForegroundColor Green
            $healthy = $true
            break
        }
    } catch {
        Write-Host "waiting..." -ForegroundColor Yellow
    }
    Start-Sleep -Seconds 5
}

Write-Host ""
if ($healthy) {
    Write-Host "==================================================" -ForegroundColor Green
    Write-Host "  [SUCCESS] Rollback COMPLETE: $imageName is healthy!" -ForegroundColor Green
    Write-Host "  URL: http://localhost:$AppPort" -ForegroundColor Green
    Write-Host "==================================================" -ForegroundColor Green
    exit 0
} else {
    Write-Host "[ERROR] Container did not become healthy in time." -ForegroundColor Red
    docker logs $ContainerName --tail 20
    exit 1
}
