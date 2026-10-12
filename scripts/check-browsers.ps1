$paths = @(
    "C:\Program Files\Google\Chrome\Application\chrome.exe",
    "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe",
    "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
    "C:\Program Files\Microsoft\Edge\Application\msedge.exe",
    "C:\Program Files\Mozilla Firefox\firefox.exe"
)

foreach ($p in $paths) {
    if (Test-Path $p) {
        Write-Host "FOUND: $p"
    } else {
        Write-Host "NOT FOUND: $p"
    }
}
