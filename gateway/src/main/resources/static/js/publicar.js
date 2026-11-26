(function () {
  const form = document.getElementById('publicarForm');
  if (!form) return;

  if (!getStoredToken()) {
    location.replace('login.html?redirect=publicar.html');
    return;
  }

  const stepSections = Array.from(form.querySelectorAll('[data-step]'));
  const progressLabels = Array.from(document.querySelectorAll('[data-step-label]'));
  const prevButton = document.getElementById('prevStep');
  const nextButton = document.getElementById('nextStep');
  const submitButton = document.getElementById('submitStep');
  const stepError = document.getElementById('stepError');
  const totalSteps = stepSections.length;
  let currentStep = 1;

  function fieldLabel(field) {
    const label = field.closest('.field')?.querySelector('label');
    return label ? label.textContent.replace(/\s+/g, ' ').trim() : field.name;
  }

  function clearError() {
    if (!stepError) return;
    stepError.hidden = true;
    stepError.textContent = '';
  }

  function showError(message) {
    if (!stepError) return;
    stepError.hidden = false;
    stepError.textContent = Array.isArray(message) ? message.join(' ') : message;
    stepError.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  function focusFirstInvalid(step) {
    const section = stepSections[step - 1];
    const invalid = section?.querySelector('.invalid');
    if (invalid) invalid.focus({ preventScroll: true });
  }

  function validateStep(step) {
    const section = stepSections[step - 1];
    if (!section) return [];
    const fields = Array.from(section.querySelectorAll('input, textarea, select'));
    const errors = [];

    fields.forEach((field) => {
      field.classList.remove('invalid');
      field.removeAttribute('aria-invalid');
      if (field.checkValidity()) return;

      field.classList.add('invalid');
      field.setAttribute('aria-invalid', 'true');

      if (field.validity.valueMissing) {
        errors.push(fieldLabel(field) + ' es obligatorio.');
      } else if (field.validity.rangeUnderflow) {
        errors.push(fieldLabel(field) + ' debe ser mayor o igual a ' + field.min + '.');
      } else if (field.validity.rangeOverflow) {
        errors.push(fieldLabel(field) + ' debe ser menor o igual a ' + field.max + '.');
      } else {
        errors.push(fieldLabel(field) + ' no es válido.');
      }
    });

    return errors;
  }

  function updateProgress() {
    progressLabels.forEach((label) => {
      const step = Number(label.dataset.stepLabel);
      const isCurrent = step === currentStep;
      label.classList.toggle('current', isCurrent);
      label.classList.toggle('done', step < currentStep);
      if (isCurrent) {
        label.setAttribute('aria-current', 'step');
      } else {
        label.removeAttribute('aria-current');
      }
    });
  }

  function showStep(step, focus) {
    currentStep = Math.min(Math.max(step, 1), totalSteps);
    stepSections.forEach((section) => {
      section.classList.toggle('active', Number(section.dataset.step) === currentStep);
    });
    updateProgress();
    if (prevButton) prevButton.hidden = currentStep === 1;
    if (nextButton) nextButton.hidden = currentStep === totalSteps;
    if (submitButton) submitButton.hidden = currentStep !== totalSteps;
    clearError();

    if (focus) {
      const firstField = stepSections[currentStep - 1]?.querySelector('input, textarea, select');
      if (firstField) firstField.focus({ preventScroll: true });
      form.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  function goNext() {
    const errors = validateStep(currentStep);
    if (errors.length) {
      showError(errors);
      focusFirstInvalid(currentStep);
      return;
    }
    if (currentStep < totalSteps) showStep(currentStep + 1, true);
  }

  function goPrev() {
    if (currentStep > 1) showStep(currentStep - 1, true);
  }

  function parseUrls(value) {
    return String(value || '')
      .split(/\r?\n|,/)
      .map((url) => url.trim())
      .filter(Boolean);
  }

  function textValue(formData, name) {
    return String(formData.get(name) || '').trim();
  }

  function clearFieldError(field) {
    if (!field.matches('input, textarea, select')) return;
    field.classList.remove('invalid');
    field.removeAttribute('aria-invalid');
    if (!form.querySelector('.invalid')) clearError();
  }

  form.addEventListener('input', (event) => clearFieldError(event.target));
  form.addEventListener('change', (event) => clearFieldError(event.target));

  form.addEventListener('keydown', (event) => {
    if (event.key !== 'Enter' || !event.target.matches('input, select')) return;
    if (currentStep < totalSteps) {
      event.preventDefault();
      goNext();
    }
  });

  if (prevButton) prevButton.addEventListener('click', goPrev);
  if (nextButton) nextButton.addEventListener('click', goNext);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();

    for (let step = 1; step <= totalSteps; step += 1) {
      const errors = validateStep(step);
      if (errors.length) {
        showStep(step);
        showError(errors);
        focusFirstInvalid(step);
        return;
      }
    }

    if (!getStoredToken()) {
      location.href = 'login.html?redirect=publicar.html';
      return;
    }

    const formData = new FormData(form);
    const ciudad = textValue(formData, 'ciudad');
    const body = {
      categoria: textValue(formData, 'categoria'),
      marca: textValue(formData, 'marca'),
      modelo: textValue(formData, 'modelo'),
      autonomia_km: Number(formData.get('autonomiaKm')),
      velocidad_max: Number(formData.get('velocidadMaxima')),
      estado_fisico: textValue(formData, 'estadoFisico'),
      tarifa: {
        valor_hora: Number(formData.get('valorHora')),
        valor_dia: Number(formData.get('valorDia')),
        valor_deposito: Number(formData.get('valorDeposito'))
      },
      punto_entrega: {
        direccion: textValue(formData, 'direccion'),
        latitud: Number(formData.get('latitud')),
        longitud: Number(formData.get('longitud')),
        verificado: false,
        zona: { id_zona: 1, nombre: ciudad, ciudad: ciudad }
      },
      fotos: parseUrls(formData.get('fotos')),
      documentos: parseUrls(formData.get('documentos'))
    };

    try {
      if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = 'Guardando...';
      }
      const creada = await fetchJson('/api/catalogo/mis-patinetas', { method: 'POST', body });
      location.href = 'disponibilidad.html?id=' + encodeURIComponent(creada._id || creada.id || '');
    } catch (error) {
      if (error.status === 401) {
        location.href = 'login.html?redirect=publicar.html';
        return;
      }
      showError(error.message || String(error));
      if (submitButton) {
        submitButton.disabled = false;
        submitButton.textContent = 'Guardar borrador';
      }
    }
  });

  showStep(1);
})();
