# Starts LocalStack only. Postgres + Redis must already be running locally.
$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

Write-Host "==> Starting LocalStack (offline AWS emulator)..." -ForegroundColor Cyan
docker compose up -d

Write-Host "==> Waiting for LocalStack..." -ForegroundColor Cyan
$maxAttempts = 60
for ($i = 1; $i -le $maxAttempts; $i++) {
    $localstack = docker inspect --format='{{.State.Health.Status}}' msp-localstack 2>$null
    if ($localstack -eq "healthy") {
        Write-Host "LocalStack is ready." -ForegroundColor Green
        & (Join-Path $PSScriptRoot "init-localstack.ps1")
        exit 0
    }
    Start-Sleep -Seconds 2
}

Write-Host "Timed out. Check: docker logs msp-localstack" -ForegroundColor Red
exit 1
