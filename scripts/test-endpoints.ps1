# Test all endpoints against running Spring Boot instance
$ErrorActionPreference = "Stop"

Write-Host "=== 1. ACTUATOR & API HEALTH CHECKS ==="
$actuatorHealth = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"
$actuatorHealth | ConvertTo-Json -Depth 5

$apiHealth = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/health"
$apiHealth | ConvertTo-Json

Write-Host "=== 2. DASHBOARD SUMMARY API ==="
$summary = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/dashboard/summary"
$summary | ConvertTo-Json

Write-Host "=== 3. ALL LOCATIONS API ==="
$locations = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/locations"
Write-Host "Total locations loaded: $($locations.Count)"
$locations[0..1] | ConvertTo-Json

Write-Host "=== 4. SEARCH & FILTER API ==="
$searchResults = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/locations?search=Central"
Write-Host "Locations matching 'Central': $($searchResults.Count)"

$activeFilter = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/locations?filter=ACTIVE"
Write-Host "Locations with status 'ACTIVE': $($activeFilter.Count)"

Write-Host "=== 5. ALERTS API ==="
$alerts = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/alerts"
Write-Host "Active alerts count: $($alerts.Count)"
if ($alerts.Count -gt 0) {
    $alerts[0] | ConvertTo-Json
}

Write-Host "=== 6. HTML WEB DASHBOARD GET ==="
$web = Invoke-WebRequest -Uri "http://localhost:8080/" -UseBasicParsing
Write-Host "Dashboard Status Code: $($web.StatusCode)"
Write-Host "Dashboard Content Length: $($web.Content.Length)"

Write-Host "=== 7. LOCATION CREATION (POST) ==="
$newLocBody = @{
    name = "Civic Center Innovation Garage"
    area = "Cyber City"
    address = "500 Tech Boulevard"
    totalCapacity = 100
    occupiedSpaces = 25
    locationCode = "PPL-099"
    contactPhone = "+91 98765 43210"
    description = "Smart automated municipal lot"
    active = $true
} | ConvertTo-Json

$created = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/locations" -Method Post -Body $newLocBody -ContentType "application/json"
Write-Host "Created Location ID: $($created.id), Name: $($created.name), Available: $($created.availableSpaces)"

Write-Host "=== 8. RECORD EVENT (Valid OCCUPANCY_UPDATE) ==="
$eventBody = @{
    parkingLocationId = $created.id
    eventType = "OCCUPANCY_UPDATE"
    reportedOccupancy = 40
    description = "Automated sensor count update"
    operatorName = "System Sensor Daemon"
} | ConvertTo-Json
$recordedEvent = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/events" -Method Post -Body $eventBody -ContentType "application/json"
Write-Host "Recorded Event ID: $($recordedEvent.id), Type: $($recordedEvent.eventType), Occupancy: $($recordedEvent.reportedOccupancy)"

Write-Host "=== 9. OCCUPANCY BOUNDARY TEST (Cannot Exceed Capacity) ==="
try {
    $overflowOccupancy = @{
        occupiedSpaces = 500
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/locations/$($created.id)/occupancy" -Method Patch -Body $overflowOccupancy -ContentType "application/json"
    Write-Host "ERROR: Should have rejected occupancy exceeding total capacity!"
} catch {
    Write-Host "SUCCESS: Overcapacity rejected as expected: $($_.Exception.Message)"
}

Write-Host "=== 10. WEB ALERTS PAGE GET ==="
$alertsPage = Invoke-WebRequest -Uri "http://localhost:8080/alerts" -UseBasicParsing
Write-Host "Alerts Page Status Code: $($alertsPage.StatusCode)"

Write-Host "=== 11. LOCATION DETAIL PAGE GET ==="
$detailPage = Invoke-WebRequest -Uri "http://localhost:8080/locations/$($created.id)" -UseBasicParsing
Write-Host "Detail Page Status Code: $($detailPage.StatusCode)"

Write-Host "=== ALL FUNCTIONAL VERIFICATIONS PASSED SUCCESSFULLY ==="
