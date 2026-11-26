const TOKEN_KEY = 'volt_token';
const ROLES_KEY = 'volt_roles';
const TOKEN_KEYS = [TOKEN_KEY, 'access_token', 'token'];

function getCookie(name) {
  const match = document.cookie.match(new RegExp('(^|; )' + name + '=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : null;
}

function getStoredToken() {
  for (const store of [localStorage, sessionStorage]) {
    for (const key of TOKEN_KEYS) {
      try {
        const value = store.getItem(key);
        if (value) return value;
      } catch (error) { /* storage bloqueada */ }
    }
  }
  for (const key of TOKEN_KEYS) {
    const value = getCookie(key);
    if (value) return value;
  }
  return null;
}

function decodeJwtPayload(token) {
  try {
    const segment = token.split('.')[1];
    if (!segment) return null;
    const normalized = segment.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized + '='.repeat((4 - normalized.length % 4) % 4);
    return JSON.parse(atob(padded));
  } catch (error) {
    return null;
  }
}

function getStoredRoles() {
  for (const store of [localStorage, sessionStorage]) {
    try {
      const raw = store.getItem(ROLES_KEY);
      if (raw) {
        const roles = JSON.parse(raw);
        if (Array.isArray(roles)) return roles;
      }
    } catch (error) { /* storage bloqueada o dato inválido */ }
  }
  const token = getStoredToken();
  const payload = token ? decodeJwtPayload(token) : null;
  return payload && Array.isArray(payload.roles) ? payload.roles : [];
}

function isAdmin() {
  return getStoredRoles().includes('ADMINISTRADOR');
}

function saveSession(data) {
  const token = data && (data.access_token || data.token);
  if (!token) throw new Error('No se pudo obtener el token de sesión.');
  const payload = decodeJwtPayload(token);
  const roles = Array.isArray(data.roles) && data.roles.length
    ? data.roles
    : (payload && Array.isArray(payload.roles) ? payload.roles : []);
  for (const store of [localStorage, sessionStorage]) {
    try {
      store.setItem(TOKEN_KEY, token);
      store.setItem(ROLES_KEY, JSON.stringify(roles));
    } catch (error) { /* storage bloqueada */ }
  }
  try { document.cookie = 'volt_token=' + encodeURIComponent(token) + ';path=/'; } catch (error) { /* cookies bloqueadas */ }
  return token;
}

function clearSession() {
  for (const store of [localStorage, sessionStorage]) {
    for (const key of [TOKEN_KEY, ROLES_KEY, 'access_token', 'token']) {
      try { store.removeItem(key); } catch (error) { /* storage bloqueada */ }
    }
  }
  try { document.cookie = 'volt_token=;path=/;max-age=0'; } catch (error) { /* cookies bloqueadas */ }
}

function authHeaders() {
  const token = getStoredToken();
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = 'Bearer ' + token;
  return headers;
}

async function fetchJson(path, options = {}) {
  const { method = 'GET', body, headers = {}, skipAuth = false } = options;
  const auth = skipAuth ? {} : authHeaders();
  const contentType = body !== undefined ? { 'Content-Type': 'application/json' } : {};
  const res = await fetch(path, {
    method,
    headers: { ...contentType, ...auth, ...headers },
    body: body === undefined ? undefined : JSON.stringify(body)
  });
  if (!res.ok) {
    const data = await res.json().catch(() => ({}));
    const message = data.detalle || data.error || data.message || res.statusText;
    const error = new Error(message);
    error.status = res.status;
    throw error;
  }
  if (res.status === 204) return undefined;
  return res.json();
}

function showError(element, message) {
  if (element) element.textContent = message;
}

function formatMoney(value) {
  const n = Number(value || 0);
  return '$' + n.toLocaleString('es', { minimumFractionDigits: 0, maximumFractionDigits: 0 }) + ' COP';
}

function safeRedirect(value, fallback = 'index.html') {
  if (!value) return fallback;
  if (value.startsWith('//') || /[\u0000-\u001F\u007F\\]/.test(value) || /^[a-z][a-z0-9+.-]*:/i.test(value)) {
    return fallback;
  }
  return value;
}

document.addEventListener('DOMContentLoaded', function () {
  document.querySelectorAll('[data-admin-only]').forEach(function (element) {
    element.hidden = !isAdmin();
  });
});
