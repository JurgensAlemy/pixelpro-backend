package com.pixelpro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // <-- 1. IMPORTAR ESTO

@SpringBootTest
@EnableJpaAuditing
class PixelproApplicationTests {

	@Test
	void contextLoads() {
	}

}
