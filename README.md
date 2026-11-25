# VOLT

Backend para el marketplace P2P de alquiler de patinetas eléctricas, organizado de acuerdo con la arquitectura de microservicios del documento del proyecto.

## Arquitectura

```text
Cliente web
    |
API Gateway (8081)
    +-- MS-UsuariosAuth (8082)
    +-- MS-CatalogoFlota (8083)
    +-- MS-ReservasPagos (8084)
    +-- MS-OperacionesIncidencias (8085)
    +-- MS-Comunicacion (8086)
             |
       MongoDB por servicio

MS-ReservasPagos -- PostgreSQL para consistencia de franjas de reserva
```

- `gateway/`: enrutamiento central con Spring Cloud Gateway WebFlux.
- `services/ms-reservas-pagos/`: estructura MVC prevista para reservas, pagos, depósitos y liquidaciones.
- `services/ms-catalogo-flota/`: estructura en capas para catálogo y disponibilidad; su DAO usa el driver MongoDB directamente y guarda las tarifas como `Decimal128` para respetar el esquema BSON.
- `services/ms-comunicacion/`: estructura en capas para mensajes y notificaciones.
- `services/ms-usuarios-auth/`: cuentas, identidad, roles y autenticación.
- `services/ms-operaciones-incidencias/`: actas de entrega/devolución, incidencias y disputas.

Los servicios comparten el clúster local de MongoDB, pero usan bases lógicas distintas. PostgreSQL se reserva para las franjas transaccionales de reservas: el documento exige MongoDB y también ACID relacional para evitar superposiciones; esta combinación mantiene la persistencia documental y permite aplicar restricciones transaccionales a la agenda.

## Requisitos

- JDK 25
- Maven Wrapper incluido (`mvnw.cmd`)
- MongoDB ejecutándose en `localhost:27017`, o configurar `MONGODB_URI` para Atlas
- PostgreSQL ejecutándose en `localhost:5432`

## Iniciar la plataforma

Instala e inicia MongoDB y PostgreSQL localmente. En PostgreSQL, crea el usuario y la base de desarrollo si aún no existen:

```powershell
psql -U postgres -c "CREATE USER volt WITH PASSWORD 'voltdev';"
psql -U postgres -c "CREATE DATABASE volt_reservas OWNER volt;"
```

Si ya configuraste `MONGODB_URI` como variable de entorno de Windows para Atlas, Spring Boot la usará. De lo contrario, cada servicio se conecta a MongoDB local sin autenticación. La URI y las contraseñas no deben guardarse en el repositorio.

Abre seis terminales PowerShell en la raíz del proyecto y ejecuta un servicio por terminal:

```powershell
.\mvnw.cmd -pl services/ms-usuarios-auth spring-boot:run
.\mvnw.cmd -pl services/ms-catalogo-flota spring-boot:run
.\mvnw.cmd -pl services/ms-reservas-pagos spring-boot:run
.\mvnw.cmd -pl services/ms-operaciones-incidencias spring-boot:run
.\mvnw.cmd -pl services/ms-comunicacion spring-boot:run
.\mvnw.cmd -pl gateway spring-boot:run
```

El Gateway queda disponible en `http://localhost:8081`. Cada servicio expone salud en `http://localhost:<puerto>/actuator/health`.

Para compilar y probar todos los módulos desde PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.5.10002-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -B verify
```

## Rutas del Gateway

| Ruta | Servicio |
|---|---|
| `/api/auth/**` | MS-UsuariosAuth |
| `/api/catalogo/**` | MS-CatalogoFlota |
| `/api/reservas/**`, `/api/pagos/**` | MS-ReservasPagos |
| `/api/operaciones/**`, `/api/incidencias/**` | MS-OperacionesIncidencias |
| `/api/comunicacion/**` | MS-Comunicacion |

El catálogo conserva `GET /api/catalogo/patinetas` para listar por categoría y ofrece búsqueda paginada en `GET /api/catalogo/patinetas/busqueda`, con `categoria`, `ciudad`, `precioHoraMaximo`, `autonomiaMinimaKm`, `pagina` y `tamano` opcionales. El tamaño máximo de página es 50.

## Fases de implementación

1. Base del monorepo, configuración local, Gateway y arranque independiente de los cinco servicios.
2. Usuarios y autenticación: registro, roles, verificación, BCrypt, JWT, consentimiento y auditoría.
3. Catálogo: patinetas, tarifas, puntos, zonas, moderación, búsqueda y disponibilidad.
4. Reservas y pagos: cotización, solapamientos, cancelaciones, depósitos, comisión, recargos y liquidaciones.
5. Operaciones: check-in/check-out, evidencias, no-show e incidencias/disputas.
6. Comunicación: conversaciones y notificaciones.
7. Endpoints OpenAPI, pruebas de reglas de negocio, datos de prueba y criterios de operación.

El documento contiene los RF-01 a RF-35, las historias de usuario y las reglas de negocio RN-01 a RN-12. Las reglas económicas y de ciclo de reserva se implementarán y probarán en sus módulos; este primer esqueleto deja definida la separación ejecutable de servicios.

## Esquemas MongoDB

Los cuatro esquemas de colecciones compartidos están en `scripts/mongodb/volt-collection-schemas.json`. Para crear o actualizar los validadores y los índices iniciales, desde la raíz del proyecto ejecuta:

```powershell
mongosh "mongodb://localhost:27017" --file .\scripts\mongodb\apply-schemas.js
```

Para Atlas, pasa la URI de conexión a `mongosh` sin guardarla en archivos ni compartirla. El usuario debe tener permisos para crear colecciones, ejecutar `collMod` e índices en `volt_usuarios`, `volt_catalogo` y `volt_reservas`. El script es repetible: crea las colecciones faltantes y actualiza la validación de las existentes.

## Decisiones de implementación

- Los períodos son intervalos semiabiertos `[inicio, fin)`, de modo que una reserva puede empezar exactamente cuando termina otra.
- En cotizaciones por hora se cobra cada hora iniciada; desde ocho horas se usa la tarifa diaria, redondeando cada bloque de 24 horas iniciado a un día. Es el criterio adoptado para las fracciones, que el documento no detalla.
- El depósito se calcula y reembolsa separadamente del alquiler y nunca integra la base de comisión.
- La infraestructura y los cálculos iniciales no representan aún todos los RF/HU; los endpoints y las persistencias transaccionales se incorporarán por fases.
