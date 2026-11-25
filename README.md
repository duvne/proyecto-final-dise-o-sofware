# VOLT Backend

Backend completo para el marketplace P2P de alquiler de patinetas eléctricas. Incluye los cinco microservicios, el Gateway, una arquitectura de catálogo y reservas basada en MongoDB, una franja de disponibilidad transaccional en PostgreSQL, operaciones, comunicación, seguridad con JWT/BCrypt y auditoría.

## Arquitectura

```text
Cliente web
    |
API Gateway (8081)
    |-- MS-UsuariosAuth (8082)
    |-- MS-CatalogoFlota (8083)
    |-- MS-ReservasPagos (8084)
    |-- MS-OperacionesIncidencias (8085)
    |-- MS-Comunicacion (8086)
              |
       MongoDB por servicio
              |
       PostgreSQL para franjas de reserva
```

## Requisitos

- JDK 25
- Maven Wrapper incluido (`mvnw.cmd`)
- MongoDB en `mongodb+srv://duvanjavier159_db_user:<db_password>@patinetas.un4fxyk.mongodb.net/` o configurar `MONGODB_URI` (reemplaza `<db_password>`)
- PostgreSQL en `localhost:5432` para MS-ReservasPagos

## Iniciar el backend

1. Inicia MongoDB y PostgreSQL.
2. Crea la base de datos de reservas si es necesario:

```powershell
psql -U postgres -c "CREATE USER volt WITH PASSWORD 'voltdev';"
psql -U postgres -c "CREATE DATABASE volt_reservas OWNER volt;"
```

3. Abre una terminal por servicio:

```powershell
.\mvnw.cmd -pl services/ms-usuarios-auth spring-boot:run
.\mvnw.cmd -pl services/ms-catalogo-flota spring-boot:run
.\mvnw.cmd -pl services/ms-reservas-pagos spring-boot:run
.\mvnw.cmd -pl services/ms-operaciones-incidencias spring-boot:run
.\mvnw.cmd -pl services/ms-comunicacion spring-boot:run
.\mvnw.cmd -pl gateway spring-boot:run
```

El Gateway está en `http://localhost:8081` y cada servicio tiene `/actuator/health`.

## Verificación

```powershell
.\mvnw.cmd -B verify
```

## Variables de entorno (`-Dspring-boot.run.arguments` o variables globales)

```dotenv
SERVER_PORT=8081
MONGODB_URI=mongodb+srv://duvanjavier159_db_user:<db_password>@patinetas.un4fxyk.mongodb.net/
RESERVAS_DB_URL=jdbc:postgresql://localhost:5432/volt_reservas
RESERVAS_DB_USERNAME=volt
RESERVAS_DB_PASSWORD=voltdev
JWT_SECRET=volt-dev-secret-change-me-32-bytes-minimum-2026
VOLT_INTERNAL_KEY=volt-internal-dev-key
PAGOS_PROVEEDOR=SIMULADA
MAIL_SMTP_HOST=localhost
```

⚠️ **Importante:** cambia `JWT_SECRET` y `VOLT_INTERNAL_KEY` en producción y no compartas credenciales. Los valores de desarrollo están solo en código/documentación para pruebas locales.

## Rutas del Gateway

| Ruta | Servicio |
|---|---|
| `/api/auth/**` | MS-UsuariosAuth |
| `/api/catalogo/**` | MS-CatalogoFlota |
| `/api/reservas/**`, `/api/pagos/**` | MS-ReservasPagos |
| `/api/operaciones/**`, `/api/ops/**`, `/api/incidencias/**` | MS-OperacionesIncidencias |
| `/api/comunicacion/**` | MS-Comunicacion |

## Seguridad básica

- No guardes el `MONGODB_URI` real ni el `JWT_SECRET` en repositorios: usa `.env` en desarrollo y `.env.example` solo para plantilla.
- Reemplaza `<db_password>` por una contraseña fuerte y distinta por entorno.
- Reemplaza `JWT_SECRET` por un valor aleatorio de 32 bytes o más.
- Si cambias el `JWT_SECRET`, invalida tokens antiguos y revisa a los usuarios deberán iniciar sesión de nuevo.
- Usa un usuario PostgreSQL con acceso mínimo necesario, preferiblemente sin acceso DDL.
- En producción, pon todo detrás de HTTPS/TLS (el gateway por defecto es HTTP; put un proxy inverso si no puedes hacerlo dentro de Java).
- El seeding del admin solo debe crear credenciales temporales en entornos locales; en producción desactiva `volt.seed.admin` y crea el admin desde un entorno seguro.

## Esquemas MongoDB

Tienes dos opciones:

1. **Usar la base de datos que ya creaste.** `squema.txt` es la fuente actualizada para los validadores JSON de `reservas`, `usuarios`, `cupones`, `patinetas` y el resto de colecciones de catálogo/operaciones. Los servicios se conectan con:

```dotenv
MONGODB_URI=mongodb+srv://duvanjavier159_db_user:<db_password>@patinetas.un4fxyk.mongodb.net/
```

Reemplaza `<db_password>` por tu contraseña de Atlas antes de iniciar los servicios.
2. **Crear o actualizar los esquemas.** Desde la raíz del proyecto ejecuta:

```powershell
mongosh "mongodb+srv://duvanjavier159_db_user:<db_password>@patinetas.un4fxyk.mongodb.net/" --file .\scripts\mongodb\apply-schemas.js
```

El script usa `squema.txt` como referencia, crea las colecciones faltantes y actualiza los validadores con `collMod`. Es idempotente, así que puedes volver a ejecutarlo cuando cambie el esquema.

Para Atlas, pasa la URI de conexión a `mongosh` sin guardarla en archivos ni compartirla.

La interfaz web es el siguiente paso después de verificar el backend.
