
$env:MONGODB_URI = "mongodb+srv://duvanjavier159_db_user:DH22092005@patinetas.un4fxyk.mongodb.net/"
$env:VOLT_ADMIN_EMAIL="uac@uac.com"
$env:VOLT_ADMIN_PASSWORD="12345678"
$services = @(
    "services/ms-usuarios-auth",
    "services/ms-catalogo-flota",
    "services/ms-reservas-pagos",
    "services/ms-operaciones-incidencias",
    "services/ms-comunicacion",
    "gateway"
)

Start-Process -FilePath ".\mvnw.cmd" -ArgumentList "-pl", "shared", "-DskipTests", "install" -WindowStyle Hidden
Start-Sleep -Seconds 5

foreach ($service in $services) {
    Start-Process -FilePath ".\mvnw.cmd" -ArgumentList "-pl", $service, "spring-boot:run" -WindowStyle Hidden
}

Write-Host "Started: $($services -join ', ') - GW on http://localhost:8081"
