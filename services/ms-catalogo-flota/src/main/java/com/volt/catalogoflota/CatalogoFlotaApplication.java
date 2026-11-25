package com.volt.catalogoflota;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.volt.catalogoflota", "com.volt.shared" })
public class CatalogoFlotaApplication {
	public static void main(String[] args) {
		SpringApplication.run(CatalogoFlotaApplication.class, args);
	}
}
