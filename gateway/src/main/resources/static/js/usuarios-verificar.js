(function () {
  const TOKEN_KEY = 'volt_token';
  const ROLES_KEY = 'volt_roles';

  function getStoredToken() {
    for (const store of [localStorage, sessionStorage]) {
      try { const v = store.getItem(TOKEN_KEY); if (v) return v; } catch {/* */}
    }
    const cookieMatch = document.cookie.match(/(^|; )volt_token=([^;]+)/);
    return cookieMatch ? decodeURIComponent(cookieMatch[2]) : null;
  }

  function getStoredRoles() {
    for (const store of [localStorage, sessionStorage]) {
      try { const raw = store.getItem(ROLES_KEY); if (raw) { const roles = JSON.parse(raw); if (Array.isArray(roles)) return roles; } } catch {/* */}
    }
    return [];
  }

  function isAdmin() { return getStoredRoles().includes('ADMINISTRADOR'); }

  function authHeaders() {
    const token = getStoredToken();
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = 'Bearer ' + token;
    return headers;
  }

  function showError(msg) {
    const el = document.getElementById('error');
    if (el) { el.hidden = false; el.textContent = msg; }
  }

  function hideError() {
    const el = document.getElementById('error');
    if (el) el.hidden = true;
  }

  function formatRol(rol) {
    if (!rol) return '—';
    const r = rol.toUpperCase();
    return r === 'ADMINISTRADOR' ? 'Admin' : r === 'PROPIETARIO' ? 'Propietario' : r === 'ARRENDATARIO' ? 'Arrendatario' : rol;
  }

  async function fetchJson(path, options = {}) {
    const { skipAuth = false, ...rest } = options;
    const auth = skipAuth ? {} : authHeaders();
    const contentType = rest.body !== undefined ? { 'Content-Type': 'application/json' } : {};
    const res = await fetch(path, { ...rest, headers: { ...contentType, ...auth, ...(rest.headers || {}) } });
    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      const message = data.detalle || data.error || data.message || res.statusText;
      const error = new Error(message);
      error.status = res.status;
      throw error;
    }
    return res.json().catch(() => null);
  }

  // ---- UI elements ----
  const inputBuscar = document.getElementById('buscarUsuario');
  const btnBuscar = document.getElementById('btnBuscar');
  const tablaUsuarios = document.getElementById('tablaUsuarios');
  const tbody = tablaUsuarios.querySelector('tbody');
  const msgVacia = document.getElementById('msgVacia');
  const usuarioResultado = document.getElementById('usuarioResultado');
  const formVerificar = document.getElementById('formVerificar');
  const inputUsuarioId = document.getElementById('usuarioId');
  const selectAccion = document.getElementById('accion');
  const inputMotivo = document.getElementById('motivo');
  const btnAplicar = document.getElementById('btnAplicar');
  const btnCancel = document.getElementById('btnCancel');

  // ---- Inicialización ----
  document.addEventListener('DOMContentLoaded', function () {
    // Chequeo de sesión y permisos
    const token = getStoredToken();
    if (!token) {
      location.replace('login.html');
      return;
    }
    if (!isAdmin()) {
      showError('Tu cuenta no tiene permisos de administrador. ');
      const notice = document.getElementById('error');
      notice.innerHTML = 'Tu cuenta no tiene permisos de administrador. <a href="login.html?redirect=usuarios-verificar.html">Cambiar de cuenta</a>';
      tablaUsuarios.style.display = 'none';
      msgVacia.style.display = 'none';
      return;
    }

    // Por defecto, ocultar formulario y mensaje hasta que el usuario pulse Buscar
    tablaUsuarios.style.display = 'none';
    msgVacia.style.display = 'none';
    usuarioResultado.hidden = true;
    formVerificar.hidden = true;

    // Event: Buscar usuario
    btnBuscar.addEventListener('click', async function (e) {
      e.preventDefault();
      const query = (inputBuscar.value || '').trim();
      tablaUsuarios.style.display = 'table';
      tbody.innerHTML = '';
      msgVacia.style.display = 'none';

      if (!query) {
        showError('Ingresa un ID o correo electrónico para buscar');
        tablaUsuarios.style.display = 'none';
        return;
      }

      try {
        const data = await fetchJson(`/api/auth/usuarios?pagina=0&tamano=20&q=${encodeURIComponent(query)}`);
        const elementos = data && data.elementos ? data.elementos : [];

        if (elementos.length > 0) {
          tbody.innerHTML = elementos.map((u, i) => {
            const id = u.id || u.correo || '—';
            const correo = u.correo || '—';
            const verificado = u.verificado === true ? 'Sí' : u.verificado === false ? 'No' : '—';
            const estadoCuenta = u.estadoCuenta || '—';
            const roles = u.roles && u.roles.length ? u.roles : [];
            const rolesMostrar = roles.slice(0, 3).map(formatRol).join(', ') + (roles.length > 3 ? ` +${roles.length - 3} más` : '');
            const nombre = u.nombre ? u.nombre : (u.correo ? u.correo.split('@')[0] : '—');

            return `<tr data-id="${id}" data-correo="${correo}" style="cursor:pointer">
              <td>${id}</td>
              <td>${correo}</td>
              <td>${verificado}</td>
              <td>${estadoCuenta}</td>
              <td>${rolesMostrar}</td>
              <td><span class="btn light tiny" style="font-size:11px;padding:4px 8px">Ver</span></td>
            </tr>`;
          }).join('');
          tablaUsuarios.style.display = 'table';
          msgVacia.style.display = 'none';
        } else {
          msgVacia.style.display = 'block';
          tablaUsuarios.style.display = 'none';
          showError('No se encontraron usuarios con esa búsqueda.');
        }
      } catch (e) {
        console.error(e);
        showError(e.message || 'Error al cargar usuarios');
        tablaUsuarios.style.display = 'none';
        msgVacia.style.display = 'none';
      }
    });

    // Event: Click en una fila de la tabla (no en el botón "Ver" de la última celda)
    tbody.addEventListener('click', async function (e) {
      const row = e.target.closest('tr');
      if (!row) return;
      if (e.target.classList && e.target.classList.contains('btn')) return;

      const id = row.dataset.id;
      const correo = row.dataset.correo;

      // En este entorno los IDs de la tabla son correos electrónicos.
      // Intentaremos usar el correo directamente.
      const searchId = correo || id;

      if (!searchId) { showError('ID de usuario no encontrado'); return; }

      try {
        const data = await fetchJson(`/api/auth/usuarios/${encodeURIComponent(searchId)}`);
        if (!data) { showError('No se encontró el usuario.'); return; }

        inputUsuarioId.value = data.id || '';
        selectAccion.value = 'verificar';
        inputMotivo.value = '';
        mostrarUsuario(data);
      } catch (e) {
        showError(e.message || 'Error al cargar usuario');
      }
    });
  });

  // ---- Helper: mostrar ficha de usuario y formulario de acción ----
  function mostrarUsuario(usuario) {
    const roles = usuario.roles || [];
    const verificado = usuario.verificado;
    const estadoCuenta = usuario.estadoCuenta;
    const nombre = usuario.nombre || (usuario.correo ? usuario.correo.split('@')[0] : 'N/A');

    usuarioResultado.innerHTML = ''
      + '<p><strong>ID:</strong> ' + (usuario.id || '—') + '</p>'
      + '<p><strong>Correo:</strong> ' + (usuario.correo || '—') + '</p>'
      + '<p><strong>Nombre:</strong> ' + nombre + '</p>'
      + '<p><strong>Verificado:</strong> ' + (verificado === true ? 'Sí' : verificado === false ? 'No' : '—') + '</p>'
      + '<p><strong>EstadoCuenta:</strong> ' + (estadoCuenta || '—') + '</p>'
      + '<p><strong>Roles:</strong> ' + (Array.isArray(roles) ? roles.join(', ') : '—') + '</p>'
      + '<p class="muted">Selecciona una acción a aplicar.</p>';

    usuarioResultado.hidden = false;
    formVerificar.hidden = false;
    tablaUsuarios.style.display = 'none';
  }
})();