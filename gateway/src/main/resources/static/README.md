# VOLT - Pantallas web

Interfaz HTML + CSS + JavaScript servida por el gateway de Spring Boot.

## Pantallas
- `index.html`: catálogo, búsqueda y acceso a la cuenta.
- `login.html`: inicio de sesión.
- `registro.html`: creación de cuenta de arrendatario o propietario.
- `detalle.html`: detalle del vehículo y resumen de reserva.
- `pago.html`: confirmación y autorización de pago.
- `publicar.html`: formulario de publicación por pasos (ficha, archivos, tarifas y disponibilidad).
- `propietario.html`: panel del propietario.
- `disponibilidad.html`: calendario y disponibilidad de una patineta.
- `admin.html`: moderación de actas, disputas y reservas (solo rol `ADMINISTRADOR`).
- `usuarios-verificar.html`: ventana para que el admin busque usuarios y los verifique/rechace (habilita la publicación de patinetas).
- `css/styles.css`: estilos compartidos y diseño responsive.
- `js/app.js`: sesión, roles, llamadas a la API y utilidades comunes.
- `js/login.js`: formulario de login de la página y modal de `index.html`.
- `js/publicar.js`: navegación por pasos y validación del formulario de publicación.
- `js/usuarios-verificar.js`: lógica de la ventana de verificación de usuarios.

## Cómo abrir
Con la plataforma levantada, entra a `http://localhost:8081/`. No abras los HTML
directamente como archivos: las pantallas consumen `/api/...` a través del gateway.

## Autenticación
El login guarda el token y los roles en `localStorage`, `sessionStorage` y una cookie.
El enlace `Admin` aparece solo para usuarios con rol `ADMINISTRADOR`; `admin.html`
redirige a `login.html?redirect=admin.html` si no hay sesión y muestra un aviso si la
cuenta no es administradora.
