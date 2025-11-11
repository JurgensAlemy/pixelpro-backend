package com.pixelpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // <-- 1. IMPORTAR ESTO

@SpringBootApplication
@EnableJpaAuditing // <-- 2. AÑADIR ESTA LÍNEA
public class PixelproApplication {

	public static void main(String[] args) {
		SpringApplication.run(PixelproApplication.class, args);
	}

}
