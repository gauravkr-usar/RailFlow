#!/usr/bin/env pwsh
# Start RailFlow React frontend dev server

Set-Location "$PSScriptRoot\frontend"

Write-Host "Starting React frontend..." -ForegroundColor Cyan
Write-Host "Frontend will be available at: http://localhost:5173" -ForegroundColor Green
Write-Host ""

npm run dev
