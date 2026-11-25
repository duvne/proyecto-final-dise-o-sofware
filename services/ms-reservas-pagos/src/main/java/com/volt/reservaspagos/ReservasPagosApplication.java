package com.volt.reservaspagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = { "com.volt.reservaspagos", "com.volt.shared" })
@EnableScheduling
public class ReservasPagosApplication {
	public static void main(String[] args) {
		SpringApplication.run(ReservasPagosApplication.class, args);
	}
}
