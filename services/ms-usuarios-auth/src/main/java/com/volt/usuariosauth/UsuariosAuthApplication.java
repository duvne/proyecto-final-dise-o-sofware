package com.volt.usuariosauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.volt.usuariosauth", "com.volt.shared" })
public class UsuariosAuthApplication {
	public static void main(String[] args) {
		SpringApplication.run(UsuariosAuthApplication.class, args);
	}
}
