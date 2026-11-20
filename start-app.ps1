#!/usr/bin/env pwsh
# Start RailFlow Spring Boot application
# Make sure start-infra.ps1 has been run first

$env:JAVA_HOME = "C:\Users\gaura\.jdks\ms-21.0.12.1"
$env:PATH = "C:\Users\gaura\tools\apache-maven-3.9.6\bin;" + $env:PATH

Set-Location $PSScriptRoot

Write-Host "Starting RailFlow Spring Boot application..." -ForegroundColor Cyan
Write-Host "API will be available at: http://localhost:8080" -ForegroundColor Green
Write-Host "Swagger UI:               http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host ""

# Skip tests, run with dev profile
mvn spring-boot:run -DskipTests
