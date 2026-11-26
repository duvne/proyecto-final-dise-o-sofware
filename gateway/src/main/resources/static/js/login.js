function loginModalElement() {
  return document.getElementById('loginModal');
}

function openLoginModal() {
  const modal = loginModalElement();
  if (!modal) {
    location.href = 'login.html';
    return;
  }
  modal.hidden = false;
  const email = document.getElementById('loginEmail');
  if (email) email.focus();
}

function closeLoginModal() {
  const modal = loginModalElement();
  if (modal) modal.hidden = true;
}

async function loginSubmit(event) {
  event.preventDefault();
  const error = document.getElementById('loginError');
  const correo = document.getElementById('loginEmail').value.trim();
  const password = document.getElementById('loginPassword').value;
  if (error) error.textContent = '';
  try {
    const data = await fetchJson('/api/auth/login', {
      method: 'POST',
      body: { correo, contrasena: password },
      skipAuth: true
    });
    saveSession(data);
    if (loginModalElement()) {
      closeLoginModal();
      location.reload();
      return;
    }
    const redirect = new URLSearchParams(location.search).get('redirect');
    location.href = safeRedirect(redirect);
  } catch (e) {
    if (error) error.textContent = e.message;
  }
}

document.addEventListener('DOMContentLoaded', function () {
  const form = document.getElementById('loginForm');
  if (form) form.addEventListener('submit', loginSubmit);

  const open = document.getElementById('loginButton');
  if (open && loginModalElement()) {
    open.addEventListener('click', function (event) {
      event.preventDefault();
      openLoginModal();
    });
  }

  const close = document.getElementById('loginClose');
  if (close) close.addEventListener('click', closeLoginModal);

  const modal = loginModalElement();
  if (modal) {
    modal.addEventListener('click', function (event) {
      if (event.target === modal) closeLoginModal();
    });
    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape' && !modal.hidden) closeLoginModal();
    });
  }
});
