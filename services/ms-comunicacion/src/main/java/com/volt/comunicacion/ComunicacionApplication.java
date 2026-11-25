package com.volt.comunicacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = { "com.volt.comunicacion", "com.volt.shared" })
@EnableScheduling
public class ComunicacionApplication {
	public static void main(String[] args) {
		SpringApplication.run(ComunicacionApplication.class, args);
	}
}
