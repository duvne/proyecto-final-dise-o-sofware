package com.volt.reservaspagos.model.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.volt.reservaspagos.model.domain.CuponDocument;
import com.volt.reservaspagos.model.domain.CuponInvalidoException;
import com.volt.reservaspagos.persistence.ICuponDAO;
import com.volt.reservaspagos.view.dto.CuponRequest;
import com.volt.reservaspagos.view.dto.CuponResponse;
import com.volt.reservaspagos.view.dto.ValidarCuponResponse;
import com.volt.shared.audit.AuditoriaService;

@Service
public class CuponService {
	private final ICuponDAO cuponDAO;
	private final AuditoriaService auditoriaService;

	public CuponService(ICuponDAO cuponDAO, AuditoriaService auditoriaService) {
		this.cuponDAO = cuponDAO;
		this.auditoriaService = auditoriaService;
	}

	public List<CuponResponse> listar() {
		return cuponDAO.listar().stream().map(CuponResponse::desde).toList();
	}

	public CuponResponse crear(CuponRequest request, String adminId, String ip) {
		String codigo = request.codigo().trim().toUpperCase();
		if (cuponDAO.buscarPorCodigo(codigo).isPresent()) {
			throw new CuponInvalidoException("Ya existe un cupon con ese codigo.");
		}
		CuponDocument creado = cuponDAO.guardar(new CuponDocument(null, codigo, request.porcentaje(),
				request.topeDescuento(), request.fechaVencimiento(), request.activo(), 0,
				request.limiteUsos()));
		auditoriaService.registrar(adminId, "CREAR_CUPON", "CUPON", creado.id(), null, codigo, ip);
		return CuponResponse.desde(creado);
	}

	public CuponResponse actualizar(String id, CuponRequest request, String adminId, String ip) {
		CuponDocument actual = cuponDAO.buscarPorId(id)
				.orElseThrow(() -> new CuponInvalidoException("El cupon no existe."));
		CuponDocument actualizado = cuponDAO.actualizar(new CuponDocument(id,
				request.codigo().trim().toUpperCase(), request.porcentaje(), request.topeDescuento(),
				request.fechaVencimiento(), request.activo(), actual.usos(), request.limiteUsos()));
		auditoriaService.registrar(adminId, "ACTUALIZAR_CUPON", "CUPON", id,
				actual.codigo(), actualizado.codigo(), ip);
		return CuponResponse.desde(actualizado);
	}

	public void eliminar(String id, String adminId, String ip) {
		CuponDocument actual = cuponDAO.buscarPorId(id)
				.orElseThrow(() -> new CuponInvalidoException("El cupon no existe."));
		cuponDAO.eliminar(id);
		auditoriaService.registrar(adminId, "ELIMINAR_CUPON", "CUPON", id, actual.codigo(), null, ip);
	}

	public ValidarCuponResponse validar(String codigo) {
		try {
			CuponDocument cupon = obtenerValido(codigo);
			return new ValidarCuponResponse(cupon.codigo(), true, cupon.porcentaje(),
					cupon.topeDescuento(), "Cupon valido.");
		} catch (CuponInvalidoException exception) {
			return new ValidarCuponResponse(codigo == null ? "" : codigo.trim().toUpperCase(), false,
					null, null, exception.getMessage());
		}
	}

	public CuponDocument obtenerValido(String codigo) {
		if (codigo == null || codigo.isBlank()) {
			throw new CuponInvalidoException("El codigo del cupon es obligatorio.");
		}
		CuponDocument cupon = cuponDAO.buscarPorCodigo(codigo)
				.orElseThrow(() -> new CuponInvalidoException("El cupon no existe."));
		if (!cupon.activo()) {
			throw new CuponInvalidoException("El cupon no esta activo.");
		}
		if (cupon.fechaVencimiento().isBefore(Instant.now())) {
			throw new CuponInvalidoException("El cupon ya vencio.");
		}
		if (cupon.limiteUsos() > 0 && cupon.usos() >= cupon.limiteUsos()) {
			throw new CuponInvalidoException("El cupon alcanzo su limite de usos.");
		}
		return cupon;
	}

	public BigDecimal calcularDescuento(CuponDocument cupon, BigDecimal subtotal) {
		BigDecimal descuento = subtotal.multiply(cupon.porcentaje());
		if (cupon.topeDescuento() != null && cupon.topeDescuento().signum() > 0) {
			descuento = descuento.min(cupon.topeDescuento());
		}
		descuento = descuento.min(subtotal);
		return descuento.setScale(2, RoundingMode.HALF_UP);
	}
}
