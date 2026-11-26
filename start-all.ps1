# VOLT Platform - Start All Services
$projectRoot = "C:\Users\duvan\OneDrive\DOCUME~1\VISUAL~1\PROYEC~1\PROYEC~1"

Write-Host "=========================================="
Write-Host "VOLT Platform - Iniciando todos los servicios"
Write-Host "=========================================="
Write-Host ""

Write-Host "[1/6] Gateway (puerto 8081)..."
$process = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "mvnw.cmd spring-boot:run -pl gateway" -WorkingDirectory $projectRoot -WindowStyle Normal -PassThru
Start-Sleep -Seconds 15

Write-Host "[2/6] ms-usuarios-auth (puerto 8082)..."
$process = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "mvnw.cmd spring-boot:run -pl services\ms-usuarios-auth" -WorkingDirectory $projectRoot -WindowStyle Normal -PassThru
Start-Sleep -Seconds 10

Write-Host "[3/6] ms-catalogo-flota (puerto 8083)..."
$process = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "mvnw.cmd spring-boot:run -pl services\ms-catalogo-flota" -WorkingDirectory $projectRoot -WindowStyle Normal -PassThru
Start-Sleep -Seconds 10

Write-Host "[4/6] ms-reservas-pagos (puerto 8084)..."
$process = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "mvnw.cmd spring-boot:run -pl services\ms-reservas-pagos" -WorkingDirectory $projectRoot -WindowStyle Normal -PassThru
Start-Sleep -Seconds 10

Write-Host "[5/6] ms-operaciones-incidencias (puerto 8085)..."
$process = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "mvnw.cmd spring-boot:run -pl services\ms-operaciones-incidencias" -WorkingDirectory $projectRoot -WindowStyle Normal -PassThru
Start-Sleep -Seconds 10

Write-Host "[6/6] ms-comunicacion (puerto 8086)..."
$process = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "mvnw.cmd spring-boot:run -pl services\ms-comunicacion" -WorkingDirectory $projectRoot -WindowStyle Normal -PassThru
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "=========================================="
Write-Host "Todos los servicios iniciados!"
Write-Host "=========================================="
Write-Host ""
Write-Host "Gateway:    http://localhost:8081"
Write-Host "Usuarios:   http://localhost:8082"
Write-Host "Catalogo:   http://localhost:8083"
Write-Host "Reservas:   http://localhost:8084"
Write-Host "Ops:        http://localhost:8085"
Write-Host "Comun:      http://localhost:8086"
Write-Host ""
Write-Host "Para detener todos: Get-Process java | Stop-Process -Force"
Write-Host ""
Read-Host "Presiona Enter para cerrar"