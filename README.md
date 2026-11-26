# VOLT Backend

Backend completo para el marketplace P2P de alquiler de patinetas eléctricas. Incluye los cinco microservicios, el Gateway, catálogo, reservas, operaciones, comunicación, seguridad con JWT/BCrypt y auditoría.

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
```

## Requisitos

- JDK 25
- Maven Wrapper incluido (`mvnw.cmd`)
- MongoDB en `mongodb+srv://duvanjavier159_db_user:<db_password>@patinetas.un4fxyk.mongodb.net/` o configurar `MONGODB_URI` (reemplaza `<db_password>`)

## Iniciar el backend

1. Inicia MongoDB.
2. Tienes dos opciones:

```powershell
# Opción rápida: inicia los 6 servicios en una sola ejecución
.\scripts\start-platform.ps1
```

o abre una terminal por servicio:

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
JWT_SECRET=volt-dev-secret-2026-8f7a9c3d2b5e4f60
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
- En producción, pon todo detrás de HTTPS/TLS (el gateway por defecto es HTTP; pon un proxy inverso si no puedes hacerlo dentro de Java).
- El seeding del admin solo debe crear credenciales temporales en entornos locales; en producción desactiva `volt.seed.admin` y crea el admin desde un entorno seguro.

## Esquemas MongoDB

`squema.txt` es la fuente de verdad para los validadores JSON de las colecciones (`reservas`, `usuarios`, `cupones`, `patinetas`, etc.). Puedes volver a aplicarlos repetidamente:

```powershell
mongosh "mongodb+srv://duvanjavier159_db_user:<db_password>@patinetas.un4fxyk.mongodb.net/" --file .\scripts\mongodb\apply-schemas.js
```

Para Atlas, pasa la URI de conexión a `mongosh` sin guardarla en archivos ni compartirla.

La interfaz web es el siguiente paso después de verificar el backend.
