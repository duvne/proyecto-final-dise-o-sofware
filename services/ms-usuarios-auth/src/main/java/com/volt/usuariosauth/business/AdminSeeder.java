package com.volt.usuariosauth.business;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.volt.shared.security.Roles;
import com.volt.usuariosauth.domain.EstadoCuenta;
import com.volt.usuariosauth.domain.PerfilArrendatario;
import com.volt.usuariosauth.domain.Usuario;
import com.volt.usuariosauth.persistence.IUsuarioDAO;

@Component
public class AdminSeeder implements ApplicationRunner {
	private static final Logger LOG = LoggerFactory.getLogger(AdminSeeder.class);

	private final IUsuarioDAO usuarioDAO;
	private final PasswordEncoder passwordEncoder;
	private final boolean habilitado;
	private final String correo;
	private final String password;

	public AdminSeeder(
			IUsuarioDAO usuarioDAO,
			PasswordEncoder passwordEncoder,
			@Value("${volt.seed.admin:true}") boolean habilitado,
			@Value("${volt.seed.admin.email:admin@volt.com}") String correo,
			@Value("${volt.seed.admin.password:Admin123456!}") String password) {
		this.usuarioDAO = usuarioDAO;
		this.passwordEncoder = passwordEncoder;
		this.habilitado = habilitado;
		this.correo = correo;
		this.password = password;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!habilitado || usuarioDAO.existeCorreo(correo)) {
			return;
		}
		Usuario admin = new Usuario(
				UUID.randomUUID().toString(),
				"Administrador VOLT",
				"ADMIN-VOLT",
				correo,
				passwordEncoder.encode(password),
				"0000000000",
				java.time.LocalDate.of(1990, 1, 1),
				true,
				EstadoCuenta.ACTIVA,
				Instant.now(),
				List.of(Roles.ADMINISTRADOR, Roles.ARRENDATARIO),
				null,
				new PerfilArrendatario(null, 0, 0.0, 0),
				List.of(),
				null);
		usuarioDAO.guardar(admin);
		LOG.warn("Usuario administrador inicial creado: {}. Reemplaza JWT_SECRET, VOLT_INTERNAL_KEY y VOLT_ADMIN_PASSWORD por valores seguros antes de produccion.", correo);
	}
}
