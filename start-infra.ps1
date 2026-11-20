#!/usr/bin/env pwsh
# Start RailFlow infrastructure (PostgreSQL, Redis, Kafka, Zookeeper) via Docker Compose
# Run this BEFORE starting the Spring Boot app

Write-Host "Starting RailFlow infrastructure..." -ForegroundColor Cyan

# Fix Docker config permission issue if needed
$dockerConfig = "$env:USERPROFILE\.docker\config.json"
if (-not (Test-Path $dockerConfig)) {
    New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\.docker" | Out-Null
    '{}' | Set-Content $dockerConfig
}

Set-Location $PSScriptRoot

# Start only infra services (not the app itself — we run that via Maven)
docker compose up -d postgres redis zookeeper kafka

Write-Host ""
Write-Host "Waiting for services to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host ""
Write-Host "Infrastructure status:" -ForegroundColor Cyan
docker compose ps

Write-Host ""
Write-Host "Services are ready! Now run: .\start-app.ps1" -ForegroundColor Green
