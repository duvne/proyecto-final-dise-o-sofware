package com.volt.operaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.volt.operaciones", "com.volt.shared" })
public class OperacionesIncidenciasApplication {
	public static void main(String[] args) {
		SpringApplication.run(OperacionesIncidenciasApplication.class, args);
	}
}
