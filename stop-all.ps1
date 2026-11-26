# VOLT Platform - Stop All Services
Write-Host "=========================================="
Write-Host "Deteniendo todos los servicios VOLT"
Write-Host "=========================================="
Write-Host ""

Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host "Todos los servicios detenidos."
Write-Host ""
Read-Host "Presiona Enter para cerrar"