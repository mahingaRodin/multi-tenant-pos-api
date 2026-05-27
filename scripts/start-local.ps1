# Starts LocalStack, then runs the Spring Boot app with the local profile.
# Prerequisites: PostgreSQL on localhost:5432 and Redis on localhost:6379 (see .env).
$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

Write-Host "==> Starting LocalStack..." -ForegroundColor Cyan
docker compose up -d

Write-Host "==> Waiting for LocalStack..." -ForegroundColor Cyan
$maxAttempts = 60
$ready = $false
for ($i = 1; $i -le $maxAttempts; $i++) {
    $localstack = docker inspect --format='{{.State.Health.Status}}' msp-localstack 2>$null
    if ($localstack -eq "healthy") {
        Write-Host "LocalStack is ready." -ForegroundColor Green
        $ready = $true
        break
    }
    Start-Sleep -Seconds 2
}
if (-not $ready) {
    Write-Host "Timed out waiting for LocalStack. Check: docker logs msp-localstack" -ForegroundColor Red
    exit 1
}

Write-Host "==> Initializing AWS resources in LocalStack..." -ForegroundColor Cyan
& (Join-Path $PSScriptRoot "init-localstack.ps1")

# Load .env if present (optional overrides)
$envFile = Join-Path $ProjectRoot ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            Set-Item -Path "Env:$name" -Value $value
        }
    }
}

Write-Host "==> Starting Spring Boot (profile: local) on http://localhost:5000" -ForegroundColor Cyan
Write-Host "    Swagger UI: http://localhost:5000/swagger-ui.html" -ForegroundColor Cyan
& .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
