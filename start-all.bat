@echo off
title VOLT Platform - Start All Services
echo ==========================================
echo VOLT Platform - Iniciando todos los servicios
echo ==========================================
echo.

cd /d C:\Users\duvan\OneDrive\DOCUME~1\VISUAL~1\PROYEC~1\PROYEC~1

echo [1/6] Gateway (puerto 8081)...
start /B cmd /c "mvnw.cmd spring-boot:run -pl gateway" <nul
timeout /t 15 /nobreak >nul

echo [2/6] ms-usuarios-auth (puerto 8082)...
start /B cmd /c "mvnw.cmd spring-boot:run -pl services\ms-usuarios-auth" <nul
timeout /t 10 /nobreak >nul

echo [3/6] ms-catalogo-flota (puerto 8083)...
start /B cmd /c "mvnw.cmd spring-boot:run -pl services\ms-catalogo-flota" <nul
timeout /t 10 /nobreak >nul

echo [4/6] ms-reservas-pagos (puerto 8084)...
start /B cmd /c "mvnw.cmd spring-boot:run -pl services\ms-reservas-pagos" <nul
timeout /t 10 /nobreak >nul

echo [5/6] ms-operaciones-incidencias (puerto 8085)...
start /B cmd /c "mvnw.cmd spring-boot:run -pl services\ms-operaciones-incidencias" <nul
timeout /t 10 /nobreak >nul

echo [6/6] ms-comunicacion (puerto 8086)...
start /B cmd /c "mvnw.cmd spring-boot:run -pl services\ms-comunicacion" <nul
timeout /t 10 /nobreak >nul

echo.
echo ==========================================
echo Todos los servicios iniciados!
echo ==========================================
echo.
echo Gateway:    http://localhost:8081
echo Usuarios:   http://localhost:8082
echo Catalogo:   http://localhost:8083
echo Reservas:   http://localhost:8084
echo Ops:        http://localhost:8085
echo Comun:      http://localhost:8086
echo.
echo Para detener todos: taskkill /F /IM java.exe
pause